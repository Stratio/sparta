/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.elasticsearch

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.workflow.step.OutputStep._
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import org.apache.spark.sql.crossdata.XDSession

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class ElasticSearchStepOutputIT extends TemporalSparkContext
  with ShouldMatchers
  with BeforeAndAfterAll {

  self: FlatSpec =>

  //Gestion del elastic
  private lazy val config = ConfigFactory.load()
  val esHost = Try(config.getString("es.host")) match {
    case Success(configHost) =>
      log.info(s"Elasticsearch from config: $configHost")
      s"$configHost"
    case Failure(e) =>
      log.info(s"Elasticsearch from default")
      "localhost"
  }

  trait CommonValues {
    val xdSession = XDSession.builder().config(sc.getConf).create("dummyUser")

    import xdSession.implicits._

    val time = DateTime.now.getMillis
    val data = sc.parallelize(
      Seq(Person("Marcos", 18, time), Person("Juan", 21, time), Person("Jose", 26, time)))
      .toDS().toDF
  }

  trait WithEventData extends CommonValues {
    val properties = Map("nodes" -> JsoneyString(
      s"""
         |[{
         |  "node":"$esHost",
         |  "httpPort":"9200",
         |  "tcpPort":"9300"
         |}]
           """.stripMargin),
      "indexMapping" -> "people"
    )

    val elasticOutput = new ElasticSearchOutputStep("ES.out", sparkSession, properties)
  }

  "ElasticSearch" should "save a dataFrame and read the same information" in new WithEventData {
    elasticOutput.save(data, SaveModeEnum.Upsert, Map(
      TableNameKey -> "sparta",
      PrimaryKey -> "age"
    ))

    val loadData = xdSession.read.format(elasticOutput.ElasticSearchClass)
      .options(Map("es.nodes" -> s"$esHost", "es.port" -> "9200")).load("sparta")
    loadData.count() should be (3)
  }
  
}

case class Person(name: String, age: Int, minute: Long) extends Serializable