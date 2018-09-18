/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import java.util.UUID

import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.constants.AppConstant.DefaultGroup
import com.stratio.sparta.serving.core.dao.GroupDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.workflow.Group
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection
import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class GroupPostgresDao extends GroupDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.db

  private val workflowPgService = new WorkflowPostgresDao()

  import profile.api._

  override def initializeData(): Unit = {
    log.debug("Initializing default group")
    for {
      _ <- upsert(DefaultGroup)
    } yield {
      log.debug("The default group initialization has been completed")
    }
  }

  def findGroupByName(name: String): Future[Group] = findByNameHead(name)


  def findGroupById(id: String): Future[Group] = findByIdHead(id)

  def createFromGroup(group: Group): Future[Group] =
    if (Group.isValid(group)) {
      filterByName(group.name).flatMap { groups =>
        if (groups.nonEmpty)
          throw new ServerException(s"Unable to create group ${group.name} because it already exists")
        else createAndReturn(addID(group))
      }
    } else throw new ServerException(s"Unable to create group ${group.name} because its name is invalid")

  def update(group: Group): Future[Group] = {
    if (Group.isValid(group)) {
      for {
        groups <- filterByName(group.name)
        oldGroup <-
          if (groups.nonEmpty)
            throw new ServerException(
              s"Unable to update group ${group.id.get} with name ${group.name} because target group already exists")
          else findByIdHead(group.id.get)
        groupsToUpdate <- findGroupAndSubgroups(oldGroup.name)
      } yield {
        groupsToUpdate.foreach { currentGroup =>
          val newGroup = Group(currentGroup.id, currentGroup.name.replaceFirst(oldGroup.name, group.name))
          upsert(newGroup)
        }
        group
      }
    } else throw new ServerException(s"Unable to create group ${group.name} because its name is invalid")
  }

  def deleteById(id: String): Future[Boolean] = {
    if (id == DefaultGroup.id.get) {
      throw new ServerException(s"Unable to delete default group")
    } else {
      for {
        group <- findByIdHead(id)
        groups <- findGroupAndSubgroups(group.name)
      } yield deleteYield(groups)
    }
  }

  def deleteByName(name: String): Future[Boolean] = {
    if (name == DefaultGroup.name) {
      throw new ServerException(s"Unable to delete default group")
    } else {
      for {
        groups <- findGroupAndSubgroups(name)
      } yield deleteYield(groups)
    }
  }

  def deleteAllGroups(): Future[Boolean] =
    for {
      groups <- findAll()
    } yield deleteYield(groups)


  /** PRIVATE METHODS **/

  private[services] def addID(group: Group): Group = {
    if ((group.id.isEmpty ||
      group.id.notBlank.isEmpty && !group.id.get.equals(DefaultGroup.id.get)) &&
      !group.name.equals(DefaultGroup.name))
      group.copy(id = Some(UUID.randomUUID.toString))
    else group
  }

  private[services] def filterByName(name: String): Future[Seq[Group]] =
    db.run(table.filter(_.name === name).result)

  private[services] def findGroupAndSubgroups(name: String): Future[Seq[Group]] =
    db.run(table.filter(g => g.name === name || g.name.startsWith(s"$name/")).result)

  private[services] def findByNameHead(name: String): Future[Group] =
    for {
      group <- filterByName(name)
    } yield {
      if (group.nonEmpty)
        group.head
      else throw new ServerException(s"No group found by name $name")
    }

  private[services] def findByIdHead(id: String): Future[Group] =
    for {
      group <- db.run(filterById(id).result)
    } yield {
      if (group.nonEmpty)
        group.head
      else throw new ServerException(s"No group found by id $id")
    }

  private[services] def deleteYield(groups: Seq[Group]): Boolean = {
    groups.foreach { currentGroup =>
      if (currentGroup.id.get != DefaultGroup.id.get && currentGroup.name != DefaultGroup.name) {
        log.debug(s"Deleting group ${currentGroup.id} ${currentGroup.name}")
        for {
          ids <- workflowPgService.findByGroupID(currentGroup.id.get)
          _ <- workflowPgService.deleteWorkflowList(ids.flatMap(_.id))
        } yield {
          Try(deleteByID(currentGroup.id.get)) match {
            case Success(_) =>
              log.info(s"Group ${currentGroup.name} with id ${currentGroup.id.get} deleted")
              true
            case Failure(e) =>
              throw e
          }
        }
      }
    }
    true
  }

}
