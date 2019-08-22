/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.select

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class SelectTransformStepTest extends WordSpecLike with Matchers {

  //scalastyle:off
  "A SelectTransformStep" should {
    "check if the sql expression contains where statement" in {
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

      val selectTransform = new SelectTransformStepStreaming(
        "dummy",
        outputOptions,
        TransformationStepManagement(),
        None,
        null,
        Map(
          "selectExp" -> "select color where",
          "selectType" -> "EXPRESSION"
        )
      )
      val expected = true
      val result = selectTransform.containsWord("where")
      expected shouldEqual result
    }

    "check if the sql expression contains from statement" in {
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

      val selectTransform = new SelectTransformStepStreaming(
        "dummy",
        outputOptions,
        TransformationStepManagement(),
        None,
        null,
        Map(
          "selectExp" -> "select color from",
          "selectType" -> "EXPRESSION"
        )
      )
      val expected = true
      val result = selectTransform.containsWord("from")
      expected shouldEqual result
    }

    "check if the sql expression contains from statement and is well parsed" in {
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

      val selectTransform = new SelectTransformStepStreaming(
        "dummy",
        outputOptions,
        TransformationStepManagement(),
        None,
        null,
        Map(
          "selectExp" -> "select color from2",
          "selectType" -> "EXPRESSION"
        )
      )
      val expected = false
      val result = selectTransform.containsWord("from")
      expected shouldEqual result
    }

    "check if the sql expression contains where statement and is well parsed" in {
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

      val selectTransform = new SelectTransformStepStreaming(
        "dummy",
        outputOptions,
        TransformationStepManagement(),
        None,
        null,
        Map(
          "selectExp" -> "select color twhere",
          "selectType" -> "EXPRESSION"
        )
      )
      val expected = false
      val result = selectTransform.containsWord("where")
      expected shouldEqual result
    }
  }
}
