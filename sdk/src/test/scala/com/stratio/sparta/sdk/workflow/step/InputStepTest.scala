/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.workflow.step

import java.io.Serializable

import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class InputStepTest extends WordSpec with Matchers with MockitoSugar {

  val sparkSession = mock[XDSession]
  val ssc = mock[Option[StreamingContext]]

  "InputStep" should {
    val name = "input"
    val outputsFields = Seq(OutputFields("color", "string"), OutputFields("price", "double"))
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
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

    "Properties map should be cast to Map[String,String]" in {
      val inputStep = new MockInputStep(
        name,
        outputOptions,
        ssc,
        sparkSession,
        properties
      )

      inputStep.lineageProperties() shouldBe a[Map[String,String]]
    }
  }

  "Input classSuffix must be corrected" in {
    val expected = "input"
    val result = InputStep.StepType
    result should be(expected)
  }
}
