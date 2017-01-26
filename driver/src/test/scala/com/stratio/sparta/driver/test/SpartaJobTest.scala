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

import com.stratio.sparta.driver.SpartaJob
import com.stratio.sparta.driver.utils.ReflectionUtils
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.models.policy.{PolicyElementModel, PolicyModel}
import org.apache.spark.sql.Row
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.mockito.Matchers.{any, eq => mockEq}
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, ShouldMatchers}

import scala.util.{Failure, Try}

@RunWith(classOf[JUnitRunner])
class SpartaJobTest extends FlatSpec with ShouldMatchers with MockitoSugar {

  val aggModel: PolicyModel = mock[PolicyModel]

  val method: String = "getSparkConfiguration"

  val suffix: String = "Output"

  val myOutput: PolicyElementModel = mock[PolicyElementModel]

  "SpartaJob" should "return configs" in {
    when(myOutput.`type`).thenReturn("Test")
    when(aggModel.outputs).thenReturn(Seq(myOutput))
    val reflecMoc = mock[ReflectionUtils]
    when(reflecMoc.getClasspathMap).thenReturn(Map("TestOutput" -> "TestOutput"))
    val result = Try(SpartaJob.getSparkConfigs(aggModel, method, suffix, Some(reflecMoc))) match {
      case Failure(ex) => ex
    }
    result.isInstanceOf[ClassNotFoundException] should be(true)
  }


  it should "parse a event" in {
    val parser: Parser = mock[Parser]
    val event: Row = mock[Row]
    val parsedEvent = mock[Option[Row]]
    when(parser.parse(event, removeRaw = false)).thenReturn(parsedEvent)

    val result = SpartaJob.parseEvent(event, parser)
    result should be(parsedEvent)
  }
  it should "return none if a parse Event fails" in {
    val parser: Parser = mock[Parser]
    val event: Row = mock[Row]
    when(parser.parse(event, removeRaw = false)).thenThrow(new RuntimeException("testEx"))

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
    when(reflection.tryToInstantiate[Input](mockEq("InputInput"), any())).thenReturn(myInputClass)

    val result = SpartaJob.getInput(aggModel, ssc, reflection)
    result should be(myInputClass)
  }
}
