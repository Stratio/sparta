/**
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
/**
  * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparta.testat.outputs

import akka.util.Timeout
import com.stratio.sparta.testat.SpartaATSuite
import com.stratio.sparta.testat.embedded._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.parsing.json.JSON

/**
  * Acceptance test:
  * [Input]: Socket.
  * [Output]: Elasticsearcg.
  * [Operators]: accumulator, avg, count, firsValue, fullText, lastValue, max,
  * median, min, range, stddev, sum, variance.
  */
@RunWith(classOf[JUnitRunner])
class ISocketOElasticsearchOperatorsIT extends SpartaATSuite {

  override val policyFile = "policies/ISocket-OElasticsearch-operators.json"
  override val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data-operators.csv").getPath

  val NumExecutors = 4
  val TimeElastisearchStarts: Long = 5000
  val NumEventsExpected: Int = 8

  "Sparta" should {
    "starts and executes a policy that reads from a socket and writes in Elasticsearch" in {
      spartaRunner
      checkData("testcubewithtime")
      checkData("testcubewithouttime")
    }

    def checkData(tableName: String): Unit = {
      val productA = getData("producta", tableName)
      productA("acc_price") should be(
        Seq("10", "500", "1000", "500", "1000", "500", "1002", "600"))
      productA("avg_price") should be(639.0d)
      productA("avg_associative_price").asInstanceOf[Map[String, Double]].get("mean").get should be(639.0d)
      productA("sum_price") should be(5112.0d)
      productA("count_price") should be(NumEventsExpected)
      productA("first_price") should be("10")
      productA("last_price") should be("600")
      productA("max_price") should be(1002.0d)
      productA("min_price") should be(10.0d)
      productA("mode_price") should be(List("500"))
      productA("fulltext_price") should be("10 500 1000 500 1000 500 1002 600")
      productA("stddev_price") should be(347.9605889013459d)
      productA("variance_price") should be(121076.57142857143d)
      productA("range_price") should be(992.0d)
      productA("entityCount_text") should be(Map("hola" -> 16L, "holo" -> 8L))
      productA("totalEntity_text") should be(24)

      val productB = getData("productb", tableName)
      productB("acc_price") should be(
        Seq("15", "1000", "1000", "1000", "1000", "1000", "1001", "50"))
      productB("avg_price") should be(758.25d)
      productB("avg_associative_price").asInstanceOf[Map[String, Double]].get("mean").get should be(758.25d)
      productB("sum_price") should be(6066.0d)
      productB("count_price") should be(NumEventsExpected)
      productB("first_price") should be("15")
      productB("last_price") should be("50")
      productB("max_price") should be(1001.0d)
      productB("min_price") should be(15.0d)
      productB("mode_price") should be(List("1000"))
      productB("fulltext_price") should be("15 1000 1000 1000 1000 1000 1001 50")
      productB("stddev_price") should be(448.04041590655d)
      productB("variance_price") should be(200740.2142857143d)
      productB("range_price") should be(986.0d)
      productB("entityCount_text") should be(Map("hola" -> 16L, "holo" -> 8L))
      productB("totalEntity_text") should be(24)
    }

    def getData(productName: String, tableName: String): Map[String, Any] = {
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
      val productArequest: Future[HttpResponse] =
        pipeline(Get(s"http://$Localhost:9200/$tableName/day_v1/_search?q=product:$productName"))
      val response: HttpResponse = Await.result(productArequest, Timeout(5.seconds).duration)
      JSON.globalNumberParser = { input: String => input.toDouble }
      val json = JSON.parseFull(response.entity.data.asString)
      val rows = json.get.asInstanceOf[Map[String, Any]]
        .get("hits").get.asInstanceOf[Map[String, Any]]
        .get("hits").get.asInstanceOf[List[Map[String, Any]]]
      rows.map(tuple => tuple("_source").asInstanceOf[Map[String, Any]]).head
    }
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
    deletePath(s"$CheckpointPath/${"ATSocketElasticsearch".toLowerCase}")
  }
}
