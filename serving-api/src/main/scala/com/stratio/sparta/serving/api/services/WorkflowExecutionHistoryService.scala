/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.services

import scala.util.Try

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile

import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.api.dao.ExecutionHistoryDaoImpl
import com.stratio.sparta.serving.core.models.SpartaSerializer

//scalastyle:off
class WorkflowExecutionHistoryService extends SpartaSerializer with ExecutionHistoryDaoImpl with SLF4JLogging {

  override val profile = PostgresProfile

  override val db = WorkflowExecutionHistoryService.db

  def queryByUserId(id: String) =
    Try(findByUserId(id))

  def queryByWorkflowId(id: String) =
    Try(findByWorkflowId(id))
}

object WorkflowExecutionHistoryService {

  lazy val db = {
    val conf = ConfigFactory.parseString("poolName = queryExecutionPool").withFallback(SpartaConfig.getSpartaPostgres.getOrElse(ConfigFactory.load()))
    Database.forConfig("", conf)
  }
}