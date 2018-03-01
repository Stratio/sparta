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

package com.stratio.sparta.plugin.workflow.output.jdbc

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.plugin.helper.SecurityHelper._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum.SpartaSaveMode
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, OutputStep}
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.jdbc.SpartaJdbcUtils
import org.apache.spark.sql.jdbc.SpartaJdbcUtils._

import scala.util.{Failure, Success, Try}

class JdbcOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val url = properties.getString("url")
  lazy val tlsEnable = Try(properties.getBoolean("tlsEnabled")).getOrElse(false)

  val sparkConf = xDSession.conf.getAll
  val securityUri = getDataStoreUri(sparkConf)
  val urlWithSSL = if (tlsEnable) url + securityUri else url

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if(url.isEmpty)
      validation = ErrorValidations(valid = false, messages = validation.messages :+ s"$name url must be provided")
    if(tlsEnable && securityUri.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name when TLS is enable the sparkConf must contain the security options"
      )

    validation
  }

  override def supportedSaveModes: Seq[SpartaSaveMode] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite,
      SaveModeEnum.Upsert)

  //scalastyle:off
  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(url.nonEmpty, "JDBC url must be provided")
    validateSaveMode(saveMode)
    val tableName = getTableNameFromOptions(options)
    val sparkSaveMode = getSparkSaveMode(saveMode)
    val connectionProperties = new JDBCOptions(urlWithSSL,
      tableName,
      propertiesWithCustom.mapValues(_.toString).filter(_._2.nonEmpty)
    )

    Try {
      if (sparkSaveMode == SaveMode.Overwrite)
        SpartaJdbcUtils.dropTable(connectionProperties, name)

      synchronized {
        SpartaJdbcUtils.tableExists(connectionProperties, dataFrame, name)
      }
    } match {
      case Success(tableExists) =>
        if (tableExists) {
          if (saveMode == SaveModeEnum.Upsert) {
            val updateFields = getPrimaryKeyOptions(options) match {
              case Some(pk) => pk.split(",").toSeq
              case None => Seq.empty[String]
            }

            require(updateFields.nonEmpty, "The primary key fields must be provided")

            SpartaJdbcUtils.upsertTable(dataFrame, connectionProperties, updateFields, name)
          }

          if (saveMode == SaveModeEnum.Ignore) return

          if (saveMode == SaveModeEnum.ErrorIfExists) sys.error(s"Table $tableName already exists")

          if (saveMode == SaveModeEnum.Append || saveMode == SaveModeEnum.Overwrite)
            SpartaJdbcUtils.saveTable(dataFrame, connectionProperties, name)
        } else log.warn(s"Table not created: $tableName")
      case Failure(e) =>
        closeConnection(name)
        log.error(s"Error creating/dropping table $tableName", e)
        throw e
    }
  }

  override def cleanUp(options: Map[String, String]): Unit = {
    log.info(s"Closing connections in JDBC Output: $name")
    closeConnection(name)
  }
}

object JdbcOutputStep {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}