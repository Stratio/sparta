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

package com.stratio.sparta.plugin.common.kafka.serializers

import java.util

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.enumerations.SchemaInputMode
import com.stratio.sparta.plugin.helper.JsonHelper
import com.stratio.sparta.sdk.workflow.enumerators.InputFormatEnum
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.json.RowJsonHelper._
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import scala.collection.JavaConversions._
import scala.util.Try


class RowDeserializer extends Deserializer[Row] with SLF4JLogging {

  private val stringDeserializer = new StringDeserializer
  private var inputFormat = InputFormatEnum.STRING
  private var jsonSchema: Option[StructType] = None
  private var stringSchema: Option[StructType] = None
  private var jsonConf: Map[String, String] = Map.empty[String, String]

  //scalastyle:off
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {
    inputFormat = InputFormatEnum.withName {
      {
        if (isKey)
          configs.getOrElse("key.deserializer.inputFormat", "STRING")
        else configs.getOrElse("value.deserializer.inputFormat", "STRING")
      }.toString
    }

    inputFormat match {
      case InputFormatEnum.JSON =>
        val useRowSchema = {
          if (isKey)
            configs.getOrElse("key.deserializer.json.schema.fromRow", "true")
          else configs.getOrElse("value.deserializer.json.schema.fromRow", "true")
        }.toString.toBoolean
        val schemaInputMode = SchemaInputMode.withName {
          {
            if (isKey)
              configs.getOrElse("key.deserializer.json.schema.inputMode", "SPARKFORMAT")
            else configs.getOrElse("value.deserializer.json.schema.inputMode", "SPARKFORMAT")
          }.toString.toUpperCase
        }
        val schemaProvided = {
          val inputSchema = {
            if (isKey)
              configs.getOrElse("key.deserializer.schema.provided", "")
            else configs.getOrElse("value.deserializer.schema.provided", "")
          }.toString

          if (inputSchema.isEmpty) None
          else Option(inputSchema)
        }

        jsonConf = {
          if (isKey)
            configs.filterKeys(key => key.contains("key.deserializer.json"))
              .map { case (key, value) => (key.replace("key.deserializer.json.", ""), value.toString) }
          else configs.filterKeys(key => key.contains("value.deserializer.json"))
            .map { case (key, value) => (key.replace("value.deserializer.json.", ""), value.toString) }
        }.toMap

        jsonSchema = JsonHelper.getJsonSchema(useRowSchema, schemaInputMode, schemaProvided, jsonConf)
      case InputFormatEnum.STRING =>
        val outputFieldName = {
          if (isKey)
            configs.getOrElse("key.deserializer.outputField", "raw")
          else configs.getOrElse("value.deserializer.outputField", "raw")
        }.asInstanceOf[String]

        stringSchema = Option(StructType(Seq(StructField(outputFieldName, StringType))))
    }

    stringDeserializer.configure(configs, isKey)
  }

  override def deserialize(topic: String, data: Array[Byte]): Row = {
    val stringData = stringDeserializer.deserialize(topic, data)

    inputFormat match {
      case InputFormatEnum.JSON =>
        Try {
          jsonSchema match {
            case Some(schemaProvided) =>
              toRow(stringData, jsonConf, schemaProvided)
            case None =>
              toRow(stringData, jsonConf, extractSchemaFromJson(stringData, jsonConf))
          }
        }.getOrElse(toRow(stringData, jsonConf, extractSchemaFromJson(stringData, jsonConf)))
      case _ => new GenericRowWithSchema(Array(stringData), stringSchema.get)
    }
  }

  override def close(): Unit =
    stringDeserializer.close()

}