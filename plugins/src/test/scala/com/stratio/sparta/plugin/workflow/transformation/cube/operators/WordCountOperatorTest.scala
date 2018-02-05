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

package com.stratio.sparta.plugin.workflow.transformation.cube.operators

import com.stratio.sparta.plugin.workflow.transformation.cube.sdk.Operator
import com.stratio.sparta.sdk.workflow.enumerators.{WhenError, WhenFieldError, WhenRowError}
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
