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

package com.stratio.sparta.plugin.output.csv

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.Output._
import com.stratio.sparta.sdk.ValidatingPropertyMap._
import com.stratio.sparta.sdk._
import org.apache.spark.Logging
import org.apache.spark.sql._

import scala.util.Try

/**
 * This output prints all AggregateOperations or DataFrames information on screen. Very useful to debug.
 *
 * @param keyName
 * @param properties
 * @param schemas
 */
class CsvOutput(keyName: String,
                version: Option[Int],
                properties: Map[String, JSerializable],
                schemas: Seq[TableSchema])
  extends Output(keyName, version, properties, schemas) with Logging {

  val path = properties.getString("path", None)

  val header = Try(properties.getString("header").toBoolean).getOrElse(false)

  val inferSchema = Try(properties.getString("inferSchema").toBoolean).getOrElse(false)

  val delimiter = getValidDelimiter(properties.getString("delimiter", ","))

  val codecOption = properties.getString("codec", None)

  val compressExtension = properties.getString("compressExtension", None).getOrElse(".gz")

  val datePattern = properties.getString("datePattern", None)

  val dateGranularityFile = properties.getString("dateGranularityFile", "day")

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(path.isDefined, "Destination path is required. You have to set 'path' on properties")
    val pathParsed = if (path.get.endsWith("/")) path.get else path.get + "/"
    val subPath = DateOperations.subPath(dateGranularityFile, datePattern)
    val tableName = getTableNameFromOptions(options)
    val optionsParsed =
      Map("header" -> header.toString, "delimiter" -> delimiter, "inferSchema" -> inferSchema.toString) ++
        codecOption.fold(Map.empty[String, String]) { codec => Map("codec" -> codec) }
    val fullPath = s"$pathParsed${versionedTableName(tableName)}$subPath.csv"
    val pathWithExtension = codecOption.fold(fullPath) { codec => fullPath + compressExtension }

    dataFrame.write
      .format("com.databricks.spark.csv")
      .mode(getSparkSaveMode(saveMode))
      .options(optionsParsed)
      .save(pathWithExtension)
  }

  def getValidDelimiter(delimiter: String): String = {
    if (delimiter.size > 1) {
      val firstCharacter = delimiter.head.toString
      log.warn(s"Invalid length to delimiter in csv: '$delimiter' . The system choose the first: '$firstCharacter'")
      firstCharacter
    }
    else delimiter
  }
}
