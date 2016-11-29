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
package com.stratio.sparta.plugin.output.parquet

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.Output._
import com.stratio.sparta.sdk.ValidatingPropertyMap._
import com.stratio.sparta.sdk._
import org.apache.spark.Logging
import org.apache.spark.sql.SaveMode._
import org.apache.spark.sql._

/**
 * This output save as parquet file the information.
 * @param keyName
 * @param properties
 * @param schemas
 */
class ParquetOutput(keyName: String,
                    version: Option[Int],
                    properties: Map[String, JSerializable],
                    schemas: Seq[TableSchema])
  extends Output(keyName, version, properties, schemas) with Logging {

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)
    val timeDimension = getTimeFromOptions(options)
    val path = properties.getString("path", None)
    require(path.isDefined, "Destination path is required. You have to set 'path' on properties")
    val partitionBy = properties.getString("partitionBy", None)

    val dataFrameWriter = dataFrame
      .write
      .format("parquet")
      .mode(getSparkSaveMode(saveMode))

    partitionBy match {
      case Some(partition) => dataFrameWriter.partitionBy(partition)
      case None => if (timeDimension.isDefined)
        dataFrameWriter.partitionBy(timeDimension.get)
    }

    dataFrameWriter.save(s"${path.get}/${versionedTableName(tableName)}")
  }
}
