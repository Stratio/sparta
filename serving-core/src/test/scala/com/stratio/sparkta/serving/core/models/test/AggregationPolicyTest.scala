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

package com.stratio.sparkta.serving.core.models.test

import com.stratio.sparkta.serving.core.helpers.ParseAggregationToCommonModel
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import com.stratio.sparkta.sdk.{DimensionType, Input}
import com.stratio.sparkta.serving.core.models._

@RunWith(classOf[JUnitRunner])
class AggregationPolicyTest extends WordSpec with Matchers {

  val rawData = new RawDataModel
  val fragmentModel = new FragmentElementModel(
    Some("id"),
    "fragmentType",
    "name",
    "description",
    "shortDescription",
    PolicyElementModel("name", "type", Map()))

  val fragmentType = FragmentType

  val transformations = Seq(TransformationsModel(
    name = "transformation1",
    "Morphlines",
    0,
    Input.RawDataKey,
    Seq("out1", "out2"),
    Map()))

  val computeLast = "600000"
  val computeLastBad = "6000"
  val checkpointModel = CommonCheckpointModel("minute", "minute", Some("30000"), Some(computeLast))
  val checkpointModelBad = CommonCheckpointModel("minute", "minute", Some("30000"), Some(computeLastBad))

  val dimensionsModel = Seq(CommonDimensionModel(
    "minute",
    "field1",
    DimensionType.IdentityName,
    DimensionType.DefaultDimensionClass,
    Some(Map())
  ))

  val dimensionModel = CommonDimensionModel(
    "dimensionName",
    "field1",
    DimensionType.IdentityName,
    DimensionType.DefaultDimensionClass,
    Some(Map()))

  val operators = Seq(OperatorModel("Count", "countoperator", Map()))

  val cubeModel = CommonCubeModel("cube1",
    checkpointModel,
    dimensionsModel,
    operators: Seq[OperatorModel])

  val cubes = Seq(CommonCubeModel("cube1",
    checkpointModel,
    dimensionsModel,
    operators: Seq[OperatorModel]))
  val cubesBad = Seq(CommonCubeModel("cube1",
    checkpointModelBad,
    dimensionsModel,
    operators: Seq[OperatorModel]))

  val outputs = Seq(PolicyElementModel("mongo", "MongoDb", Map()))
  val input = Some(PolicyElementModel("kafka", "Kafka", Map()))
  val policy = CommonPoliciesModel(id = None,
    version = None,
    storageLevel = CommonPoliciesModel.storageDefaultValue,
    name = "testpolicy",
    description = "whatever",
    sparkStreamingWindow = CommonPoliciesModel.sparkStreamingWindow,
    checkpointPath = "test/test",
    rawData,
    transformations,
    cubes,
    input,
    outputs,
    Seq())

  val policyBad = CommonPoliciesModel(id = None,
    version = None,
    storageLevel = CommonPoliciesModel.storageDefaultValue,
    name = "testpolicy",
    description = "whatever",
    sparkStreamingWindow = CommonPoliciesModel.sparkStreamingWindow,
    checkpointPath = "test/test",
    rawData,
    transformations,
    cubesBad,
    input,
    outputs,
    Seq())

  "AggregationPolicySpec" should {

    "CommonPoliciesValidator should return a tuple (True, ) if the policy is well formed" in {
      val res = CommonPoliciesValidator.validateDto(policy)
      res should be((true, ""))
    }
    "CommonPoliciesValidator should return an error message because the computeLast value is smaller than" +
      " the precision(granularity)" in {

      val res = CommonPoliciesValidator.validateDto(policyBad)
      res should be(false,
        """{"i18nCode":"305","message":"ComputeLast value has to be greater than the precision in order to prevent data loss\n"}""")

    }
  }
}

