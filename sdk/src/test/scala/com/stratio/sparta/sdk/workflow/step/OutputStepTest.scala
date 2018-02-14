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

package com.stratio.sparta.sdk.workflow.step

import java.io.Serializable

import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class OutputStepTest extends WordSpec with Matchers with MockitoSugar {

  val sparkSession = mock[XDSession]
  val ssc = mock[StreamingContext]

  "InputStep" should {
    val name = "output"
    val properties = Map.empty[String, Serializable]

    "Return supportedSaveModes" in {
      val outputStep = new MockOutputStep(
        name,
        sparkSession,
        properties
      )
      val result = outputStep.supportedSaveModes
      val expected = SaveModeEnum.allSaveModes
      result should be(expected)
    }

    "Properties map should be cast to Map[String,String]" in {
      val outputStep = new MockOutputStep(
        name,
        sparkSession,
        properties
      )

      outputStep.lineageProperties() shouldBe a[Map[String,String]]
    }
  }

  "Ouput classSuffix must be corrected" in {
    val expected = "output"
    val result = OutputStep.StepType
    result should be(expected)
  }
}
