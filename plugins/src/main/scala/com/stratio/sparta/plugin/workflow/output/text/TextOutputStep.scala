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
package com.stratio.sparta.plugin.workflow.output.text

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputStep
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.functions.{col, concat_ws}

import scala.util.Try

class TextOutputStep(
                      name: String,
                      xDSession: XDSession,
                      properties: Map[String, JSerializable]
                    ) extends OutputStep(name, xDSession, properties) {

  lazy val FieldName = "extractedData"
  lazy val path: Option[String] = properties.getString("path", None).notBlank
  lazy val delimiter: String = properties.getString("delimiter", ",")

  require(path.isDefined, "Destination path is required. You have to set 'path' on properties")

  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)

    validateSaveMode(saveMode)

    val df = dataFrame.withColumn(
        FieldName,
        concat_ws(delimiter, dataFrame.schema.fields.flatMap(field => Some(col(field.name))).toSeq: _*)
      ).select(FieldName)

    applyPartitionBy(
      options,
      df.write.mode(getSparkSaveMode(saveMode)).options(getCustomProperties),
      df.schema.fields
    ).text(s"${path.get}/$tableName")
  }
}
