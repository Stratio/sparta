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

package com.stratio.sparkta.testat.outputs

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.parsing.json.JSON

import akka.util.Timeout
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import spray.client.pipelining._
import spray.http._

import com.stratio.sparkta.testat.SparktaATSuite
import com.stratio.sparkta.testat.embedded.{ElasticThread, ElasticsearchEmbeddedServer, JVMProcess}

@RunWith(classOf[JUnitRunner])
class ISocketOElasticsearchIT extends SparktaATSuite {

  val policyFile = "policies/ISocket-OElasticsearch.json"
  val TimeElastisearchStarts: Long = 5000
  override val PolicyEndSleep = 60000
  val ProductAAvg: Double = 750d
  val ProductASum: Double = 6000d
  val ProductBAvg: Double = 1000d
  val ProductBSum: Double = 8000d

  "Sparkta" should {
    "starts and executes a policy that reads from a socket and writes in ElasticSearch" in {
      sparktaRunner
      checkESData
    }
  }

  def checkESData: Unit = {

    val hitsA: List[Map[String, Any]] = rowsByProductFilteredByAvgAndSum("producta", ProductAAvg, ProductASum)

    hitsA.size should be(1)

    val hitsB = rowsByProductFilteredByAvgAndSum("productb", ProductBAvg, ProductBSum)

    hitsB.size should be(1)
  }

  def rowsByProductFilteredByAvgAndSum(productName: String, avg: Double, sum: Double): List[Map[String, Any]] = {

    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val productArequest: Future[HttpResponse] =
      pipeline(Get(s"http://${Localhost}:9200/id_product_minute/_search?q=product:${productName}"))

    val response: HttpResponse = Await.result(productArequest, Timeout(5.seconds).duration)

    JSON.globalNumberParser = { input: String => input.toDouble }
    val json = JSON.parseFull(response.entity.data.asString)
    val rows = json.get.asInstanceOf[Map[String, Any]]
      .get("hits").get.asInstanceOf[Map[String, Any]]
      .get("hits").get.asInstanceOf[List[Map[String, Any]]]

    rows.filter(tuple =>
      tuple.get("_source").get.asInstanceOf[Map[String, Any]].get("avg_price").get == avg &&
        tuple.get("_source").get.asInstanceOf[Map[String, Any]].get("sum_price").get == sum)
  }

  /**
   * Running the embedded server in the test fails when writing data.
   * This is the reason why we need to run it in a different process.
   * We have already tested it making ElasticsearchEmbeddedServer implements Runnable and running it in a thread but
   * no success...
   */
  override def extraBefore: Unit = JVMProcess.runMain(ElasticThread.getClass.getCanonicalName.dropRight(1), false)

  override def extraAfter: Unit = {
    JVMProcess.shutdown()
    ElasticsearchEmbeddedServer.cleanData
  }
}