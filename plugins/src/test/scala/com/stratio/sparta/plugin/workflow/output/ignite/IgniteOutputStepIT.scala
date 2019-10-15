/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.output.ignite

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.common.ignite.IgniteSuiteBase
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.jdbc.SpartaJdbcUtils
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, Matchers, ShouldMatchers}
import org.scalatest.junit.JUnitRunner

import scala.util.{Random, Try}
//scalastyle:off
@RunWith(classOf[JUnitRunner])
class IgniteOutputStepIT extends
  TemporalSparkContext with
  IgniteSuiteBase with
  ShouldMatchers with
  Matchers with
  BeforeAndAfterAll {

  val properties: Map[String, JSerializable] = Map("host" -> igniteURL, "fetchSize" -> fetchSize, "batchSize" -> batchSize)

  trait IgniteCommons {

    def createTable()(table: String) = s"CREATE TABLE $table USING org.apache.spark.sql.jdbc OPTIONS (url '$igniteURL', dbtable '$table', driver 'org.apache.ignite.IgniteJdbcThinDriver', fetchSize '$fetchSize')"

    val tableCreate = createTable() _

    def createTableCaseSensitive(crossdataTableName: String, table: String, schema: String = "public") =
      s"""CREATE TABLE $crossdataTableName USING org.apache.spark.sql.jdbc OPTIONS (url '$igniteURL', dbtable '\"$table\"', driver 'org.apache.ignite.IgniteJdbcThinDriver', fetchSize '$fetchSize')"""

    var xdSession = sparkSession
  }

  trait JdbcCommons extends IgniteCommons {

    val schema = StructType(Seq(StructField("ID", IntegerType), StructField("NAME", StringType)))

    val data = Seq(
      new GenericRowWithSchema(Array(1, "test1"), schema),
      new GenericRowWithSchema(Array(2, "test2"), schema)
    )
    val rdd = sc.parallelize(data, 1).asInstanceOf[RDD[Row]]
    val okData = xdSession.createDataFrame(rdd, schema)

    val dataFail = Seq(
      new GenericRowWithSchema(Array(1, "test1"), schema),
      new GenericRowWithSchema(Array(1, "test1"), schema)
    )
    val rddFail = sc.parallelize(dataFail,1).asInstanceOf[RDD[Row]]
    val duplicateData = xdSession.createDataFrame(rddFail, schema)

    val upsert = Seq(
      new GenericRowWithSchema(Array(1, "test1"), schema),
      new GenericRowWithSchema(Array(2, "test2"), schema),
      new GenericRowWithSchema(Array(3, "test3"), schema),
      new GenericRowWithSchema(Array(1, "test1Updated"), schema)
    )
    val rddUpsert = sc.parallelize(upsert, 1).asInstanceOf[RDD[Row]]
    val upsertData = xdSession.createDataFrame(rddUpsert, schema)

    val upsertDataOut = Seq(
      new GenericRowWithSchema(Array(1, "test1Updated"), schema),
      new GenericRowWithSchema(Array(2, "test2"), schema),
      new GenericRowWithSchema(Array(3, "test3"), schema)
    )
  }

  trait JdbcCommonsUpsertFieldsCamelCase extends IgniteCommons {
    val schema = StructType(Seq(StructField("ID", IntegerType), StructField("NAME", StringType), StructField("COLOR", StringType), StructField("DATETIME", LongType)))

    val upsertFields = Seq(
      new GenericRowWithSchema(Array(1, "text1", "red", 1L), schema),
      new GenericRowWithSchema(Array(2, "text2", "blue", 2L), schema),
      new GenericRowWithSchema(Array(3, "text3", "green", 3L), schema),
      new GenericRowWithSchema(Array(1, "text1", "pink", 10L), schema)
    )
    val rddUpsert = sc.parallelize(upsertFields, 1).asInstanceOf[RDD[Row]]
    val upsertData = xdSession.createDataFrame(rddUpsert, schema)

    val upsertFieldsDataOut = Seq(
      new GenericRowWithSchema(Array(1, "text1", "pink", 1L), schema),
      new GenericRowWithSchema(Array(2, "text2", "blue", 2L), schema),
      new GenericRowWithSchema(Array(3, "text3", "green", 3L), schema)
    )

    val upsertWithConstraintData: DataFrame = {
      val upsertWithConstraint = Seq(
        new GenericRowWithSchema(Array(1, "text2", "black", 15L), schema),
        new GenericRowWithSchema(Array(1, "text1", "pink", 10L), schema)
      )
      val rdd = sc.parallelize(upsertWithConstraint, 1).asInstanceOf[RDD[Row]]
      xdSession.createDataFrame(rdd, schema)
    }

    val upsertWithConstraintDataOut = Seq(
      new GenericRowWithSchema(Array(1, "text1", "pink", 1L), schema),
      new GenericRowWithSchema(Array(2, "text2", "blue", 2L), schema),
      new GenericRowWithSchema(Array(3, "text3", "green", 3L), schema),
      new GenericRowWithSchema(Array(1, "text2", "black", 15L), schema)
    )
  }

  trait JdbcCommonsUpsertFields extends IgniteCommons {
    val schema = StructType(Seq(StructField("ID", IntegerType), StructField("NAME", StringType), StructField("COLOR", StringType), StructField("DATETIME", LongType)))
    val upsertFields = Seq(
      new GenericRowWithSchema(Array(1, "text1", "red", 1L), schema),
      new GenericRowWithSchema(Array(2, "text2", "blue", 2L), schema),
      new GenericRowWithSchema(Array(3, "text3", "green", 3L), schema),
      new GenericRowWithSchema(Array(1, "text1", "pink", 1L), schema)
    )
    val rddUpsert = sc.parallelize(upsertFields, 1).asInstanceOf[RDD[Row]]
    val upsertData = xdSession.createDataFrame(rddUpsert, schema)

    val upsertFieldsDataOut = Seq(
      new GenericRowWithSchema(Array(1, "text1", "pink", 1L), schema),
      new GenericRowWithSchema(Array(2, "text2", "blue", 2L), schema),
      new GenericRowWithSchema(Array(3, "text3", "green", 3L), schema)
    )
  }

  "Tx batch statement without duplicate data" should "insert allrecords" in new JdbcCommons {
    val tableName = s"test_batch_${System.currentTimeMillis()}"
    val primaryKey = "ID"
    val igniteOutputStep = new IgniteOutputStep("igniteOutBatch", xdSession, properties ++ Map("igniteSaveMode" -> "STATEMENT"))

    igniteOutputStep.save(okData, SaveModeEnum.Append, Map[String, String]("tableName" -> tableName, "primaryKey" -> primaryKey))

    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }

  "Tx one by one statement with failFast in false" should "insert record by record and not fail" in new JdbcCommons {
    val tableName = s"test_onebyone_${System.currentTimeMillis()}"
    val primaryKey = "ID"
    val igniteOutputStep = new IgniteOutputStep("igniteOutOne", xdSession, properties
      ++ Map("igniteSaveMode" -> "SINGLE_STATEMENT", "failFast" -> "false"))
    igniteOutputStep.save(okData, SaveModeEnum.Append, Map("tableName" -> tableName,
      "auto-commit" -> "false",
      "isolationLevel" -> "READ-COMMITED",
      "primaryKey" -> primaryKey))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }

  "Truncate" should "first truncate table and then insert data in table" in new JdbcCommons {
    val tableName = s"test_overwrite_${System.currentTimeMillis()}"
    val primaryKey = "ID"
    val connectionProperties = new JDBCOptions(igniteURL,
      tableName,
      properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.apache.ignite.IgniteJdbcThinDriver")
    )
    SpartaJdbcUtils.createTable(connectionProperties, upsertData, "overwriteTableIgnite", Seq("id"),  Map("TEXT" -> "VARCHAR(255)"))
    val dataInsert = Seq(
      new GenericRowWithSchema(Array(4, "testTruncateRecord"), schema)
    )
    dataInsert.foreach(row => {
      val sql = s"INSERT INTO $tableName(ID,NAME) VALUES (${row.getInt(0)},'${row.getString(1)}')"
      withConnectionExecute(sql)
    })
    val igniteOutputStep = new IgniteOutputStep("igniteOutSingle", xdSession, properties
      ++ Map("igniteSaveMode" -> "STATEMENT", "failFast" -> "true"))
    igniteOutputStep.save(okData, SaveModeEnum.Overwrite, Map("tableName" -> tableName,
      "auto-commit" -> "true",
      "isolationLevel" -> "READ-COMMITED",
      "primaryKey" -> primaryKey))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }

  "Native upsert with single primary key statement" should "insert 3 records and update one of them" in new JdbcCommons {
    val tableName = s"test_upsert_${System.currentTimeMillis()}"
    val primaryKey = "ID"
    val igniteOutputStep = new IgniteOutputStep("igniteNativeUpsert", xdSession, properties
      ++ Map("igniteSaveMode" -> "STATEMENT", "failFast" -> "true"))
    igniteOutputStep.save(upsertData, SaveModeEnum.Upsert, Map("tableName" -> tableName,
      "auto-commit" -> "false", "numPartitions" -> "1",
      "isolationLevel" -> "READ-COMMITED",
      "primaryKey" -> primaryKey))
    xdSession.sql(tableCreate(tableName))
    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()
    rows.foreach(row => assert(upsertDataOut.contains(row)))
  }

  "Native upsert only fields with  primary key statement" should "update color column" in new JdbcCommonsUpsertFields {
    val tableName = s"test_upsert_pk_fields_${System.currentTimeMillis()}"
    val connectionProperties = new JDBCOptions(igniteURL,
      tableName,
      properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.apache.ignite.IgniteJdbcThinDriver")
    )
    SpartaJdbcUtils.createTable(connectionProperties, upsertData, "upsertFieldsTableIgnite", Seq("ID", "NAME"),  Map("TEXT" -> "VARCHAR(255)"))
    val dataInsert = Seq(
      new GenericRowWithSchema(Array(1, "text1", "red", 1L), schema),
      new GenericRowWithSchema(Array(2, "text2", "blue", 2L), schema),
      new GenericRowWithSchema(Array(3, "text3", "green", 3L), schema)
    )
    dataInsert.foreach(row => {
      val sql = s"INSERT INTO $tableName(id,name,color,datetime) VALUES (${row.getInt(0)},'${row.getString(1)}','${row.getString(2)}',${row.getLong(3)})"
      withConnectionExecute(sql)
    })

    val primaryKey = "ID,NAME"
    val igniteOutputStep = new IgniteOutputStep("igniteNativeUpsert", xdSession, properties
      ++ Map("igniteSaveMode" -> "STATEMENT", "failFast" -> "true"))
    igniteOutputStep.save(upsertData, SaveModeEnum.Upsert, Map("tableName" -> tableName,
      "auto-commit" -> "false", "numPartitions" -> "1",
      "isolationLevel" -> "READ-COMMITED",
      "primaryKey" -> primaryKey))
    xdSession.sql(tableCreate(tableName))

    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()
    rows.foreach(row => assert(upsertFieldsDataOut.contains(row)))
  }

  private def jdbcOptions(tableName: String): JDBCOptions =
    new JDBCOptions(igniteURL, tableName, properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.apache.ignite.IgniteJdbcThinDriver"))


  it should "insert in tables containing upper case letters" in new JdbcCommons {

    val randomLetters = (Random.alphanumeric take 5).dropWhile(!_.isLetter).mkString
    val tableNames = Seq("lowerorupper", "LOWERORUPPER", "LowerOrUpper").map(_ + randomLetters)
    val primaryKey = "ID"
    tableNames.foreach{ tableName =>
      val igniteOutputStep = new IgniteOutputStep("igniteOutBatch", xdSession, properties ++ Map("caseSensitiveEnabled" -> "true", "igniteSaveMode" -> "STATEMENT"))
      igniteOutputStep.save(okData, SaveModeEnum.Append, Map("tableName" -> tableName, "primaryKey" -> primaryKey))
    }

    tableNames.foreach{ tableName =>
      val crossdataTableName = (Random.alphanumeric take 5).dropWhile(!_.isLetter).mkString
      xdSession.sql(createTableCaseSensitive(crossdataTableName, tableName))
      xdSession.sql(s"SELECT * FROM $crossdataTableName").count() shouldBe 2
    }
  }

  "Delete that no affects records" should "maintain record with id = 4 " in new JdbcCommons {
    val tableName = s"test_delete_${System.currentTimeMillis()}"
    val primaryKey = "ID"
    val connectionProperties = new JDBCOptions(igniteURL,
      tableName,
      properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.apache.ignite.IgniteJdbcThinDriver")
    )
    SpartaJdbcUtils.createTable(connectionProperties, okData, "deleteTableIgnite", Seq(primaryKey),  Map("TEXT" -> "VARCHAR(255)"))
    val dataInsert = Seq(
      new GenericRowWithSchema(Array(4, "testNoDeleteRecord"), schema)
    )
    dataInsert.foreach(row => {
      val sql = s"INSERT INTO $tableName(ID,NAME) VALUES (${row.getInt(0)},'${row.getString(1)}')"
      withConnectionExecute(sql)
    })
    val igniteOutputStep = new IgniteOutputStep("igniteDelete", xdSession, properties ++ Map("igniteSaveMode" -> "SINGLE_STATEMENT", "failFast" ->
      "true"))
    igniteOutputStep.save(okData, SaveModeEnum.Delete, Map("tableName" -> tableName, "primaryKey" -> primaryKey, "auto-commit" -> "false", "isolationLevel" ->
      "READ-COMMITED"))
    xdSession.sql(tableCreate(tableName))
    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()
    rows.length shouldBe 1
    rows.foreach(row => assert(dataInsert.contains(row)))
    SpartaJdbcUtils.dropTable(connectionProperties, "deleteTableIgnite", Some(tableName))
  }

  "Delete despite duplicated id = 2" should "remove records when predicate is true on 'name' column" in new JdbcCommons {
    val tableName = s"test_delete_${System.currentTimeMillis()}"
    val primaryKey = "NAME"
    val connectionProperties = new JDBCOptions(igniteURL,
      tableName,
      properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.apache.ignite.IgniteJdbcThinDriver")
    )
    SpartaJdbcUtils.createTable(connectionProperties, okData, "deleteTableIgnite", Seq(primaryKey), Map("TEXT" -> "VARCHAR(255)"))
    val dataInsert = Seq(
      new GenericRowWithSchema(Array(1, "test1"), schema),
      new GenericRowWithSchema(Array(2, "test2"), schema),
      new GenericRowWithSchema(Array(2, "testPkDuplicate"), schema)
    )
    dataInsert.foreach(row => {
      val sql = s"INSERT INTO $tableName(ID,NAME) VALUES (${row.getInt(0)},'${row.getString(1)}')"
      withConnectionExecute(sql)
    })
    val igniteOutputStep = new IgniteOutputStep("igniteDelete", xdSession, properties ++ Map("igniteSaveMode" -> "STATEMENT", "failFast" -> "true"))
    igniteOutputStep.save(okData, SaveModeEnum.Delete, Map("tableName" -> tableName, "primaryKey" -> primaryKey, "auto-commit" -> "false", "isolationLevel" ->
      "READ-COMMITED"))
    xdSession.sql(tableCreate(tableName))
    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()
    rows.length shouldBe 1
    rows.foreach(row => assert(dataInsert.contains(row)))
    SpartaJdbcUtils.dropTable(connectionProperties, "deleteTableIgnite", Some(tableName))
  }
}
