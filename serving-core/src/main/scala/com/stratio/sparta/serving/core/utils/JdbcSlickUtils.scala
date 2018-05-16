/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

import slick.jdbc.{JdbcProfile, PostgresProfile}

private[core] trait JdbcSlickUtils {

  val profile: JdbcProfile

  import profile.api._

  val db: Database
}

trait PostgresJdbcSlickImpl extends JdbcSlickUtils {

  val profile = PostgresProfile

  import profile.api._

  val db: profile.api.Database = Database.forConfig("pgTest")
}

//scalastyle:off
trait ExecutionHistoryDao {

  this: JdbcSlickUtils =>

  import profile.api._

  import com.stratio.sparta.serving.core.actor.ExecutionHistoryActor._

  val table = workflowExecutionHistoryTable

  def insert(workflowExecution: WorkflowExecutionHistory): Future[_] = {
    val insertAction = DBIO.seq(
      table += workflowExecution
    ).transactionally
    val errorHandleAction = insertAction.asTry.flatMap {
      case Failure(e: Throwable) => DBIO.failed(e)
      case Success(s) => DBIO.successful(s)
    }
    db.run(errorHandleAction)
  }

  def update(workflowExecution: WorkflowExecutionHistory): Future[_] = {
    val q = for {c <- table if c.id === workflowExecution.id} yield c
    val updateAction = DBIO.seq(
      q.update(workflowExecution)
    ).transactionally
    val errorHandleAction = updateAction.asTry.flatMap {
      case Failure(e: Throwable) => DBIO.failed(e)
      case Success(s) => DBIO.successful(s)
    }
    db.run(errorHandleAction)
  }

  def createSchema(): Future[Unit] = {
    val dbioAction = (for {
      _ <- table.schema.create
    } yield ()).transactionally
    db.run(dbioAction)
  }

  def selectAll(): Future[List[WorkflowExecutionHistory]] = {
    db.run(table.result).map(_.toList)
  }

  def findByWorkflowId(workflowId: String): Future[List[WorkflowExecutionHistory]] = {
    db.run(table.filter(_.id === workflowId).result).map(_.toList)
  }

  def findByUserId(userId: String): Future[List[WorkflowExecutionHistory]] = {
    db.run(table.filter(_.userId === userId).result).map(_.toList)
  }
}

trait ExecutionHistoryDaoImpl extends ExecutionHistoryDao with PostgresJdbcSlickImpl