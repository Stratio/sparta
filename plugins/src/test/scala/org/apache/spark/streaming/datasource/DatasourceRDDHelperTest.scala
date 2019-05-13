/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource

import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.catalog.{CatalogStorageFormat, CatalogTable, CatalogTableType}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.datasource.receiver.DatasourceRDDHelper
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class DatasourceRDDHelperTest extends WordSpec with Matchers with MockitoSugar with DatasourceRDDHelper{

  private val simpleQuery1 = "Select * from TableA"
  private val simpleQuery2 = "Select * from TableA WHERE TableA.id > 30"

  private val provider = "jdbc"
  private val additionalSql = "where offsetcol > 20 LIMIT 5"

  val jdbcTableName = "jdbctable"
  val xdtableName = "tabletest"
  val catalogStorageFormat = CatalogStorageFormat(None, None, None, None, false, properties = Map("dbtable" -> jdbcTableName))

  import DatasourceRDDHelper._

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


    "be able to create a JDBC view in order to pushdown the xd query (select *)" in {

      val dbTableOptions =
        getCreateViewIncludingQuery(
          CatalogTable(
            TableIdentifier(xdtableName),
            CatalogTableType.EXTERNAL,
            catalogStorageFormat,
            StructType(Seq(StructField("col", StringType))),
            provider = Some(provider)
          ),
          s"SELECT * FROM $xdtableName",
          additionalSql
        ).options("dbtable")

      dbTableOptions shouldBe s"(SELECT * FROM $jdbcTableName $additionalSql) tmpalias"


    }

    "be able to create a JDBC view in order to pushdown the xd query (select somecol)" in {
      val dbTableOptions =
        getCreateViewIncludingQuery(
          CatalogTable(
            TableIdentifier(xdtableName),
            CatalogTableType.EXTERNAL,
            catalogStorageFormat,
            StructType(Seq(StructField("col", StringType))),
            provider = Some(provider)
          ),
          s"SELECT offsetcol, 'hola' as othercol FROM $xdtableName",
          additionalSql
        ).options("dbtable")

      dbTableOptions shouldBe s"(SELECT offsetcol, 'hola' as othercol FROM $jdbcTableName $additionalSql) tmpalias"
    }

    "be able to create a JDBC view in order to pushdown the xd query (select xdtable.somecol)" in {
      val additionalSqlQualified: String => String = str => s"where $str.offsetcol > 20 LIMIT 5"
      val dbTableOptions =
        getCreateViewIncludingQuery(
          CatalogTable(
            TableIdentifier(xdtableName),
            CatalogTableType.EXTERNAL,
            catalogStorageFormat,
            StructType(Seq(StructField("col", StringType))),
            provider = Some(provider)
          ),
          s"SELECT $xdtableName.offsetcol FROM $xdtableName",
          additionalSqlQualified(xdtableName)
        ).options("dbtable")

      dbTableOptions shouldBe s"(SELECT $jdbcTableName.offsetcol FROM $jdbcTableName ${additionalSqlQualified(jdbcTableName)}) tmpalias"
    }

  }


}
