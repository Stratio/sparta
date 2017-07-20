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

import com.stratio.sparta.plugin.helper.{SecurityHelper, VaultHelper}
import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.SaveModeEnum.SpartaSaveMode
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.jdbc.SpartaJdbcUtils
import org.apache.spark.sql.jdbc.SpartaJdbcUtils._
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

class PostgresOutput(
                      name: String,
                      sparkSession: XDSession,
                      properties: Map[String, JSerializable]
                    ) extends Output(name, sparkSession, properties) {

  require(properties.getString("url", None).isDefined, "Postgres url must be provided")

  val url = properties.getString("url")
  val delimiter = properties.getString("delimiter", "\t")
  val newLineSubstitution = properties.getString("newLineSubstitution", " ")
  val encoding = properties.getString("encoding", "UTF8")
  val postgresSaveMode = PostgresSaveMode.withName(properties.getString("postgresSaveMode", "CopyIn").toUpperCase)
  val tlsEnable = Try(properties.getBoolean("tlsEnable")).getOrElse(false)

  override def supportedSaveModes: Seq[SpartaSaveMode] =
    Seq(SaveModeEnum.Append, SaveModeEnum.Overwrite, SaveModeEnum.Upsert)

  override def save(dataFrame: DataFrame, saveMode: SpartaSaveMode, options: Map[String, String]): Unit = {
    validateSaveMode(saveMode)
    val tableName = getTableNameFromOptions(options)
    val sparkSaveMode = getSparkSaveMode(saveMode)
    val urlWithSSL = if (tlsEnable) {
      s"$url&ssl=true&sslmode=verify-full&sslcert=/tmp/cert.crt&sslrootcert=/tmp/caroot.crt&sslkey=/tmp/key.pkcs8"
    } else url
    val connectionProperties = new JDBCOptions(urlWithSSL,
      tableName,
      propertiesWithCustom.mapValues(_.toString).filter(_._2.nonEmpty)
    )

    Try {
      if (sparkSaveMode == SaveMode.Overwrite)
        SpartaJdbcUtils.dropTable(urlWithSSL, connectionProperties, tableName, name)

      synchronized {
        SpartaJdbcUtils.tableExists(urlWithSSL, connectionProperties, tableName, dataFrame.schema, name)
      }
    } match {
      case Success(tableExists) =>
        if (tableExists) {
          if (saveMode == SaveModeEnum.Upsert) {
            val updateFields = getPrimaryKeyOptions(options) match {
              case Some(pk) => pk.split(",").toSeq
              case None => dataFrame.schema.fields.filter(stField =>
                stField.metadata.contains(Output.PrimaryKeyMetadataKey)).map(_.name).toSeq
            }
            SpartaJdbcUtils.upsertTable(dataFrame, urlWithSSL, tableName, connectionProperties, updateFields, name)
          } else {
            if (postgresSaveMode == PostgresSaveMode.COPYIN) {
              dataFrame.foreachPartition { rows =>
                val conn = getConnection(connectionProperties, name)
                val cm = new CopyManager(conn.asInstanceOf[BaseConnection])

                cm.copyIn(
                  s"""COPY $tableName FROM STDIN WITH (NULL 'null', ENCODING '$encoding', FORMAT CSV, DELIMITER E'$delimiter')""",
                  rowsToInputStream(rows)
                )
              }
            } else {
              SpartaJdbcUtils.saveTable(dataFrame, urlWithSSL, tableName, connectionProperties, name)
            }
          }
        } else log.warn(s"Table not created in Postgres: $tableName")
      case Failure(e) =>
        closeConnection(name)
        log.error(s"Error creating/dropping table $tableName", e)
    }
  }

  def rowsToInputStream(rows: Iterator[Row]): InputStream = {
    val bytes: Iterator[Byte] = rows.flatMap { row =>
      (row.mkString(delimiter).replace("\n", newLineSubstitution) + "\n").getBytes(encoding)
    }

    new InputStream {
      override def read(): Int =
        if (bytes.hasNext) bytes.next & 0xff
        else -1
    }
  }

  override def cleanUp(options: Map[String, String]): Unit = {
    log.info(s"Closing connections in Postgres Output: $name")
    closeConnection(name)
  }
}

object PostgresOutput {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataSourceSecurityConf(configuration)
  }
}