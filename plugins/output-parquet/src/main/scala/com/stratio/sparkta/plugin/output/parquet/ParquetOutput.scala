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
import scala.util.Try

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql._
import org.apache.spark.{Logging, SparkContext}

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

/**
 * This output save as parquet file the information.
 * @param keyName
 * @param properties
 * @param sparkContext
 * @param operationTypes
 * @param bcSchema
 */
class ParquetOutput(keyName: String,
                    properties: Map[String, JSerializable],
                    @transient sparkContext: SparkContext,
                    operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                    bcSchema: Option[Broadcast[Seq[TableSchema]]],
                    timeName: String)
  extends Output(keyName, properties, sparkContext, operationTypes, bcSchema, timeName: String) with Logging {

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.IncBig, WriteOp.Set, WriteOp.Max, WriteOp.Min,
    WriteOp.Range, WriteOp.AccAvg, WriteOp.AccMedian, WriteOp.AccVariance, WriteOp.AccStddev, WriteOp.FullText,
    WriteOp.AccSet)

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val fixedPrecisions: Array[String] = properties.getString("fixedBuckets", None) match {
    case None => Array()
    case Some(fixPrecisions) => fixPrecisions.split(fieldsSeparator)
  }

  val fixedAgg = properties.getString("fixedAggregation", None)

  override val fixedAggregation: Map[String, Option[Any]] =
    if(fixedAgg.isDefined){
      val fixedAggSplited = fixedAgg.get.split(Output.FixedAggregationSeparator)
      Map(fixedAggSplited.head -> Some(fixedAggSplited.last))
    } else Map()

  override val isAutoCalculateId = Try(properties.getString("isAutoCalculateId").toBoolean).getOrElse(false)

  override def upsert(dataFrame: DataFrame, tableName: String): Unit = {
    val path = properties.getString("path", None)
    require(path.isDefined, "Destination path is required. You have to set 'path' on properties")
    val subPath = DateOperations.subPath(timeName, properties.getString("datePattern", None))
    dataFrame.save("parquet", SaveMode.Overwrite, Map("spark.sql.parquet.binaryAsString" -> "true",
      "path" -> s"${path.get}/$tableName$subPath"))
  }
}
