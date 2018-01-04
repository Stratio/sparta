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
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types._
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.reflect.runtime.universe._

import collection.mutable.Queue

@RunWith(classOf[JUnitRunner])
class JsonTransformStepIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  implicit def fields2schema(fields: Seq[(String, DataType)]): StructType =
    StructType(fields map { case (name, t) => StructField(name, t)} toArray)

  val fields = Seq(
    "x" -> IntegerType,
    "json1" -> StringType,
    "json2" -> StringType
  )

  val json1 =
    """
      |{
      |  "a" : 1,
      |  "b" : "hello"
      |}
    """.stripMargin

  val json2 =
    """
      |{
      |  "a" : "hello",
      |  "b" : 1
      |}
    """.stripMargin

  val sampleSchema: StructType = fields
  val sampleRow: Row = new GenericRowWithSchema(Array(1,json1, json2), sampleSchema)
  def sampleStream: DStream[Row] = ssc.queueStream(Queue(sc.parallelize(sampleRow::Nil)))
  def sampleDataFrame: RDD[Row] = sc.parallelize(sampleRow::Nil)

  def newStepWithOptions(properties: Map[String, JSerializable])(
    implicit ssc: StreamingContext, xDSession: XDSession
  ): JsonTransformStepStream = {
    val name = "JsonTransformStep"
    new JsonTransformStepStream(name, OutputOptions(tableName = name), Option(ssc), xDSession, properties)
  }

  def doTransformStream(ds: DStream[Row], properties: Map[String, JSerializable]): DStream[Row] =
    new JsonTransformStepStream(
      "dummy",
      OutputOptions(tableName = "jsonTransform"),
      Option(ssc),
      sparkSession,
      properties).transform(Map("step1" -> ds)).ds

  def doTransformBatch(df: RDD[Row], properties: Map[String, JSerializable]): RDD[Row] =
    new JsonTransformStepBatch(
      "dummy",
      OutputOptions(tableName = "jsonTransform"),
      None,
      sparkSession,
      properties).transform(Map("step1" -> df)).ds

  def assertExpectedSchema[Underlying[Row]](input: DistributedMonad[Underlying], properties: Map[String, JSerializable])(
    expected: => StructType
  )(implicit ttagEv: TypeTag[Underlying[Row]]): Unit = {
    val check: RDD[Row] => Unit = _.collect() foreach { r => assert(r.schema == expected) }
    if (typeOf[Underlying[Row]] == typeOf[DStream[Row]])
      doTransformStream(input.asInstanceOf[DStreamAsDistributedMonad].ds, properties) foreachRDD check
    else
      check(doTransformBatch(input.asInstanceOf[RDDDistributedMonad].ds, properties))
  }

  import DistributedMonad.Implicits._

  val maxWaitMillis = 500L

  val produceRow = "produce Rows from string fields containing JSON documents, "

  "A JsonTransformStep" should s"$produceRow inferring the schema from the contents" in {

    // Batch

    assertExpectedSchema(sampleDataFrame,  Map("inputField" -> "json1")) {
      Seq(
        "x" -> IntegerType,
        "a" -> LongType,
        "b" -> StringType,
        "json2" -> StringType
      )
    }

    assertExpectedSchema(sampleDataFrame,  Map("inputField" -> "json2")) {
      Seq(
        "x" -> IntegerType,
        "json1" -> StringType,
        "a" -> StringType,
        "b" -> LongType
      )
    }

    // Streaming

    assertExpectedSchema(sampleStream,  Map("inputField" -> "json1")) {
      Seq(
        "x" -> IntegerType,
        "a" -> LongType,
        "b" -> StringType,
        "json2" -> StringType
      )
    }

    assertExpectedSchema(sampleStream,  Map("inputField" -> "json2")) {
      Seq(
        "x" -> IntegerType,
        "json1" -> StringType,
        "a" -> StringType,
        "b" -> LongType
      )
    }

    ssc.start()
    ssc.awaitTerminationOrTimeout(maxWaitMillis)
    ssc.stop()
  }

  it should "remove other fields when `fieldsPreservationPolicy` is JUST_EXTRACTED" in {

    val properties = Map(
      "inputField" -> "json2",
      "fieldsPreservationPolicy" -> "JUST_EXTRACTED"
    )

    // Batch

    assertExpectedSchema(sampleDataFrame, properties) {
      Seq(
        "a" -> StringType,
        "b" -> LongType
      )
    }

    // Streaming

    assertExpectedSchema(sampleStream, properties) {
      Seq(
        "a" -> StringType,
        "b" -> LongType
      )
    }

    ssc.start()
    ssc.awaitTerminationOrTimeout(maxWaitMillis)
    ssc.stop()
  }

  it should "append extracted fields when `fieldsPreservationPolicy` is APPEND" in {

    val properties = Map(
      "inputField" -> "json2",
      "fieldsPreservationPolicy" -> "APPEND"
    )

    // Batch

    assertExpectedSchema(sampleDataFrame, properties) {
      Seq(
        "x" -> IntegerType,
        "json1" -> StringType,
        "json2" -> StringType,
        "a" -> StringType,
        "b" -> LongType
      )
    }

    // Streaming

    assertExpectedSchema(sampleStream, properties) {
      Seq(
        "x" -> IntegerType,
        "json1" -> StringType,
        "json2" -> StringType,
        "a" -> StringType,
        "b" -> LongType
      )
    }

    ssc.start()
    ssc.awaitTerminationOrTimeout(maxWaitMillis)
    ssc.stop()
  }

  it should "be able to use a string serialized schema" in {

    val properties = Map(
      "inputField" -> "json2",
      "schema.fromRow" -> "false",
      "schema.provided" -> """StructType((StructField(a,StringType,true)))"""
    )

    // Streaming

    assertExpectedSchema(sampleStream, properties) {
      Seq(
        "a" -> StringType
      )
    }

    ssc.start()
    ssc.awaitTerminationOrTimeout(maxWaitMillis)
    ssc.stop()
  }

  it should "be able to infer the schema from an example" in {

    val properties = Map(
      "inputField" -> "json2",
      "fieldsPreservationPolicy" -> "JUST_EXTRACTED",
      "schema.fromRow" -> "false",
      "schema.inputMode" -> "EXAMPLE",
      "schema.provided" -> """{"a": "hello dolly"}"""
    )

    // Batch

    assertExpectedSchema(sampleDataFrame, properties) {
      Seq(
        "a" -> StringType
      )
    }

    // Streaming

    assertExpectedSchema(sampleStream, properties) {
      Seq(
        "a" -> StringType
      )
    }

    ssc.start()
    ssc.awaitTerminationOrTimeout(maxWaitMillis)
    ssc.stop()
  }

}
