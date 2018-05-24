/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.actor.StatusHistoryActor._
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.StatusChange
import com.stratio.sparta.serving.core.dao.StatusHistoryDaoImpl
import com.stratio.sparta.serving.core.models.history.WorkflowStatusHistory
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import com.typesafe.config.Config
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class StatusHistoryActor(val profileHistory: JdbcProfile, val config: Config) extends Actor
  with StatusHistoryDaoImpl with SLF4JLogging {

  val profile = profileHistory

  import profile.api._

  override val db: profile.api.Database = Database.forConfig("", config)

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[StatusChange])
    createSchema() onComplete {
      case Success(s) => log.info("Schema for status history table created")
      case Failure(e) => log.error(e.getMessage, e)
    }
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[StatusChange])
    db.close()
  }

  override def receive: Receive = {
    case sc: StatusChange => upsert(sc.workflowStatus)
    case FindAll() => sender ! findAll()
    case FindByWorkflowId(id) => sender ! findByWorkflowId(id)
  }
}

//scalastyle:off
object StatusHistoryActor {

  case class FindAll()

  case class FindByWorkflowId(id: String)

  case class FindByUserId(userId: String)

  implicit def statusToDb(workflowStatus: WorkflowStatus): WorkflowStatusHistory = {
    WorkflowStatusHistory(
      workflowId = workflowStatus.id,
      statusId = workflowStatus.statusId,
      statusInfo = workflowStatus.statusInfo,
      creationDate = workflowStatus.creationDate.map(_.getMillis).orElse(None),
      lastUpdateDate = workflowStatus.lastUpdateDate.map(_.getMillis).orElse(None),
      lastUpdateDateWorkflow = workflowStatus.lastUpdateDateWorkflow.map(_.getMillis).orElse(None)
    )
  }
}
