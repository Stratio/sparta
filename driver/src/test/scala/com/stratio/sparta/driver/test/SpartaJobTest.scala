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
package com.stratio.sparta.driver.test

import scala.util.Failure
import scala.util.Try

import com.stratio.sparta.driver.SpartaJob
import com.stratio.sparta.driver.util.ReflectionUtils
import com.stratio.sparta.sdk.{Input, JsoneyString, Parser}
import com.stratio.sparta.serving.core.models.{AggregationPoliciesModel, PolicyElementModel}
import org.apache.spark.sql.Row
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, ShouldMatchers}

import com.stratio.sparta.driver.SpartaJob
import com.stratio.sparta.driver.util.ReflectionUtils
import com.stratio.sparta.sdk.Input
import com.stratio.sparta.sdk.JsoneyString
import com.stratio.sparta.sdk.Parser
import com.stratio.sparta.serving.core.models.AggregationPoliciesModel
import com.stratio.sparta.serving.core.models.PolicyElementModel

@RunWith(classOf[JUnitRunner])
class SpartaJobTest extends FlatSpec with ShouldMatchers with MockitoSugar {

  val aggModel: AggregationPoliciesModel = mock[AggregationPoliciesModel]

  val method: String = "getSparkConfiguration"

  val suffix: String = "Output"

  val myOutput: PolicyElementModel = mock[PolicyElementModel]

  "SpartaJob" should "return configs" in {
    when(myOutput.`type`).thenReturn("Test")
    when(aggModel.outputs).thenReturn(Seq(myOutput))
    val reflecMoc = mock[ReflectionUtils]
    when(reflecMoc.getClasspathMap).thenReturn(Map("TestOutput" -> "TestOutput"))
    val result = Try(SpartaJob.getSparkConfigs(aggModel, method, suffix, Some(reflecMoc))) match {
      case Failure(ex) => {
        ex
      }
    }
    result.isInstanceOf[ClassNotFoundException] should be(true)
  }


  it should "parse a event" in {
    val parser: Parser = mock[Parser]
    val event: Row = mock[Row]
    val parsedEvent = mock[Row]
    when(parser.parse(event, false)).thenReturn(parsedEvent)

    val result = SpartaJob.parseEvent(event, parser)
    result should be(Some(parsedEvent))
  }
  it should "return none if a parse Event fails" in {
    val parser: Parser = mock[Parser]
    val event: Row = mock[Row]
    when(parser.parse(event, false)).thenThrow(new RuntimeException("testEx"))

    val result = SpartaJob.parseEvent(event, parser)
    result should be(None)
  }

  it should "create a input" in {
    val myInput = Some(mock[PolicyElementModel])
    val ssc = mock[StreamingContext]
    val reflection = mock[ReflectionUtils]
    val myInputClass = mock[Input]
    when(aggModel.storageLevel).thenReturn(Some("StorageLevel"))
    when(aggModel.input).thenReturn(myInput)
    when(myInput.get.name).thenReturn("myInput")
    when(myInput.get.`type`).thenReturn("Input")
    when(myInput.get.configuration).thenReturn(Map("" -> JsoneyString("")))
    when(myInputClass.setUp(ssc, aggModel.storageLevel.get)).thenReturn(mock[DStream[Row]])
    when(reflection.tryToInstantiate[Input]("InputInput",
      (c) => reflection.instantiateParameterizable[Input]
        (c, Map("" -> JsoneyString(""))))).thenReturn(myInputClass)


    val result = Try(SpartaJob.getInput(aggModel, ssc, reflection)) match {
      case Failure(ex) => ex
    }
    result.isInstanceOf[NullPointerException] should be(true)
  }
}
