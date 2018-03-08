/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
