/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.dao

import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import com.stratio.sparta.serving.core.utils.{JdbcSlickUtils, PostgresJdbcSlickImpl}
import com.stratio.sparta.serving.core.actor.StatusHistoryActor._


import scala.concurrent.Future

trait StatusHistoryDao {

 this: JdbcSlickUtils =>

  val table = workflowStatusHistoryTable

  def upsert(workflowStatus: WorkflowStatus): Future[_]  = ???

  def queryAll() : Future[_] = ???

  def queryByWorkflowId(id: String): Future[_]  = ???

  def queryByUserId(userId: String): Future[_]  = ???
}

trait StatusHistoryDaoImpl extends StatusHistoryDao with PostgresJdbcSlickImpl
