/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.services

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.dao.StatusHistoryDaoImpl
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile

import scala.util.{Failure, Success, Try}

//scalastyle:off
class WorkflowStatusHistoryService extends SpartaSerializer
  with SLF4JLogging
  with StatusHistoryDaoImpl{

  val profile = PostgresProfile

  override val db = WorkflowExecutionHistoryService.db

  def findAllStatusByWorkflowId(id: String) = {
    log.debug(s"Finding all status history entries with workflow id: $id")
    Try(findByWorkflowId(id))
  }
}

object WorkflowStatusHistoryService extends SLF4JLogging {
  lazy val db = {
    val conf = ConfigFactory.parseString("poolName = queryExecutionPool")
      .withFallback(SpartaConfig.getSpartaPostgres.getOrElse(ConfigFactory.load()))
    val dbconf = Database.forConfig("", conf)
    Try(dbconf.createSession.conn) match {
      case Success(con) => {
        con.close()
      }
      case Failure(f) => {
        dbconf.close()
        dbconf.shutdown
        log.warn(s"Unable to connect to dataSource ${f.getMessage} ")
      }
    }
    dbconf
  }
}