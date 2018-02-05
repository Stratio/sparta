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

package com.stratio.sparta.plugin.workflow.transformation.intersection

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IntersectionTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A IntersectionTransformStepBatch" should "intersect RDD" in {

    val data1 = Seq(Row.fromSeq(Seq("blue", 12.1)), Row.fromSeq(Seq("red", 12.2)))
    val data2 = Seq(Row.fromSeq(Seq("blue", 12.1)), Row.fromSeq(Seq("red", 12.2)))
    val rdd1 = sc.parallelize(data1)
    val rdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> rdd1, "step2" -> rdd2)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val result = new IntersectionTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
 Option(ssc),
      sparkSession,
      Map()
    ).transform(inputData)
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)

  }
}