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

package com.stratio.sparkta.driver.test

import com.stratio.sparkta.driver.SparktaJob
import com.stratio.sparkta.driver.util.ReflectionUtils
import com.stratio.sparkta.sdk.{Event, Input, JsoneyString, Parser}
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, PolicyElementModel}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, ShouldMatchers}

import scala.util.{Failure, Try}

@RunWith(classOf[JUnitRunner])
class SparktaJobTest extends FlatSpec with ShouldMatchers with MockitoSugar {

  val aggModel: AggregationPoliciesModel = mock[AggregationPoliciesModel]

  val method: String = "getSparkConfiguration"

  val suffix: String = "Output"

  val myOutput: PolicyElementModel = mock[PolicyElementModel]

  it should "parse a event" in {
    val parser: Parser = mock[Parser]
    val event: Event = mock[Event]
    val parsedEvent = mock[Event]
    when(parser.parse(event)).thenReturn(parsedEvent)

    val result = SparktaJob.parseEvent(event, parser)
    result should be(Some(parsedEvent))
  }
  it should "return none if a parse Event fails" in {
    val parser: Parser = mock[Parser]
    val event: Event = mock[Event]
    when(parser.parse(event)).thenThrow(new RuntimeException("testEx"))

    val result = SparktaJob.parseEvent(event, parser)
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
    when(myInputClass.setUp(ssc, aggModel.storageLevel.get)).thenReturn(mock[DStream[Event]])
    when(reflection.tryToInstantiate[Input]("InputInput",
      (c) => reflection.instantiateParameterizable[Input]
        (c, Map("" -> JsoneyString(""))))).thenReturn(myInputClass)


    val result = Try(SparktaJob.getInput(aggModel, ssc, reflection)) match {
      case Failure(ex) => ex
    }
    result.isInstanceOf[NullPointerException] should be(true)
  }
}
