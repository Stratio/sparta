/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.serving.core.services

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.{Group, WorkflowsQuery}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

class GroupService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  private val workflowService = new WorkflowService(curatorFramework)

  def initialize(): Try[Unit] = {
    Try {
      if (find(DefaultGroup).toOption.isEmpty) {
        create(Group(DefaultGroup))
        log.debug("The group initialization have been completed")
      } else log.debug("The default group is already created")
    }
  }

  def find(name: String): Try[Group] =
    Try {
      val groupLocation = s"$GroupZkPath/$name"

      if (CuratorFactoryHolder.existsPath(groupLocation)) {
        read[Group](new String(curatorFramework.getData.forPath(groupLocation)))
      } else throw new ServerException(s"No group found")
    }

  def findAll: Seq[Group] = {
    Try {
      JavaConversions.asScalaBuffer(curatorFramework.getChildren.forPath(GroupZkPath)).toList.map(name =>
        read[Group](new String(curatorFramework.getData.forPath(s"$GroupZkPath/$name"))))
    }.getOrElse(Seq.empty[Group])
  }

  def create(group: Group): Try[Group] = {
    val groupLocation = s"$GroupZkPath/${group.name}"

    if (CuratorFactoryHolder.existsPath(groupLocation)) {
      update(group)
    } else {
      Try {
        log.debug(s"Creating group")
        curatorFramework.create.creatingParentsIfNeeded.forPath(groupLocation, write(group).getBytes)
        group
      }
    }
  }

  def createList(groups: Seq[Group]): Seq[Group] =
    groups.flatMap(group => create(group).toOption)

  def update(group: Group): Try[Group] = {
    Try {
      val groupLocation = s"$GroupZkPath/${group.name}"
      if (CuratorFactoryHolder.existsPath(groupLocation)) {
        curatorFramework.setData().forPath(groupLocation, write(group).getBytes)
        group
      } else throw new ServerException(s"Unable to create group")
    }
  }

  def delete(name: String): Try[Unit] =
    Try {
      val groupLocation = s"$GroupZkPath/$name"
      if (CuratorFactoryHolder.existsPath(groupLocation) && name != DefaultGroup) {
        log.debug(s"Deleting group")
        val workflowsInGroup = workflowService.findList(WorkflowsQuery(Option(name))).flatMap(_.id)
        workflowService.deleteList(workflowsInGroup) match {
          case Success(_) =>
            curatorFramework.delete().forPath(groupLocation)
          case Failure(e) =>
            log.error("Error deleting workflows in group", e)
        }
      } else throw new ServerException(s"No group available to delete")
    }

  def deleteAll(): Try[Unit] =
    Try {
      if (CuratorFactoryHolder.existsPath(GroupZkPath)) {
        log.debug(s"Deleting all existing groups")
        val children = curatorFramework.getChildren.forPath(GroupZkPath)
        val groups = JavaConversions.asScalaBuffer(children).toList.map(group =>
          read[Group](new String(curatorFramework.getData.forPath(s"$GroupZkPath/$group"))))
        groups.foreach { group =>
          if (group.name != DefaultGroup)
            try {
              delete(group.name)
            } catch {
              case e: Exception => log.error(s"Error deleting group ${group.name}", e)
            }
        }
        log.debug(s"All groups deleted")
      }
    }

}
