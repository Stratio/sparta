/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.sql

import java.io.{Serializable => JSerializable}
import java.util.{Properties, UUID}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.models.OutputWriterOptions
import com.stratio.sparta.plugin.common.postgresql.PostgresSuiteBase
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JdbcUtils}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class SQLInputStepStreamingIT extends TemporalSparkContext with PostgresSuiteBase with Matchers {


  "SQLInput " should "read all the records in one streaming batch" in {
    SparkSession.clearActiveSession()
    val schema = new StructType(Array(
      StructField("id", IntegerType, nullable = true),
      StructField("id2", IntegerType, nullable = true)
    ))
    val tableName = "tableName"
    val totalRegisters = 1000
    val registers = for (a <- 1 to totalRegisters) yield Row(a,a)
    val rdd = sc.parallelize(registers)

    sparkSession.createDataFrame(rdd, schema).createOrReplaceTempView(tableName)

    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    val offsetFields =
      """[
        |{
        |"offsetField":"id",
        |"offsetOperator":">",
        |"offsetValue": "500"
        |},
        |{
        |"offsetField":"id2",
        |"offsetOperator":">",
        |"offsetValue": "750"
        |}
        |]
      """.stripMargin

    val datasourceParams = Map(
      "query" -> s"select * from $tableName",
      "rememberDuration" -> "20000",
      "offsetFields" -> offsetFields.asInstanceOf[JSerializable]
    )

    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
    val sqlInput = new SQLInputStepStreaming("sqlInput", outputOptions, Option(ssc), sparkSession, datasourceParams)
    val inputStream = sqlInput.init

    inputStream.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty())
        assert(streamingRegisters === registers.reverse)
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === (totalRegisters.toLong/4))
  }



  it should "read postgres records using an autoincremental column" in {


    SparkSession.clearActiveSession()

    val tableName = (Random.alphanumeric take 10).dropWhile(!_.isLetter).mkString
    val testView = "testview"
    val totalRecords = 40

    withConnectionExecute(
      Seq(
        s"CREATE TABLE $tableName (id SERIAL, age INT);"
      ) ++  (1 to totalRecords).map( age => s"INSERT INTO $tableName (age) VALUES ($age);"): _*
    )

    val offsetFields =
      """[
        |{
        |"offsetField":"id",
        |"offsetOperator":">=",
        |"offsetValue": "0"
        |}
        |]
      """.stripMargin
    val offsetLimit = 20

    val postgresTableOptions =
      Map("url" -> postgresURL, "dbtable" -> s"public.$tableName", "user" -> "postgres", "driver" -> "org.postgresql.Driver", "stratiosecurity" -> "false")

    withCrossdataTable(testView, "jdbc", postgresTableOptions, sparkSession){
      val totalEvents = ssc.sparkContext.longAccumulator("Number of events received")

      val sqlInput = {
        val datasourceParams: Map[String, JSerializable] = Map(
          "query" -> s"select * from $testView",
          "limitRecords" -> offsetLimit,
          "offsetFields" -> offsetFields
        )

        new SQLInputStepStreaming("sqlInput",
          OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName")), Option(ssc), sparkSession, datasourceParams)
      }

      val inputStream = sqlInput.init

      inputStream.ds.foreachRDD(rdd => {
        val streamingEvents = rdd.count()
        log.info(s" EVENTS COUNT : \t $streamingEvents")
        totalEvents.add(streamingEvents)
        log.info(s" TOTAL EVENTS : \t $totalEvents")
        if (streamingEvents != 0){
          streamingEvents shouldBe offsetLimit
        }
      })

      ssc.start()
      ssc.awaitTerminationOrTimeout(2000L)
      ssc.stop()

      totalEvents.value.intValue() shouldBe totalRecords
    }


  }



}

