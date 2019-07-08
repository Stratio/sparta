/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.kafka.serializers

import java.util

import com.databricks.spark.avro.RowAvroHelper
import com.stratio.sparta.core.enumerators.InputFormatEnum
import com.stratio.sparta.core.helpers.SSLHelper
import com.stratio.sparta.plugin.common.kafka.SchemaRegistryClientFactory
import com.stratio.sparta.plugin.enumerations.SchemaInputMode
import com.stratio.sparta.plugin.helper.SchemaHelper
import com.twitter.bijection.Injection
import com.twitter.bijection.avro.GenericAvroCodecs
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.rest.RestService
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, Deserializer, StringDeserializer}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.{GenericRow, GenericRowWithSchema}
import org.apache.spark.sql.json.RowJsonHelper._
import org.apache.spark.sql.types.{BinaryType, StringType, StructField, StructType}

import scala.collection.JavaConversions._

class RowDeserializer extends Deserializer[Row] {

  private var stringDeserializer : Option[StringDeserializer] = None
  private var byteDeserializer : Option[ByteArrayDeserializer] = None
  private var inputFormat = InputFormatEnum.STRING
  private var jsonSchema: Option[StructType] = None
  private var stringSchema: Option[StructType] = None
  private var binarySchema: Option[StructType] = None
  private var jsonConf: Map[String, String] = Map.empty[String, String]
  private var avroConf: Map[String, String] = Map.empty[String, String]
  private var avroSchema: Option[Schema] = None
  private var avroSparkSchema: Option[StructType] = None
  private var avroRecordInjection: Option[Injection[GenericRecord, Array[Byte]]] = None
  private var kafkaAvroDeserializer : Option[KafkaAvroDeserializer] = None

  //scalastyle:off
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {

    val configPrefix = if (isKey) "key" else "value"

    stringDeserializer = Option(new StringDeserializer)

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

        avroSchema = {
          if (avroInputSchema.nonEmpty) Option(SchemaHelper.getAvroSchemaFromString(avroInputSchema))
          else None
        }
        avroSparkSchema = avroSchema.map(schema => SchemaHelper.getSparkSchemaFromAvroSchema(schema))
        avroRecordInjection = avroSchema.map(schema => GenericAvroCodecs.toBinary[GenericRecord](schema))
        avroConf = {
          configs.filterKeys(key => key.contains(s"$configPrefix.deserializer.avro"))
            .map { case (key, value) => (key.replace(s"$configPrefix.deserializer.avro.", ""), value.toString) }
        }.toMap
      case InputFormatEnum.SCHEMAREGISTRY =>

        val schemaRegistryClient =
          SchemaRegistryClientFactory.getOrCreate(
            configs.getOrElse(s"$configPrefix.deserializer.schema.registry.url", "").toString,
            configs.getOrElse("tlsSchemaRegistryEnabled", "false").toString.toBoolean,
            configs
          )

        kafkaAvroDeserializer = Option(new KafkaAvroDeserializer(schemaRegistryClient))


      case InputFormatEnum.STRING =>
        val outputFieldName = configs.getOrElse(s"$configPrefix.deserializer.outputField", "raw").toString

        stringSchema = Option(StructType(Seq(StructField(outputFieldName, StringType))))
      case InputFormatEnum.BINARY =>
        val outputFieldName = configs.getOrElse(s"$configPrefix.deserializer.outputField", "raw").toString

        byteDeserializer = Option(new ByteArrayDeserializer)
        binarySchema = Option(StructType(Seq(StructField(outputFieldName, BinaryType))))
    }

    stringDeserializer.foreach(_.configure(configs, isKey))
    byteDeserializer.foreach(_.configure(configs, isKey))
    kafkaAvroDeserializer.foreach(_.configure(configs, isKey))
  }

  override def deserialize(topic: String, data: Array[Byte]): Row = {
    inputFormat match {
      case InputFormatEnum.JSON =>
        stringDeserializer match {
          case Some(strDeserializer) =>
            val stringData = strDeserializer.deserialize(topic, data)
            val jSchema = jsonSchema.getOrElse(extractSchemaFromJson(stringData, jsonConf))
            toRow(stringData, jsonConf, jSchema)
          case None =>
            throw new Exception("Impossible to parse Json data without string deserializer")
        }
      case InputFormatEnum.AVRO =>
        (avroSparkSchema, avroSchema, avroRecordInjection) match {
          case (Some(sparkSchema), Some(schema), Some(recordInjection)) =>
            val record = recordInjection.invert(data).get
            val converter = RowAvroHelper.getAvroConverter(schema, sparkSchema)

            new GenericRowWithSchema(converter(record).asInstanceOf[GenericRow].toSeq.toArray, sparkSchema)
          case _ =>
            throw new Exception("Impossible to parse Avro data without schema and converter")
        }
      case InputFormatEnum.SCHEMAREGISTRY =>
        kafkaAvroDeserializer match {
          case Some(schemaRegistryDeserializer) =>
            val record = schemaRegistryDeserializer.deserialize(topic, data).asInstanceOf[GenericRecord]
            val sparkSchema = SchemaHelper.getSparkSchemaFromAvroSchema(record.getSchema)
            val converter = RowAvroHelper.getAvroConverter(record.getSchema, sparkSchema)

            new GenericRowWithSchema(converter(record).asInstanceOf[GenericRow].toSeq.toArray, sparkSchema)
          case None =>
            throw new Exception("Impossible to parse Avro data without schema registry client")
        }
      case InputFormatEnum.BINARY =>
        byteDeserializer match {
          case Some(bDeserializer) =>
            new GenericRowWithSchema(Array(bDeserializer.deserialize(topic, data)), binarySchema.get)
          case None =>
            throw new Exception("Impossible to parse Binary data without binary deserializer")
        }
      case _ =>
        stringDeserializer match {
          case Some(strDeserializer) =>
            new GenericRowWithSchema(Array(strDeserializer.deserialize(topic, data)), stringSchema.get)
          case None =>
            throw new Exception("Impossible to parse String data without string deserializer")
        }
    }
  }

  override def close(): Unit = {
    stringDeserializer.foreach(_.close())
    byteDeserializer.foreach(_.close())
    kafkaAvroDeserializer.foreach(_.close())
  }

}