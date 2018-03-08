/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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