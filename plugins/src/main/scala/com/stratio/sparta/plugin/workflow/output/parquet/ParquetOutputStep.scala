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
package com.stratio.sparta.plugin.workflow.output.parquet

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.workflow.step.OutputStep
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession


/**
  * This output saves as a parquet file the information received from the stream.
  *
  * @param name
  * @param properties
  */
class ParquetOutputStep(
                        name : String,
                        xDSession: XDSession,
                        properties: Map[String, JSerializable]
                        ) extends OutputStep(name, xDSession, properties){

  val path = properties.getString("path", None).notBlank
  require(path.isDefined, "Destination path is required. You have to set 'path' on properties")

  override def supportedSaveModes : Seq[SaveModeEnum.Value] = {
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)
  }

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String,String]): Unit = {
    val tableName = getTableNameFromOptions(options)

    validateSaveMode(saveMode)

    val dataFrameWriter = dataFrame.write
      .options(getCustomProperties)
      .mode(getSparkSaveMode(saveMode))

    applyPartitionBy(options, dataFrameWriter, dataFrame.schema.fields).parquet(s"${path.get}/$tableName")
  }
}
