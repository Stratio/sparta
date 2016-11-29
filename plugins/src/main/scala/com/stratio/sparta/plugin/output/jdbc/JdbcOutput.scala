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

package com.stratio.sparta.plugin.output.jdbc

import java.io.{Serializable => JSerializable}
import java.util.Properties

import com.stratio.sparta.sdk.Output._
import com.stratio.sparta.sdk.TypeOp._
import com.stratio.sparta.sdk.ValidatingPropertyMap._
import com.stratio.sparta.sdk.WriteOp.WriteOp
import com.stratio.sparta.sdk._
import org.apache.spark.sql.SaveMode._
import org.apache.spark.sql._
import org.apache.spark.streaming.dstream.DStream

import scala.collection.JavaConversions._

class JdbcOutput(keyName: String,
                 version: Option[Int],
                 properties: Map[String, JSerializable],
                 bcSchema: Seq[TableSchema])
  extends Output(keyName, version, properties, bcSchema) {

  require(properties.getString("url", None).isDefined, "url must be provided")
  val url = properties.getString("url")

  val connectionProperties = {
    val props = new Properties()
    props.putAll(properties.mapValues(_.toString))
    props
  }

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)
    dataFrame.write
      .mode(getSparkSaveMode(saveMode))
      .jdbc(url, tableName, connectionProperties)
  }
}