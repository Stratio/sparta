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
package com.stratio.sparta.plugin.workflow.output.elasticsearch

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
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
      "indexMapping" -> "people",
      "timeStampMapperFormat" -> "minute"
    )

    val elasticOutput = new ElasticSearchOutputStep("ES.out", sparkSession, properties)
  }

  "ElasticSearch" should "save a dataFrame and read the same information" in new WithEventData {
    elasticOutput.save(data, SaveModeEnum.Upsert, Map(
      elasticOutput.TableNameKey -> "sparta",
      elasticOutput.PrimaryKey -> "age"
    ))

    val loadData = xdSession.read.format(elasticOutput.ElasticSearchClass)
      .options(Map("es.nodes" -> s"$esHost", "es.port" -> "9200")).load("sparta")
    loadData.count() should be (3)
  }
  
}

case class Person(name: String, age: Int, minute: Long) extends Serializable