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
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class InputStepTest extends WordSpec with Matchers with MockitoSugar {

  val sparkSession = mock[XDSession]
  val ssc = mock[StreamingContext]

  "InputStep" should {
    val name = "input"
    val outputsFields = Seq(OutputFields("color", "string"), OutputFields("price", "double"))
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val properties = Map.empty[String, Serializable]

    "Return default storageLevel" in {
      val outputsFields = Seq.empty[OutputFields]
      val inputStep = new MockInputStep(
        name,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = inputStep.storageLevel
      val expected = StorageLevel.MEMORY_ONLY
      result should be(expected)
    }

    "Return user storageLevel" in {
      val outputsFields = Seq.empty[OutputFields]
      val properties = Map("storageLevel" -> "MEMORY_AND_DISK")
      val inputStep = new MockInputStep(
        name,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )
      val result = inputStep.storageLevel
      val expected = StorageLevel.MEMORY_AND_DISK
      result should be(expected)
    }

  }

  "Input classSuffix must be corrected" in {
    val expected = "input"
    val result = InputStep.StepType
    result should be(expected)
  }
}
