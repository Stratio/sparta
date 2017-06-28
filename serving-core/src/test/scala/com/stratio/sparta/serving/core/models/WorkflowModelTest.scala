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

package com.stratio.sparta.serving.core.models

import com.stratio.sparta.sdk.pipeline.aggregation.cube.DimensionType
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.models.workflow.cube.{CubeModel, DimensionModel, OperatorModel}
import com.stratio.sparta.serving.core.models.workflow.fragment.{FragmentElementModel, FragmentType}
import com.stratio.sparta.serving.core.models.workflow.rawData.RawDataModel
import com.stratio.sparta.serving.core.models.workflow.transformations.{OutputFieldsModel, TransformationModel, TransformationsModel}
import com.stratio.sparta.serving.core.models.workflow.writer.WriterModel
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class WorkflowModelTest extends WordSpec with Matchers with MockitoSugar {

  val rawData = None
  val fragmentModel = new FragmentElementModel(
    Some("id"),
    "fragmentType",
    "name",
    "description",
    "shortDescription",
    WorkflowElementModel("name", "type", Map()))

  val fragmentType = FragmentType

  val transformations = Option(TransformationsModel(Seq(TransformationModel("Morphlines",
    0,
    Some(Input.RawDataKey),
    Seq(OutputFieldsModel("out1"), OutputFieldsModel("out2")),
    Map("removeInputField" -> JsoneyString.apply("true")
    )))))

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

  val outputs = Seq(WorkflowElementModel("mongo", "MongoDb", Map()))
  val input = Some(WorkflowElementModel("kafka", "Kafka", Map()))
  val settingsModel = SettingsModel(
    GlobalSettings(),
    CheckpointSettings("test/test"),
    StreamingSettings(),
    SparkSettings("local[*]", false, None, None, None, SubmitArguments(),
      SparkConf(SparkResourcesConf(), SparkDockerConf(), SparkMesosConf()))
  )
  val policy = WorkflowModel(
    id = None,
    settings = settingsModel,
    name = "testpolicy",
    description = "whatever",
    rawData = rawData,
    transformations = transformations,
    streamTriggers = Seq(),
    cubes = cubes,
    input = input,
    outputs = outputs,
    fragments = Seq()
  )

  "AggregationPoliciesValidator" should {
    "not throw an exception if the policy is well formed" in {
      WorkflowValidator.validateDto(policy)
    }
  }

  val wrongPrecisionCubes = Seq(CubeModel("cube1", wrongDimensionModel, operators: Seq[OperatorModel], cubeWriter))
  val wrongComputeLastCubes = Seq(CubeModel("cube1", wrongComputeLastModel, operators: Seq[OperatorModel], cubeWriter))

  "AggregationPoliciesValidator" when {
    "there is a cube without name" should {
      "throw an exception because the policy is not well formed" in {
        val wrongCubeWithNoName = Seq(CubeModel("", wrongComputeLastModel, operators: Seq[OperatorModel], cubeWriter))
        val wrongCubeNamePolicy = policy.copy(cubes = wrongCubeWithNoName)
        val exceptionThrown = the[ServingCoreException] thrownBy {
          WorkflowValidator.validateDto(wrongCubeNamePolicy)
        }
        val awfullyWrongErrorModel = ErrorModel.toErrorModel(exceptionThrown.getMessage)
        awfullyWrongErrorModel.i18nCode shouldBe ErrorModel.ValidationError
        awfullyWrongErrorModel.subErrorModels shouldBe defined
        val seqErrors = awfullyWrongErrorModel.subErrorModels.get
        seqErrors.exists(_.i18nCode == ErrorModel.ValidationError_There_is_at_least_one_cube_without_name) shouldBe true
      }
    }
  }

  "AggregationPoliciesValidator" when {
    "there is neither a cube, nor a trigger nor raw-data nor transformation" should {
      "throw an exception because the policy is not well formed" in {
        val emptyPolicy = policy.copy(transformations = Option(TransformationsModel(Seq(), None)), cubes = Seq())
        val exceptionThrown = the[ServingCoreException] thrownBy {
          WorkflowValidator.validateDto(emptyPolicy)
        }
        val awfullyWrongErrorModel = ErrorModel.toErrorModel(exceptionThrown.getMessage)
        awfullyWrongErrorModel.i18nCode shouldBe ErrorModel.ValidationError
        awfullyWrongErrorModel.subErrorModels shouldBe defined
        val seqErrors = awfullyWrongErrorModel.subErrorModels.get
        seqErrors.exists(_.i18nCode ==
          ErrorModel.
            ValidationError_The_policy_needs_at_least_one_cube_or_one_trigger_or_raw_data_or_transformations_with_save
        ) shouldBe true
      }
    }
  }

  "AggregationPoliciesValidator" when {
    "a RawDataModel has no data and time field and no table name" should {
      "throw exceptions because is not correctly filled up " in {
        val exceptionThrown = the[ServingCoreException] thrownBy {
          val rawDataModel = RawDataModel("", "", writer = WriterModel(Seq("output1"),
            tableName = Option("FakeTable")), Map.empty)
          val policyWrongRawData = policy.copy(rawData = Option(rawDataModel),
            outputs = Seq(WorkflowElementModel("output1", "output2")))
          WorkflowValidator.validateDto(policyWrongRawData)
        }
        val awfullyWrongErrorModel = ErrorModel.toErrorModel(exceptionThrown.getMessage)
        awfullyWrongErrorModel.i18nCode shouldBe ErrorModel.ValidationError
        awfullyWrongErrorModel.subErrorModels shouldBe defined
        val seqErrors = awfullyWrongErrorModel.subErrorModels.get
        seqErrors.exists(_.i18nCode == ErrorModel.ValidationError_Raw_data_with_a_bad_output) shouldBe false
        seqErrors.exists(_.i18nCode == ErrorModel.ValidationError_Raw_data_with_bad_table_name) shouldBe false
        seqErrors.exists(_.i18nCode == ErrorModel.ValidationError_Raw_data_with_bad_data_field) shouldBe true
        seqErrors.exists(_.i18nCode == ErrorModel.ValidationError_Raw_data_with_bad_time_field) shouldBe true
      }
    }
  }
}

