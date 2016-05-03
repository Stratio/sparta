/**
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

package com.stratio.sparta.serving.core.models

import com.stratio.sparta.sdk.{JsoneyString, DimensionType, Input}
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.sdk.{DimensionType, Input}
import com.stratio.sparta.serving.core.models._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class AggregationPolicyTest extends WordSpec with Matchers with MockitoSugar {

  val rawData = new RawDataModel
  val fragmentModel = new FragmentElementModel(
    Some("id"),
    "fragmentType",
    "name",
    "description",
    "shortDescription",
    PolicyElementModel("name", "type", Map()))

  val fragmentType = FragmentType

  val transformations = Seq(TransformationsModel("Morphlines",
    0,
    Input.RawDataKey,
    Seq(OutputFieldsModel("out1"), OutputFieldsModel("out2")),
    Map()))

  val dimensionModel = Seq(
    DimensionModel(
      "dimensionName",
      "field1",
      DimensionType.IdentityName,
      DimensionType.DefaultDimensionClass,
      configuration = Some(Map())),
    DimensionModel(
      "dimensionName2",
      "field2",
      "minute",
      "DateTime",
      Option("61s"),
      configuration = Some(Map())))

  val operators = Seq(OperatorModel("Count", "countoperator", Map()
  ))

  val wrongDimensionModel = Seq(
    DimensionModel(
      "dimensionName2",
      "field2",
      "9s",
      "DateTime",
      Option("minute"),
      configuration = Some(Map())))

  val wrongComputeLastModel = Seq(
    DimensionModel(
      "dimensionName2",
      "field2",
      "minute",
      "DateTime",
      Option("59s"),
      configuration = Some(Map())))

  val cubeWriter = WriterModel(outputs = Seq("mongo"))
  val cubes = Seq(CubeModel("cube1", dimensionModel, operators: Seq[OperatorModel], cubeWriter))
  val wrongPrecisionCubes = Seq(CubeModel("cube1", wrongDimensionModel, operators: Seq[OperatorModel], cubeWriter))
  val wrongComputeLastCubes = Seq(CubeModel("cube1", wrongComputeLastModel, operators: Seq[OperatorModel], cubeWriter))

  val outputs = Seq(PolicyElementModel("mongo", "MongoDb", Map()))
  val input = Some(PolicyElementModel("kafka", "Kafka", Map()))
  val policy = AggregationPoliciesModel(id = None,
    version = None,
    storageLevel = AggregationPoliciesModel.storageDefaultValue,
    name = "testpolicy",
    description = "whatever",
    sparkStreamingWindow = AggregationPoliciesModel.sparkStreamingWindow,
    checkpointPath = "test/test",
    rawData,
    transformations,
    streamTriggers = Seq(),
    cubes,
    input,
    outputs,
    Seq(),
    userPluginsJars = Seq.empty[String],
    remember = None)

  val wrongComputeLastPolicy = AggregationPoliciesModel(id = None,
    version = None,
    storageLevel = AggregationPoliciesModel.storageDefaultValue,
    name = "testpolicy",
    description = "whatever",
    sparkStreamingWindow = AggregationPoliciesModel.sparkStreamingWindow,
    checkpointPath = "test/test",
    rawData,
    transformations,
    streamTriggers = Seq(),
    wrongComputeLastCubes,
    input,
    outputs,
    Seq(),
    Seq(),
    remember = None)

  "AggregationPoliciesValidator" should {
    "non throw an exception if the policy is well formed" in {
      AggregationPoliciesValidator.validateDto(policy)
    }
  }
}

