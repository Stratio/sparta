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

import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SaveMode._
import org.apache.spark.sql._
import org.apache.spark.streaming.dstream.DStream

import com.stratio.sparkta.plugin.output.cassandra.dao.CassandraDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._

class CassandraOutput(keyName: String,
                      properties: Map[String, JSerializable],
                      @transient sparkContext: SparkContext,
                      operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                      bcSchema: Option[Broadcast[Seq[TableSchema]]],
                      timeName: String)
  extends Output(keyName, properties, sparkContext, operationTypes, bcSchema, timeName)
  with CassandraDAO {

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.IncBig, WriteOp.Set, WriteOp.Max, WriteOp.Min,
    WriteOp.AccAvg, WriteOp.AccMedian, WriteOp.AccVariance, WriteOp.AccStddev, WriteOp.FullText)

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val isAutoCalculateId = Try(properties.getString("isAutoCalculateId").toBoolean).getOrElse(false)

  override val fieldsSeparator = properties.getString("fieldsSeparator", ",")

  override val fixedPrecisions: Array[String] = properties.getString("fixedPrecisions", None) match {
    case None => Array()
    case Some(fixPrecisions) => fixPrecisions.split(fieldsSeparator)
  }

  override val cluster = properties.getString("cluster", "Test Cluster")

  override val keyspace = properties.getString("keyspace", "sparkta")

  override val keyspaceClass = properties.getString("class", "SimpleStrategy")

  override val replicationFactor = properties.getString("replication_factor", "1")

  override val compactStorage = properties.getString("compactStorage", None)

  override val clusteringPrecisions = properties.getString("clusteringPrecisions", None).map(_.split(fieldsSeparator))

  override val indexFields = properties.getString("indexFields", None).map(_.split(fieldsSeparator))

  override val textIndexFields = properties.getString("textIndexFields", None).map(_.split(fieldsSeparator))

  override val analyzer = properties.getString("analyzer", DefaultAnalyzer)

  override val dateFormat = properties.getString("dateFormat", DefaultDateFormat)

  override val refreshSeconds = properties.getString("refreshSeconds", DefaultRefreshSeconds)

  override val textIndexName = properties.getString("textIndexName", "lucene")

  val fixedAgg = properties.getString("fixedAggregation", None)

  override val fixedAggregation: Map[String, Option[Any]] =
    if (textIndexFields.isDefined && !textIndexFields.get.isEmpty && fixedAgg.isDefined) {
      val fixedAggSplited = fixedAgg.get.split(Output.FixedAggregationSeparator)
      Map(fixedAggSplited.head -> Some(""))
    } else Map()

  override val connector = Some(CassandraConnector(sparkContext.getConf))

  val keyspaceCreated = createKeypace

  val schemaFiltered = bcSchema.get.value.filter(tschema => (tschema.outputName == keyName))
    .map(tschemaFiltered => getTableSchemaFixedId(tschemaFiltered))

  val tablesCreated = if (keyspaceCreated) {
    bcSchema.exists(bc => createTables(schemaFiltered, timeName, isAutoCalculateId))
  } else false

  val indexesCreated = if (keyspaceCreated && tablesCreated && indexFields.isDefined && !indexFields.get.isEmpty) {
    bcSchema.exists(bc => createIndexes(schemaFiltered, timeName, isAutoCalculateId))
  } else false

  val textIndexesCreated = if (keyspaceCreated &&
    tablesCreated &&
    textIndexFields.isDefined &&
    !textIndexFields.isEmpty &&
    !fixedAggregation.isEmpty &&
    fixedAgg.get == textIndexName) {
    bcSchema.exists(bc => createTextIndexes(schemaFiltered))
  } else false

  /*
  * The next two methods are beta.
  * With the fork of PR 112 of datastax-spark-connector.
  * https://github.com/datastax/spark-cassandra-connector/pull/648
  */
  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    if (bcSchema.isDefined && keyspaceCreated && tablesCreated) persistDataFrame(stream)
  }

  override def upsert(dataFrame: DataFrame, tableName: String): Unit = {
    dataFrame.save("org.apache.spark.sql.cassandra", Append, Map("c_table" -> tableName, "keyspace" -> keyspace))
  }
}

object CassandraOutput {

  final val DefaultHost = "127.0.0.1"

  def getSparkConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    val connectionHost = configuration.getString("connectionHost", DefaultHost)
    Seq(("spark.cassandra.connection.host", connectionHost))
  }
}
