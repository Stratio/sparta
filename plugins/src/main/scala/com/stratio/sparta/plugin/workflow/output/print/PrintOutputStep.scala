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

package com.stratio.sparta.plugin.workflow.output.print

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputStep
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession

import scala.util.Try

class PrintOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  lazy val printData = Try(properties.getBoolean("printData")).getOrElse(false)
  lazy val printSchema = Try(properties.getBoolean("printSchema")).getOrElse(false)
  lazy val printMetadata = Try(properties.getBoolean("printMetadata")).getOrElse(true)
  lazy val logLevel = properties.getString("logLevel", "warn")

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val metadataInfo = if (printMetadata)
      Option(s"Table name : ${getTableNameFromOptions(options)}\tElements: ${dataFrame.count()}")
    else None
    val schemaInfo = if (printSchema) Option(s"DataFrame schema: ${dataFrame.schema.toString()}") else None

    logLevel match {
      case "warn" =>
        metadataInfo.foreach(info => log.warn(info))
        schemaInfo.foreach(schema => log.warn(schema))
        if (printData) dataFrame.foreach(row => log.warn(row.mkString(",")))
      case "error" =>
        metadataInfo.foreach(info => log.error(info))
        schemaInfo.foreach(schema => log.error(schema))
        if (printData) dataFrame.foreach(row => log.error(row.mkString(",")))
      case _ =>
        metadataInfo.foreach(info => log.info(info))
        schemaInfo.foreach(schema => log.info(schema))
        if (printData) dataFrame.foreach(row => log.info(row.mkString(",")))
    }
  }
}
