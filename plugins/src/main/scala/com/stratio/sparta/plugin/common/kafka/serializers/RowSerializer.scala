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

import com.databricks.spark.avro.RowAvroHelper
import com.stratio.sparta.plugin.helper.SchemaHelper
import com.stratio.sparta.sdk.workflow.enumerators.OutputFormatEnum
import com.twitter.bijection.Injection
import com.twitter.bijection.avro.GenericAvroCodecs
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, Serializer, StringSerializer}
import org.apache.spark.sql.Row
import org.apache.spark.sql.json.RowJsonHelper

import scala.collection.JavaConversions._


class RowSerializer extends Serializer[Row] {

  private val stringSerializer = new StringSerializer
  private val byteArraySerializer = new ByteArraySerializer
  private var outputFormat = OutputFormatEnum.ROW
  private var delimiter = ","
  private var recordNamespace = ","
  private var recordName = "topLevelRecord"
  private var avroSchema: Option[Schema] = None
  private var recordInjection: Option[Injection[GenericRecord, Array[Byte]]] = None
  private var avroConverter: Option[(Any) => Any] = None
  private var jsonConf: Map[String, String] = Map.empty[String, String]

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {

    val configPrefix = if(isKey) "key" else "value"

    val format = configs.getOrElse(s"$configPrefix.serializer.outputFormat", "ROW").toString

    val useRowSchema =
      configs.getOrElse(s"$configPrefix.serializer.avro.schema.fromRow", "true").toString.toBoolean

    val schemaProvided = configs.getOrElse(s"$configPrefix.serializer.avro.schema.provided", "") match {
      case "" => None
      case v: String => Some(v)
    }

    recordNamespace =
      configs.getOrElse(s"$configPrefix.serializer.avro.recordNamespace", "").toString

    recordName =
      configs.getOrElse(s"$configPrefix.serializer.avro.recordName", "topLevelRecord").toString

    delimiter = configs.getOrElse(s"$configPrefix.serializer.row.delimiter", ",").toString

    avroSchema = SchemaHelper.getAvroSchema(useRowSchema, schemaProvided)
    avroConverter = SchemaHelper.getAvroSparkSchema(useRowSchema, schemaProvided)
      .map(schema => RowAvroHelper.createConverterToAvro(schema, recordName, recordNamespace))
    outputFormat = OutputFormatEnum.withName(format)
    stringSerializer.configure(configs, isKey)
    byteArraySerializer.configure(configs, isKey)

    jsonConf = {
      configs.filterKeys(key => key.contains(s"$configPrefix.serializer.json"))
        .map { case (key, value) => (key.replace(s"$configPrefix.serializer.json.", ""), value.toString) }
    }.toMap
  }

  override def serialize(topic: String, data: Row): Array[Byte] = {
    outputFormat match {
      case OutputFormatEnum.AVRO =>
        val record = avroConverter match {
          case Some(converter) =>
            converter(data).asInstanceOf[GenericRecord]
          case None =>
            val converter = RowAvroHelper.createConverterToAvro(data.schema, recordName, recordNamespace)
            converter(data).asInstanceOf[GenericRecord]
        }
        val injection = recordInjection
          .getOrElse(GenericAvroCodecs.toBinary[GenericRecord](avroSchema.getOrElse(record.getSchema)))

        byteArraySerializer.serialize(topic, injection.apply(record))
      case OutputFormatEnum.ROW =>
        stringSerializer.serialize(topic, data.mkString(delimiter))
      case _ =>
        stringSerializer.serialize(topic, RowJsonHelper.toJSON(data, jsonConf))
    }
  }

  override def close(): Unit = {
    stringSerializer.close()
    byteArraySerializer.close()
  }

}