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

import com.datastax.spark.connector.cql.CassandraConnector
import com.stratio.sparkta.plugin.output.cassandra.dao.CassandraDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SaveMode._
import org.apache.spark.sql._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.sql.DataFrameWriter

class CassandraOutput[T](keyName: String,
                      version: Option[Int],
                      properties: Map[String, JSerializable],
                      operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                      bcSchema: Option[Seq[TableSchema]])
  extends Output[T](keyName, version, properties, operationTypes, bcSchema)
  with CassandraDAO {

  override val keyspace = properties.getString("keyspace", "sparkta")

  override val keyspaceClass = properties.getString("class", "SimpleStrategy")

  override val replicationFactor = properties.getString("replication_factor", "1")

  override val compactStorage = properties.getString("compactStorage", None)

  override val clusteringPrecisions = properties.getString("clusteringPrecisions", None).map(_.split(FieldsSeparator))

  override val indexFields = properties.getString("indexFields", None).map(_.split(FieldsSeparator))

  override val textIndexFields = properties.getString("textIndexFields", None).map(_.split(FieldsSeparator))

  override val analyzer = properties.getString("analyzer", DefaultAnalyzer)

  override val dateFormat = properties.getString("dateFormat", DefaultDateFormat)

  override val refreshSeconds = properties.getString("refreshSeconds", DefaultRefreshSeconds)

  override val textIndexName = properties.getString("textIndexName", "lucene")

  val cluster = properties.getString("cluster", "default")

  override val supportedWriteOps: Seq[WriteOp.Value] = Seq(WriteOp.FullText, WriteOp.Inc, WriteOp.IncBig,
    WriteOp.Set, WriteOp.Range, WriteOp.Max, WriteOp.Min, WriteOp.Avg, WriteOp.Median,
    WriteOp.Variance, WriteOp.Stddev, WriteOp.WordCount, WriteOp.EntityCount)

  override val tableVersion = version

  override def setup: Unit = {

    val connector = getCassandraConnector()

    val keyspaceCreated = createKeypace(connector)

    val schemaFiltered = bcSchema.get.filter(tschema => tschema.outputName == keyName)
      .map(tschemaFiltered => getTableSchemaFixedId(tschemaFiltered))

    val tablesCreated = if (keyspaceCreated) {
      bcSchema.exists(bc => createTables(connector, schemaFiltered, isAutoCalculateId))
    } else false

    if (keyspaceCreated && tablesCreated) {
      if (indexFields.isDefined && indexFields.get.nonEmpty) {
        bcSchema.exists(bc => createIndexes(connector, schemaFiltered, isAutoCalculateId))
      }
      if (textIndexFields.isDefined &&
        textIndexFields.nonEmpty &&
        fixedMeasures.values.nonEmpty &&
        fixedMeasure.get == textIndexName) {
        bcSchema.exists(bc => createTextIndexes(connector, schemaFiltered))
      }
    }
  }

  override def doPersist(stream: DStream[(T, MeasuresValues)]): Unit = {
    persistDataFrame(stream)
  }

  override def upsert(dataFrame: DataFrame, tableName: String, timeDimension: String): Unit = {
    val tableNameVersioned = getTableName(tableName.toLowerCase)
    write(dataFrame,tableNameVersioned)
  }

  def write(dataFrame: DataFrame, tableNameVersioned: String): Unit = {
    dataFrame.write
      .format("org.apache.spark.sql.cassandra")
      .mode(Append)
      .options(Map("table" -> tableNameVersioned, "keyspace" -> keyspace, "cluster" -> cluster)).save()
  }

  def getCassandraConnector(): CassandraConnector = {
    val sparkConf = sqlContext.sparkContext.getConf
    CassandraConnector(sparkConf)
  }

}

object CassandraOutput {

  final val DefaultHost = "127.0.0.1"
  final val DefaultPort = "9042"

  def getSparkConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    val connectionHost = configuration.getString("connectionHost", DefaultHost)
    val connectionPort = configuration.getString("connectionPort", DefaultPort)

    val sparkProperties = getSparkCassandraProperties(configuration)

    sparkProperties ++
      Seq(
        ("spark.cassandra.connection.host", connectionHost),
        ("spark.cassandra.connection.port", connectionPort)
      )
  }

  private def getSparkCassandraProperties(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    configuration.get("sparkProperties") match {
      case Some(properties) =>
        val conObj = configuration.getMapFromJsoneyString("sparkProperties")
        conObj.map(propKeyPair => {
          val key = propKeyPair("sparkPropertyKey")
          val value = propKeyPair("sparkPropertyValue")
          (key, value)
        })
      case None => Seq()
    }

  }

}
