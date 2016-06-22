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


package com.stratio.sparta.testat.internal

import akka.util.Timeout
import com.stratio.sparta.testat.SpartaATSuite
import com.stratio.sparta.testat.embedded.{ElasticThread, ElasticsearchEmbeddedServer, JVMProcess}
import spray.client.pipelining._
import spray.http._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.parsing.json.JSON

/**
  * Test used in a internal project.
  * @author anistal
  */
class ISocketOElasticsearchJsonAT extends SpartaATSuite {

  val policyFile = "policies/ISocket-OElasticsearchJSON.json"
  override val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-json-data").getPath

  "Sparta" should {
    "starts and executes a policy that reads from a socket and writes in ElasticSearch" in {
      spartaRunner
      checkData
    }
  }

  def checkData: Unit = {
    numberOfEventsGrouped(indexName = "id_smfprocess_minute",
      mappingName = "day_v1",
      field = "id",
      value = "P0001_1435139880000") should be(2d)

    numberOfEventsGrouped(indexName = "id_minute",
      mappingName = "day_v1",
      field = "id",
      value = "1435139880000") should be(2d)
  }

  private def numberOfEventsGrouped(indexName: String, mappingName: String, field: String, value: Any): Double = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val productArequest: Future[HttpResponse] =
      pipeline(Get(s"http://${Localhost}:9200/$indexName/$mappingName/_search?q=*:*"))

    val response: HttpResponse = Await.result(productArequest, Timeout(5.seconds).duration)

    JSON.globalNumberParser = { input: String => input.toDouble }
    val json = JSON.parseFull(response.entity.data.asString)
    val rows = json.get.asInstanceOf[Map[String, Any]]
      .get("hits").get.asInstanceOf[Map[String, Any]]
      .get("hits").get.asInstanceOf[List[Map[String, Any]]]

    rows.filter(tuple =>
      tuple.get("_source").get.asInstanceOf[Map[String, Any]].get(field).get == value).head.get("_source").get
      .asInstanceOf[Map[String, String]].get("countoperator").get.asInstanceOf[Double]
  }

  override def extraBefore: Unit = JVMProcess.runMain(ElasticThread.getClass.getCanonicalName.dropRight(1), false)

  override def extraAfter: Unit = {
    JVMProcess.shutdown()
    ElasticsearchEmbeddedServer.cleanData
    deletePath(s"$CheckpointPath/${"ATSocketElasticsearch".toLowerCase}")
  }
}
