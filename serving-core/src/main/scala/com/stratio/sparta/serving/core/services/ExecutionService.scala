/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

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

  def setLaunchDate(executionId: String, launchDate: DateTime): Try[WorkflowExecution] = {
    findById(executionId) match {
      case Success(execution) =>
        update(execution.copy(
          genericDataExecution = execution.genericDataExecution.map(_.copy(launchDate = Option(launchDate)))
        ))
      case Failure(_) => throw new ServerException(s"Unable to update launch date with id: $executionId.")
    }
  }

  def setStartDate(executionId: String, startDate: DateTime): Try[WorkflowExecution] = {
    findById(executionId) match {
      case Success(execution) =>
        update(execution.copy(
          genericDataExecution = execution.genericDataExecution.map(_.copy(startDate = Option(startDate)))
        ))
      case Failure(_) => throw new ServerException(s"Unable to update start date with id: $executionId.")
    }
  }

  def setEndDate(executionId: String, endDate: DateTime): Try[WorkflowExecution] = {
    findById(executionId) match {
      case Success(execution) =>
        update(execution.copy(
          genericDataExecution = execution.genericDataExecution.map(_.copy(endDate = Option(endDate)))
        ))
      case Failure(_) => throw new ServerException(s"Unable to update end date with id: $executionId.")
    }
  }

  def setLastError(executionId: String, error: WorkflowError): Try[WorkflowExecution] = {
    findById(executionId) match {
      case Success(execution) =>
        update(execution.copy(
          genericDataExecution = execution.genericDataExecution.map(_.copy(lastError = Option(error)))
        ))
      case Failure(_) => throw new ServerException(s"Unable to update error with id: $executionId.")
    }
  }

  def setSparkUri(executionId: String, uri: Option[String]): Try[WorkflowExecution] = {
      findById(executionId) match {
        case Success(execution) =>
          update(execution.copy(
            marathonExecution = execution.marathonExecution.map(_.copy(sparkURI = uri))
          ))
        case Failure(_) => throw new ServerException(s"Unable to update Spark URI with id: $executionId.")
      }
  }

  def clearLastError(executionId: String): Try[WorkflowExecution] = {
    findById(executionId) match {
      case Success(execution) =>
        log.debug(s"Clearing last workflow execution error with id $executionId")
        update(execution.copy(
          genericDataExecution = execution.genericDataExecution.map(_.copy(lastError = None))
        ))
      case Failure(_) =>
        throw new ServerException(s"Unable to clear last error with id: $executionId.")
    }
   }
}
