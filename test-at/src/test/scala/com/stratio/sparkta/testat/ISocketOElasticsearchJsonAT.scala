/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.testat

import akka.util.Timeout
import com.stratio.sparkta.testat.embedded.{ElasticThread, ElasticsearchEmbeddedServer, JVMProcess}
import spray.client.pipelining._
import spray.http._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.parsing.json.JSON

/**
 * Test used in a internal project.
 * @author anistal
 */
class ISocketOElasticsearchJsonAT extends SparktaATSuite {

  val PathToPolicy = getClass.getClassLoader.getResource("policies/ISocket-OElasticsearchJSON.json").getPath
  val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-json-data").getPath
  val TimeElastisearchStarts: Long = 5000
  val PolicyEndSleep = 30000
  val ProductAAvg: Double = 750d
  val ProductASum: Double = 6000d
  val ProductBAvg: Double = 1000d
  val ProductBSum: Double = 8000d


  before {
    zookeeperStart
    socketStart
    JVMProcess.runMain(ElasticThread.getClass.getCanonicalName.dropRight(1), false)
  }

  after {
    serverSocket.close()
    zkTestServer.stop()
    JVMProcess.shutdown()
    ElasticsearchEmbeddedServer.cleanData
  }

  "Sparkta" should {
    "starts and executes a policy that reads from a socket and writes in ElasticSearch" in {
      sleep(TimeElastisearchStarts)
      startSparkta
      sendPolicy(PathToPolicy)
      sendDataToSparkta(PathToCsv)
      sleep(PolicyEndSleep)
      checkData
    }
  }

  def checkData: Unit = {
    query("id_smfprocess_minute", "id","P0001_2015-06-24 11:58:00.0").head.get("_source").get
      .asInstanceOf[Map[String,String]].get("count").get.asInstanceOf[Double] should be (2d)
    query("id_minute", "id", "2015-06-24 11:58:00.0").head.get("_source").get
      .asInstanceOf[Map[String,String]].get("count").get.asInstanceOf[Double] should be (2d)
  }

  def query(index: String, field: String, value: Any): List[Map[String, Any]] = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val productArequest: Future[HttpResponse] =
      pipeline(Get(s"http://${Localhost}:9200/$index/_search?q=*:*"))

    val response: HttpResponse = Await.result(productArequest, Timeout(5.seconds).duration)

    JSON.globalNumberParser = { input: String => input.toDouble }
    val json = JSON.parseFull(response.entity.data.asString)
    val rows = json.get.asInstanceOf[Map[String, Any]]
      .get("hits").get.asInstanceOf[Map[String, Any]]
      .get("hits").get.asInstanceOf[List[Map[String, Any]]]
    
    rows.filter(tuple =>
        tuple.get("_source").get.asInstanceOf[Map[String, Any]].get(field).get == value)
  }
}
