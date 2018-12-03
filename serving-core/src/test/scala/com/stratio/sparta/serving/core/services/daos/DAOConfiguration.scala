/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.daos

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.utils.JdbcSlickHelper
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success, Try}

trait DAOConfiguration extends JdbcSlickHelper
  with SLF4JLogging {

  val config = ConfigFactory.load()
  val hostConf = Try(config.getString("postgresql.host")) match {
    case Success(configHost) =>
      val hostUrl = s""""$configHost:5432""""
      log.info(s"Postgres host from config: $hostUrl")
      s"host = $hostUrl\n"
    case Failure(e) =>
      log.info(s"Postgres host from default")
      val hostUrl =s""""localhost:5432""""
      s"host = $hostUrl\n"
  }


  val updatedConf = ConfigFactory.parseString(hostConf).withFallback(SpartaConfig.getPostgresConfig().get)
  SpartaConfig.postgresConfig = Option(updatedConf)
  val properties = ConfigFactory.parseProperties(slickConnectionProperties(updatedConf)).withFallback(updatedConf)

  val queryTimeout = 20000

}
