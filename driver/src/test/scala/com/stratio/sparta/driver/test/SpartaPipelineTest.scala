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

import com.stratio.sparta.driver.SpartaPipeline
import com.stratio.sparta.driver.utils.ReflectionUtils
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.policy.{PolicyElementModel, PolicyModel}
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.Row
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.mockito.Matchers.{any, eq => mockEq}
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, ShouldMatchers}

@RunWith(classOf[JUnitRunner])
class SpartaPipelineTest extends FlatSpec with ShouldMatchers with MockitoSugar {

  val aggModel: PolicyModel = mock[PolicyModel]

  val method: String = "getSparkConfiguration"

  val suffix: String = "Output"

  val myOutput: PolicyElementModel = mock[PolicyElementModel]


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

    SpartaConfig.initMainConfig(Option(SpartaPipelineTest.config))

    val result = new SpartaPipeline(aggModel).inputStage(ssc, reflection)
    result should be(myInputClass)
  }

}

object SpartaPipelineTest {

  val config = ConfigFactory.parseString(
    s"""
      "sparta": {
        "config": {
          "executionMode": "local"
        }
      }
    """.stripMargin)
}
