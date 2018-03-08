/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.Operator
import com.stratio.sparta.sdk.workflow.enumerators.{WhenError, WhenFieldError, WhenRowError}
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}


@RunWith(classOf[JUnitRunner])
class CountOperatorTest extends WordSpec with Matchers {

  "Count operator" should {

    "processMap must be " in {
      val operator = new CountOperator("count", WhenRowError.RowError, WhenFieldError.FieldError)
      operator.processMap(Row(1, 2)) should be(Some(1L))
    }

    "processReduce must be " in {
      val operator = new CountOperator("count", WhenRowError.RowError, WhenFieldError.FieldError)
      operator.processReduce(Seq(Some(1L), Some(1L), None)) should be(Some(2L))

      val operator2 = new CountOperator("count", WhenRowError.RowError, WhenFieldError.FieldError)
      operator2.processReduce(Seq(Some(1L), Some(1L))) should be(Some(2L))
    }

    "associative process must be " in {
      val operator = new CountOperator("count", WhenRowError.RowError, WhenFieldError.FieldError)
      val resultInput = Seq((Operator.OldValuesKey, Some(1L)),
        (Operator.NewValuesKey, Some(1L)),
        (Operator.NewValuesKey, None))
      operator.associativity(resultInput) should be(Some(2L))
    }
  }
}
