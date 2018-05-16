/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.actor

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.{Actor, Props}
import akka.event.slf4j.SLF4JLogging
import slick.jdbc.H2Profile.api._

import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.{ExecutionChange, ExecutionRemove}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution
import com.stratio.sparta.serving.core.utils.ExecutionHistoryDaoImpl
import org.json4s.jackson.Serialization._
//scalastyle:off
class ExecutionHistoryActor extends Actor with ExecutionHistoryDaoImpl with SLF4JLogging {

  import ExecutionHistoryActor._

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[ExecutionChange])
    context.system.eventStream.subscribe(self, classOf[ExecutionRemove])
    createSchema()
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[ExecutionChange])
    context.system.eventStream.unsubscribe(self, classOf[ExecutionRemove])
  }

  override def receive: Receive = {
    case ec: ExecutionChange =>
      ec.execution.genericDataExecution.map(ge => ge.workflow.lastUpdateDate match {
        case Some(_) => update(ec.execution)  onFailure {
          case f: Exception => log.error(f.getMessage, f)
        }
        case None => insert(ec.execution) onFailure {
          case f: Exception => log.error(f.getMessage, f)
        }
      })
    case QueryAll() => sender ! selectAll()
    case QueryByWorkflowId(id) => sender ! findByWorkflowId(id)
    case QueryByUserId(id) => sender ! findByUserId(id)
    case _ => unhandled("")
  }
}

//scalastyle:off
object ExecutionHistoryActor extends SpartaSerializer {

  case class QueryAll()

  case class QueryByWorkflowId(workflowId: String)

  case class QueryByUserId(userId: String)


  implicit def executionToDb(workflowExecution: WorkflowExecution): WorkflowExecutionHistory = {
    WorkflowExecutionHistory(
      id = workflowExecution.id,
      workflowId = workflowExecution.genericDataExecution.flatMap(_.workflow.id).get,
      executionMode = workflowExecution.genericDataExecution.get.executionMode.toString,
      launchDate = workflowExecution.genericDataExecution.get.endDate.map(d => d.getMillis),
      startDate = workflowExecution.genericDataExecution.get.endDate.map(d => d.getMillis),
      endDate = workflowExecution.genericDataExecution.get.endDate.map(d => d.getMillis),
      workflow = write(workflowExecution.genericDataExecution.get.workflow))
  }

  def props() = Props[ExecutionHistoryActor]

  case class WorkflowExecutionHistory(id: String, workflowId: String, executionMode: String,
                                      launchDate: Option[Long] = None, startDate: Option[Long] = None,
                                      endDate: Option[Long] = None, userId: Option[String] = None,
                                      workflow: String)

  class WorkflowExecutionHistoryTable(tag: Tag) extends Table[WorkflowExecutionHistory](tag, "workflow_execution_history") {

    def id = column[String]("execution_id")

    def workflowId = column[String]("workflow_id")

    def executionMode = column[String]("execution_mode")

    def launchDate = column[Option[Long]]("launch_date")

    def startDate = column[Option[Long]]("start_date")

    def endDate = column[Option[Long]]("end_date")

    def userId = column[Option[String]]("user_id")

    def workflow = column[String]("workflow")

    def * = (id, workflowId, executionMode, launchDate, startDate, endDate, userId, workflow) <> (WorkflowExecutionHistory.tupled, WorkflowExecutionHistory.unapply)

    def pk = primaryKey("pk_wokflow_exec_history", id)
  }

  val workflowExecutionHistoryTable = TableQuery[WorkflowExecutionHistoryTable]

  val WorkflowExecutionHistoryKey = "workflow-exec-history"
}
