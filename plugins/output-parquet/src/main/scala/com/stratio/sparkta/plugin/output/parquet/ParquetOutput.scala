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

package com.stratio.sparkta.plugin.output.parquet

import java.io.{Serializable => JSerializable}

import org.apache.spark.Logging
import org.apache.spark.sql.SaveMode._
import org.apache.spark.sql._

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

/**
  * This output save as parquet file the information.
  * @param keyName
  * @param properties
  * @param operationTypes
  * @param bcSchema
  */
class ParquetOutput(keyName: String,
                    version: Option[Int],
                    properties: Map[String, JSerializable],
                    operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                    bcSchema: Option[Seq[TableSchema]])
  extends Output(keyName, version, properties, operationTypes, bcSchema) with Logging {

  override def upsert(dataFrame: DataFrame, tableName: String, timeDimension: String): Unit = {
    val path = properties.getString("path", None)
    require(path.isDefined, "Destination path is required. You have to set 'path' on properties")

    if (timeDimension.isEmpty){
      dataFrame.write.format("parquet")
        .mode(Append)
        .save(s"${path.get}/${versionedTableName(tableName)}")
    } else {
      dataFrame.write.format("parquet")
        .partitionBy(timeDimension)
        .mode(Append)
        .save(s"${path.get}/${versionedTableName(tableName)}")
    }
  }
}
