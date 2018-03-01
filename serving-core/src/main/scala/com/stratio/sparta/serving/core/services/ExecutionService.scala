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
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.Try

class ExecutionService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  def findById(id: String): Try[WorkflowExecution] =
    Try {
      val executionPath = s"${AppConstant.WorkflowExecutionsZkPath}/$id"

      if (CuratorFactoryHolder.existsPath(executionPath))
        read[WorkflowExecution](new String(curatorFramework.getData.forPath(executionPath)))
      else throw new ServerException(s"No execution context with id $id")
    }

  def findAll(): Try[Seq[WorkflowExecution]] =
    Try {
      val executionPath = s"${AppConstant.WorkflowExecutionsZkPath}"

      if (CuratorFactoryHolder.existsPath(executionPath)) {
        val children = curatorFramework.getChildren.forPath(executionPath)
        val workflowExecutions = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[WorkflowExecution](new String(curatorFramework.getData.forPath(
            s"${AppConstant.WorkflowExecutionsZkPath}/$element")))
        )
        workflowExecutions
      } else Seq.empty[WorkflowExecution]
    }

  def create(execution: WorkflowExecution): Try[WorkflowExecution] = {
    val executionPath = s"${AppConstant.WorkflowExecutionsZkPath}/${execution.id}"

    if (CuratorFactoryHolder.existsPath(executionPath)) {
      update(execution)
    } else {
      Try {
        log.info(s"Creating execution with id: ${execution.id}")
        curatorFramework.create.creatingParentsIfNeeded.forPath(executionPath, write(execution).getBytes)
        execution
      }
    }
  }

  def update(execution: WorkflowExecution): Try[WorkflowExecution] = {
    Try {
      val executionPath = s"${AppConstant.WorkflowExecutionsZkPath}/${execution.id}"

      if (CuratorFactoryHolder.existsPath(executionPath)) {
        curatorFramework.setData().forPath(executionPath, write(execution).getBytes)
        execution
      } else create(execution).getOrElse(throw new ServerException(
        s"Unable to create execution with id: ${execution.id}."))
    }
  }

  def delete(id: String): Try[Unit] =
    Try {
      val executionPath = s"${AppConstant.WorkflowExecutionsZkPath}/$id"

      if (CuratorFactoryHolder.existsPath(executionPath)) {
        log.info(s"Deleting execution with id: $id")
        curatorFramework.delete().forPath(executionPath)
      } else throw new ServerException(s"No execution with id $id")
    }

  def deleteAll(): Try[Unit] =
    Try {
      val executionPath = s"${AppConstant.WorkflowExecutionsZkPath}"

      if (CuratorFactoryHolder.existsPath(executionPath)) {
        val children = curatorFramework.getChildren.forPath(executionPath)

        JavaConversions.asScalaBuffer(children).toList.foreach(element =>
          curatorFramework.delete().forPath(s"${AppConstant.WorkflowExecutionsZkPath}/$element"))
      }
    }
}
