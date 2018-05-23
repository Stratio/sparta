/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.actor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

import akka.actor.{Actor, Props}
import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.{Config, ConfigFactory}
import org.json4s.jackson.Serialization._
import slick.jdbc.JdbcProfile

import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.ExecutionChange
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.dao.ExecutionHistoryDaoImpl
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode
import com.stratio.sparta.serving.core.models.history.WorkflowExecutionHistory
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution

//scalastyle:off
class ExecutionHistoryActor(val profileHistory: JdbcProfile, val config: Config) extends Actor with ExecutionHistoryDaoImpl with SLF4JLogging {

  override val profile = profileHistory

  import profile.api._

  override val db: profile.api.Database = Database.forConfig("", config)

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

  def props(profile: JdbcProfile, config: Config = SpartaConfig.getSpartaPostgres.getOrElse(ConfigFactory.load())) = Props(new ExecutionHistoryActor(profile, config))

  case class QueryAll()

  case class QueryByWorkflowId(workflowId: String)

  case class QueryByUserId(userId: String)

  implicit def executionToDb(workflowExecution: WorkflowExecution): WorkflowExecutionHistory = {
    WorkflowExecutionHistory(
      executionId = workflowExecution.genericDataExecution.map(_.executionId).get,
      workflowId = workflowExecution.genericDataExecution.flatMap(_.workflow.id).get,
      executionMode = workflowExecution.genericDataExecution.map(ge => ge.executionMode.toString).getOrElse(WorkflowExecutionMode.marathon.toString),
      launchDate = workflowExecution.genericDataExecution.flatMap(ge => ge.launchDate.map(d => d.getMillis)).orElse(None),
      startDate = workflowExecution.genericDataExecution.flatMap(ge => ge.startDate.map(d => d.getMillis)).orElse(None),
      endDate = workflowExecution.genericDataExecution.flatMap(ge => ge.endDate.map(d => d.getMillis)).orElse(None),
      userId = workflowExecution.genericDataExecution.flatMap(ge => ge.userId).orElse(None),
      lastError = workflowExecution.genericDataExecution.flatMap(ge => ge.lastError.map(le => write(le))).orElse(None),
      genericExecution = write(workflowExecution.genericDataExecution))
  }

}
