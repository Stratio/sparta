/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.dao

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.history.WorkflowStatusHistory
import com.stratio.sparta.serving.api.utils.JdbcSlickUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

//scalastyle:off
trait StatusHistoryDao extends JdbcSlickUtils with SLF4JLogging{

  import profile.api._

  lazy val statusTableName : String = Try(SpartaConfig.getSpartaPostgres.get.getString("statusHistory.table"))
    .getOrElse("workflow_status_history")
  lazy val statusHistoryTable = TableQuery[WorkflowStatusHistoryTable]

  private def txHandler(dbioAction: DBIOAction[Unit, NoStream, Effect.All with Effect.Transactional]) = {
    dbioAction.asTry.flatMap {
      case Success(s) => DBIO.successful(s)
      case Failure(e) => {
        log.error(s"Error while trying to execute action over table $statusTableName", e)
        DBIO.failed(e)
      }
    }
  }

  def createSchema() : Unit = {
    db.run(statusHistoryTable.exists.result) onComplete {
      case Success(_) =>
        log.info(s"Schema already exists")
      case Failure(_) =>
        log.info(s"Creating schema for table $statusTableName")
        val dbioAction = (for {
          _ <- statusHistoryTable.schema.create
        } yield ()).transactionally
        db.run(txHandler(dbioAction))
    }
  }

  def upsert(workflowStatus: WorkflowStatusHistory): Future[Unit]  = {
    val dbioAction = DBIO.seq(
      statusHistoryTable.insertOrUpdate(workflowStatus)
    ).transactionally
    db.run(txHandler(dbioAction))
  }

  def findAll() : Future[List[WorkflowStatusHistory]] = db.run(statusHistoryTable.result).map(_.toList)

  def findByWorkflowId(workflowId: String): Future[List[WorkflowStatusHistory]]  = db.run(
    statusHistoryTable.filter(_.workflowId === workflowId).result).map(_.toList)

  class WorkflowStatusHistoryTable(tag: Tag) extends Table[WorkflowStatusHistory](tag, Some("public"), statusTableName){

    def workflowId = column[String]("workflow_id")

    def status = column[String]("status")

    def statusId = column[Option[String]]("status_id")

    def statusInfo = column[Option[String]]("status_info")

    def creationDate = column[Option[Long]]("creation_date")

    def lastUpdateDate = column[Option[Long]]("last_update_date")

    def lastUpdateDateWorkflow = column[Option[Long]]("last_update_date_workflow")

    def * = (workflowId,  status, statusId, statusInfo, creationDate, lastUpdateDate, lastUpdateDateWorkflow) <>
      (WorkflowStatusHistory.tupled, WorkflowStatusHistory.unapply)

    def pk = primaryKey(s"pk_$statusTableName", statusId)

    def statusIndex = index(s"idx_$statusTableName", statusId, unique = true)
  }
}

trait StatusHistoryDaoImpl extends StatusHistoryDao