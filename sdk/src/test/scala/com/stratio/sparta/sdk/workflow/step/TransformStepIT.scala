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

import com.stratio.sparta.sdk.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.streaming.Time
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class TransformStepIT extends TemporalSparkContext with Matchers {

  val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
  def passSameStream(stepName: String, generateDStream: DStream[Row]): DStream[Row] = generateDStream

  "TransformStep" should "applyHeadTransform return exception with no input data" in {
    val name = "transform"
    val schema = StructType(Seq(
      StructField("inputField", StringType),
      StructField("color", StringType),
      StructField("price", DoubleType)
    ))
    val inputSchemas = Map("input" -> schema)
    val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable], "whenError" -> "Error")
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

    an[AssertionError] should be thrownBy transformStep.applyHeadTransform(Map.empty[String, DStream[Row]])(passSameStream)

  }

  "TransformStep" should "applyHeadTransform return DStream with empty rdd" in {
    val name = "transform"
    val schema = StructType(Seq(
      StructField("inputField", StringType),
      StructField("color", StringType),
      StructField("price", DoubleType)
    ))
    val inputSchemas = Map("input" -> schema)
    val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable], "whenError" -> "Error")
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
    val rddQueue = new mutable.Queue[RDD[Row]]
    val rddData = sc.emptyRDD[Row]
    rddQueue += rddData
    val inputDStream = ssc.queueStream(rddQueue)
    val inputData = Map("input" -> inputDStream)

    val result = transformStep.applyHeadTransform(inputData)(passSameStream)
    val expected = sc.emptyRDD[Row].collect()

    result.foreachRDD(rdd =>
      rdd.collect() should be(expected)
    )
  }

  "TransformStep" should "applyHeadTransform return DStream with output fields data" in {
    val name = "transform"
    val schema = StructType(Seq(
      StructField("inputField", StringType),
      StructField("color", StringType),
      StructField("price", DoubleType)
    ))
    val inputSchemas = Map("input" -> schema)
    val properties = Map("addAllInputFields" -> false.asInstanceOf[Serializable], "whenError" -> "Error")
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

    val rddQueue = new mutable.Queue[RDD[Row]]
    val rddData = sc.parallelize(Seq(Row.fromSeq(Seq("inputfield", "blue", 12.1))))
    rddQueue += rddData
    val inputDStream = ssc.queueStream(rddQueue)
    val inputData = Map("input" -> inputDStream)

    val result = transformStep.applyHeadTransform(inputData)(passSameStream)
    val expected = sc.parallelize(Seq(Row.fromSeq(Seq("inputfield", "12.1")))).collect()

    result.foreachRDD(rdd =>
      rdd.collect() should be(expected)
    )
  }
}
