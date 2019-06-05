/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.output.postgres

import java.io.{Serializable => JSerializable}
import java.sql.Connection

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.common.postgresql.PostgresSuiteBase
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.jdbc.SpartaJdbcUtils
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, ShouldMatchers}

import scala.util.{Random, Try}

//scalastyle:off
@RunWith(classOf[JUnitRunner])
class PostgresOutputStepIT extends
  TemporalSparkContext with
  PostgresSuiteBase with
  ShouldMatchers with
  Matchers with
  BeforeAndAfterAll {


  val properties: Map[String, JSerializable] = Map("url" -> postgresURL)

  Class.forName("org.postgresql.Driver")

  trait PostgresCommons {

    def createTable()(table: String) = s"CREATE TABLE $table USING org.apache.spark.sql.jdbc OPTIONS (url '$postgresURL', dbtable '$table', driver 'org.postgresql.Driver')"

    val tableCreate = createTable() _

    def createTableCaseSensitive(crossdataTableName: String, table: String, schema: String = "public") =
      s"""CREATE TABLE $crossdataTableName USING org.apache.spark.sql.jdbc OPTIONS (url '$postgresURL', dbtable '\"public\".\"$table\"', driver 'org.postgresql.Driver')"""


    var xdSession = sparkSession
  }

  trait JdbcCommons extends PostgresCommons {

    val schema = StructType(Seq(StructField("id", IntegerType), StructField("text", StringType)))

    val data = Seq(
      new GenericRowWithSchema(Array(1, "test1"), schema),
      new GenericRowWithSchema(Array(2, "test2"), schema)
    )
    val rdd = sc.parallelize(data).asInstanceOf[RDD[Row]]
    val okData = xdSession.createDataFrame(rdd, schema)

    val dataFail = Seq(
      new GenericRowWithSchema(Array(1, "test1"), schema),
      new GenericRowWithSchema(Array(1, "test1"), schema)
    )
    val rddFail = sc.parallelize(dataFail).asInstanceOf[RDD[Row]]
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

  trait JdbcCommonsUpsertFieldsCamelCase extends PostgresCommons {
    val schema = StructType(Seq(StructField("Id", IntegerType), StructField("text", StringType), StructField("color", StringType), StructField("datetime", LongType)))
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

  trait JdbcCommonsUpsertFields extends PostgresCommons {
    val schema = StructType(Seq(StructField("id", IntegerType), StructField("text", StringType), StructField("color", StringType), StructField("datetime", LongType)))
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
  }

  "Tx batch statement without duplicate data" should "insert allrecords" in new JdbcCommons {
    val tableName = s"test_batch_${System.currentTimeMillis()}"
    val postgresOutputStep = new PostgresOutputStep("postgresOutBatch", xdSession, properties ++ Map("postgresSaveMode" -> "STATEMENT"))
    postgresOutputStep.save(okData, SaveModeEnum.Append, Map[String, String]("tableName" -> tableName))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }

  "Tx one by one statement with failFast in false" should "insert record by record and not fail" in new JdbcCommons {
    val tableName = s"test_onebyone_${System.currentTimeMillis()}"
    val postgresOutputStep = new PostgresOutputStep("postgresOutOne", xdSession, properties
      ++ Map("postgresSaveMode" -> "SINGLE_STATEMENT", "failFast" -> "false", "dropTemporalTableSuccess" -> "true", "dropTemporalTableFailure" -> "true"))
    postgresOutputStep.save(okData, SaveModeEnum.Append, Map("tableName" -> tableName,
      "auto-commit" -> "false",
      "isolationLevel" -> "READ-COMMITED"))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }

  "Tx single statement with dropTemporalTable false" should "insert data and keep temporal table" in new JdbcCommons {
    val tableName = s"test_single_${System.currentTimeMillis()}"
    val postgresOutputStep = new PostgresOutputStep("postgresOutSingle", xdSession, properties
      ++ Map("postgresSaveMode" -> "ONE_TRANSACTION", "failFast" -> "true", "dropTemporalTableSuccess" -> "true", "dropTemporalTableFailure" -> "true"))
    postgresOutputStep.save(okData, SaveModeEnum.Append, Map("tableName" -> tableName,
      "auto-commit" -> "false",
      "isolationLevel" -> "READ-COMMITED"))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }

  "CopyIn" should "first truncate table and then insert data in table" in new JdbcCommons {
    val tableName = s"test_overwrite_${System.currentTimeMillis()}"
    val connectionProperties = new JDBCOptions(postgresURL,
      tableName,
      properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.postgresql.Driver")
    )
    SpartaJdbcUtils.createTable(connectionProperties, upsertData, "overwriteTable")
    val connection = SpartaJdbcUtils.getConnection(connectionProperties, "overwriteTable")
    val dataInsert = Seq(
      new GenericRowWithSchema(Array(4, "testTruncateRecord"), schema)
    )
    dataInsert.foreach(row => {
      val sql = s"INSERT INTO $tableName(id,text) VALUES (${row.getInt(0)},'${row.getString(1)}')"
      connection.prepareStatement(sql).execute()
    })
    val postgresOutputStep = new PostgresOutputStep("postgresOutSingle", xdSession, properties
      ++ Map("postgresSaveMode" -> "COPYIN", "failFast" -> "true", "dropTemporalTableSuccess" -> "true", "dropTemporalTableFailure" -> "true"))
    postgresOutputStep.save(okData, SaveModeEnum.Overwrite, Map("tableName" -> tableName,
      "auto-commit" -> "true",
      "isolationLevel" -> "READ-COMMITED"))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }

  "CopyIn" should "insert data in table" in new JdbcCommons {
    val tableName = s"test_copy_${System.currentTimeMillis()}"
    val postgresOutputStep = new PostgresOutputStep("postgresOutSingle", xdSession, properties
      ++ Map("postgresSaveMode" -> "COPYIN", "failFast" -> "true", "dropTemporalTableSuccess" -> "true", "dropTemporalTableFailure" -> "true"))
    postgresOutputStep.save(okData, SaveModeEnum.Append, Map("tableName" -> tableName,
      "auto-commit" -> "true",
      "isolationLevel" -> "READ-COMMITED"))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }

  "Native upsert with single primary key statement" should "insert 3 records and update one of them" in new JdbcCommons {
    val tableName = s"test_upsert_${System.currentTimeMillis()}"
    val primaryKey = "id"
    val postgresOutputStep = new PostgresOutputStep("postgresNativeUpsert", xdSession, properties
      ++ Map("postgresSaveMode" -> "STATEMENT", "failFast" -> "true", "dropTemporalTableSuccess" -> "true", "dropTemporalTableFailure" -> "true"))
    postgresOutputStep.save(upsertData, SaveModeEnum.Upsert, Map("tableName" -> tableName,
      "auto-commit" -> "false", "numPartitions" -> "1",
      "isolationLevel" -> "READ-COMMITED",
      "primaryKey" -> primaryKey))
    xdSession.sql(tableCreate(tableName))
    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()
    rows.foreach(row => assert(upsertDataOut.contains(row)))
  }

  "Native upsert only fields with  primary key statement" should "update color column" in new JdbcCommonsUpsertFields {
    val tableName = s"test_upsert_pk_fields_${System.currentTimeMillis()}"
    val connectionProperties = new JDBCOptions(postgresURL,
      tableName,
      properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.postgresql.Driver")
    )
    SpartaJdbcUtils.createTable(connectionProperties, upsertData, "upsertFieldsTable")
    val connection = SpartaJdbcUtils.getConnection(connectionProperties, "upsertFieldsTable")
    val dataInsert = Seq(
      new GenericRowWithSchema(Array(1, "text1", "red", 1L), schema),
      new GenericRowWithSchema(Array(2, "text2", "blue", 2L), schema),
      new GenericRowWithSchema(Array(3, "text3", "green", 3L), schema)
    )
    connection.prepareStatement(s"ALTER TABLE $tableName ADD CONSTRAINT constraint_$tableName PRIMARY KEY (id,text)").execute()
    dataInsert.foreach(row => {
      val sql = s"INSERT INTO $tableName(id,text,color,datetime) VALUES (${row.getInt(0)},'${row.getString(1)}','${row.getString(2)}',${row.getLong(3)})"
      connection.prepareStatement(sql).execute()
    })

    val primaryKey = "id,text"
    val postgresOutputStep = new PostgresOutputStep("postgresNativeUpsert", xdSession, properties
      ++ Map("postgresSaveMode" -> "STATEMENT", "failFast" -> "true", "dropTemporalTableSuccess" -> "true", "dropTemporalTableFailure" -> "true"))
    postgresOutputStep.save(upsertData, SaveModeEnum.Upsert, Map("tableName" -> tableName,
      "auto-commit" -> "false", "numPartitions" -> "1",
      "isolationLevel" -> "READ-COMMITED",
      "primaryKey" -> primaryKey,
      "updateFields" -> "id,text,color"))
    xdSession.sql(tableCreate(tableName))

    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()
    rows.foreach(row => assert(upsertFieldsDataOut.contains(row)))
  }

  "Native upsert with unique constraint statement" should "return the same records" in new JdbcCommons {
    val tableName = s"test_upsert_${System.currentTimeMillis()}"
    val uniqueConstraint = "id,text"
    val postgresOutputStep = new PostgresOutputStep("postgresNativeUpsert", xdSession, properties
      ++ Map("postgresSaveMode" -> "STATEMENT", "failFast" -> "true", "dropTemporalTableSuccess" -> "true", "dropTemporalTableFailure" -> "true"))
    postgresOutputStep.save(upsertData, SaveModeEnum.Upsert, Map("tableName" -> tableName,
      "auto-commit" -> "false", "numPartitions" -> "1",
      "isolationLevel" -> "READ-COMMITED",
      "uniqueConstraintName" -> s"uniqueConstraint_$tableName",
      "uniqueConstraintFields" -> uniqueConstraint))
    xdSession.sql(tableCreate(tableName))
    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()
    rows.foreach(row => assert(upsert.contains(row)))
  }

  "Native upsert only fields with unique constraint statement" should "update color column" in new JdbcCommonsUpsertFieldsCamelCase {
    val tableName = s"test_upsert_fields_${System.currentTimeMillis()}"
    val connectionProperties = new JDBCOptions(postgresURL,
      tableName,
      properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.postgresql.Driver")
    )
    SpartaJdbcUtils.createTable(connectionProperties, upsertData, "upsertFieldsTable")
    val connection = SpartaJdbcUtils.getConnection(connectionProperties, "upsertFieldsTable")
    val dataInsert = Seq(
      new GenericRowWithSchema(Array(1, "text1", "red", 1L), schema),
      new GenericRowWithSchema(Array(2, "text2", "blue", 2L), schema),
      new GenericRowWithSchema(Array(3, "text3", "green", 3L), schema)
    )
    val id = "\"Id\""
    connection.prepareStatement(s"ALTER TABLE $tableName ADD CONSTRAINT constraint_$tableName UNIQUE($id,text)").execute()
    dataInsert.foreach(row => {
      val sql = s"INSERT INTO $tableName($id,text,color,datetime) VALUES (${row.getInt(0)},'${row.getString(1)}','${row.getString(2)}',${row.getLong(3)})"
      connection.prepareStatement(sql).execute()
    })

    val uniqueConstraint = "Id,text"
    val postgresOutputStep = new PostgresOutputStep("postgresNativeUpsert", xdSession, properties
      ++ Map("postgresSaveMode" -> "STATEMENT", "failFast" -> "true", "dropTemporalTableSuccess" -> "true", "dropTemporalTableFailure" -> "true"))
    postgresOutputStep.save(upsertData, SaveModeEnum.Upsert, Map("tableName" -> tableName,
      "auto-commit" -> "false", "numPartitions" -> "1",
      "isolationLevel" -> "READ-COMMITED",
      "uniqueConstraintName" -> s"uniqueConstraint_$tableName",
      "uniqueConstraintFields" -> uniqueConstraint,
      "updateFields" -> "Id,text,color"))
    xdSession.sql(tableCreate(tableName))

    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()
    rows.foreach(row => assert(upsertFieldsDataOut.contains(row)))
  }


  private def jdbcOptions(tableName: String): JDBCOptions =
    new JDBCOptions(postgresURL,
      tableName,
      properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.postgresql.Driver")
    )

  "PostgreSQL output" should "insert or update rows using unique constraint statement" in new JdbcCommonsUpsertFieldsCamelCase {

    val tableName = s"testupsert_fields_${System.currentTimeMillis()}"
    val id = "\"Id\""
    val uniqueConstraintOption = "Id, text"
    val upsertFieldsOption = "color,  text,Id"
    val spartaOutputName = "upsertUniqueFieldsTable"
    val constraintName = s"constraint_$tableName"

    val connection: Connection = {
      val connectionProperties: JDBCOptions = jdbcOptions(tableName)
      SpartaJdbcUtils.createTable(connectionProperties, upsertData, spartaOutputName)
      SpartaJdbcUtils.getConnection(connectionProperties, spartaOutputName)
    }

    connection.prepareStatement(s"ALTER TABLE $tableName ADD CONSTRAINT $constraintName UNIQUE($id,text)").execute()

    //insert data
    {
      val dataInsert = Seq(
        new GenericRowWithSchema(Array(1, "text1", "red", 1L), schema),
        new GenericRowWithSchema(Array(2, "text2", "blue", 2L), schema),
        new GenericRowWithSchema(Array(3, "text3", "green", 3L), schema)
      )

      dataInsert.foreach(row => {
        val sql = s"INSERT INTO $tableName($id,text,color,datetime) VALUES (${row.getInt(0)},'${row.getString(1)}','${row.getString(2)}',${row.getLong(3)})"
        connection.prepareStatement(sql).execute()
      })
    }

    val postgresOutputStep = new PostgresOutputStep(spartaOutputName, xdSession, properties
      ++ Map("postgresSaveMode" -> "STATEMENT", "failFast" -> "true", "dropTemporalTableSuccess" -> "true", "dropTemporalTableFailure" -> "true"))

    postgresOutputStep.save(upsertWithConstraintData, SaveModeEnum.Upsert, Map("tableName" -> tableName,
      "auto-commit" -> "false", "numPartitions" -> "1",
      "isolationLevel" -> "READ-COMMITED",
      "uniqueConstraintName" -> constraintName,
      "uniqueConstraintFields" -> uniqueConstraintOption,
      "updateFields" -> upsertFieldsOption))

    xdSession.sql(tableCreate(tableName))
    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()

    rows should contain theSameElementsAs upsertWithConstraintDataOut
  }

  it should "insert in tables containing upper case letters" in new JdbcCommons {

    val randomLetters = (Random.alphanumeric take 5).dropWhile(!_.isLetter).mkString
    val tableNames = Seq("lowerorupper", "LOWERORUPPER", "LowerOrUpper").map(_ + randomLetters)

    tableNames.foreach{ tableName =>
      val postgresOutputStep = new PostgresOutputStep("postgresOutBatch", xdSession, properties ++ Map("caseSensitiveEnabled" -> "true", "postgresSaveMode" -> "STATEMENT"))
      postgresOutputStep.save(okData, SaveModeEnum.Append, Map("tableName" -> tableName))
    }

    tableNames.foreach{ tableName =>
      val crossdataTableName = (Random.alphanumeric take 5).dropWhile(!_.isLetter).mkString
      xdSession.sql(createTableCaseSensitive(crossdataTableName, tableName))
      xdSession.sql(s"SELECT * FROM $crossdataTableName").count() shouldBe 2
    }

  }


  "Native upsert with one transaction" should "fail with two records with same primary key in temporal table" in new JdbcCommons {
    val tableName = s"test_upsert_${System.currentTimeMillis()}"
    val connectionProperties = new JDBCOptions(postgresURL,
      tableName,
      properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.postgresql.Driver")
    )
    SpartaJdbcUtils.createTable(connectionProperties, upsertData, "upsertTable")
    val connection = SpartaJdbcUtils.getConnection(connectionProperties, "upsertTable")
    val primaryKey = "id"
    val postgresOutputStep = new PostgresOutputStep("postgresNativeUpsert", xdSession, properties
      ++ Map("postgresSaveMode" -> "ONE_TRANSACTION", "failFast" -> "true", "dropTemporalTableSuccess" -> "true", "dropTemporalTableFailure" -> "true"))

    assert(Try(postgresOutputStep.save(upsertData, SaveModeEnum.Upsert, Map("tableName" -> tableName,
      "auto-commit" -> "true", "numPartitions" -> "1",
      "isolationLevel" -> "READ-COMMITED",
      "primaryKey" -> primaryKey))).isFailure)
  }

  "Native upsert with composed primary key statement and OneTx" should "return the same records " in new JdbcCommons {
    val tableName = s"test_upsert_${System.currentTimeMillis()}"
    val uniqueConstraint = "id,text"
    val postgresOutputStep = new PostgresOutputStep("postgresNativeUpsert", xdSession, properties
      ++ Map("postgresSaveMode" -> "ONE_TRANSACTION", "failFast" -> "true", "dropTemporalTableSuccess" -> "true", "dropTemporalTableFailure" -> "true"))
    postgresOutputStep.save(upsertData, SaveModeEnum.Upsert, Map("tableName" -> tableName,
      "auto-commit" -> "false", "numPartitions" -> "1",
      "isolationLevel" -> "READ-COMMITED",
      "primaryKey" -> uniqueConstraint))
    xdSession.sql(tableCreate(tableName))
    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()
    rows.foreach(row => assert(upsert.contains(row)))
  }

  "Delete that no affects records" should "maintain record with id = 4 " in new JdbcCommons {
    val tableName = s"test_delete_${System.currentTimeMillis()}"
    val connectionProperties = new JDBCOptions(postgresURL,
      tableName,
      properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.postgresql.Driver")
    )
    SpartaJdbcUtils.createTable(connectionProperties, okData, "deleteTable")
    val connection = SpartaJdbcUtils.getConnection(connectionProperties, "deleteTable")
    val dataInsert = Seq(
      new GenericRowWithSchema(Array(4, "testNoDeleteRecord"), schema)
    )
    dataInsert.foreach(row => {
      val sql = s"INSERT INTO $tableName(id,text) VALUES (${row.getInt(0)},'${row.getString(1)}')"
      connection.prepareStatement(sql).execute()
    })
    val postgresOutputStep = new PostgresOutputStep("postgresDelete", xdSession, properties ++ Map("postgresSaveMode" -> "SINGLE_STATEMENT", "failFast" ->
      "true"))
    postgresOutputStep.save(okData, SaveModeEnum.Delete, Map("tableName" -> tableName, "primaryKey" -> "id", "auto-commit" -> "false", "isolationLevel" ->
      "READ-COMMITED"))
    xdSession.sql(tableCreate(tableName))
    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()
    rows.length shouldBe 1
    rows.foreach(row => assert(dataInsert.contains(row)))
    SpartaJdbcUtils.dropTable(connectionProperties, "deleteTable", Some(tableName))
  }

  "Delete despite duplicated id = 2" should "remove records when predicate is true on 'text' column" in new JdbcCommons {
    val tableName = s"test_delete_${System.currentTimeMillis()}"
    val connectionProperties = new JDBCOptions(postgresURL,
      tableName,
      properties.mapValues(_.toString).filter(_._2.nonEmpty) + ("driver" -> "org.postgresql.Driver")
    )
    SpartaJdbcUtils.createTable(connectionProperties, okData, "deleteTable")
    val connection = SpartaJdbcUtils.getConnection(connectionProperties, "deleteTable")
    val dataInsert = Seq(
      new GenericRowWithSchema(Array(1, "test1"), schema),
      new GenericRowWithSchema(Array(2, "test2"), schema),
      new GenericRowWithSchema(Array(2, "testPkDuplicate"), schema)
    )
    dataInsert.foreach(row => {
      val sql = s"INSERT INTO $tableName(id,text) VALUES (${row.getInt(0)},'${row.getString(1)}')"
      connection.prepareStatement(sql).execute()
    })
    val postgresOutputStep = new PostgresOutputStep("postgresDelete", xdSession, properties ++ Map("postgresSaveMode" -> "STATEMENT", "failFast" -> "true"))
    postgresOutputStep.save(okData, SaveModeEnum.Delete, Map("tableName" -> tableName, "primaryKey" -> "text", "auto-commit" -> "false", "isolationLevel" ->
      "READ-COMMITED"))
    xdSession.sql(tableCreate(tableName))
    val rows = xdSession.sql(s"SELECT * FROM $tableName").collect()
    rows.length shouldBe 1
    rows.foreach(row => assert(dataInsert.contains(row)))
    SpartaJdbcUtils.dropTable(connectionProperties, "deleteTable", Some(tableName))
  }

  "Savemode ignore" should "thrown an exception" in new JdbcCommons {
    val tableName = s"test_delete_${System.currentTimeMillis()}"
    val postgresOutputStep = new PostgresOutputStep("postgresIgnore", xdSession, properties ++ Map("postgresSaveMode" -> "STATEMENT", "failFast" -> "true"))
    an[RuntimeException] should be thrownBy
      postgresOutputStep.save(okData, SaveModeEnum.Ignore, Map("tableName" -> tableName, "primaryKey" -> "text", "auto-commit" -> "false",
        "isolationLevel" ->
          "READ-COMMITED"))
  }
}