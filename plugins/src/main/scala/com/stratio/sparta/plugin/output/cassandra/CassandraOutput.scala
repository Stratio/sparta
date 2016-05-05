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
package com.stratio.sparta.plugin.output.cassandra

import java.io.{Serializable => JSerializable}

import com.datastax.spark.connector.cql.CassandraConnector
import com.stratio.sparta.plugin.output.cassandra.dao.CassandraDAO
import com.stratio.sparta.sdk.Output._
import com.stratio.sparta.sdk.ValidatingPropertyMap._
import com.stratio.sparta.sdk._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SaveMode._
import org.apache.spark.sql._

class CassandraOutput(keyName: String,
                      version: Option[Int],
                      properties: Map[String, JSerializable],
                      schemas: Seq[TableSchema])
  extends Output(keyName, version, properties, schemas)
  with CassandraDAO {

  override val keyspace = properties.getString("keyspace", "sparta")

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

  override val tableVersion = version

  override def setup(options: Map[String, String]): Unit = {
    val connector = getCassandraConnector
    val keyspaceCreated = createKeypace(connector)
    val tablesCreated = if (keyspaceCreated) {
      createTables(connector, schemas)
    } else false
    if (keyspaceCreated && tablesCreated) {
      if (indexFields.isDefined && indexFields.get.nonEmpty) {
        createIndexes(connector, schemas)
      }
      if (textIndexFields.isDefined &&
        textIndexFields.nonEmpty) {
        createTextIndexes(connector, schemas)
      }
    }
  }

  override def upsert(dataFrame: DataFrame, options: Map[String, String]): Unit = {
    val tableNameVersioned = getTableName(getTableNameFromOptions(options).toLowerCase)
    write(dataFrame, tableNameVersioned)
  }

  def write(dataFrame: DataFrame, tableNameVersioned: String): Unit = {
    dataFrame.write
      .format("org.apache.spark.sql.cassandra")
      .mode(Append)
      .options(Map("table" -> tableNameVersioned, "keyspace" -> keyspace, "cluster" -> cluster)).save()
  }

  def getCassandraConnector: CassandraConnector = {
    val sparkConf = new SparkConf().setAll(CassandraOutput.getSparkConfiguration(properties))
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
