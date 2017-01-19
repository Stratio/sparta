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

import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql._

class CassandraOutput(keyName: String,
                      version: Option[Int],
                      properties: Map[String, JSerializable],
                      schemas: Seq[SpartaSchema])
  extends Output(keyName, version, properties, schemas) {

  val MaxTableNameLength = 48

  val keyspace = properties.getString("keyspace", "sparta")
  val cluster = properties.getString("cluster", "default")
  val tableVersion = version

  override def supportedSaveModes: Seq[SaveModeEnum.Value] =
    Seq(SaveModeEnum.Append, SaveModeEnum.ErrorIfExists, SaveModeEnum.Ignore, SaveModeEnum.Overwrite)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val tableNameVersioned = getTableName(getTableNameFromOptions(options).toLowerCase)

    validateSaveMode(saveMode)

    dataFrame.write
      .format("org.apache.spark.sql.cassandra")
      .mode(getSparkSaveMode(saveMode))
      .options(Map("table" -> tableNameVersioned, "keyspace" -> keyspace, "cluster" -> cluster))
      .save()
  }

  def getTableName(table: String): String = {
    val tableNameCut = if (table.length > MaxTableNameLength - 3) table.substring(0, MaxTableNameLength - 3) else table
    tableVersion match {
      case Some(v) => s"$tableNameCut${Output.Separator}v$v"
      case None => tableNameCut
    }
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
