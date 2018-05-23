/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import com.stratio.sparta.sdk.enumerators.{WhenError, WhenFieldError, WhenRowError}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}


@RunWith(classOf[JUnitRunner])
class MedianOperatorTest extends WordSpec with Matchers {

  "Median operator" should {

    val initSchema = StructType(Seq(
      StructField("field1", IntegerType, nullable = false),
      StructField("field2", IntegerType, nullable = false),
      StructField("field3", IntegerType, nullable = false)
    ))

    "processMap must be " in {
      val operator = new MedianOperator("median", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      operator.processMap(new GenericRowWithSchema(Array(1, 2, 3), initSchema)) should be(Some(1))
    }

    "processReduce must be " in {
      val operator = new MedianOperator("median", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      operator.processReduce(Seq(Some(1L), Some(3L), None)) should be(Some(2d))

      val operator2 = new MedianOperator("median", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      operator2.processReduce(Seq(Some(1), Some(1))) should be(Some(1d))

      val operator3 = new MedianOperator("median", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      operator3.processReduce(Seq(None)) should be(Some(0))
    }
  }
}
