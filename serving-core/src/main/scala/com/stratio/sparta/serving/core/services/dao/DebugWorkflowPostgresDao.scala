/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import org.joda.time.DateTime
import slick.jdbc.PostgresProfile

import com.stratio.sparta.core.models.{DebugResults, WorkflowError}
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.dao.DebugWorkflowDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.workflow.{DebugWorkflow, Workflow}
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection
import java.util.UUID

//scalastyle:off
class DebugWorkflowPostgresDao extends DebugWorkflowDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.db

  import profile.api._

  def findDebugWorkflowById(id: String): Future[DebugWorkflow] = findByIdHead(id)

  def getResultsByID(id: String): Future[DebugWorkflow] = {
    for {
      debugWorkflow <- findDebugWorkflowById(id)
      resultStep <- getDebugStepData(id)
    } yield {
      val copyDebug = debugWorkflow.result match {
        case Some(result) => {
          val map = resultStep.flatMap { element => Map(element.step -> element) }.toMap
          debugWorkflow.copy(result = Option(result.copy(stepResults = map)))
        }
        case None => throw new ServerException(s"No results for workflow id=$id")
      }
      copyDebug
    }
  }

  def createDebugWorkflow(debugWorkflow: DebugWorkflow): Future[DebugWorkflow] = {
    debugWorkflow.workflowOriginal.id match {
      case Some(id) => {
        (for {
          exists <- findByID(id)
        } yield {
          val newDebug = exists.fold(writeDebugWorkflow(createDebugWorkflowFromOriginal(debugWorkflow))) { existingWk =>
            updateDateDebugWorkflow(createDebugWorkflowFromOriginal(debugWorkflow), existingWk)
          }
          upsert(newDebug).map(_ => newDebug)
        }).flatMap(f => f)
      }
      case None => {
        val newDebug = writeDebugWorkflow(createDebugWorkflowFromOriginal(debugWorkflow))
        upsert(newDebug).map(_ => newDebug)
      }
    }
  }

  def setSuccessful(id: String, state: Boolean): Future[Unit] = {
    for {
      actualDebug <- findDebugWorkflowById(id)
    } yield {
      val newResult = actualDebug.result match {
        case Some(result) => result.copy(debugSuccessful = state)
        case None => DebugResults(state)
      }
      val newDebug = actualDebug.copy(result = Option(newResult))
      db.run(table.filter(_.id === id).update(newDebug))
    }
  }

  def setEndDate(id: String): Future[Unit] = {
    log.debug(s"Setting end date to debug execution with id $id")
    for {
      actualDebug <- findDebugWorkflowById(id)
    } yield {
      actualDebug.result.foreach { result =>
        val newResult = result.copy(endExecutionDate = Option(new DateTime()))
        val newDebug = actualDebug.copy(result = Option(newResult))
        db.run(table.filter(_.id === id).update(newDebug))
      }
    }
  }

  def clearLastError(id: String): Future[Unit] = {
    log.debug(s"Clearing last debug execution error with id $id")
    setError(id, None)
  }

  //scalastyle:off
  def setError(id: String, error: Option[WorkflowError]): Future[Unit] = {
    log.debug(s"Setting error to debug execution error with id $id")
    for {
      actualDebug <- findDebugWorkflowById(id)
    } yield {
      val debugWithErrors = error match {
        case Some(wError) =>
          wError.step match {
            case Some(wErrorStep) => {
              val debugResult = actualDebug.result match {
                case Some(result) => result.copy(stepErrors = result.stepErrors ++ Map(wErrorStep -> wError))
                case None => DebugResults(false, stepErrors = Map(wErrorStep -> wError))
              }
              actualDebug.copy(result = Option(debugResult))
            }
            case None => {
              val newDebugResult = actualDebug.result match {
                case Some(result) => result.copy(genericError = error)
                case None => DebugResults(debugSuccessful = true, stepResults = Map.empty, stepErrors = Map.empty, genericError = error)
              }
              actualDebug.copy(result = Option(newDebugResult))
            }
          }
        case None =>
          actualDebug.copy(result = actualDebug.result.map(result => result.copy(genericError = None, stepErrors = Map.empty)))
      }
      db.run(table.filter(_.id === id).update(debugWithErrors))
    }
  }

  def removeDebugStepData(id: String): Future[Boolean] = {
    for {
      debug <- findDebugWorkflowById(id)
      resultStep <- db.run(resultTable.filter(_.id startsWith debug.id.get).result)
    } yield {
      Try(deleteResultList(resultStep.flatMap(_.id.toList))) match {
        case Success(_) =>
          log.info(s"DebugStep data for workflow $id  deleted")
        case Failure(e) =>
          throw e
      }
      true
    }
  }

  def deleteDebugWorkflowByID(id: String): Future[Boolean] = {
    for {
      debug <- findByIdHead(id)
    } yield deleteYield(Seq(debug))
  }

  def deleteAllDebugWorkflows(): Future[Boolean] = {
    for {
      all <- findAll()
    } yield deleteYield(all)
  }

  /** PRIVATE METHODS */

  private[services] def deleteResultList(items: Seq[String]): Future[_] = {
    val dbioAction = DBIO.seq(
      resultTable.filter(_.id inSet items).delete
    ).transactionally
    db.run(dbioAction)
  }

  private[services] def filterByIdReal(id: String): Future[Seq[DebugWorkflow]] =
    db.run(table.filter(_.id === Option(id)).result)

  private[services] def findByIdHead(id: String): Future[DebugWorkflow] =
    for {
      debugList <- filterByIdReal(id)
    } yield {
      if (debugList.nonEmpty)
        debugList.head
      else throw new ServerException(s"No DebugWorkflow  found with  id $id")
    }

  private[services] def deleteYield(debugWorkflowLists: Seq[DebugWorkflow]): Boolean = {
    val ids = debugWorkflowLists.flatMap(_.id.toList)
    Try(deleteList(ids)) match {
      case Success(_) =>
        log.info(s"Debug Workflows with ids=${ids.mkString(",")} deleted")
      case Failure(e) =>
        throw e
    }
    true
  }

  private[services] def getDebugStepData(id: String) = {
    for {
      resultStep <- db.run(resultTable.filter(_.id startsWith id).result)
    } yield {
      resultStep
    }
  }

  private[services] def addUpdateDate(workflow: Workflow): Workflow =
    workflow.copy(lastUpdateDate = Some(new DateTime()))

  private[services] def addCreationDate(workflow: Workflow): Workflow =
    workflow.creationDate match {
      case None => workflow.copy(creationDate = Some(new DateTime()))
      case Some(_) => workflow
    }

  private[services] def addSpartaVersion(workflow: Workflow): Workflow =
    workflow.versionSparta match {
      case None => workflow.copy(versionSparta = Some(AppConstant.version))
      case Some(_) => workflow
    }

  private[services] def createDebugWorkflowFromOriginal(debugWorkflow: DebugWorkflow): DebugWorkflow =
    debugWorkflow.copy(id = debugWorkflow.workflowOriginal.id,
      workflowDebug = Option(debugWorkflow.transformToWorkflowRunnable))

  private[services] def writeDebugWorkflow(debugWorkflow: DebugWorkflow): DebugWorkflow = {
    val updatedOriginalWorkflow = debugWorkflow.copy(workflowOriginal =
      addCreationDate(addId(addSpartaVersion(debugWorkflow.workflowOriginal))))
    val updatedDebugWorkflow = updatedOriginalWorkflow.copy(
      workflowDebug = debugWorkflow.workflowDebug.map { workflow =>
        workflow.copy(id = updatedOriginalWorkflow.workflowOriginal.id)
      }
    )
    updatedDebugWorkflow
  }

  private[services] def addId(workflow: Workflow, force: Boolean = false): Workflow =
    if (workflow.id.notBlank.isEmpty || (workflow.id.notBlank.isDefined && force))
      workflow.copy(id = Some(UUID.randomUUID.toString))
    else workflow

  private[services] def updateDateDebugWorkflow(debugWorkflow: DebugWorkflow, oldDebugWorkflow: DebugWorkflow): DebugWorkflow = {
    val updatedWorkflowId = debugWorkflow.copy(workflowOriginal = addUpdateDate(debugWorkflow.workflowOriginal
      .copy(id = oldDebugWorkflow.workflowOriginal.id)))
    updatedWorkflowId
  }
}
