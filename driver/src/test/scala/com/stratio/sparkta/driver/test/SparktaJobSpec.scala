/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.driver.test

import com.stratio.sparkta.driver.SparktaJob
import com.stratio.sparkta.sdk.{DimensionType, JsoneyString}
import com.stratio.sparkta.serving.core.models._
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

@RunWith(classOf[JUnitRunner])
class SparktaJobSpec extends FlatSpec with ShouldMatchers
with MockitoSugar {


  "SparktaJobSpec" should "run a Sparkta job" in  {
    val sparkStreamingWindow = 2000
    val checkpointInterval = 10000
    val checkpointAvailable = 60000
    val storageLevel = Some("MEMORY_AND_DISK_SER_2")
    val checkpointGranularity = "minute"
    val checkpointDir = "checkpoint"
    val checkpointDto =
      new CheckpointModel(checkpointGranularity, checkpointGranularity, checkpointInterval, checkpointAvailable)

    val configuration: Map[String, JsoneyString] =
      Map(("topics", new JsoneyString("zion2:1")), ("kafkaParams.group.id", new JsoneyString("kafka-pruebas")))
    val input = new PolicyElementModel("kafka-input", "KafkaInput", configuration)

    val cubeName = "cubeTest"
    val DimensionToCube = "dimension2"
    val cubeDto = new CubeModel(cubeName, checkpointDto, Seq(new DimensionModel(
      DimensionToCube, "field1", DimensionType.IdentityName, DimensionType.DefaultDimensionClass, None)),
      Seq())

    val rawDataDto = new RawDataModel()
    val aggregationPolicy = AggregationPoliciesModel(
      None,
      storageLevel,
      "policy-name",
      "policy description",
      sparkStreamingWindow,
      checkpointDir,
      rawDataDto,
      Seq(),
      Seq(cubeDto),
      Some(input),
      Seq(mock[PolicyElementModel]),
      Seq(mock[FragmentElementModel]))

    val result = SparktaJob.runSparktaJob(aggregationPolicy)

    result should be("kaka")
  }

}
