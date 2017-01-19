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
package com.stratio.sparta.plugin.output.avro

import java.io.Serializable

import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.Logging
import org.apache.spark.sql._


/**
  * This output save as avro file the information.
  */
class AvroOutput(keyName: String,
                 version: Option[Int],
                 properties: Map[String, Serializable],
                 schemas: Seq[SpartaSchema])
  extends Output(keyName, version, properties, schemas) with Logging {

  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {


    val tableName = getTableNameFromOptions(options)
    val timeDimension = getTimeFromOptions(options).notBlank
    val path = properties.getString("path", None).notBlank
    require(path.isDefined, "Destination path is required. You have to set 'path' on properties")
    val partitionBy = properties.getString("partitionBy", None)

    validateSaveMode(saveMode)

    val dataFrameWriter = dataFrame
      .write
      .format("com.databricks.spark.avro")
      .mode(getSparkSaveMode(saveMode))

    partitionBy match {
      case Some(partition) => dataFrameWriter.partitionBy(partition)
      case None => if (timeDimension.isDefined)
        dataFrameWriter.partitionBy(timeDimension.get)
    }

    dataFrameWriter.save(s"${path.get}/${versionedTableName(tableName)}")
  }
}
