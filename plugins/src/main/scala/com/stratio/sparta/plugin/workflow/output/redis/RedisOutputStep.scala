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
package com.stratio.sparta.plugin.workflow.output.redis

import java.io.Serializable

import com.stratio.sparta.plugin.workflow.output.redis.dao.AbstractRedisDAO
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputStep
import com.stratio.sparta.sdk.workflow.step.OutputStep._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row}

/**
  * Saves calculated cubes on Redis.
  * The hashKey will have this kind of structure -> A:valueA:B:valueB:C:valueC.It is important to see that
  * values will be part of the key and the objective of it is to perform better searches in the hash.
  *
  * @author anistal
  */
class RedisOutputStep(name: String, xDSession: XDSession, properties: Map[String, Serializable])
  extends OutputStep(name, xDSession, properties) with AbstractRedisDAO with Serializable {

  override val hostname = properties.getString("hostname", DefaultRedisHostname)
  override val port = properties.getString("port", DefaultRedisPort).toInt

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)
    val updateFields = getPrimaryKeyOptions(options) match {
      case Some(pk) => pk.split(",").toSeq
      case None => Seq.empty[String]
    }
    val schema = dataFrame.schema

    validateSaveMode(saveMode)

    dataFrame.foreachPartition { rowList =>
      rowList.foreach { row =>
        val valuesList = getValuesList(row, schema.fieldNames)
        val hashKey = getHashKeyFromRow(valuesList, schema, updateFields)
        getMeasuresFromRow(valuesList, schema, updateFields).foreach { case (measure, value) =>
          hset(hashKey, measure.name, value)
        }
      }
    }
  }

  def getHashKeyFromRow(valuesList: Seq[(String, String)], schema: StructType, updateFields: Seq[String]): String =
    valuesList.flatMap { case (key, value) =>
      val fieldSearch = schema.fields.find(structField =>
        updateFields.contains(structField.name) && structField.name == key)

      fieldSearch.map(structField => s"${structField.name}$IdSeparator$value")
    }.mkString(IdSeparator)

  def getMeasuresFromRow(valuesList: Seq[(String, String)],
                         schema: StructType,
                         updateFields: Seq[String]): Seq[(StructField, String)] =
    valuesList.flatMap { case (key, value) =>
      val fieldSearch = schema.fields.find(structField =>
        !updateFields.contains(structField.name) && structField.name == key)
      fieldSearch.map(field => (field, value))
    }

  def getValuesList(row: Row, fieldNames: Array[String]): Seq[(String, String)] =
    fieldNames.zip(row.toSeq).map { case (key, value) => (key, value.toString) }.toSeq
}