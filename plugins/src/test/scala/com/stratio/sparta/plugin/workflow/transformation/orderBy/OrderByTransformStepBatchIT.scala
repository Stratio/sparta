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
package com.stratio.sparta.plugin.workflow.transformation.orderBy

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class OrderByTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A OrderByTransformStepBatch" should "order fields of events from RDD[Row]" in {

    val schema = StructType(Seq(StructField("color", StringType)))
    val schemaResult = StructType(Seq(StructField("color", StringType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue"), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red"), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red"), schema).asInstanceOf[Row]
    )
    val unorderedData = Seq(
      new GenericRowWithSchema(Array("red"), schemaResult).asInstanceOf[Row],
      new GenericRowWithSchema(Array("blue"), schemaResult).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red"), schemaResult).asInstanceOf[Row]

    )

    val inputRDD = sc.parallelize(unorderedData)
    val expectedRDD = data1
    val inputData = Map("step1" -> inputRDD)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val result = new OrderByTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
 Option(ssc),
      sparkSession,
      Map("orderExp" -> "color")
    ).transform(inputData)

    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (!result.ds.isEmpty())
      streamingRegisters.shouldEqual(expectedRDD)

    assert(streamingEvents === 3)

  }
}
