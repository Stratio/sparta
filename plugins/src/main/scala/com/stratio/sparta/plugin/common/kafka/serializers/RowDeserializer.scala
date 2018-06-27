/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.kafka.serializers

import java.util

import akka.event.slf4j.SLF4JLogging
import com.databricks.spark.avro.RowAvroHelper
import com.stratio.sparta.plugin.enumerations.SchemaInputMode
import com.stratio.sparta.plugin.helper.SchemaHelper
import com.stratio.sparta.core.enumerators.InputFormatEnum
import com.twitter.bijection.Injection
import com.twitter.bijection.avro.GenericAvroCodecs
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.{GenericRow, GenericRowWithSchema}
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
  private var avroConf: Map[String, String] = Map.empty[String, String]
  private var avroSparkSchema: Option[StructType] = None
  private var avroConverter: Option[AnyRef => AnyRef] = None
  private var avroRecordInjection: Option[Injection[GenericRecord, Array[Byte]]] = None

  //scalastyle:off
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {

    val configPrefix = if(isKey) "key" else "value"

    inputFormat = InputFormatEnum.withName {
      configs.getOrElse(s"$configPrefix.deserializer.inputFormat", "STRING").toString
    }

    inputFormat match {
      case InputFormatEnum.JSON =>
        val useRowSchema =
          configs.getOrElse(s"$configPrefix.deserializer.json.schema.fromRow", "true").toString.toBoolean

        val schemaInputMode = SchemaInputMode.withName {
          configs.getOrElse(s"$configPrefix.deserializer.json.schema.inputMode", "SPARKFORMAT").toString.toUpperCase
        }

        val schemaProvided = {
          val inputSchema = configs.getOrElse(s"$configPrefix.deserializer.schema.provided", "").toString

          if (inputSchema.isEmpty) None
          else Option(inputSchema)
        }

        jsonConf = {
          configs.filterKeys(key => key.contains(s"$configPrefix.deserializer.json"))
            .map { case (key, value) => (key.replace(s"$configPrefix.deserializer.json.", ""), value.toString) }
        }.toMap

        jsonSchema = SchemaHelper.getJsonSparkSchema(useRowSchema, schemaInputMode, schemaProvided, jsonConf)
      case InputFormatEnum.AVRO =>
        val avroInputSchema = configs.getOrElse(s"$configPrefix.deserializer.avro.schema", "").toString

        val avroSchema = {
          if (avroInputSchema.nonEmpty) Option(SchemaHelper.getAvroSchemaFromString(avroInputSchema))
          else None
        }

        avroSparkSchema = avroSchema.map(schema => SchemaHelper.getSparkSchemaFromAvroSchema(schema))

        avroConverter = (avroSchema, avroSparkSchema) match {
          case (Some(schema), Some(sparkSchema)) => Option(RowAvroHelper.getAvroConverter(schema, sparkSchema))
          case _ => None
        }

        avroRecordInjection = avroSchema.map(schema => GenericAvroCodecs.toBinary[GenericRecord](schema))

        avroConf = {
          configs.filterKeys(key => key.contains(s"$configPrefix.deserializer.avro"))
            .map { case (key, value) => (key.replace(s"$configPrefix.deserializer.avro.", ""), value.toString) }
        }.toMap
      case InputFormatEnum.STRING =>
        val outputFieldName = configs.getOrElse(s"$configPrefix.deserializer.outputField", "raw").toString

        stringSchema = Option(StructType(Seq(StructField(outputFieldName, StringType))))
    }

    stringDeserializer.configure(configs, isKey)
  }

  //scalastyle:on

  override def deserialize(topic: String, data: Array[Byte]): Row = {
    inputFormat match {
      case InputFormatEnum.JSON =>
        val stringData = stringDeserializer.deserialize(topic, data)
        Try {
          jsonSchema match {
            case Some(schemaProvided) =>
              toRow(stringData, jsonConf, schemaProvided)
            case None =>
              toRow(stringData, jsonConf, extractSchemaFromJson(stringData, jsonConf))
          }
        }.getOrElse(toRow(stringData, jsonConf, extractSchemaFromJson(stringData, jsonConf)))
      case InputFormatEnum.AVRO =>
        (avroSparkSchema, avroConverter, avroRecordInjection) match {
          case (Some(sparkSchema), Some(converter), Some(recordInjection)) =>
            val record = recordInjection.invert(data).get
            new GenericRowWithSchema(converter(record).asInstanceOf[GenericRow].toSeq.toArray, sparkSchema)
          case _ =>
            throw new Exception("Impossible to parse Avro data without schema and converter")
        }
      case _ =>
        new GenericRowWithSchema(Array(stringDeserializer.deserialize(topic, data)), stringSchema.get)
    }
  }

  override def close(): Unit =
    stringDeserializer.close()

}