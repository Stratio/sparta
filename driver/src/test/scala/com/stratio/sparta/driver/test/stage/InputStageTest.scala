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

import com.stratio.sparta.driver.stage.{InputStage, LogError, ZooKeeperError}
import com.stratio.sparta.driver.utils.ReflectionUtils
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.serving.core.models.policy.{PolicyElementModel, PolicyModel}
import org.apache.spark.sql.Row
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.mockito.Matchers.{any, eq => mockEq}
import org.mockito.Mockito.{when, _}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, ShouldMatchers}

@RunWith(classOf[JUnitRunner])
class InputStageTest extends FlatSpec with ShouldMatchers with MockitoSugar {

  case class TestInput(policy: PolicyModel) extends InputStage with LogError

  case class TestInputZK(policy: PolicyModel) extends InputStage with ZooKeeperError

  def mockPolicy: PolicyModel = {
    val policy = mock[PolicyModel]
    when(policy.storageLevel).thenReturn(Some("StorageLevel"))
    when(policy.id).thenReturn(Some("id"))
    policy
  }

  "inputStage" should "Generate a input" in {
    val policy = mockPolicy
    val input = mock[PolicyElementModel]
    val ssc = mock[StreamingContext]
    val reflection = mock[ReflectionUtils]
    val myInputClass = mock[Input]
    when(policy.input).thenReturn(Some(input))
    when(input.name).thenReturn("input")
    when(input.`type`).thenReturn("Input")
    when(reflection.tryToInstantiate(mockEq("InputInput"), any())).thenReturn(myInputClass)

    val result = TestInput(policy).inputStage(ssc, reflection)

    verify(reflection).tryToInstantiate(mockEq("InputInput"), any())
    result should be(myInputClass)
  }

  "inputStage" should "Fail gracefully with bad input" in {
    val policy = mockPolicy
    val input = mock[PolicyElementModel]
    val ssc = mock[StreamingContext]
    val reflection = mock[ReflectionUtils]
    when(policy.input).thenReturn(Some(input))
    when(input.name).thenReturn("input")
    when(input.`type`).thenReturn("Input")
    when(reflection.tryToInstantiate(mockEq("InputInput"), any())).thenThrow(new RuntimeException("Fake"))

    the[IllegalArgumentException] thrownBy {
      TestInput(policy).inputStage(ssc, reflection)
    } should have message "Something gone wrong creating the input: input. Please re-check the policy."
  }

  "inputStage" should "Fail when reflectionUtils don't behave correctly" in {
    val policy = mockPolicy
    val input = mock[PolicyElementModel]
    val ssc = mock[StreamingContext]
    val reflection = mock[ReflectionUtils]
    val output = mock[Output]

    when(policy.input).thenReturn(Some(input))
    when(input.name).thenReturn("input")
    when(input.`type`).thenReturn("Input")
    when(reflection.tryToInstantiate(mockEq("InputInput"), any())).thenReturn(output)

    the[IllegalArgumentException] thrownBy {
      TestInput(policy).inputStage(ssc, reflection)
    } should have message "Something gone wrong creating the input: input. Please re-check the policy."
  }


  "inputStage" should "Fail gracefully with bad input and no ZK" in {
    val policy = mockPolicy
    val input = mock[PolicyElementModel]
    val ssc = mock[StreamingContext]
    val reflection = mock[ReflectionUtils]
    when(policy.input).thenReturn(Some(input))
    when(input.name).thenReturn("input")
    when(input.`type`).thenReturn("Input")
    when(reflection.tryToInstantiate(mockEq("InputInput"), any())).thenThrow(new RuntimeException("Fake"))

    the[IllegalArgumentException] thrownBy {
      TestInput(policy).inputStage(ssc, reflection)
    } should have message "Something gone wrong creating the input: input. Please re-check the policy."
  }

  "inputStreamStage" should "Generate a inputStream" in {
    val policy = mockPolicy
    val input = mock[PolicyElementModel]
    val ssc = mock[StreamingContext]
    val inputClass = mock[Input]
    val row = mock[DStream[Row]]
    when(policy.input).thenReturn(Some(input))
    when(input.name).thenReturn("input")
    when(input.`type`).thenReturn("Input")
    when(inputClass.setUp(ssc, policy.storageLevel.get)).thenReturn(row)

    val result = TestInput(policy).inputStreamStage(ssc, inputClass)

    verify(inputClass).setUp(ssc, "StorageLevel")
    result should be(row)
  }

  "inputStreamStage" should "Fail gracefully with bad input" in {
    val policy = mockPolicy
    val input = mock[PolicyElementModel]
    val ssc = mock[StreamingContext]
    val inputClass = mock[Input]
    when(policy.input).thenReturn(Some(input))
    when(input.name).thenReturn("input")
    when(input.`type`).thenReturn("Input")
    when(inputClass.setUp(ssc, policy.storageLevel.get)).thenThrow(new RuntimeException("Fake"))

    the[IllegalArgumentException] thrownBy {
      TestInput(policy).inputStreamStage(ssc, inputClass)
    } should have message "Something gone wrong creating the input stream for: input."

    verify(inputClass).setUp(ssc, "StorageLevel")

  }

}
