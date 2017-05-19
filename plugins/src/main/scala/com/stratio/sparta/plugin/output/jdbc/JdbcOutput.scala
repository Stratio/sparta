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

package com.stratio.sparta.plugin.output.jdbc

import java.io.{Serializable => JSerializable}
import java.util.Properties

import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.SaveModeEnum.SpartaSaveMode
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql._
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.jdbc.SpartaJdbcUtils
import org.apache.spark.sql.jdbc.SpartaJdbcUtils._

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

class JdbcOutput(name: String, properties: Map[String, JSerializable]) extends Output(name, properties) {

  require(properties.getString("url", None).isDefined, "url must be provided")

  val url = properties.getString("url")

  override def supportedSaveModes : Seq[SpartaSaveMode] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)

  //scalastyle:off
  override def save(dataFrame: DataFrame, saveMode: SpartaSaveMode, options: Map[String, String]): Unit = {
    validateSaveMode(saveMode)
    val tableName = getTableNameFromOptions(options)
    val sparkSaveMode = getSparkSaveMode(saveMode)
    val connectionProperties = new JDBCOptions(url, tableName, propertiesWithCustom.mapValues(_.toString))

    Try {
      if (sparkSaveMode == SaveMode.Overwrite) SpartaJdbcUtils.dropTable(url, connectionProperties, tableName)

      SpartaJdbcUtils.tableExists(url, connectionProperties, tableName, dataFrame.schema)
    } match {
      case Success(tableExists) =>
        if (tableExists) {
          if (saveMode == SaveModeEnum.Upsert) {
            val updateFields = getPrimaryKeyOptions(options) match {
              case Some(pk) => pk.split(",").toSeq
              case None => dataFrame.schema.fields.filter(stField =>
                stField.metadata.contains(Output.PrimaryKeyMetadataKey)).map(_.name).toSeq
            }
            SpartaJdbcUtils.upsertTable(dataFrame, url, tableName, connectionProperties, updateFields)
          }

          if (saveMode == SaveModeEnum.Ignore) return

          if (saveMode == SaveModeEnum.ErrorIfExists) sys.error(s"Table $tableName already exists.")

          if (saveMode == SaveModeEnum.Append || saveMode == SaveModeEnum.Overwrite)
            SpartaJdbcUtils.saveTable(dataFrame, url, tableName, connectionProperties)
        } else log.warn(s"Table not created in Postgres: $tableName")
      case Failure(e) =>
        closeConnection()
        log.error(s"Error creating/dropping table $tableName")
    }
  }

  override def cleanUp(options: Map[String, String]): Unit = {
    log.info(s"Closing connections in JDBC Output: $name")
    closeConnection()
  }
}