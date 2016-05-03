/**
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
package com.stratio.sparta.plugin.output.redis

import java.io.Serializable

import com.stratio.sparta.plugin.output.redis.dao.AbstractRedisDAO
import com.stratio.sparta.sdk.Output._
import com.stratio.sparta.sdk.TypeOp._
import com.stratio.sparta.sdk.ValidatingPropertyMap._
import com.stratio.sparta.sdk.WriteOp.WriteOp
import com.stratio.sparta.sdk._
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{Row, DataFrame}
import org.apache.spark.streaming.dstream.DStream

import scala.collection.immutable.Iterable

/**
 * Saves calculated cubes on Redis.
 * The hashKey will have this kind of structure -> A:valueA:B:valueB:C:valueC.It is important to see that
 * values will be part of the key and the objective of it is to perform better searches in the hash.
 * @author anistal
 */
class RedisOutput(keyName: String,
                  version: Option[Int],
                  properties: Map[String, Serializable],
                  schemas: Seq[TableSchema])
  extends Output(keyName, version, properties, schemas)
  with AbstractRedisDAO with Serializable {

  override val hostname = properties.getString("hostname", DefaultRedisHostname)

  override val port = properties.getString("port", DefaultRedisPort).toInt

  override def upsert(dataFrame: DataFrame, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)
    val timeDimension = getTimeFromOptions(options)
    val schema = dataFrame.schema
    dataFrame.foreachPartition{ rowList =>
      rowList.foreach{ row =>
        val valuesList = getValuesList(row,schema.fieldNames)
        val hashKey = getHashKeyFromRow(valuesList, schema, timeDimension)
        getMeasuresFromRow(valuesList, schema, timeDimension).foreach { case (measure, value) =>
          hset(hashKey, measure.name, value)
        }
      }
    }
  }

  def getHashKeyFromRow(valuesList: Seq[(String, String)], schema: StructType, timeDimension: Option[String]): String =
    valuesList.flatMap{ case (key, value) =>
      val fieldSearch = schema.fields.find(structField =>
        (!structField.nullable &&
          !structField.metadata.contains(Output.MeasureMetadataKey) &&
          structField.name == key) ||
          timeDimension.exists(name => structField.name == name))
      fieldSearch.map(structField => s"${structField.name}$IdSeparator$value")
    }.mkString(IdSeparator)

  def getMeasuresFromRow(valuesList: Seq[(String, String)],
                         schema: StructType,
                         timeDimension: Option[String]): Seq[(StructField, String)] =
    valuesList.flatMap{ case (key, value) =>
      val fieldSearch = schema.fields.find(structField =>
        !structField.nullable &&
          structField.metadata.contains(Output.MeasureMetadataKey) &&
          structField.name == key &&
          timeDimension.forall(name => structField.name != name))
      fieldSearch.map(field => (field, value))
    }

  def getValuesList(row: Row, fieldNames: Array[String]): Seq[(String, String)] =
    fieldNames.zip(row.toSeq).map{ case (key, value) => (key, value.toString)}.toSeq
}