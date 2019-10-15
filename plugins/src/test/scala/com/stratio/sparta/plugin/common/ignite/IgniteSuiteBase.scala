/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.common.ignite

import java.sql.Connection

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JdbcUtils}

import scala.util.{Failure, Success, Try}

trait IgniteSuiteBase extends SLF4JLogging {
  private lazy val config = ConfigFactory.load()

  lazy val igniteURL = Try(config.getString("ignite.host")) match {
    case Success(configHost) =>
      val hostUrl = s"jdbc:ignite:thin://$configHost:10800"
      log.info(s"Ignite host from config: $hostUrl")
      hostUrl
    case Failure(e) =>
      log.info(s"Ignite host from default")
      "jdbc:ignite:thin://localhost:10800"
  }

  lazy val fetchSize = "2000"
  lazy val batchSize = "1000"

  private val jdbcOptions: JDBCOptions = new JDBCOptions(
    igniteURL,
    "test",
    Map("driver" -> "org.apache.ignite.IgniteJdbcThinDriver")
  )

  lazy val igniteConnection: Connection = JdbcUtils.createConnectionFactory(jdbcOptions)()

  def withConnectionExecute(sql: String*): Unit =
    sql.foreach{ sqlString =>
      val statement = igniteConnection.createStatement

      try {
        statement.execute(sqlString)
      } finally {
        statement.close()
      }
    }

}
