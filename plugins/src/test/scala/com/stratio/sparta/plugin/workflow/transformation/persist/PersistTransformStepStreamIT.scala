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
package com.stratio.sparta.plugin.workflow.transformation.persist

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable


@RunWith(classOf[JUnitRunner])
class PersistTransformStepStreamIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {
  "A PersistTransformStepBatchIT" should "persist RDD" in {
    val schema = StructType(Seq(StructField("color", StringType)))
    val schemaResult = StructType(Seq(StructField("color", StringType)))
    val unorderedData = Seq(
      new GenericRowWithSchema(Array("red"), schemaResult).asInstanceOf[Row],
      new GenericRowWithSchema(Array("blue"), schemaResult).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red"), schemaResult).asInstanceOf[Row]

    )

    val fields =
      """[
        |{
        |   "name":"color",
        |   "type":"string"
        |},
        |{
        |   "name":"price",
        |   "type":"double"
        |}]
        |""".stripMargin

    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val inputField = "csv"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn = Seq(
      new GenericRowWithSchema(Array("blue,12.1"), inputSchema),
      new GenericRowWithSchema(Array("red,12.2"), inputSchema)
    )
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)

    val result = new PersistTransformStepStream(
      "dummy",
      outputOptions,
      Option(ssc),
      sparkSession,
      Map("schema.fields" -> fields.asInstanceOf[JSerializable],
        "inputField" -> inputField,
        "schema.inputMode" -> "FIELDS",
        "storageLevel" -> "MEMORY_AND_DISK_SER",
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transform(inputData)

    result.ds.foreachRDD(rdd => {
      assert(rdd.getStorageLevel == StorageLevel.MEMORY_AND_DISK_SER)
    })

    ssc.start()
    ssc.awaitTerminationOrTimeout(3000L)
    ssc.stop()
  }
}
