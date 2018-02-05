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

import com.stratio.sparta.sdk.workflow.enumerators.{WhenError, WhenFieldError, WhenRowError}
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
