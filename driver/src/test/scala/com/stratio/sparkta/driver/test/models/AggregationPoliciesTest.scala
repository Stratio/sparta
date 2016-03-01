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

  "A AggregationPoliciesValidator should" should {
    "validate dimensions are required and have at least 1 element" in {

      val storageLevel = Some("MEMORY_AND_DISK_SER_2")
      val checkpointDir = "checkpoint"

      val configuration: Map[String, JsoneyString] =
        Map(("topics", new JsoneyString("zion2:1")), ("kafkaParams.group.id", new JsoneyString("kafka-pruebas")))
      val input = new PolicyElementModel("kafka-input", "KafkaInput", configuration)

      val cubeName = "cubeTest"
      val DimensionToCube = "dimension2"
      val cubeDto = new CubeModel(
        cubeName,
        Seq(new DimensionModel(DimensionToCube, "field1", DimensionType.IdentityName, DimensionType.DefaultDimensionClass, configuration = None)),
        Seq())

      val rawDataDto = new RawDataModel()

      val apd = new AggregationPoliciesModel(
        None,
        None,
        storageLevel,
        "policy-name",
        "policy description",
        AggregationPoliciesModel.sparkStreamingWindow,
        checkpointDir,
        rawDataDto,
        Seq(),
        Seq(cubeDto),
        Some(input),
        Seq(mock[PolicyElementModel]),
        Seq(mock[FragmentElementModel]),
        userPluginsJars = Seq.empty[String])

      val test = AggregationPoliciesValidator.validateDto(apd)

      test._1 should equal(true)
    }
  }
}
