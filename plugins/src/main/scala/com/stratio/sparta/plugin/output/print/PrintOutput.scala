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

package com.stratio.sparta.plugin.output.print

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import org.apache.spark.Logging
import org.apache.spark.sql._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._

import scala.util.Try

class PrintOutput(keyName: String,
                  version: Option[Int],
                  properties: Map[String, JSerializable],
                  schemas: Seq[SpartaSchema])
  extends Output(keyName, version, properties, schemas) with Logging {

  val printData = Try(properties.getBoolean("printData")).getOrElse(false)
  val printSchema = Try(properties.getBoolean("printSchema")).getOrElse(false)
  val logLevel = properties.getString("logLevel", "warn")

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val metadataInfo = s"\tTable name : ${Output.getTableNameFromOptions(options)}\tElements: ${dataFrame.count()}"
    val schemaInfo = if(printSchema) Option(s"DataFrame schema: ${dataFrame.schema.toString()}") else None
        
    logLevel match {
      case "warn" =>
        log.warn(metadataInfo)
        schemaInfo.foreach(schema => log.warn(schema))
        if(printData) dataFrame.foreach(row => log.warn(row.mkString(",")))
      case "error" =>
        log.error(metadataInfo)
        schemaInfo.foreach(schema => log.error(schema))
        if(printData) dataFrame.foreach(row => log.error(row.mkString(",")))
      case _ =>
        log.info(metadataInfo)
        schemaInfo.foreach(schema => log.info(schema))
        if(printData) dataFrame.foreach(row => log.info(row.mkString(",")))
    }

    if(printData) {

    }
  }
}
