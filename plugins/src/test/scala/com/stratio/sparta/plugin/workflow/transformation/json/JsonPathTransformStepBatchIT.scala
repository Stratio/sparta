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

package com.stratio.sparta.plugin.workflow.transformation.json

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class JsonPathTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A JsonTransformStepBatchIT" should "transform json events the input Dataset" in {
    val JSON =
      """{ "store": {
        |    "book": [
        |      { "category": "reference",
        |        "author": "Nigel Rees",
        |        "title": "Sayings of the Century",
        |        "price": 8.95
        |      }
        |    ],
        |    "bicycle": {
        |      "color": "red",
        |      "price": 19.95
        |    }
        |  }
        |}""".stripMargin
    val queries =
      """[
        |{
        |   "field":"color",
        |   "query":"$.store.bicycle.color",
        |   "type":"string"
        |},
        |{
        |   "field":"price",
        |   "query":"$.store.bicycle.price",
        |   "type":"double"
        |}]
        | """.stripMargin

    val inputField = "json"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataIn = Seq(new GenericRowWithSchema(Array(JSON), inputSchema))
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(new GenericRowWithSchema(Array("red", 19.95), outputSchema))
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)

    val result = new JsonPathTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
 Option(ssc),
      sparkSession,
      Map("queries" -> queries.asInstanceOf[JSerializable],
        "inputField" -> inputField,
        "fieldsPreservationPolicy" -> "REPLACE")
    ).transform(inputData)

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
          assert(dataOut.contains(row))
          assert(outputSchema == row.schema)
        }

    assert(arrayValues.length === 1)
  }
}