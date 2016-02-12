/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.driver.test.models

import com.stratio.sparkta.serving.core.models._
import com.stratio.sparkta.sdk.{DimensionType, JsoneyString}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class AggregationPoliciesTest extends WordSpecLike
with MockitoSugar
with Matchers {

  "A CommonPoliciesValidator should" should {
    "validate dimensions are required and have at least 1 element" in {

      val sparkStreamingWindow = 2000
      val checkpointInterval = "10000"
      val checkpointAvailable = "600000"
      val storageLevel = Some("MEMORY_AND_DISK_SER_2")
      val checkpointGranularity = "minute"
      val checkpointDir = "checkpoint"
      val checkpointDto =
        new CommonCheckpointModel(checkpointGranularity, checkpointGranularity,
          Some(checkpointInterval), Some(checkpointAvailable))

      val configuration: Map[String, JsoneyString] =
        Map(("topics", new JsoneyString("zion2:1")), ("kafkaParams.group.id", new JsoneyString("kafka-pruebas")))
      val input = new PolicyElementModel("kafka-input", "KafkaInput", configuration)

      val cubeName = "cubeTest"
      val DimensionToCube = "dimension2"
      val cubeDto = new CommonCubeModel(cubeName, checkpointDto, Seq(new CommonDimensionModel(
        DimensionToCube, "field1", DimensionType.IdentityName, DimensionType.DefaultDimensionClass, None)),
        Seq())
      val outputs = Seq(PolicyElementModel("mongo", "MongoDb", Map()))
      val rawDataDto = new RawDataModel()

      val fragmentModel = new FragmentElementModel(
        Some("id"),
        "fragmentType",
        "name",
        "description",
        "shortDescription",
        PolicyElementModel("name", "type", Map()))

      val fragmentType = FragmentType
      val apd = new CommonPoliciesModel(
        None,
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
        outputs,
        Seq(fragmentModel))
      val test = CommonPoliciesValidator.validateDto(apd)

      test should be((true, ""))

      val sparkStreamingWindowBad = 20000
      val apdBad = new CommonPoliciesModel(
        None,
        None,
        storageLevel,
        "policy-name",
        "policy-description",
        sparkStreamingWindowBad,
        checkpointDir,
        rawDataDto,
        Seq(),
        Seq(cubeDto),
        Some(input),
        outputs,
        Seq(fragmentModel))

      val test2 = CommonPoliciesValidator.validateDto(apdBad)

      test2 should be (false, """{"i18nCode":"305","message":"The Checkpoint interval has to be at least 5 times greater than the Spark Streaming Window and also they have to be multiples\n"}""")
    }
  }
}
