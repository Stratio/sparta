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

package org.apache.spark.streaming.datasource

import org.apache.spark.streaming.datasource.receiver.DatasourceRDDHelper
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

@RunWith(classOf[JUnitRunner])
class DatasourceRDDHelperTest extends WordSpec with Matchers with MockitoSugar with DatasourceRDDHelper{
  val simpleQuery1 = "Select * from TableA"
  val simpleQuery2 = "Select * from TableA WHERE TableA.id > 30"

    "DatasourceRDDHelper" should {
        "be able to recognize complex queries" in {
          val complexQuery1 = "Select * from TableA JOIN TableB on TableA.id = TableB.id"
          val complexQuery2 = "Select * from TableA GROUP BY TableA.id HAVING TableA.id > 30"
          val complexQuery3 = "Select * from (Select * from TableA WHERE TableA.id > 30)"
          checkIfComplexQuery(complexQuery1)
          checkIfComplexQuery(complexQuery2)
          checkIfComplexQuery(complexQuery3)
        }

        "be able to recognize simple queries" in {
          checkIfComplexQuery(simpleQuery1) == false
        }

        "extract where condition if simple query" in {
          retrieveWhereCondition(simpleQuery1) shouldEqual None
          retrieveWhereCondition(simpleQuery2) shouldEqual Some(" TableA.id > 30")
        }
    }
}
