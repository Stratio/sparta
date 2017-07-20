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
package com.stratio.sparta.driver.test.stage

import com.stratio.sparta.driver.stage.{LogError, OutputStage}
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.models.workflow.{WorkflowElementModel, WorkflowModel}
import com.stratio.sparta.serving.core.utils.ReflectionUtils
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.junit.runner.RunWith
import org.mockito.Matchers.{any, eq => mockEq}
import org.mockito.Mockito.{when, _}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, ShouldMatchers}

@RunWith(classOf[JUnitRunner])
class OutputStageTest extends FlatSpec with ShouldMatchers with MockitoSugar {

  case class TestStage(workflow: WorkflowModel) extends OutputStage with LogError

  def mockPolicy: WorkflowModel = {
    val policy = mock[WorkflowModel]
    when(policy.id).thenReturn(Some("id"))
    policy
  }

  "OutputStage" should "Generate an empty list with no policies" in {
    val policy = mockPolicy
    val reflection = mock[ReflectionUtils]
    val xDSession = mock[XDSession]
    when(policy.outputs).thenReturn(Seq.empty)

    val result = TestStage(policy).outputStage(reflection, xDSession)

    result should be(List.empty)
  }

  "OutputStage" should "Generate an output " in {
    val policy = mockPolicy
    val reflection = mock[ReflectionUtils]
    val outputs = Seq(WorkflowElementModel("output", "Output", Map.empty))
    val outputClass = mock[Output]
    val xDSession = mock[XDSession]
    when(policy.outputs).thenReturn(outputs)
    when(reflection.tryToInstantiate(mockEq("OutputOutput"), any())).thenReturn(outputClass)

    val result = TestStage(policy).outputStage(reflection, xDSession)
    verify(reflection).tryToInstantiate(mockEq("OutputOutput"), any())
    result should be(List(outputClass))
  }

  "OutputStage" should "Fail gracefully with bad input" in {
    val policy = mockPolicy
    val reflection = mock[ReflectionUtils]
    val xDSession = mock[XDSession]
    val outputs = Seq(WorkflowElementModel("output", "Output", Map.empty))
    when(policy.outputs).thenReturn(outputs)
    when(reflection.tryToInstantiate(any(), any())).thenThrow(new RuntimeException("Fake"))

    the[IllegalArgumentException] thrownBy {
      TestStage(policy).outputStage(reflection, xDSession)
    } should have message "Something went wrong while creating the output: output. Please re-check the policy"
  }


  "OutputStage" should "Fail when reflectionUtils don't behave correctly" in {
    val policy = mockPolicy
    val reflection = mock[ReflectionUtils]
    val outputs = Seq(WorkflowElementModel("output", "Output", Map.empty))
    val myInputClass = mock[Input]
    val xDSession = mock[XDSession]
    when(policy.outputs).thenReturn(outputs)
    when(reflection.tryToInstantiate(any(), any())).thenReturn(myInputClass)

    the[IllegalArgumentException] thrownBy {
      TestStage(policy).outputStage(reflection, xDSession)
    } should have message "Something went wrong while creating the output: output. Please re-check the policy"
  }

  "OutputStage" should "Generate a list of output for multiple Outputs " in {
    val policy = mockPolicy
    val reflection = mock[ReflectionUtils]
    val outputs = Seq(
      WorkflowElementModel("output", "Output", Map.empty),
      WorkflowElementModel("output", "OtherOutput", Map.empty)
    )
    val outputClass = mock[Output]
    val xDSession = mock[XDSession]
    when(policy.outputs).thenReturn(outputs)
    when(reflection.tryToInstantiate(mockEq("OutputOutput"), any())).thenReturn(outputClass)
    when(reflection.tryToInstantiate(mockEq("OtherOutputOutput"), any())).thenReturn(outputClass)

    val result = TestStage(policy).outputStage(reflection, xDSession)
    verify(reflection).tryToInstantiate(mockEq("OutputOutput"), any())
    verify(reflection).tryToInstantiate(mockEq("OtherOutputOutput"), any())
    result should be(List(outputClass, outputClass))
  }

  "OutputStage" should "Filter outputs " in {
    val policy = mockPolicy
    val reflection = mock[ReflectionUtils]
    val firstOutput = WorkflowElementModel("output", "Output", Map.empty)
    val secondOutput = WorkflowElementModel("outputOne", "OtherOutput", Map.empty)
    val outputs = Seq(firstOutput, secondOutput)
    val outputClass = mock[Output]
    val xDSession = mock[XDSession]
    when(policy.outputs).thenReturn(outputs)
    when(reflection.tryToInstantiate(mockEq("OutputOutput"), any())).thenReturn(outputClass)
    when(reflection.tryToInstantiate(mockEq("OtherOutputOutput"), any())).thenReturn(outputClass)

    val spyResult = spy(TestStage(policy))
    val result = spyResult.outputStage(reflection, xDSession)

    verify(reflection).tryToInstantiate(mockEq("OutputOutput"), any())
    verify(reflection).tryToInstantiate(mockEq("OtherOutputOutput"), any())
    verify(spyResult).createOutput(firstOutput, reflection, xDSession)
    verify(spyResult).createOutput(secondOutput, reflection, xDSession)
    result should be(List(outputClass, outputClass))
  }


}
