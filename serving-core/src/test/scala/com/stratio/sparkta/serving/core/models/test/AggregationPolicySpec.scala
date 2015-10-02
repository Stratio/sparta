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

package com.stratio.sparkta.serving.core.models.test

import com.stratio.sparkta.sdk.DimensionType
import org.scalatest.{Matchers, WordSpec}
import com.stratio.sparkta.serving.core.models._
import com.stratio.sparkta.sdk.{Input}

class AggregationPolicySpec extends WordSpec with Matchers{

  val rawData = new RawDataModel
  val fragmentModel = new FragmentElementModel(
    Some("id"),
    "fragmentType",
    "name",
    "description",
    "shortDescription",
    PolicyElementModel("name", "type",Map()))

  val fragmentType = FragmentType

  val transformations = Seq(TransformationsModel(
    name = "transformation1",
    "Morphlines",
    0,
    Input.RawDataKey,
    Seq("out1", "out2"),
    Map()))

  val checkpointModel = CheckpointModel("minute", "minute", 30000, 60000)

  val dimensionModel = Seq(DimensionModel(
    "dimensionName",
    "field1",
    DimensionType.IdentityName,
    DimensionType.DefaultDimensionClass,
    Some(Map())
  ))
  val operators = Seq(OperatorModel("Count", "countoperator", Map()
  ))

  val cubes = Seq(CubeModel("cube1",
    checkpointModel,
    dimensionModel,
    operators: Seq[OperatorModel],
    CubeModel.Multiplexer))

  val outputs = Seq(PolicyElementModel("mongo", "MongoDb", Map()))
  val input = Some(PolicyElementModel("kafka", "Kafka",Map()))
  val policy = AggregationPoliciesModel(id = None,
    storageLevel = AggregationPoliciesModel.storageDefaultValue,
    name = "testpolicy",
    description = "whatever",
    sparkStreamingWindow = AggregationPoliciesModel.sparkStreamingWindow,
    checkpointPath = "test/test",
    rawData,
    transformations,
    cubes,
    input,
    outputs,
    Seq())

  "AggregationPolicySpec" should {

    "AggregationPoliciesValidator should return a touple (True, ) if the policy is well formed" in {
      val res = AggregationPoliciesValidator.validateDto(policy)
      res should be ((true,""))
    }
  }
}

