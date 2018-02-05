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

package com.stratio.sparta.plugin.workflow.transformation.distinct

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DistinctTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A DistinctTransformStepBatch" should "distinct events from input RDD" in {

    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val dataDistinct = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val inputRDD = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRDD)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val result = new DistinctTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
 Option(ssc),
      sparkSession,
      Map()
    ).transform(inputData)
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (!result.ds.isEmpty())
      streamingRegisters.foreach(row => assert(dataDistinct.contains(row)))

    assert(streamingEvents === 2)

  }
}