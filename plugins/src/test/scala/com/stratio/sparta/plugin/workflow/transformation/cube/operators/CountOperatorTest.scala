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
import com.stratio.sparta.sdk.workflow.enumerators.WhenError
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}


@RunWith(classOf[JUnitRunner])
class CountOperatorTest extends WordSpec with Matchers {

  "Count operator" should {

    "processMap must be " in {
      val operator = new CountOperator("count", WhenError.Error)
      operator.processMap(Row(1, 2)) should be(Some(1L))
    }

    "processReduce must be " in {
      val operator = new CountOperator("count", WhenError.Error)
      operator.processReduce(Seq(Some(1L), Some(1L), None)) should be(Some(2L))

      val operator2 = new CountOperator("count", WhenError.Error)
      operator2.processReduce(Seq(Some(1L), Some(1L))) should be(Some(2L))
    }

    "associative process must be " in {
      val operator = new CountOperator("count", WhenError.Error)
      val resultInput = Seq((Operator.OldValuesKey, Some(1L)),
        (Operator.NewValuesKey, Some(1L)),
        (Operator.NewValuesKey, None))
      operator.associativity(resultInput) should be(Some(2L))
    }
  }
}
