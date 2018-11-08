/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import java.util.UUID
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import org.joda.time.DateTime
import slick.jdbc.PostgresProfile

import com.stratio.sparta.core.models.{DebugResults, WorkflowError}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.dao.DebugWorkflowDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.workflow.{DebugWorkflow, Workflow}
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection

//scalastyle:off
class DebugWorkflowPostgresDao extends DebugWorkflowDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  import profile.api._

  def findDebugWorkflowById(id: String): Future[DebugWorkflow] = findByIdHead(id)

  def getResultsByID(id: String): Future[DebugResults] = {
    for {
      resultStep <- getDebugStepData(id)
      debugWorkflow <- findDebugWorkflowById(id)
    } yield {
      debugWorkflow.result match {
        case Some(result) if result.endExecutionDate.isDefined =>
          val map = resultStep.flatMap { element => Map(element.step -> element) }.toMap
          result.copy(stepResults = map)
        case _ => throw new ServerException(s"No results for workflow id=$id")
      }
    }
  }

  def createDebugWorkflow(debugWorkflow: DebugWorkflow): Future[DebugWorkflow] = {
    debugWorkflow.workflowOriginal.id.orElse(debugWorkflow.id) match {
      case Some(id) =>
        removeDebugStepData(id)
        findByID(id).flatMap { exists =>
          val newDebug = exists.fold(addFieldsToCreatedDebugWorkflow(debugWorkflow)) { existingWk =>
            addFieldsToUpdatedDebugWorkflow(debugWorkflow, existingWk)
          }
          upsert(newDebug).map(_ => newDebug)
        }
      case None =>
        val newDebug = addFieldsToCreatedDebugWorkflow(debugWorkflow)
        upsert(newDebug).map(_ => newDebug)
    }
  }

  def setSuccessful(id: String, state: Boolean): Future[DebugWorkflow] = {
    for {
      actualDebug <- findDebugWorkflowById(id)
      newResult = actualDebug.result match {
        case Some(result) =>
          result.copy(debugSuccessful = state)
        case None =>
          DebugResults(state)
      }
      newDebug = actualDebug.copy(result = Option(newResult))
      _ <- upsert(newDebug)
    } yield newDebug

  }

  def setEndDate(id: String): Future[DebugWorkflow] = {
    log.debug(s"Setting end date to debug execution with id $id")
    for {
      actualDebug <- findDebugWorkflowById(id)
      newDebug <- actualDebug.result match {
        case Some(result) =>
          val newResult = result.copy(endExecutionDate = Option(new DateTime()))
          val newDebug = actualDebug.copy(result = Option(newResult))

          upsert(newDebug).map(_ => newDebug)
        case None => Future(actualDebug)
      }
    } yield newDebug
  }

  def clearLastError(id: String): Future[DebugWorkflow] = {
    log.debug(s"Clearing last debug execution error with id $id")
    setError(id, None)
  }

  //scalastyle:off
  def setError(id: String, error: Option[WorkflowError]): Future[DebugWorkflow] = {
    log.debug(s"Setting error to debug execution error with id $id")
    for {
      actualDebug <- findDebugWorkflowById(id)
      debugWithErrors = error match {
        case Some(wError) =>
          wError.step match {
            case Some(wErrorStep) =>
              val debugResult = actualDebug.result match {
                case Some(result) => result.copy(stepErrors = result.stepErrors ++ Map(wErrorStep -> wError))
                case None => DebugResults(debugSuccessful = false, stepErrors = Map(wErrorStep -> wError))
              }
              actualDebug.copy(result = Option(debugResult))
            case None =>
              val newDebugResult = actualDebug.result match {
                case Some(result) => result.copy(genericError = error)
                case None => DebugResults(debugSuccessful = false, stepResults = Map.empty, stepErrors = Map.empty, genericError = error)
              }
              actualDebug.copy(result = Option(newDebugResult))
          }
        case None =>
          actualDebug.copy(result = actualDebug.result.map(result => result.copy(genericError = None, stepErrors = Map.empty)))
      }
      newDebug <- upsert(debugWithErrors).map(_ => debugWithErrors)
    } yield newDebug
  }

  def removeDebugStepData(id: String): Future[Boolean] = {
    for {
      resultStep <- db.run(resultTable.filter(_.workflowId === id).result)
      _ <- deleteResultList(resultStep.map(_.id))
    } yield {
      log.info(s"DebugStep data for workflow $id  deleted")
      true
    }
  }

  def deleteDebugWorkflowByID(id: String): Future[Boolean] = {
    for {
      debug <- findByIdHead(id)
      result <- deleteYield(Seq(debug))
    } yield result
  }

  def deleteAllDebugWorkflows(): Future[Boolean] = {
    for {
      all <- findAll()
      result <- deleteYield(all)
    } yield result
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

  private[services] def deleteYield(debugWorkflowLists: Seq[DebugWorkflow]): Future[Boolean] = {
    val ids = debugWorkflowLists.flatMap(_.id.toList)
    for {
      _ <- deleteList(ids)
      _ <- Future.sequence(ids.map(removeDebugStepData))
    } yield {
      log.info(s"Debug Workflows with ids = ${ids.mkString(",")} deleted")
      true
    }
  }

  private[services] def getDebugStepData(id: String) = {
    for {
      resultStep <- db.run(resultTable.filter(_.workflowId === id).result)
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

  private[services] def addId(workflow: Workflow, force: Boolean = false): Workflow =
    if (workflow.id.notBlank.isEmpty || (workflow.id.notBlank.isDefined && force))
      workflow.copy(id = Some(UUID.randomUUID.toString))
    else workflow

  private[services] def addFieldsToCreatedDebugWorkflow(
                                                         debugWorkflow: DebugWorkflow
                                                       ): DebugWorkflow = {
    val debugId = debugWorkflow.id.orElse(addId(debugWorkflow.workflowOriginal).id)
    val workflowOriginalModified = addSpartaVersion(addCreationDate(debugWorkflow.workflowOriginal.copy(id = debugId)))
    val workflowDebugModified = debugWorkflow.transformToWorkflowRunnable.copy(id = debugId)

    debugWorkflow.copy(
      workflowOriginal = workflowOriginalModified,
      id = debugId,
      workflowDebug = Option(workflowDebugModified),
      result = None
    )
  }

  private[services] def addFieldsToUpdatedDebugWorkflow(
                                                         debugWorkflow: DebugWorkflow,
                                                         oldDebugWorkflow: DebugWorkflow
                                                       ): DebugWorkflow = {
    val debugId = oldDebugWorkflow.id.orElse(addId(oldDebugWorkflow.workflowOriginal).id)
    val workflowOriginalModified = addSpartaVersion(addUpdateDate(debugWorkflow.workflowOriginal.copy(id = debugId)))
    val workflowDebugModified = debugWorkflow.transformToWorkflowRunnable.copy(id = debugId)

    debugWorkflow.copy(
      workflowOriginal = workflowOriginalModified,
      id = debugId,
      workflowDebug = Option(workflowDebugModified),
      result = None
    )
  }
}
