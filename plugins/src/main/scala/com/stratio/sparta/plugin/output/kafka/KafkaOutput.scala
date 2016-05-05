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
package com.stratio.sparta.plugin.output.kafka

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.output.kafka.producer.KafkaProducer
import com.stratio.sparta.sdk.Output._
import com.stratio.sparta.sdk._
import org.apache.spark.sql._
import KafkaOutputFormat._
import com.stratio.sparta.sdk.ValidatingPropertyMap._

class KafkaOutput(keyName: String,
                  version: Option[Int],
                  properties: Map[String, JSerializable],
                  schemas: Seq[TableSchema])
  extends Output(keyName, version, properties, schemas) with KafkaProducer {

  val ouputFormat = KafkaOutputFormat.withName(properties.getString("format", "json").toUpperCase)

  val rowSeparator = properties.getString("rowSeparator", ",")

  override def upsert(dataFrame: DataFrame, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)

    ouputFormat match {
      case KafkaOutputFormat.ROW => dataFrame.rdd.foreachPartition(messages =>
        messages.foreach(message => send(properties, tableName, message.mkString(rowSeparator))))
      case _ => dataFrame.toJSON.foreachPartition { messages =>
        messages.foreach(message => send(properties, tableName, message))
      }
    }
  }
}

