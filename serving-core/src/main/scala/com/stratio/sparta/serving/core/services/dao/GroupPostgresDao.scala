/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import java.util.UUID
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.lang.IgniteBiPredicate
import slick.jdbc.PostgresProfile

import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.constants.AppConstant.DefaultGroup
import com.stratio.sparta.serving.core.dao.GroupDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.workflow.Group
import com.stratio.sparta.serving.core.utils.{JdbcSlickConnection, PostgresDaoFactory}

class GroupPostgresDao extends GroupDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  private val workflowPgService = PostgresDaoFactory.workflowPgService

  import profile.api._

  override def initializeData(): Unit = {
    log.debug("Initializing default group")
    for {
      _ <- upsert(DefaultGroup)
    } yield {
      log.debug("The default group initialization has been completed")
    }
    initialCacheLoad()
  }

  def findAllGroups(): Future[List[Group]] = {
    if (cacheEnabled)
      Try {
        cache.iterator().allAsScala()
      }.getOrElse(super.findAll())
    else
      findAll()
  }

  def findGroupByName(name: String): Future[Group] = {
    if (cacheEnabled)
      predicateHead(ignitePredicateByName(name))(findByNameHead(name))
    else
      findByNameHead(name)
  }

  def findGroupById(id: String): Future[Group] = {
    if (cacheEnabled)
      cacheById(id)(findByIdHead(id))
    else
      findByIdHead(id)
  }

  def createFromGroup(group: Group): Future[Group] =
    if (Group.isValid(group)) {
      filterByName(group.name).flatMap { groups =>
        if (groups.nonEmpty)
          throw new ServerException(s"Unable to create group ${group.name} because it already exists")
        else createAndReturn(addID(group)).cached()
      }
    } else throw new ServerException(s"Unable to create group ${group.name} because its name is invalid")

  def update(group: Group): Future[Group] = {
    if (Group.isValid(group)) {
      val id = group.id.getOrElse(
        throw new ServerException(s"It's mandatory the id field"))

      findByIdHead(id).flatMap { oldGroup =>
        val updateActions = for {
          groupsToUpdate <- findGroupAndSubgroups(oldGroup.name)
          workflowActions <- Future.sequence {
            groupsToUpdate.map { groupToUpdate =>
              workflowPgService.findByGroupID(groupToUpdate.id.get).map { workflows =>
                workflows.map { workflow =>
                  val newWorkflow = workflow.copy(
                    groupId = Option(id),
                    group = group
                  )
                  workflowPgService.upsertAction(newWorkflow)
                }
              }
            }
          }
          groupActions = groupsToUpdate.map { currentGroup =>
            val newGroup = Group(currentGroup.id, currentGroup.name.replaceFirst(oldGroup.name, group.name))
            upsertAction(newGroup)
          }
        } yield workflowActions.flatten ++ groupActions

        updateActions.flatMap { actionsToExecute =>
          db.run(txHandler(DBIO.seq(actionsToExecute: _*).transactionally))
        }
      }.map(_ => group).cached(replace = true)
    } else throw new ServerException(s"Unable to update group ${group.name} because its name is invalid")
  }

  def deleteById(id: String): Future[Boolean] = {
    if (id == DefaultGroup.id.get) {
      throw new ServerException(s"Unable to delete default group")
    } else {
      for {
        group <- findByIdHead(id)
        groups <- findGroupAndSubgroups(group.name)
        result <- deleteYield(groups)
      } yield result
    }
  }

  def deleteByName(name: String): Future[Boolean] = {
    if (name == DefaultGroup.name) {
      throw new ServerException(s"Unable to delete default group")
    } else {
      for {
        groups <- findGroupAndSubgroups(name)
        result <- deleteYield(groups)
      } yield result
    }
  }

  def deleteAllGroups(): Future[Boolean] =
    for {
      groups <- findAll()
      result <- deleteYield(groups)
    } yield result

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

  //scalastyle:off
  private[services] def deleteYield(groups: Seq[Group]): Future[Boolean] = {
    val deleteActions = groups.map { currentGroup =>
      if (currentGroup.id.isDefined && currentGroup.id.get != DefaultGroup.id.get && currentGroup.name != DefaultGroup.name) {
        log.debug(s"Deleting group ${currentGroup.id} ${currentGroup.name}")
        for {
          workflowsToUpdate <- workflowPgService.findByGroupID(currentGroup.id.get)
          workflowActions = workflowsToUpdate.map { workflow =>
            workflowPgService.deleteByIDAction(workflow.id.get)
          }
        } yield workflowActions :+ deleteByIDAction(currentGroup.id.get)
      } else Future(Seq.empty)
    }

    Future.sequence(deleteActions).map { actionsSequence =>
      val actions = actionsSequence.flatten
      Try(db.run(txHandler(DBIO.seq(actions: _*).transactionally))
        .remove(groups.filterNot(_.id != DefaultGroup.id.get).map(getSpartaEntityId(_)): _*)) match {
        case Success(_) =>
          log.info(s"Groups ${groups.map(_.name).mkString(",")} deleted")
          true
        case Failure(e) =>
          throw e
      }
    }
  }

  /** Ignite predicates */
  private def ignitePredicateByName(name: String): ScanQuery[String, Group] = new ScanQuery[String, Group](
    new IgniteBiPredicate[String, Group]() {
      override def apply(k: String, value: Group) = value.name.equals(name)
    }
  )
}
