/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.kafka.serializers

import java.util

import akka.event.slf4j.SLF4JLogging
import com.databricks.spark.avro.RowAvroHelper
import com.stratio.sparta.core.enumerators.OutputFormatEnum
import com.stratio.sparta.plugin.common.kafka.SchemaRegistryClientFactory
import com.stratio.sparta.plugin.helper.SchemaHelper
import com.twitter.bijection.Injection
import com.twitter.bijection.avro.GenericAvroCodecs
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, Serializer, StringSerializer}
import org.apache.spark.sql.Row
import org.apache.spark.sql.json.RowJsonHelper

import scala.collection.JavaConversions._


class RowSerializer extends Serializer[Row] with SLF4JLogging{

  private val stringSerializer = new StringSerializer
  private var byteArraySerializer: Option[ByteArraySerializer] = None

  private var outputFormat = OutputFormatEnum.ROW
  private var delimiter = ","
  private var recordNamespace = ","
  private var recordName = "topLevelRecord"
  private var avroSchema: Option[Schema] = None
  private val recordInjection: Option[Injection[GenericRecord, Array[Byte]]] = None
  private var avroConverter: Option[Any => Any] = None
  private var jsonConf: Map[String, String] = Map.empty[String, String]
  private var schemaRegistryClient: Option[CachedSchemaRegistryClient] = None
  private var kafkaAvroSerializer: Option[KafkaAvroSerializer] = None

  //scalastyle:off
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {

    val configPrefix = if (isKey) "key" else "value"
    val serializerConfigs = configs.filterKeys(key => key.startsWith(s"$configPrefix.serializer")).map { case (key, value) => key.replaceAll(s"$configPrefix.serializer.", "") -> value }.toMap ++ configs

    val format = serializerConfigs.getOrElse(s"$configPrefix.serializer.outputFormat", "ROW").toString
    outputFormat = OutputFormatEnum.withName(format)

    outputFormat match {
      case OutputFormatEnum.ROW =>
        delimiter = serializerConfigs.getOrElse(s"$configPrefix.serializer.row.delimiter", ",").toString
      case OutputFormatEnum.JSON =>
        jsonConf = {
          serializerConfigs.filterKeys(key => key.contains(s"$configPrefix.serializer.json"))
            .map { case (key, value) => (key.replace(s"$configPrefix.serializer.json.", ""), value.toString) }
        }
      case OutputFormatEnum.SCHEMAREGISTRY =>

        log.debug(s"Creating Schema Registry client with options ${serializerConfigs}")

        val schemaRegistryClient = Option(
          SchemaRegistryClientFactory.getOrCreate(
            serializerConfigs.getOrElse(s"$configPrefix.serializer.schema.registry.url", "").toString,
            serializerConfigs.getOrElse("tlsEnabled", "false").toString.toBoolean,
            configs
          )
        )

        kafkaAvroSerializer = Option(new KafkaAvroSerializer(schemaRegistryClient.getOrElse(throw new RuntimeException("Fail when trying to create Schema Registry client"))))

      case OutputFormatEnum.AVRO =>
        val useRowSchema =
          serializerConfigs.getOrElse(s"$configPrefix.serializer.avro.schema.fromRow", "true").toString.toBoolean
        recordNamespace =
          serializerConfigs.getOrElse(s"$configPrefix.serializer.avro.schema.recordNamespace", "").toString
        recordName =
          serializerConfigs.getOrElse(s"$configPrefix.serializer.avro.schema.recordName", "topLevelRecord").toString

        val schemaProvided = serializerConfigs.getOrElse(s"$configPrefix.serializer.avro.schema.provided", "") match {
          case "" => None
          case v: String => Some(v)
        }

        avroSchema = SchemaHelper.getAvroSchema(useRowSchema, schemaProvided)
        avroConverter = SchemaHelper.getAvroSparkSchema(useRowSchema, schemaProvided)
          .map(schema => RowAvroHelper.createConverterToAvro(schema, recordName, recordNamespace))
        byteArraySerializer = Option(new ByteArraySerializer)
      case OutputFormatEnum.BINARY =>
        byteArraySerializer = Option(new ByteArraySerializer)

    }

    stringSerializer.configure(serializerConfigs, isKey)
    byteArraySerializer.foreach(_.configure(serializerConfigs, isKey))
    kafkaAvroSerializer.foreach(_.configure(serializerConfigs, isKey))
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

        byteArraySerializer match {
          case Some(bArraySerializer) =>
            bArraySerializer.serialize(topic, injection.apply(record))
          case None =>
            throw new RuntimeException("Fail when trying to serialize with Avro serializer")
        }
      case OutputFormatEnum.SCHEMAREGISTRY =>

        val record = avroConverter match {
          case Some(converter) =>
            converter(data).asInstanceOf[GenericRecord]
          case None =>
            val converter = RowAvroHelper.createConverterToAvro(data.schema, recordName, recordNamespace)
            converter(data).asInstanceOf[GenericRecord]
        }

        kafkaAvroSerializer match {
          case Some(schemaRegistrySerializer) =>
            schemaRegistrySerializer.serialize(topic, record)
          case None =>
            throw new RuntimeException("Fail when trying to serialize Schema Registry client")
        }
      case OutputFormatEnum.ROW =>
        stringSerializer.serialize(topic, data.mkString(delimiter))
      case OutputFormatEnum.BINARY =>
        byteArraySerializer match {
          case Some(bArraySerializer) =>
            bArraySerializer.serialize(topic, data.toString().getBytes)
          case None =>
            throw new RuntimeException("Fail when trying to serialize with Avro serializer")
        }
      case OutputFormatEnum.JSON =>
        stringSerializer.serialize(topic, RowJsonHelper.toJSON(data, jsonConf))
      case _ =>
        stringSerializer.serialize(topic, data.toString())
    }
  }

  override def close(): Unit = {
    stringSerializer.close()
    byteArraySerializer.foreach(_.close())
    kafkaAvroSerializer.foreach(_.close())
  }

}