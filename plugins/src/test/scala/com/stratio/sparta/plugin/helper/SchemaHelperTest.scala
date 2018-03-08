/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.helper

import com.stratio.sparta.plugin.enumerations.{FieldsPreservationPolicy, SchemaInputMode}
import org.apache.avro.SchemaBuilder
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class SchemaHelperTest extends WordSpec with Matchers {

  "getJsonSparkSchema" should {
    "return none" in {
      val result = SchemaHelper.getJsonSparkSchema(true, SchemaInputMode.EXAMPLE, None, Map())
      result should be(None)
    }

    "return correct schema from example" in {
      val json = """{"a": "hello dolly"}"""
      val result = SchemaHelper.getJsonSparkSchema(
        false,
        SchemaInputMode.EXAMPLE,
        Option(json),
        Map()
      )
      val expected = StructType(Seq(StructField("a", StringType)))
      result should be(Some(expected))
    }

    "return correct schema from sparkformat" in {
      val schema = """StructType((StructField(a,StringType,true)))"""

      val result = SchemaHelper.getJsonSparkSchema(
        false,
        SchemaInputMode.SPARKFORMAT,
        Option(schema),
        Map()
      )
      val expected = StructType(Seq(StructField("a", StringType)))
      result should be(Some(expected))
    }
  }

  "getAvroSparkSchema" should {
    "return none" in {
      val result = SchemaHelper.getAvroSparkSchema(true, None)
      result should be(None)
    }

    "return correct schema from example" in {
      val avro =
        s"""{"type":"record","name":"myrecord","fields":[
           | { "name":"a", "type":["string","null"] }
           | ]}""".stripMargin
      val result = SchemaHelper.getAvroSparkSchema(false, Option(avro))
      val expected = StructType(Seq(StructField("a", StringType)))
      result should be(Some(expected))
    }
  }

  "getAvroSchema" should {
    "return none" in {
      val result = SchemaHelper.getAvroSchema(true, None)
      result should be(None)
    }

    "return correct schema from example" in {
      val avro =
        s"""{"type":"record","name":"myrecord","fields":[
           | { "name":"a", "type":["string","null"] }
           | ]}""".stripMargin
      val result = SchemaHelper.getAvroSchema(false, Option(avro))
      val expected = SchemaBuilder.record("myrecord").fields
        .name("a").`type`().nullable().stringType().noDefault()
        .endRecord()
      result should be(Some(expected))
    }
  }

  "getNewOutputSchema" should {
    val inputSchema = StructType(Seq(
      StructField("inputField1", StringType, true),
      StructField("inputField2", StringType, true)))
    val outputSchema = StructType(Seq(
      StructField("outputField", StringType, true)))

    "append the new schema to the old one" in {

      val result = SchemaHelper.getNewOutputSchema(inputSchema, FieldsPreservationPolicy.APPEND,
          outputSchema, "input")
      val expected =
        StructType(Seq(
          StructField("inputField1", StringType, true),
          StructField("inputField2", StringType, true),
          StructField("outputField", StringType, true))
        )

      result should be(expected)
    }

    "replace the old schema with the old one" in {
      val result = SchemaHelper.getNewOutputSchema(inputSchema, FieldsPreservationPolicy.REPLACE,
        outputSchema, "inputField2")
      val expected =
        StructType(Seq(
          StructField("inputField1", StringType, true),
          StructField("outputField", StringType, true))
        )

      result should be(expected)
    }

    "keep only the extracted data" in {
      val result = SchemaHelper.getNewOutputSchema(inputSchema, FieldsPreservationPolicy.JUST_EXTRACTED,
        outputSchema, "inputField2")
      val expected =
        StructType(Seq(
          StructField("outputField", StringType, true)
        ))

      result should be(expected)
    }
  }

  "updateRow" should {
    val inputSchema = StructType(Seq(
      StructField("inputField", StringType, true)))
    val outputSchema = StructType(Seq(
      StructField("outputField", StringType, true)))

    val inputRow = new GenericRowWithSchema(Seq("valueInput").toArray, inputSchema)
    val outputRow = new GenericRowWithSchema(Seq("valueOutput").toArray, outputSchema)

    "append the new row to the old one" in {

      val result = SchemaHelper.updateRow(inputRow, outputRow , 0, FieldsPreservationPolicy.APPEND)
      val expectedSchema =
        StructType(Seq(
          StructField("inputField", StringType, true),
          StructField("outputField", StringType, true))
        )

      val expectedValue = new GenericRowWithSchema((inputRow.toSeq ++ outputRow.toSeq).toArray, expectedSchema)

      result should be(expectedValue)
    }


    "replace the old row with the old  one" in {
      val result = SchemaHelper.updateRow(inputRow, outputRow , 0, FieldsPreservationPolicy.REPLACE)
      val expectedSchema =
        StructType(Seq(
          StructField("outputField", StringType, true))
        )
      val expectedValue = new GenericRowWithSchema(outputRow.toSeq.toArray, expectedSchema)

      result should be(expectedValue)
    }

    "keep only the extracted data" in {
      val result = SchemaHelper.updateRow(inputRow, outputRow , 0, FieldsPreservationPolicy.JUST_EXTRACTED)
      val expectedSchema =
        StructType(Seq(
          StructField("outputField", StringType, true))
        )
      val expectedValue = new GenericRowWithSchema(outputRow.toSeq.toArray, expectedSchema)

      result should be(expectedValue)
    }
  }
}

