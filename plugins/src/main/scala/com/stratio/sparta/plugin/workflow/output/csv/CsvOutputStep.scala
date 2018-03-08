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
package com.stratio.sparta.plugin.workflow.output.csv

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, OutputStep}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

import scala.util.Try

class CsvOutputStep(
                     name: String,
                     xDSession: XDSession,
                     properties: Map[String, JSerializable]
                   ) extends OutputStep(name, xDSession, properties) {

  lazy val path: String = properties.getString("path", "").trim
  lazy val header = Try(properties.getString("header", "false").toBoolean).getOrElse(false)
  lazy val inferSchema = Try(properties.getString("inferSchema", "false").toBoolean).getOrElse(false)
  lazy val delimiter = getValidDelimiter(properties.getString("delimiter", ","))
  lazy val codecOption = properties.getString("codec", None).notBlank
  lazy val compressExtension = properties.getString("compressExtension", None).notBlank.getOrElse(".gz")

  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (path.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the destination path can not be empty"
      )

    validation
  }

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(path.nonEmpty, "Input path can not be empty")

    val pathParsed = if (path.endsWith("/")) path else path + "/"
    val tableName = getTableNameFromOptions(options)
    val optionsParsed =
      Map(
        "header" -> header.toString,
        "delimiter" -> delimiter,
        "inferSchema" -> inferSchema.toString
      ) ++ codecOption.fold(Map.empty[String, String]) { codec => Map("codec" -> codec) }
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
    } else delimiter
  }
}
