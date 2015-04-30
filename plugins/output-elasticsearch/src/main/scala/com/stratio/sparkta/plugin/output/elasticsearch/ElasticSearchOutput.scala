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

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql._
import org.apache.spark.streaming.dstream.DStream

import org.elasticsearch.spark.sql._

import com.stratio.sparkta.plugin.output.elasticsearch.dao.AbstractElasticSearchDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

class ElasticSearchOutput(keyName: String,
                          properties: Map[String, JSerializable],
                          @transient sqlContext: SQLContext,
                          operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                          bcSchema: Option[Broadcast[Seq[TableSchema]]])
  extends Output(keyName, properties, sqlContext, operationTypes, bcSchema)
  with AbstractElasticSearchDAO {

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.Set, WriteOp.Max, WriteOp.Min, WriteOp.AccAvg,
    WriteOp.AccMedian, WriteOp.AccVariance, WriteOp.AccStddev, WriteOp.FullText, WriteOp.AccSet)

  override val dateType = AbstractElasticSearchDAO.getDateTimeType(properties.getString("dateType", None))

  override val nodes = properties.getString("nodes", AbstractElasticSearchDAO.DEFAULT_NODE)

  override val defaultPort = properties.getString("defaultPort", AbstractElasticSearchDAO.DEFAULT_PORT)

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val timeBucket = properties.getString("dateBucket", None)

  override val granularity = properties.getString("granularity", None)

  override val autoCalculateId = Try(properties.getString("autoCalculateId").toBoolean).getOrElse(false)

  override val idField = properties.getString("idField", None)

  override val defaultIndexMapping = properties.getString("indexMapping",
    Some(AbstractElasticSearchDAO.DEFAULT_INDEX_TYPE))

  override val indexMapping = AbstractElasticSearchDAO.getIndexType(defaultIndexMapping)

  override protected def doPersist(stream: DStream[UpdateMetricOperation]): Unit = {
    if (indexMapping.isDefined) {
      persistDataFrame(getStreamsFromOptions(stream, multiplexer, timeBucket))
    }
  }

  override def upsert(dataFrame: DataFrame, tableName: String): Unit = {
    val indexNameType = tableName + "/" + indexMapping.get
    if (idField.isDefined || autoCalculateId) {
      dataFrame.saveToEs(indexNameType, getSparkConfig(timeBucket))
    } else dataFrame.saveToEs(indexNameType)
  }
}
