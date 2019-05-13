/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.common.postgresql

import java.sql.Connection

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JdbcUtils}

import scala.util.{Failure, Success, Try}


trait PostgresSuiteBase extends SLF4JLogging {

  private lazy val config = ConfigFactory.load()

  val postgresURL = Try(config.getString("postgresql.host")) match {
    case Success(configHost) =>
      val hostUrl = s"jdbc:postgresql://$configHost:5432/postgres?user=postgres"
      log.info(s"Postgres host from config: $hostUrl")
      hostUrl
    case Failure(e) =>
      log.info(s"Postgres host from default")
      "jdbc:postgresql://127.0.0.1:5432/postgres?user=postgres"
  }

  private val jdbcOptions: JDBCOptions = new JDBCOptions(
    postgresURL,
    "fakeTable",
    Map("driver" -> "org.postgresql.Driver")
  )



  val postgresConnection: Connection = JdbcUtils.createConnectionFactory(jdbcOptions)()

  def withConnectionExecute(sql: String*): Unit =
    sql.foreach{ sqlString =>
      val statement = postgresConnection.createStatement

      try {
        statement.execute(sqlString)
      } finally {
        statement.close()
      }
    }

  def withCrossdataTable(table: String, provider: String, props: Map[String, String], sparkSession: SparkSession)(blockCode: => Unit): Unit = {
    try {
      sparkSession.catalog.createTable(table, "jdbc", props)
      blockCode
    } finally {
      sparkSession.sql(s"DROP TABLE $table")
    }

  }



}
