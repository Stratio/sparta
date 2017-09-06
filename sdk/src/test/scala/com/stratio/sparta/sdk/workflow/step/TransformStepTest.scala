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

package com.stratio.sparta.sdk.workflow.step

import java.io.Serializable

import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class TransformStepTest extends WordSpec with Matchers with MockitoSugar {

  val sparkSession = mock[XDSession]
  val ssc = mock[StreamingContext]

  "TransformStep" should {
    val name = "transform"
    val schema = StructType(Seq(StructField("inputField", StringType)))
    val inputSchemas = Map("input" -> schema)
    val outputsFields = Seq(OutputFields("color", "string"), OutputFields("price", "double"))
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val properties = Map("addAllInputFields" -> true.asInstanceOf[Serializable])

    "Return addAllInputFields" in {
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = transformStep.addAllInputFields
      result should be(true)
    }

    "Return outputSchema" in {
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = transformStep.outputSchema
      val expected = StructType(Seq(
        StructField("inputField", StringType),
        StructField("color", StringType),
        StructField("price", DoubleType)
      ))
      result should be(expected)
    }

    "Return outputSchema map" in {
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = transformStep.outputSchemaMap
      val expected = Map(
        "inputField" -> StringType,
        "color" -> StringType,
        "price" -> DoubleType
      )

      result should be(expected)
    }


    "Return outputSchema with addAllInputFields false" in {
      val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable])
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = transformStep.outputSchema
      val expected = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
      result should be(expected)
    }

    "Return outputSchema with inputFields when no output fields are provided" in {
      val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable])
      val outputsFields = Seq.empty[OutputFields]
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = transformStep.outputSchema
      val expected = StructType(Seq(StructField("inputField", StringType)))
      result should be(expected)
    }

    "Compare schemas function return true when schemas are the same" in {
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val inputSchema = StructType(Seq(
        StructField("inputField", StringType),
        StructField("color", StringType),
        StructField("price", DoubleType)
      ))
      val result = transformStep.compareToOutputSchema(inputSchema)
      val expected = true
      result should be(expected)
    }

    "Compare schemas function return true when input schemas are included" in {
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val inputSchema = StructType(Seq(
        StructField("color", StringType),
        StructField("price", DoubleType)
      ))
      val result = transformStep.compareToOutputSchema(inputSchema)
      val expected = true
      result should be(expected)
    }

    "Compare schemas function return false when schemas are different" in {
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val inputSchema = StructType(Seq(
        StructField("color2", StringType),
        StructField("price", DoubleType)
      ))
      val result = transformStep.compareToOutputSchema(inputSchema)
      val expected = false
      result should be(expected)
    }

    "Parse return the same row" in {
      val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable])
      val outputsFields = Seq(OutputFields("inputField", "string"))
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = transformStep.parse(Row.fromSeq(Seq("foo")), "input")
      val expected = Seq(Row.fromSeq(Seq("foo")))
      result should be(expected)
    }

    "Parse return the row with outputfields" in {
      val schema = StructType(Seq(
        StructField("inputField", StringType),
        StructField("color", StringType),
        StructField("price", DoubleType)
      ))
      val inputSchemas = Map("input" -> schema)
      val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable])
      val outputsFields = Seq(OutputFields("inputField", "string"), OutputFields("color", "string"))
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = transformStep.parse(Row.fromSeq(Seq("foo", "blue", 12.1)), "input")
      val expected = Seq(Row.fromSeq(Seq("foo", "blue")))
      result should be(expected)
    }

    "Parse return the row with outputfields casted to type" in {
      val schema = StructType(Seq(
        StructField("inputField", StringType),
        StructField("color", StringType),
        StructField("price", DoubleType)
      ))
      val inputSchemas = Map("input" -> schema)
      val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable])
      val outputsFields = Seq(OutputFields("inputField", "string"), OutputFields("price", "string"))
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = transformStep.parse(Row.fromSeq(Seq("foo", "blue", 12.1)), "input")
      val expected = Seq(Row.fromSeq(Seq("foo", "12.1")))
      result should be(expected)
    }

    "Parse return null values row with incorrect outputfields" in {
      val schema = StructType(Seq(
        StructField("inputField", StringType),
        StructField("color", StringType),
        StructField("price", DoubleType)
      ))
      val inputSchemas = Map("input" -> schema)
      val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable], "whenError" -> "Null")
      val outputsFields = Seq(OutputFields("inputField", "string"), OutputFields("price2", "string"))
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = transformStep.parse(Row.fromSeq(Seq("foo", "blue", 12.1)), "input")
      val expected = Seq(Row.fromSeq(Seq("foo", null)))
      result should be(expected)
    }

    "Parse discard event with incorrect outputfields" in {
      val schema = StructType(Seq(
        StructField("inputField", StringType),
        StructField("color", StringType),
        StructField("price", DoubleType)
      ))
      val inputSchemas = Map("input" -> schema)
      val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable], "whenError" -> "Discard")
      val outputsFields = Seq(OutputFields("inputField", "string"), OutputFields("price2", "string"))
      val transformStep = new MockTransformStep(
        name,
        inputSchemas,
        outputsFields,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = transformStep.parse(Row.fromSeq(Seq("foo", "blue", 12.1)), "input")
      val expected = Seq.empty[Row]
      result should be(expected)
    }
  }

  "Transform classSuffix must be corrected" in {
    val expected = "TransformStep"
    val result = TransformStep.ClassSuffix
    result should be(expected)
  }
}
