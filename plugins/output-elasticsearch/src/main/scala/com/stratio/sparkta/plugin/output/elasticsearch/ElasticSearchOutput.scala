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

package com.stratio.sparkta.plugin.output.elasticsearch

import java.io.{Serializable => JSerializable}
import scala.util.Try

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql._
import org.apache.spark.streaming.dstream.DStream

import org.elasticsearch.spark.sql._

import com.stratio.sparkta.plugin.output.elasticsearch.dao.ElasticSearchDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

class ElasticSearchOutput(keyName: String,
                          properties: Map[String, JSerializable],
                          @transient sparkContext: SparkContext,
                          operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                          bcSchema: Option[Broadcast[Seq[TableSchema]]],
                          timeName: String)
  extends Output(keyName, properties, sparkContext, operationTypes, bcSchema, timeName) with ElasticSearchDAO {

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.IncBig, WriteOp.Set, WriteOp.Max, WriteOp.Min,
    WriteOp.AccAvg, WriteOp.AccMedian, WriteOp.AccVariance, WriteOp.AccStddev, WriteOp.FullText, WriteOp.AccSet)

  override val dateType = ElasticSearchDAO.getDateTimeType(properties.getString("dateType", None))

  override val nodes = properties.getString("nodes", ElasticSearchDAO.DEFAULT_NODE)

  override val defaultPort = properties.getString("defaultPort", ElasticSearchDAO.DEFAULT_PORT)

  override val defaultAnalyzerType = properties.getString("defaultAnalyzerType", None)

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val fieldsSeparator = properties.getString("fieldsSeparator", ",")

  override val fixedBuckets = properties.getString("fixedBuckets", "").split(fieldsSeparator)

  override val isAutoCalculateId = Try(properties.getString("isAutoCalculateId").toBoolean).getOrElse(false)

  override val idField = properties.getString("idField", None)

  override val defaultIndexMapping = properties.getString("indexMapping",
    Some(ElasticSearchDAO.DEFAULT_INDEX_TYPE))

  override val indexMapping = ElasticSearchDAO.getIndexType(defaultIndexMapping)

  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    if (indexMapping.isDefined) persistDataFrame(stream)
  }

  override def upsert(dataFrame: DataFrame, tableName: String): Unit = {
    val indexNameType = (tableName + "/" + indexMapping.get).toLowerCase
    if (idField.isDefined || isAutoCalculateId)
      dataFrame.saveToEs(indexNameType, getSparkConfig(timeName))
    else dataFrame.saveToEs(indexNameType)
  }
}
