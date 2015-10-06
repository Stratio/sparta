/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.output.csv

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark.Logging
import org.apache.spark.sql._

import scala.util.Try

/**
 * This output prints all AggregateOperations or DataFrames information on screen. Very useful to debug.
 * @param keyName
 * @param properties
 * @param operationTypes
 * @param bcSchema
 */
class CsvOutput(keyName: String,
                properties: Map[String, JSerializable],
                operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                bcSchema: Option[Seq[TableSchema]])
  extends Output(keyName, properties, operationTypes, bcSchema) with Logging {

  val path = properties.getString("path", None)

  val header = Try(properties.getString("header").toBoolean).getOrElse(false)

  val delimiter = properties.getString("delimiter", ",")

  val datePattern = properties.getString("datePattern", None)

  val dateGranularityFile = properties.getString("dateGranularityFile", "day")

  override def upsert(dataFrame: DataFrame, tableName: String, timeDimension: String): Unit = {
    require(path.isDefined, "Destination path is required. You have to set 'path' on properties")
    val pathParsed = if (path.get.endsWith("/")) path.get else path.get + "/"
    val subPath = DateOperations.subPath(dateGranularityFile, datePattern)

    saveAction(s"$pathParsed$tableName$subPath.csv", dataFrame)
  }

  protected[csv] def saveAction(path: String, dataFrame: DataFrame): Unit = {
    import com.databricks.spark.csv.CsvSchemaRDD
    dataFrame.saveAsCsvFile(path,
      Map("header" -> header.toString, "delimiter" -> delimiter))
  }
}
