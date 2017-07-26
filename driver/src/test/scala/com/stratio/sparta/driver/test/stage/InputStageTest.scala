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

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.stratio.sparta.driver.stage.{InputStage, LogError, ZooKeeperError}
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.models.workflow.{WorkflowElementModel, WorkflowModel}
import com.stratio.sparta.serving.core.utils.ReflectionUtils
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.mockito.Matchers.{any, eq => mockEq}
import org.mockito.Mockito.{when, _}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpecLike, ShouldMatchers}

@RunWith(classOf[JUnitRunner])
class InputStageTest extends TestKit(ActorSystem("InputStageTest"))
  with FlatSpecLike with ShouldMatchers with MockitoSugar {

  case class TestInput(workflow: WorkflowModel) extends InputStage with LogError

  case class TestInputZK(workflow: WorkflowModel, curatorFramework: CuratorFramework)
    extends InputStage with ZooKeeperError

  def mockPolicy: WorkflowModel = {
    val policy = mock[WorkflowModel]
    when(policy.id).thenReturn(Some("id"))
    policy
  }

  "inputStage" should "Generate a input" in {
    val policy = mockPolicy
    val input = mock[WorkflowElementModel]
    val ssc = mock[StreamingContext]
    val reflection = mock[ReflectionUtils]
    val myInputClass = mock[Input]
    val sparkSession = mock[XDSession]
    when(policy.input).thenReturn(Some(input))
    when(input.name).thenReturn("input")
    when(input.`type`).thenReturn("Input")
    when(input.configuration).thenReturn(Map.empty[String, JsoneyString])
    when(reflection.tryToInstantiate(mockEq("InputInput"), any())).thenReturn(myInputClass)

    val result = TestInput(policy).createInput(ssc, sparkSession, reflection)

    verify(reflection).tryToInstantiate(mockEq("InputInput"), any())
    result should be(myInputClass)
  }

  "inputStage" should "Fail gracefully with bad input" in {
    val policy = mockPolicy
    val input = mock[WorkflowElementModel]
    val ssc = mock[StreamingContext]
    val sparkSession = mock[XDSession]
    val reflection = mock[ReflectionUtils]
    when(policy.input).thenReturn(Some(input))
    when(input.name).thenReturn("input")
    when(input.`type`).thenReturn("Input")
    when(reflection.tryToInstantiate(mockEq("InputInput"), any())).thenThrow(new RuntimeException("Fake"))

    the[IllegalArgumentException] thrownBy {
      TestInput(policy).createInput(ssc, sparkSession, reflection)
    } should have message "An error was encountered while creating the input: input. Please re-check the policy"
  }

  "inputStage" should "Fail when reflectionUtils don't behave correctly" in {
    val policy = mockPolicy
    val input = mock[WorkflowElementModel]
    val ssc = mock[StreamingContext]
    val reflection = mock[ReflectionUtils]
    val output = mock[Output]
    val sparkSession = mock[XDSession]

    when(policy.input).thenReturn(Some(input))
    when(input.name).thenReturn("input")
    when(input.`type`).thenReturn("Input")
    when(reflection.tryToInstantiate(mockEq("InputInput"), any())).thenReturn(output)

    the[IllegalArgumentException] thrownBy {
      TestInput(policy).createInput(ssc, sparkSession, reflection)
    } should have message "An error was encountered while creating the input: input. Please re-check the policy"
  }

  "inputStreamStage" should "Generate a inputStream" in {
    val policy = mockPolicy
    val input = mock[WorkflowElementModel]
    val inputClass = mock[Input]
    val row = mock[DStream[Row]]
    val reflection = mock[ReflectionUtils]
    when(policy.input).thenReturn(Some(input))
    when(input.name).thenReturn("input")
    when(input.`type`).thenReturn("Input")
    when(input.configuration).thenReturn(Map.empty[String, JsoneyString])
    when(reflection.tryToInstantiate(mockEq("InputInput"), any())).thenReturn(inputClass)
    when(inputClass.initStream).thenReturn(row)

    val result = TestInput(policy).inputStreamStage(inputClass)

    verify(inputClass).initStream
    result should be(row)
  }

  "inputStreamStage" should "Fail gracefully with bad input" in {
    val policy = mockPolicy
    val input = mock[WorkflowElementModel]
    val inputClass = mock[Input]
    val reflection = mock[ReflectionUtils]
    when(policy.input).thenReturn(Some(input))
    when(input.name).thenReturn("input")
    when(input.`type`).thenReturn("Input")
    when(input.configuration).thenReturn(Map.empty[String, JsoneyString])
    when(reflection.tryToInstantiate(mockEq("InputInput"), any())).thenReturn(inputClass)
    when(inputClass.initStream).thenThrow(new RuntimeException("Fake"))

    the[IllegalArgumentException] thrownBy {
      TestInput(policy).inputStreamStage(inputClass)
    } should have message "An error was encountered while creating the input stream for: input"

    verify(inputClass).initStream
  }
}
