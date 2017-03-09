/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.plugin.output.postgres

import java.io.{InputStream, Serializable => JSerializable}
import java.sql.Connection
import java.util.Properties

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql._
import org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection

class PostgresOutput(name: String, properties: Map[String, JSerializable]) extends Output(name, properties) {

  require(properties.getString("url", None).isDefined, "Postgres url must be provided")

  val url = properties.getString("url")

  val bufferSize = properties.getString("bufferSize", "65536").toInt

  val delimiter = properties.getString("delimiter", "\t")

  val newLineSubstitution = properties.getString("newLineSubstitution", " ")

  val connectionProperties = {
    val props = new Properties()
    props.putAll(properties.mapValues(_.toString))
    getCustomProperties.foreach(customProp => props.put(customProp._1, customProp._2))
    props
  }

  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.Overwrite)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {

    validateSaveMode(saveMode)

    val tableName = getTableNameFromOptions(options)
    val sparkSaveMode = getSparkSaveMode(saveMode)

    Try {
      val conn = PostgresOutput.getConnection(url, connectionProperties)
      var tableExists = JdbcUtils.tableExists(conn, url, tableName)

      if (sparkSaveMode == SaveMode.Overwrite && tableExists) {
        JdbcUtils.dropTable(conn, tableName)
        tableExists = false
      }

      if (!tableExists) {
        val schema = JdbcUtils.schemaString(dataFrame, url)
        val sql = s"CREATE TABLE $tableName ($schema)"
        val statement = conn.createStatement
        try {
          statement.executeUpdate(sql)
        } finally {
          statement.close()
        }
      }
      tableExists
    } match {
      case Success(tableExists) =>
        if (!tableExists) log.info(s"Created correctly table in Postgres: $tableName")
      case Failure(e) =>
        PostgresOutput.closeConnection()
        log.error(s"Error creating/dropping table $tableName")
    }

    dataFrame.foreachPartition { rows =>
      val conn = PostgresOutput.getConnection(url, connectionProperties)
      val cm = new CopyManager(conn.asInstanceOf[BaseConnection])

      cm.copyIn(
        s"""COPY $tableName FROM STDIN WITH (NULL 'null', FORMAT CSV, DELIMITER E'$delimiter')""",
        rowsToInputStream(rows)
      )
    }
  }

  def rowsToInputStream(rows: Iterator[Row]): InputStream = {
    val bytes: Iterator[Byte] = rows.flatMap { row =>
      (row.mkString(delimiter).replace("\n", newLineSubstitution) + "\n").getBytes
    }

    new InputStream {
      override def read(): Int =
        if (bytes.hasNext) bytes.next & 0xff
        else -1
    }
  }
}

object PostgresOutput extends SLF4JLogging {

  private var connection: Option[Connection] = None

  def getConnection(url: String, properties: Properties): Connection = {
    if (connection.isEmpty)
      synchronized {
        connection = Try(JdbcUtils.createConnectionFactory(url, properties)()) match {
          case Success(conn) =>
            Option(conn)
          case Failure(e) =>
            log.error(s"Error creating postgres connection ${e.getLocalizedMessage}")
            None
        }
      }
    connection.getOrElse(throw new IllegalStateException("The connection is empty"))
  }

  def closeConnection(): Unit = {
    synchronized {
      Try(connection.foreach(_.close())) match {
        case Success(_) => log.info("Connection correctly closed")
        case Failure(e) => log.error(s"Error closing connection, ${e.getLocalizedMessage}")
      }
      connection = None
    }
  }
}