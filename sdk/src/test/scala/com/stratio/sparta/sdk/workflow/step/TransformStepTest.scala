/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.workflow.step

import java.io.Serializable

import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class TransformStepTest extends WordSpec with Matchers with MockitoSugar {

  val sparkSession = mock[XDSession]
  val ssc = mock[StreamingContext]

  "TransformStep" should {
    val name = "transform"
    val schema = StructType(Seq(StructField("inputField", StringType)))
    val inputSchemas = Map("input" -> schema)
    val outputsFields = Seq(OutputFields("color", "string"), OutputFields("price", "double"))
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val properties = Map("addAllInputFields" -> true.asInstanceOf[Serializable])
    val transformationStepManagement = TransformationStepManagement()


    "Transform classSuffix must be corrected" in {
      val expected = "transformation"
      val result = TransformStep.StepType
      result should be(expected)
    }

    "Properties map should be cast to Map[String,String]" in {
      val transformStep = new MockTransformStep(
        name,
        outputOptions,
        transformationStepManagement,
        Option(ssc),
        sparkSession,
        properties
      )

      transformStep.lineageProperties() shouldBe a[Map[String,String]]
    }
  }

}
