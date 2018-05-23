/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.avro

import java.nio.charset.StandardCharsets

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import com.twitter.bijection.avro.GenericAvroCodecs
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AvroTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits{

  "A AvroTransformStepBatchIT" should "transform avro events the input RDD" in {

    val record = s"""{"type":"record","name":"myrecord","fields":[
      | { "name":"color", "type":["string","null"] },
      | { "name":"price", "type":["double","null"] }
      | ]}""".stripMargin
    val parser = new Schema.Parser()
    val schema = parser.parse(record)
    val recordInjection = GenericAvroCodecs.toBinary[GenericRecord](schema)
    val avroRecordBlue = new GenericData.Record(schema)
    avroRecordBlue.put("color", "blue")
    avroRecordBlue.put("price", 12.1)
    val bytesBlue = recordInjection.apply(avroRecordBlue)
    val avroRecordRed = new GenericData.Record(schema)
    avroRecordRed.put("color", "red")
    avroRecordRed.put("price", 12.2)
    val bytesRed = recordInjection.apply(avroRecordRed)
    val inputField = "avro"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataIn = Seq(
      new GenericRowWithSchema(Array(new String(bytesBlue, StandardCharsets.UTF_8)), inputSchema),
      new GenericRowWithSchema(Array(new String(bytesRed, StandardCharsets.UTF_8)), inputSchema)
    )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), outputSchema),
      new GenericRowWithSchema(Array("red", 12.2), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val transformationsStepManagement = TransformationStepManagement()
    val result = new AvroTransformStepBatch(
      "dummy",
      outputOptions,
      transformationsStepManagement,
      Some(ssc),
      sparkSession,
      Map(
        "inputField" -> inputField,
        "schema.provided" -> record,
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED"
      )
    ).transformWithDiscards(inputData)._1
    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }

  "A AvroTransformStepBatchIT" should "transform discard avro events the input RDD" in {

    val record = s"""{"type":"record","name":"myrecord","fields":[
                    | { "name":"color", "type":["string","null"] },
                    | { "name":"price", "type":["double","null"] }
                    | ]}""".stripMargin
    val parser = new Schema.Parser()
    val schema = parser.parse(record)
    val recordInjection = GenericAvroCodecs.toBinary[GenericRecord](schema)
    val avroRecordBlue = new GenericData.Record(schema)
    avroRecordBlue.put("color", "blue")
    avroRecordBlue.put("price", 12.1)
    val bytesBlue = recordInjection.apply(avroRecordBlue)
    val avroRecordRed = new GenericData.Record(schema)
    avroRecordRed.put("color", "red")
    avroRecordRed.put("price", 12.2)
    val bytesRed = recordInjection.apply(avroRecordRed)
    val inputField = "avro"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataIn = Seq(
      new GenericRowWithSchema(Array(new String(bytesBlue, StandardCharsets.UTF_8)), inputSchema),
      new GenericRowWithSchema(Array(new String(bytesRed, StandardCharsets.UTF_8)), inputSchema),
      new GenericRowWithSchema(Array("wrong data"), inputSchema)
    )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), outputSchema),
      new GenericRowWithSchema(Array("red", 12.2), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val transformationsStepManagement = TransformationStepManagement()
    val result = new AvroTransformStepBatch(
      "dummy",
      outputOptions,
      transformationsStepManagement,
      Some(ssc),
      sparkSession,
      Map(
        "inputField" -> inputField,
        "schema.provided" -> record,
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED",
        "whenRowError" -> "RowDiscard"
      )
    ).transformWithDiscards(inputData)

    val validData = result._1.ds.collect()
    val discardedData = result._3.get.ds.collect()

    validData.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    discardedData.foreach { row =>
      assert(dataIn.contains(row))
      assert(inputSchema == row.schema)
    }

    assert(validData.length === 2)
    assert(discardedData.length === 1)
  }
}