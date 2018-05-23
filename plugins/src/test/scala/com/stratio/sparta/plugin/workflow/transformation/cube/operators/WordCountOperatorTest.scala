/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.Operator
import com.stratio.sparta.sdk.enumerators.{WhenError, WhenFieldError, WhenRowError}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}


@RunWith(classOf[JUnitRunner])
class WordCountOperatorTest extends WordSpec with Matchers {

  "WordCount operator" should {

    val initSchema = StructType(Seq(
      StructField("field1", StringType, nullable = false),
      StructField("field2", IntegerType, nullable = false),
      StructField("field3", IntegerType, nullable = false)
    ))

    "processMap must be " in {
      val operator = new WordCountOperator("wordcount", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      val result = operator.processMap(new GenericRowWithSchema(Array("hi sparta", 2, 3), initSchema))
      result should be(Some(Seq("hi", "sparta")))
    }

    "processReduce must be " in {
      val operator = new WordCountOperator("wordcount", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      operator.processReduce(Seq(Some("hi"), Some("sparta"), None)) should be(Some(Seq("hi", "sparta")))

      val operator2 = new WordCountOperator("wordcount", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      operator2.processReduce(Seq(Some(Seq("hi")), Some(Seq("sparta")))) should be(Some(Seq("hi", "sparta")))
    }

    "associative process must be " in {
      val operator = new WordCountOperator("wordcount", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("field1"))
      val resultInput = Seq((Operator.OldValuesKey, Some(Map("hi" -> 1L))),
        (Operator.NewValuesKey, Some(Seq("hi", "sparta"))),
        (Operator.NewValuesKey, None))
      operator.associativity(resultInput) should be(Some(Map("hi" -> 2L, "sparta" -> 1L)))
    }
  }
}
