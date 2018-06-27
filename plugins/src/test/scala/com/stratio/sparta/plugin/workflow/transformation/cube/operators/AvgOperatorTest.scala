/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.Operator
import com.stratio.sparta.core.enumerators.{WhenError, WhenFieldError, WhenRowError}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}


@RunWith(classOf[JUnitRunner])
class AvgOperatorTest extends WordSpec with Matchers {

  "Avg associative operator" should {

    val initSchema = StructType(Seq(
      StructField("field1", IntegerType, nullable = false),
      StructField("field2", IntegerType, nullable = false),
      StructField("field3", IntegerType, nullable = false)
    ))

    "processMap must be " in {
      val operator = new AvgOperator("sum", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      operator.processMap(new GenericRowWithSchema(Array(1, 2, 3), initSchema)) should be(Some(1))
    }

    "processReduce must be " in {
      val operator = new AvgOperator("sum", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      operator.processReduce(Seq(Some(1L), Some(3L), None)) should be(Some(Seq(1,3)))

      val operator2 = new AvgOperator("sum", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      operator2.processReduce(Seq(Some(1), Some(1))) should be(Some(Seq(1,1)))
    }

    "associative process must be " in {
      val operator = new AvgOperator("sum", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      val resultInput = Seq(
        (Operator.OldValuesKey, Some(Map("sum" -> 4L, "count" -> 2, "mean" -> 2d))),
        (Operator.NewValuesKey, Some(Seq(5L))),
        (Operator.NewValuesKey, None)
      )

      operator.associativity(resultInput) should be(Some(Map("sum" -> 9L, "count" -> 3, "mean" -> 3d)))
    }
  }
}
