/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import java.util.UUID

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.{Group, WorkflowsQuery}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

class GroupService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  private val workflowService = new WorkflowService(curatorFramework)
  private val regexGroups = "^(?!.*[/]{2}.*$)(^(/home)+(/)*([a-z0-9-/]*)$)"

  private[sparta] def addID(group: Group) : Group = {
    if ( (group.id.isEmpty ||
      group.id.notBlank.isEmpty && !group.id.get.equals(DefaultGroup.id.get)) &&
      !group.name.equals(DefaultGroup.name))
      group.copy(id = Some(UUID.randomUUID.toString))
    else group
  }

  def initialize(): Try[Unit] = {
    Try {
      if (findByID(DefaultGroup.id.get).toOption.isEmpty) {
        create(DefaultGroup)
        log.debug("The group initialization has been completed")
      } else log.debug("The default group is already created")
    }
  }



  def findByID(id: String): Try[Group] =
    Try {
      val groupLocation = s"$GroupZkPath/$id"
      if (CuratorFactoryHolder.existsPath(groupLocation)) {
        read[Group](new String(curatorFramework.getData.forPath(groupLocation)))
      } else throw new ServerException(s"No group found")
    }

  def findByName(name: String): Try[Group] =
    Try {
      val searchName =
        findAll.filter(groupItem => groupItem.name.equals(name))
      if (searchName.seq.nonEmpty)
        searchName.seq.head
      else throw new ServerException(s"No group found")
    }

  def findAll: Seq[Group] = {
    Try {
      JavaConversions.asScalaBuffer(curatorFramework.getChildren.forPath(GroupZkPath)).toList.map(groupID =>
        read[Group](new String(curatorFramework.getData.forPath(s"$GroupZkPath/$groupID"))))
    }.getOrElse(Seq.empty[Group])
  }

  private def findGroupAndSubgroups(name: String): Try[Seq[Group]] =
    Try {
      val subgroups =
        findAll.filter(groupItem => groupItem.name.startsWith(name))
      if (subgroups.seq.nonEmpty)
        subgroups
      else throw new ServerException(s"No subgroups found")
    }

  def create(group: Group): Try[Group] = Try {
    findByName(group.name) match {
      case Failure(_) => if(group.name.matches(regexGroups)) createGroupZK(group)
      else throw new ServerException(s"Unable to create group ${group.name} because its name is invalid")
      case Success(alreadyExisting) =>
        throw new ServerException(s"Unable to create group ${alreadyExisting.name} because it already exists")
    }
  }

  private def createGroupZK(group: Group): Group = {
    val currentGroup = addID(group)
    val groupLocation = s"$GroupZkPath/${currentGroup.id.get}"
    log.debug(s"Creating group ${currentGroup.id.get}")
    curatorFramework.create.creatingParentsIfNeeded.forPath(groupLocation, write(currentGroup).getBytes)
    currentGroup
  }

  private def updateGroupZK(group: Group): Group = {
    val groupLocation = s"$GroupZkPath/${group.id.get}"
    if (group.id.get != DefaultGroup.id.get && CuratorFactoryHolder.existsPath(groupLocation)) {
      curatorFramework.setData().forPath(groupLocation, write(group).getBytes)
      group
    } else throw new ServerException(s"Unable to update group ${group.id.get}: group not found")
  }

  def createList(groups: Seq[Group]): Seq[Group] =
    groups.flatMap(group => create(group).toOption)

  def update(group: Group): Try[Group] =
    Try {
      if (!Group.isValid(group))
        throw new ServerException(s"Unable to update group ${group.id.get} with name ${group.name}:" +
          s"invalid name detected")
      else if (findByName(group.name).isSuccess){
        throw new ServerException(s"Unable to update group ${group.id.get} with name ${group.name}:" +
          s"target group already exists")
      }
      else {
        findByID(group.id.get) match {
          case Failure(_) => throw new ServerException(s"Unable to update group ${group.id.get}: group not found")
          case Success(oldGroup) =>
            findGroupAndSubgroups(oldGroup.name) match {
              case Failure(_) =>
                throw new ServerException(s"Unable to update group ${group.id.get} and its related subgroups")
              case Success(groups) =>
                groups.map { currentGroup =>
                  val newGroup = Group(currentGroup.id, currentGroup.name.replaceFirst(oldGroup.name, group.name))
                  updateGroupZK(newGroup)
                }
                group
            }
        }
      }
    }

  def deleteById(id: String): Try[Unit] =
    Try {
      if(id != DefaultGroup.id.get) {
        findByID(id) match {
          case Success(group) => deleteByName(group.name)
          case Failure(_) => new ServerException(s"No group available to delete")
        }
      } else throw new ServerException(s"No group available to delete")
    }


  // Private method to remove the ZKNode and all the workflows associated with that group ID
  private def deleteGroupWorkflowsAndZK(id: String): Try[Unit] =
    Try {
      val groupLocation = s"$GroupZkPath/$id"
      if (CuratorFactoryHolder.existsPath(groupLocation) && id != DefaultGroup.id.get) {
        log.debug(s"Deleting group")
        val workflowsInGroup = workflowService.findList(WorkflowsQuery(Option(id))).flatMap(_.id)
        workflowService.deleteList(workflowsInGroup) match {
          case Success(_) =>
            curatorFramework.delete().forPath(groupLocation)
          case Failure(e) =>
            log.error("Error deleting workflows in group", e)
        }
      } else throw new ServerException(s"No group available to delete")
    }

  def deleteByName(name: String): Try[Unit] =
    Try {
      findGroupAndSubgroups(name).toOption
        .fold(throw new ServerException(s"No group available to delete")){ groupsToDelete =>
          groupsToDelete.foreach(
            group =>
              deleteGroupWorkflowsAndZK(group.id.get)
          )
        }
    }

  def deleteAll(): Try[Unit] =
    Try {
      if (CuratorFactoryHolder.existsPath(GroupZkPath)) {
        log.debug(s"Deleting all existing groups")
        val children = curatorFramework.getChildren.forPath(GroupZkPath)
        val groups = JavaConversions.asScalaBuffer(children).toList.map(group =>
          read[Group](new String(curatorFramework.getData.forPath(s"$GroupZkPath/$group"))))
        groups.foreach { group =>
          if (group.id.get != DefaultGroup.id.get)
            try {
              deleteGroupWorkflowsAndZK(group.id.get)
            } catch {
              case e: Exception => log.error(s"Error deleting group ${group.name} with id ${group.id}", e)
            }
        }
        log.debug(s"All groups deleted")
      }
    }


}
