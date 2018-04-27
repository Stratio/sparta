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
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum

//scalastyle:off
@RunWith(classOf[JUnitRunner])
class PostgresOutputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  private lazy val config = ConfigFactory.load()

  val host = Try(config.getString("postgresql.host")) match {
    case Success(configHost) =>
      val hostUrl =s"jdbc:postgresql://$configHost:5432/postgres?user=postgres"
      log.info(s"Postgres host from config: $hostUrl")
      hostUrl
    case Failure(e) =>
      log.info(s"Postgres host from default")
      "jdbc:postgresql://172.17.0.2:5432/postgres?user=postgres&password=1234"
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
    val dataFail = Seq(
      new GenericRowWithSchema(Array(1, "test1"), schema),
      new GenericRowWithSchema(Array(1, "test1"), schema)
    )
    val rdd = sc.parallelize(data).asInstanceOf[RDD[Row]]
    val okData = xdSession.createDataFrame(rdd, schema)
    val rddFail = sc.parallelize(dataFail).asInstanceOf[RDD[Row]]
    val duplicateData = xdSession.createDataFrame(rddFail, schema)
  }

  "Tx batch statement without duplicate data" should "insert allrecords" in new JdbcCommons {

    val tableName = s"test_batch_${System.currentTimeMillis()}"
    val postgresOutputStep = new PostgresOutputStep("postgresOutBatch", xdSession, properties ++ Map("postgresSaveMode" -> "STATEMENT"))
    postgresOutputStep.save(okData, SaveModeEnum.Append, Map[String, String]("tableName" -> tableName))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }

  "Tx batch statement with duplicate data" should "insert only one record and fail on duplicate key" in new JdbcCommons {
    val tableName = s"test_batch_fail_${System.currentTimeMillis()}"
    val postgresOutputStep = new PostgresOutputStep("postgresOutBatch", xdSession, properties
      ++ Map("postgresSaveMode" -> "STATEMENT"))
    postgresOutputStep.save(duplicateData, SaveModeEnum.Upsert, Map("tableName" -> tableName, "primaryKey" -> "id", "dropTemporalTableFailure" -> "true"))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 1
  }

  "Tx one by one statement with failFast in false" should "insert record by record and not fail" in new JdbcCommons {
    val tableName = s"test_onebyone_${System.currentTimeMillis()}"
    val postgresOutputStep = new PostgresOutputStep("postgresOutOne", xdSession, properties
      ++ Map("postgresSaveMode" -> "SINGLE_STATEMENT", "failFast" -> "false", "dropTemporalTableSuccess" -> "true"))
    postgresOutputStep.save(okData, SaveModeEnum.Append, Map("tableName" -> tableName,
      "auto-commit" -> "false",
      "isolationLevel" -> "READ-COMMITED"))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }

  "Tx single statement with dropTemporalTable false" should "insert data and keep temporal table" in new JdbcCommons {
    val tableName = s"test_single_${System.currentTimeMillis()}"
    val postgresOutputStep = new PostgresOutputStep("postgresOutSingle", xdSession, properties
      ++ Map("postgresSaveMode" -> "ONE_TRANSACTION", "failFast" -> "true", "dropTemporalTableSuccess" -> "true"))
    postgresOutputStep.save(okData, SaveModeEnum.Append, Map("tableName" -> tableName,
      "auto-commit" -> "false",
      "isolationLevel" -> "READ-COMMITED"))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }

  "CopyIn" should "insert data in table" in new JdbcCommons {
    val tableName = s"test_copy_${System.currentTimeMillis()}"
    val postgresOutputStep = new PostgresOutputStep("postgresOutSingle", xdSession, properties
      ++ Map("postgresSaveMode" -> "COPYIN", "failFast" -> "true", "dropTemporalTableSuccess" -> "true"))
    postgresOutputStep.save(okData, SaveModeEnum.Append, Map("tableName" -> tableName,
      "auto-commit" -> "true",
      "isolationLevel" -> "READ-COMMITED"))
    xdSession.sql(tableCreate(tableName))
    xdSession.sql(s"SELECT * FROM $tableName").count() shouldBe 2
  }
}

