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

import com.stratio.sparta.serving.core.models.workflow._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class WorkflowTest extends WordSpec with Matchers with MockitoSugar {

  val settingsModel = Settings(
    GlobalSettings(),
    CheckpointSettings("test/test"),
    StreamingSettings(),
    SparkSettings("local[*]", sparkKerberos = false, sparkDataStoreTls = false, None, None, None, SubmitArguments(),
      SparkConf(SparkResourcesConf(), SparkDockerConf(), SparkMesosConf()))
  )
  val workflow = Workflow(
    id = None,
    settings = settingsModel,
    name = "testworkflow",
    description = "whatever",
    pipelineGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])
  )

  //TODO validator workflow

  /*"AggregationPoliciesValidator" when {
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
  }*/
}

