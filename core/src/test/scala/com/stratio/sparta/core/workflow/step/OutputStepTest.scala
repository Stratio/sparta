/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.step

import java.io.Serializable

import com.stratio.sparta.core.enumerators.SaveModeEnum
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
