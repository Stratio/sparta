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

import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.utils.AggregationTime
import org.apache.spark.sql._

import scala.util.Try

/**
 * This output prints all AggregateOperations or DataFrames information on screen. Very useful to debug.
 *
 * @param name
 * @param properties
 */
class CsvOutput(name: String, properties: Map[String, JSerializable]) extends Output(name, properties) {

  val path = properties.getString("path", None).notBlank
  require(path.isDefined, "Destination path is required. You have to set 'path' on properties")

  val header = Try(properties.getString("header").toBoolean).getOrElse(false)
  val inferSchema = Try(properties.getString("inferSchema").toBoolean).getOrElse(false)
  val delimiter = getValidDelimiter(properties.getString("delimiter", ","))
  val codecOption = properties.getString("codec", None)
  val compressExtension = properties.getString("compressExtension", None).getOrElse(".gz")

  override def supportedSaveModes : Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val pathParsed = if (path.get.endsWith("/")) path.get else path.get + "/"
    val tableName = getTableNameFromOptions(options)
    val optionsParsed =
      Map("header" -> header.toString, "delimiter" -> delimiter, "inferSchema" -> inferSchema.toString) ++
        codecOption.fold(Map.empty[String, String]) { codec => Map("codec" -> codec) }
    val fullPath = s"$pathParsed$tableName.csv"
    val pathWithExtension = codecOption.fold(fullPath) { codec => fullPath + compressExtension }

    validateSaveMode(saveMode)

    val dataFrameWriter = dataFrame.write
      .mode(getSparkSaveMode(saveMode))
      .options(optionsParsed ++ getCustomProperties)

    applyPartitionBy(options, dataFrameWriter, dataFrame.schema.fields).csv(pathWithExtension)
  }

  def getValidDelimiter(delimiter: String): String = {
    if (delimiter.length > 1) {
      val firstCharacter = delimiter.head.toString
      log.warn(s"Invalid length for the delimiter: '$delimiter' . " +
        s"The system chose the first character: '$firstCharacter'")
      firstCharacter
    }
    else delimiter
  }
}
