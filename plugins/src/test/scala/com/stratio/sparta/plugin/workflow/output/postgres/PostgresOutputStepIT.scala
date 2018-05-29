/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.output.postgres

import java.io.{Serializable => JSerializable}
import scala.util.{Failure, Success, Try}

import com.typesafe.config.ConfigFactory
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.jdbc.SpartaJdbcUtils
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.enumerators.SaveModeEnum

//scalastyle:off
@RunWith(classOf[JUnitRunner])
class PostgresOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  private lazy val config = ConfigFactory.load()

  val host = Try(config.getString("postgresql.host")) match {
    case Success(configHost) =>
      val hostUrl = s"jdbc:postgresql://$configHost:5432/postgres?user=postgres"
      log.info(s"Postgres host from config: $hostUrl")
      hostUrl
    case Failure(e) =>
      log.info(s"Postgres host from default")
      "jdbc:postgresql://127.0.0.1:5432/postgres?user=postgres"
  }

  val properties: Map[String, JSerializable] = Map("url" -> host)

  Class.forName("org.postgresql.Driver")

  trait JdbcCommons {

    def createTable()(table: String) = s"CREATE TABLE $table USING org.apache.spark.sql.jdbc OPTIONS (url '$host', dbtable '$table', driver 'org.postgresql.Driver')"

    val tableCreate = createTable() _

    var xdSession = sparkSession
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

  "Native upsert with unique constraint statement" should "return the same records " in new JdbcCommons {
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

  "Native upsert with one transaction" should "fail with two records with same primary key in temporal table" in new JdbcCommons {
    val tableName = s"test_upsert_${System.currentTimeMillis()}"
    val connectionProperties = new JDBCOptions(host,
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
    val connectionProperties = new JDBCOptions(host,
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
    val connectionProperties = new JDBCOptions(host,
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

