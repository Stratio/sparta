/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.dao

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import akka.event.slf4j.SLF4JLogging

import com.stratio.sparta.serving.api.utils.JdbcSlickUtils
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.history.WorkflowExecutionHistory

//scalastyle:off
trait ExecutionHistoryDao extends JdbcSlickUtils with SLF4JLogging {

  import profile.api._

  def upsert(workflowExecution: WorkflowExecutionHistory): Future[_] = {
    val dbioAction = DBIO.seq(
      table.insertOrUpdate(workflowExecution)
    ).transactionally
    db.run(txHandler(dbioAction))
  }

  private def txHandler(dbioAction: DBIOAction[Unit, NoStream, Effect.All with Effect.Transactional]) = {
    val txHandler = dbioAction.asTry.flatMap {
      case Failure(e: Throwable) => {
        log.error(s"Error in action execution", e)
        DBIO.failed(e)
      }
      case Success(s) => DBIO.successful(s)
    }
    txHandler
  }

  def createSchema(): Unit = {
    db.run(table.exists.result) onComplete {
      case Success(exists) =>
        log.info("Schema already exists")
      case Failure(e) => {
        log.info("Creating schema for executionHistory")
        val dbioAction = (
          for {
            _ <- table.schema.create
          } yield ()
          ).transactionally
        db.run(txHandler(dbioAction))
      }
    }
  }

  def selectAll(): Future[List[WorkflowExecutionHistory]] = {
    db.run(table.result).map(_.toList)
  }

  def findByWorkflowId(workflowId: String): Future[List[WorkflowExecutionHistory]] = {
    db.run(table.filter(_.workflowId === workflowId).result).map(_.toList)
  }

  def findByUserId(userId: String): Future[List[WorkflowExecutionHistory]] = {
    db.run(table.filter(_.userId === userId).result).map(_.toList)
  }

  class WorkflowExecutionHistoryTable(tag: Tag) extends Table[WorkflowExecutionHistory](tag, Some("public"),
    Try(SpartaConfig.getSpartaPostgres.get.getString("executionHistory.table")).getOrElse("workflow_execution_history")) {

    def executionId = column[String]("execution_id")

    def workflowId = column[String]("workflow_id")

    def executionMode = column[String]("execution_mode")

    def launchDate = column[Option[Long]]("launch_date")

    def startDate = column[Option[Long]]("start_date")

    def endDate = column[Option[Long]]("end_date")

    def userId = column[Option[String]]("user_id")

    def lastError = column[Option[String]]("lastError")

    def genericExecution = column[String]("genericExecution")

    def * = (executionId, workflowId, executionMode, launchDate, startDate, endDate, userId, lastError, genericExecution) <> (WorkflowExecutionHistory.tupled,
      WorkflowExecutionHistory.unapply)

    def pk = primaryKey(s"pk_${Try(SpartaConfig.getSpartaPostgres.get.getString("executionHistory.table")).getOrElse("workflow_execution_history")}", executionId)

    def executionIndex = index(s"idx__${Try(SpartaConfig.getSpartaPostgres.get.getString("executionHistory.table")).getOrElse("workflow_execution_history")}",
      executionId, unique = true)
  }

  val table = TableQuery[WorkflowExecutionHistoryTable]
}

trait ExecutionHistoryDaoImpl extends ExecutionHistoryDao