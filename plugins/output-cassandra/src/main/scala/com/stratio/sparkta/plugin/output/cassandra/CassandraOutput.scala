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

package com.stratio.sparkta.plugin.output.cassandra

import java.io.{Serializable => JSerializable}
import scala.util.Try

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SaveMode._
import org.apache.spark.sql._
import org.apache.spark.streaming.dstream.DStream

import com.stratio.sparkta.plugin.output.cassandra.dao.AbstractCassandraDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

class CassandraOutput(keyName: String,
                      properties: Map[String, JSerializable],
                      @transient sqlContext: SQLContext,
                      operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                      bcSchema: Option[Broadcast[Seq[TableSchema]]])
  extends Output(keyName, properties, sqlContext, operationTypes, bcSchema)
  with AbstractCassandraDAO {

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.Set, WriteOp.Max, WriteOp.Min)

  override val fieldsSeparator = properties.getString("fieldsSeparator", ",")

  override val connectionHost = properties.getString("connectionHost", "127.0.0.1")

  override val keyspace = properties.getString("keyspace", "sparkta")

  override val keyspaceClass = properties.getString("class", "SimpleStrategy")

  override val replicationFactor = properties.getString("replication_factor", "1")

  override val compactStorage = properties.getString("compactStorage", None)

  override val clusteringBuckets = properties.getString("clusteringBuckets", "").split(fieldsSeparator)

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val timeBucket = properties.getString("timeBucket", None)

  override val granularity = properties.getString("granularity", None)

  override val autoCalculateId = Try(properties.getString("autoCalculateId").toBoolean).getOrElse(false)

  override val indexFields = properties.getString("indexFields", "").split(fieldsSeparator)

  override val textIndexFields = properties.getString("textIndexFields", "").split(fieldsSeparator)

  override val analyzer = properties.getString("analyzer", None)

  override val textIndexName = properties.getString("textIndexName", "lucene")

  override val connector = configConnector(sqlContext.sparkContext.getConf)

  val keyspaceCreated = createKeypace

  val schemaFiltered = filterSchemaByKeyAndField(bcSchema.get.value, timeBucket)

  val tablesCreated = if (keyspaceCreated) {
    bcSchema.exists(bc => createTables(schemaFiltered, timeBucket, autoCalculateId))
  } else false

  val indexesCreated = if (keyspaceCreated && tablesCreated) {
    bcSchema.exists(bc => createIndexes(schemaFiltered, timeBucket, autoCalculateId))
  } else false

  /*
  * The next two methods are beta.
  * With the fork of PR 112 of datastax-spark-connector.
  * https://github.com/datastax/spark-cassandra-connector/pull/648
  */
  override protected def doPersist(stream: DStream[UpdateMetricOperation]): Unit = {
    if (bcSchema.isDefined && keyspaceCreated && tablesCreated) {
      persistDataFrame(getStreamsFromOptions(stream, multiplexer, timeBucket))
    }
  }

  override def upsert(dataFrame: DataFrame, tableName: String): Unit = {
    dataFrame.save("org.apache.spark.sql.cassandra",
      Overwrite,
      Map("c_table" -> tableName, "keyspace" -> keyspace))
  }
}
