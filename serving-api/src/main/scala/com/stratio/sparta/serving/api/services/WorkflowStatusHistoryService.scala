/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.services

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.dao.StatusHistoryDaoImpl
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.history.WorkflowStatusHistoryDto
import slick.jdbc.PostgresProfile

import scala.concurrent.Future
import scala.util.Try

class WorkflowStatusHistoryService extends SpartaSerializer
  with SLF4JLogging
  with StatusHistoryDaoImpl{

  val profile = PostgresProfile

  import profile.api._

  def findAllStatus(): Try[Future[Seq[WorkflowStatusHistoryDto]]] = {
    log.debug(s"Finding all status history entries")
    Try(findAll())
  }

  def findAllStatusByWorkflowId(id: String): Try[Future[Seq[WorkflowStatusHistoryDto]]] = {
    log.debug(s"Finding all status history entries with workflow id: $id")
    Try(findByWorkflowId(id))
  }
}
