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
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import com.typesafe.config.ConfigFactory
import slick.jdbc.H2Profile.api._

import scala.util.Try

class StatusHistoryActor extends Actor with StatusHistoryDaoImpl with SLF4JLogging {

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[StatusChange])
    //createSchema
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[StatusChange])
    db.close()
  }

  override def receive: Receive = {
    case sc: StatusChange => upsert(sc.workflowStatus)
    case QueryAll() => sender ! queryAll()
    case QueryByWorkflowId(id) => sender ! queryByWorkflowId(id)
    case QueryByUserId(userId) => sender ! queryByUserId(userId)
  }
}

//scalastyle:off
object StatusHistoryActor{

  case class QueryAll()

  case class QueryByWorkflowId(id: String)

  case class QueryByUserId(userId: String)

  implicit def statusToDb(workflowStatus: WorkflowStatus): WorkflowStatusHistory = {
    WorkflowStatusHistory(
      workflowId = workflowStatus.id,
      statusInfo = workflowStatus.statusInfo,
      creationDate = workflowStatus.creationDate.map(_.getMillis).orElse(None),
      lastUpdateDate = workflowStatus.lastUpdateDate.map(_.getMillis).orElse(None),
      lastUpdateDateWorkflow = workflowStatus.lastUpdateDateWorkflow.map(_.getMillis).orElse(None)
    )
  }

  case class WorkflowStatusHistory(workflowId: String,
                                    statusInfo: Option[String] = None,
                                    creationDate: Option[Long] = None,
                                    lastUpdateDate: Option[Long] = None,
                                    lastUpdateDateWorkflow: Option[Long] = None)

  class WorkflowStatusHistoryTable(tag: Tag) extends Table[WorkflowStatusHistory](tag, Some("public"),
    Try(ConfigFactory.load.getString("sparta.postgres.statusHistory.table")).getOrElse("workflow_status_history")){

    def workflowId = column[String]("workflowId")

    def statusInfo = column[Option[String]]("status_info")

    def creationDate = column[Option[Long]]("creation_date")

    def lastUpdateDate = column[Option[Long]]("last_update_date")

    def lastUpdateDateWorkflow = column[Option[Long]]("last_update_date_workflow")

    def * = (workflowId, statusInfo, creationDate, lastUpdateDate, lastUpdateDateWorkflow) <> (WorkflowStatusHistory.tupled, WorkflowStatusHistory.unapply)

    def pk = primaryKey(s"pk_${Try(ConfigFactory.load.getString("sparta.postgres.statusHistory.table")).getOrElse("workflow_status_history")}", workflowId)
  }

  val workflowStatusHistoryTable = TableQuery[WorkflowStatusHistoryTable]
}
