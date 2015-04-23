/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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

package com.stratio.sparkta.plugin.output.cassandra

import java.io.{Serializable => JSerializable}
import scala.util.Try

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.streaming.dstream.DStream

import com.stratio.sparkta.plugin.output.cassandra.dao.AbstractCassandraDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk.{WriteOp, _}
//import com.datastax.spark.connector.cql.CassandraConnector
//import com.datastax.spark.connector._

class CassandraOutput(keyName: String,
                      properties: Map[String, JSerializable],
                      sqlContext: SQLContext,
                      operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                      bcSchema: Option[Broadcast[Seq[TableSchema]]])
  extends Output(keyName, properties, sqlContext, operationTypes, bcSchema)
  with AbstractCassandraDAO {

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.Set, WriteOp.Max, WriteOp.Min)

  override val connectionSeeds = properties.getString("connectionSeeds", "127.0.0.1")

  override val keyspace = properties.getString("keyspace", "sparkta")

  override val keyspaceClass = properties.getString("class", "SimpleStrategy")

  override val replicationFactor = properties.getString("replication_factor", "1")

  override val compactStorage = properties.getStringOption("compactStorage", None)

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val timestampFieldName = properties.getString("timestampFieldName", "timestamp")

  override val timeBucket = properties.getString("timestampBucket", "")

  override val granularity = properties.getString("granularity", "")

  override val fieldsSeparator = properties.getString("fieldsSeparator", ",")

  override val textIndexFields = properties.getString("textIndexFields", "").split(fieldsSeparator)

  override val clusteringBuckets = properties.getString("clusteringBuckets", "").split(fieldsSeparator)

  override val analyzer = properties.getString("analyzer", "english")

  override val sparkConf = Some(sqlContext.sparkContext.getConf)

  override val keyspaceCreated = createKeypace

  override val tablesCreated = bcSchema.exists(bc => createTables(bc.value, keyName, timeBucket, granularity))

  override protected def doPersist(stream: DStream[UpdateMetricOperation]): Unit = {
    if (bcSchema.isDefined && tablesCreated) {
      persistDataFrame(getStreamsFromOptions(stream, multiplexer, timeBucket))
      //println("COUNT : " + stream.context.sparkContext.cassandraTable("test", "kv").count())
    }
  }

  override def upsert(dataFrame: DataFrame): Unit = {
    dataFrame.foreach(println)
  }
}
