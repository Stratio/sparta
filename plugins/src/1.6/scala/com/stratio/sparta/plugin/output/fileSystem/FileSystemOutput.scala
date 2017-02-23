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

package com.stratio.sparta.plugin.output.fileSystem

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.{Output, OutputFormatEnum, SaveModeEnum}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.DataFrame

/**
  * This output sends all AggregateOperations or DataFrames data  to a directory stored in
  * HDFS, Amazon S3, Azure, etc.
  *
  * @param name
  * @param properties
  */
class FileSystemOutput(name: String, properties: Map[String, JSerializable]) extends Output(name, properties) {

  val partitionBy = properties.getString("partitionBy", None).notBlank
  val path = properties.getString("path", None).notBlank
  require(path.isDefined, "Destination path is required. You have to set 'path' on properties")
  val outputFormat = OutputFormatEnum.withName(properties.getString("outputFormat", "json").toUpperCase)

  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)
    val dataFrameWriter = dataFrame.write.mode(getSparkSaveMode(saveMode))

    validateSaveMode(saveMode)
    applyPartitionBytoDataFrame(partitionBy, dataFrameWriter)

    if (outputFormat == OutputFormatEnum.JSON)
      dataFrameWriter.json(s"${path.get}/$tableName")
    else dataFrameWriter.text(s"${path.get}/$tableName")
  }
}

