/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.actor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

import akka.actor.{Actor, Props}
import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.ConfigFactory
import org.json4s.jackson.Serialization._
import slick.jdbc.H2Profile.api._

import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.ExecutionChange
import com.stratio.sparta.serving.core.dao.ExecutionHistoryDaoImpl
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution

//scalastyle:off
class ExecutionHistoryActor extends Actor with ExecutionHistoryDaoImpl with SLF4JLogging {

  import ExecutionHistoryActor._

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[ExecutionChange])
    createSchema() onComplete {
      case Success(_) => log.info("Schema created")
      case Failure(f) => log.error(f.getMessage, f)
    }
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[ExecutionChange])
    db.close()
  }

  override def receive: Receive = {
    case ec: ExecutionChange => upsert(ec.execution) onFailure {
      case e: Exception => log.error("Error", e)
    }
    case QueryAll() => sender ! selectAll()
    case QueryByWorkflowId(id) => sender ! findByWorkflowId(id)
    case QueryByUserId(id) => sender ! findByUserId(id)
  }
}

//scalastyle:off
object ExecutionHistoryActor extends SpartaSerializer {

  def props() = Props[ExecutionHistoryActor]

  case class QueryAll()

  case class QueryByWorkflowId(workflowId: String)

  case class QueryByUserId(userId: String)

  implicit def executionToDb(workflowExecution: WorkflowExecution): WorkflowExecutionHistory = {
    WorkflowExecutionHistory(
      uniqueId = workflowExecution.uniqueId,
      executionId = workflowExecution.id,
      workflowId = workflowExecution.genericDataExecution.flatMap(_.workflow.id).get,
      executionMode = workflowExecution.genericDataExecution.map(ge => ge.executionMode.toString).getOrElse(WorkflowExecutionMode.marathon.toString),
      launchDate = workflowExecution.genericDataExecution.flatMap(ge => ge.launchDate.map(d => d.getMillis)).orElse(None),
      startDate = workflowExecution.genericDataExecution.flatMap(ge => ge.startDate.map(d => d.getMillis)).orElse(None),
      endDate = workflowExecution.genericDataExecution.flatMap(ge => ge.endDate.map(d => d.getMillis)).orElse(None),
      userId = workflowExecution.genericDataExecution.flatMap(ge => ge.userId).orElse(None),
      lastError = workflowExecution.genericDataExecution.flatMap(ge => ge.lastError.map(le => write(le))).orElse(None),
      genericExecution = write(workflowExecution.genericDataExecution))
  }

  case class WorkflowExecutionHistory(uniqueId: String,
                                      executionId: String,
                                      workflowId: String,
                                      executionMode: String,
                                      launchDate: Option[Long] = None,
                                      startDate: Option[Long] = None,
                                      endDate: Option[Long] = None,
                                      userId: Option[String] = None,
                                      lastError: Option[String] = None,
                                      genericExecution: String)

  class WorkflowExecutionHistoryTable(tag: Tag) extends Table[WorkflowExecutionHistory](tag, Some("public"),
    Try(ConfigFactory.load.getString("sparta.postgres.execHistory.table")).getOrElse("workflow_execution_history")) {

    def uniqueId = column[String]("uniqueId")

    def executionId = column[String]("execution_id")

    def workflowId = column[String]("workflow_id")

    def executionMode = column[String]("execution_mode")

    def launchDate = column[Option[Long]]("launch_date")

    def startDate = column[Option[Long]]("start_date")

    def endDate = column[Option[Long]]("end_date")

    def userId = column[Option[String]]("user_id")

    def lastError = column[Option[String]]("lastError")

    def genericExecution = column[String]("genericExecution")

    def * = (uniqueId, executionId, workflowId, executionMode, launchDate, startDate, endDate, userId, lastError, genericExecution) <> (WorkflowExecutionHistory.tupled,
      WorkflowExecutionHistory.unapply)

    def pk = primaryKey(s"pk_${Try(ConfigFactory.load.getString("sparta.postgres.execHistory.table")).getOrElse("workflow_execution_history")}", uniqueId)
  }

  val workflowExecutionHistoryTable = TableQuery[WorkflowExecutionHistoryTable]
}
