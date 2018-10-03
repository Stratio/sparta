/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.elasticsearch

import java.sql.Timestamp
import java.time.Instant

import com.github.nscala_time.time.Imports.DateTime
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.core.workflow.step.OutputStep._
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.output.elasticsearch.ElasticSearchOutputStep
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SparkSession}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, _}

import scala.util.{Failure, Random, Success, Try}

@RunWith(classOf[JUnitRunner])
class ElasticSearchInputStepIT extends TemporalSparkContext with ShouldMatchers with BeforeAndAfterAll {

  self: FlatSpec =>

  val config = ConfigFactory.load()

  val esHost = {
    Try(config.getString("es.host")) match {
      case Success(configHost) =>
        log.info(s"Elasticsearch host retrieved from config: $configHost")
        s"$configHost"
      case Failure(_) =>
        log.info(s"Elasticsearch host set to default value: localhost")
        "localhost"
    }
  }
  val time = DateTime.now.getMillis
  val schema = StructType(Seq(
    StructField("name", StringType),
    StructField("age", IntegerType),
    StructField("date", LongType)
  ))


  val outputProperties = Map("nodes" -> JsoneyString(
    s"""
       |[{
       |  "node":"$esHost",
       |  "httpPort":"9200",
       |  "tcpPort":"9300"
       |}]
           """.stripMargin),
    "indexMapping" -> "people"
  )

  "ElasticSearch" should "read a dataFrame" in {

    SparkSession.clearActiveSession()

    val data = sparkSession.createDataFrame(sc.parallelize(Seq(
        Row("Kevin", Random.nextInt, Timestamp.from(Instant.now).getTime),
        Row("Kira", Random.nextInt, Timestamp.from(Instant.now).getTime),
        Row("Ariadne", Random.nextInt, Timestamp.from(Instant.now).getTime)
      )), schema)

    val elasticOutput = new ElasticSearchOutputStep("ES.out", sparkSession, outputProperties)
    elasticOutput.save(data, SaveModeEnum.Upsert, Map(
      TableNameKey -> "esinput",
      PrimaryKey -> "age"
    ))

    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val properties = Map("nodes" -> JsoneyString(
      s"""
         |[{
         |  "node":"$esHost",
         |  "httpPort":"9200",
         |  "tcpPort":"9300"
         |}]
           """.stripMargin),
      "resource" -> "esinput"
    )
    val elasticInput = new ElasticSearchInputStepBatch("ES-out", outputOptions, Option(ssc), sparkSession, properties)

    elasticInput.initWithSchema()._1.ds.count() should be(3)
  }

}