/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import akka.actor.{ActorRef, ActorSystem}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.models.{DebugResults, ResultStep, WorkflowError}
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.DebugWorkflow
import com.stratio.sparta.serving.core.services.WorkflowService._
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime
import org.json4s.jackson.Serialization.{read, _}

import scala.collection.JavaConversions
import scala.util.Try

class DebugWorkflowService(
                            curatorFramework: CuratorFramework,
                            override val serializerSystem: Option[ActorSystem] = None,
                            override val environmentStateActor: Option[ActorRef] = None
                          ) extends SpartaSerializer with SLF4JLogging {

  def createDebugWorkflow(debugWorkflow: DebugWorkflow): Try[DebugWorkflow] =
    Try {
      val storedWorkflow = debugWorkflow.workflowOriginal.id match {
        case Some(id) =>
          findByID(id).toOption
            .fold(writeDebugWorkflowInZk(createDebugWorkflowFromOriginal(debugWorkflow))) { existingWk =>
              updateDebugWorkflowInZk(createDebugWorkflowFromOriginal(debugWorkflow), existingWk)
            }
        case _ => writeDebugWorkflowInZk(createDebugWorkflowFromOriginal(debugWorkflow))
      }
      storedWorkflow
    }

  def deleteDebugWorkflowByID(id: String): Try[Unit] = {
    log.debug(s"Deleting workflow with id: $id")
    Try {
      val debugWorkflowPath = s"$DebugWorkflowZkPath/$id"

      if (CuratorFactoryHolder.existsPath(debugWorkflowPath)) {
        curatorFramework.delete().forPath(s"$DebugWorkflowZkPath/$id")
      } else throw new ServerException(s"No debug workflow with id $id")
    }
  }

  def deleteAllDebugWorkflows: Try[Unit] = {
    log.debug(s"Deleting all existing workflows")
    Try {
      val debugWorkflowPath = s"$DebugWorkflowZkPath"

      if (CuratorFactoryHolder.existsPath(debugWorkflowPath)) {
        val children = curatorFramework.getChildren.forPath(debugWorkflowPath)
        val workflows = JavaConversions.asScalaBuffer(children).toList.map(workflow =>
          read[DebugWorkflow](new String(curatorFramework.getData.forPath(
            s"$debugWorkflowPath/$workflow")))
        )

        try {
          workflows.foreach(workflow => deleteDebugWorkflowByID(workflow.workflowOriginal.id.get))
          log.debug(s"All workflows deleted")
        } catch {
          case e: Exception =>
            log.error("Error deleting debug workflows. The debug workflows deleted will be rolled back", e)
            Try(workflows.foreach(workflow => createDebugWorkflow(workflow)))
            throw new RuntimeException("Error deleting debug workflows", e)
        }
      }
    }
  }

  def findAll: List[DebugWorkflow] = {
    if (CuratorFactoryHolder.existsPath(DebugWorkflowZkPath)) {
      val children = curatorFramework.getChildren.forPath(DebugWorkflowZkPath)
      JavaConversions.asScalaBuffer(children).toList.flatMap(id => findByID(id).toOption)
    } else List.empty[DebugWorkflow]
  }

  def findByID(id: String): Try[DebugWorkflow] =
    Try {
      val debugWorkflowLocation = s"$DebugWorkflowZkPath/$id"
      if (CuratorFactoryHolder.existsPath(debugWorkflowLocation)) {
        read[DebugWorkflow](new String(curatorFramework.getData.forPath(debugWorkflowLocation)))
      } else throw new ServerException(errorFindById(id))
    }

  def getResultsByID(id: String): Try[DebugResults] =
    findByID(id).flatMap { debugWorkflow =>
      Try {
        debugWorkflow.result match {
          case Some(result) => result.copy(stepResults = getDebugStepData(id), stepErrors = getDebugStepError(id))
          case None => throw new ServerException(errorFindById(id))
        }
      }
    }

  def clearLastError(id: String): Try[Unit] = {
    log.debug(s"Clearing last debug execution error with id $id")
    setError(id, None)
  }

  def setSuccessful(id: String, state: Boolean): Try[DebugWorkflow] = {
    log.debug(s"Setting state to debug execution with id $id")
    Try {
      val debugWorkflowLocation = s"$DebugWorkflowZkPath/$id"
      if (CuratorFactoryHolder.existsPath(debugWorkflowLocation)) {
        val actualDebug = read[DebugWorkflow](new String(curatorFramework.getData.forPath(debugWorkflowLocation)))
        val newResult = actualDebug.result match {
          case Some(result) => result.copy(debugSuccessful = state)
          case None => DebugResults(state)
        }
        val newDebug = actualDebug.copy(result = Option(newResult))
        curatorFramework.setData().forPath(debugWorkflowLocation, write(newDebug).getBytes)
        newDebug
      } else throw new ServerException(errorFindById(id))
    }
  }

  def setEndDate(id: String): Try[Unit] = {
    log.debug(s"Setting end date to debug execution with id $id")
    Try {
      val debugWorkflowLocation = s"$DebugWorkflowZkPath/$id"
      if (CuratorFactoryHolder.existsPath(debugWorkflowLocation)) {
        val actualDebug = read[DebugWorkflow](new String(curatorFramework.getData.forPath(debugWorkflowLocation)))

        actualDebug.result.foreach { result =>
          val newResult = result.copy(endExecutionDate = Option(new DateTime()))
          val newDebug = actualDebug.copy(result = Option(newResult))

          curatorFramework.setData().forPath(debugWorkflowLocation, write(newDebug).getBytes)
        }
      } else throw new ServerException(errorFindById(id))
    }
  }

  //scalastyle:off
  def setError(id: String, error: Option[WorkflowError]): Try[Unit] = {
    log.debug(s"Setting error to debug execution error with id $id")
    Try {
      val debugWorkflowLocation = s"$DebugWorkflowZkPath/$id"
      if (CuratorFactoryHolder.existsPath(debugWorkflowLocation)) {
        val actualDebug = read[DebugWorkflow](new String(curatorFramework.getData.forPath(debugWorkflowLocation)))
        error match {
          case Some(wError) =>
            if (wError.step.isDefined) {
              val stepErrorId = s"${wError.step.get}-$id"
              val stepErrorLocation = s"$DebugStepErrorZkPath/$stepErrorId"
              if (CuratorFactoryHolder.existsPath(stepErrorLocation))
                curatorFramework.setData().forPath(stepErrorLocation, write(wError).getBytes)
              else curatorFramework.create().creatingParentsIfNeeded().forPath(stepErrorLocation, write(wError).getBytes)
            } else {
              val newDebugResult = actualDebug.result match {
                case Some(result) => result.copy(genericError = error)
                case None => DebugResults(debugSuccessful = true, stepResults = Map.empty, stepErrors = Map.empty, genericError = error)
              }
              val newDebug = actualDebug.copy(result = Option(newDebugResult))
              curatorFramework.setData().forPath(debugWorkflowLocation, write(newDebug).getBytes)
            }
          case None =>
            val newDebug = actualDebug.copy(result = actualDebug.result.map(result => result.copy(genericError = None, stepErrors = Map.empty)))
            curatorFramework.setData().forPath(debugWorkflowLocation, write(newDebug).getBytes)
            val stepErrorKey = curatorFramework.getChildren.forPath(DebugStepErrorZkPath)
            JavaConversions.asScalaBuffer(stepErrorKey).toList.foreach { element =>
              if (element.contains(id))
                curatorFramework.delete().forPath(s"$DebugStepErrorZkPath/$element")
            }
        }
      } else throw new ServerException(errorFindById(id))
    }
  }

  //scalastyle:on

  def removeDebugStepData(id: String): Try[Unit] = {
    Try {
      if (CuratorFactoryHolder.existsPath(DebugStepDataZkPath)) {
        val stepDataKey = curatorFramework.getChildren.forPath(DebugStepDataZkPath)
        JavaConversions.asScalaBuffer(stepDataKey).toList.foreach { element =>
          if (element.contains(id))
            curatorFramework.delete().forPath(s"$DebugStepDataZkPath/$element")
        }
      }
    }
  }

  private def getDebugStepData(id: String): Map[String, ResultStep] =
    Try {
      if (CuratorFactoryHolder.existsPath(DebugStepDataZkPath)) {
        val stepDataKey = curatorFramework.getChildren.forPath(DebugStepDataZkPath)
        JavaConversions.asScalaBuffer(stepDataKey).toList.flatMap { element =>
          if (element.contains(id))
            Try {
              val resultStep = read[ResultStep](
                new String(curatorFramework.getData.forPath(s"$DebugStepDataZkPath/$element")))
              resultStep.step -> resultStep
            }.toOption
          else None
        }.toMap
      } else Map.empty[String, ResultStep]
    }.getOrElse(Map.empty[String, ResultStep])

  private def getDebugStepError(id: String): Map[String, WorkflowError] =
    Try {
      if (CuratorFactoryHolder.existsPath(DebugStepErrorZkPath)) {
        val stepErrorKey = curatorFramework.getChildren.forPath(DebugStepErrorZkPath)
        JavaConversions.asScalaBuffer(stepErrorKey).toList.flatMap { element =>
          if (element.contains(id))
            Try {
              val workflowError = read[WorkflowError](
                new String(curatorFramework.getData.forPath(s"$DebugStepErrorZkPath/$element")))
              workflowError.step.get -> workflowError
            }.toOption
          else None
        }.toMap
      } else Map.empty[String, WorkflowError]
    }.getOrElse(Map.empty[String, WorkflowError])

  private def createDebugWorkflowFromOriginal(debugWorkflow: DebugWorkflow): DebugWorkflow =
    debugWorkflow.copy(workflowDebug = Option(debugWorkflow.transformToWorkflowRunnable))

  private def writeDebugWorkflowInZk(debugWorkflow: DebugWorkflow): DebugWorkflow = {
    val updatedOriginalWorkflow = debugWorkflow.copy(workflowOriginal =
      addCreationDate(addId(debugWorkflow.workflowOriginal)))
    val updatedDebugWorkflow = updatedOriginalWorkflow.copy(
      workflowDebug = debugWorkflow.workflowDebug.map { workflow =>
        workflow.copy(id = updatedOriginalWorkflow.workflowOriginal.id)
      }
    )
    curatorFramework.create.creatingParentsIfNeeded.forPath(
      s"$DebugWorkflowZkPath/${updatedDebugWorkflow.workflowOriginal.id.get}",
      write(updatedDebugWorkflow).getBytes)
    updatedDebugWorkflow
  }

  private def updateDebugWorkflowInZk(debugWorkflow: DebugWorkflow, oldDebugWorkflow: DebugWorkflow): DebugWorkflow = {
    val updatedWorkflowId = debugWorkflow.copy(workflowOriginal = addUpdateDate(debugWorkflow.workflowOriginal
      .copy(id = oldDebugWorkflow.workflowOriginal.id)))
    curatorFramework.setData().forPath(
      s"$DebugWorkflowZkPath/${debugWorkflow.workflowOriginal.id.get}", write(updatedWorkflowId).getBytes)
    updatedWorkflowId
  }

  private def errorFindById(id: String): String = s"No debug workflow found for workflow id: $id"

}
