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

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.mongodb.{BasicDBList, BasicDBObject}
import com.mongodb.casbah.{MongoClientURI, MongoCollection, MongoConnection}

/**
  * Acceptance test:
 *   [Input]: Socket.
 *   [Output]: MongoDB.
 *   [Operators]: accumulator, avg, count, firsValue, fullText, lastValue, max,
 *                median, min, range, stddev, sum, variance.
 */
class ISocketOMongoOperatorsAT extends MongoEmbedDatabase with SparktaATSuite {
  val PolicyEndSleep = 60000

  val TestMongoPort = 60000
  val PathToPolicy = getClass.getClassLoader.getResource("policies/ISocket-OMongo-operators.json").getPath
  val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data-operators.csv").getPath
  var mongoProps: MongodProps = _

  val NumEventsExpected: Int = 8

  before {
    zookeeperStart
    socketStart
    mongoProps = mongoStart(TestMongoPort)
  }

  after {
    serverSocket.close()
    zkTestServer.stop()
    mongoStop(mongoProps)
  }

  "Sparkta" should {
    "starts and executes a policy that reads from a socket and writes in mongodb" in {
      checkMongoDb
      startSparkta
      sendPolicy(PathToPolicy)
      sendDataToSparkta(PathToCsv)
      sleep(PolicyEndSleep)
      checkMongoData
    }

    def checkMongoData(): Unit = {
      val mongoColl: MongoCollection = MongoConnection(Localhost, TestMongoPort)("csvtest")("product")
      mongoColl.size should be(2)

      val productA = mongoColl.find(new BasicDBObject("id", "producta")).next()
      productA.get("acc_price").asInstanceOf[BasicDBList].toArray.toSeq should be(
        Seq("10", "500", "1000", "500", "1000", "500", "1002", "600"))
      productA.get("avg_price") should be(639.0d)
      productA.get("sum_price") should be(5112.0d)
      productA.get("count") should be(NumEventsExpected)
      productA.get("first_price") should be("10")
      productA.get("last_price") should be("600")
      productA.get("max_price") should be(1002.0d)
      productA.get("min_price") should be(10.0d)
      productA.get("fulltext_price") should be("10 500 1000 500 1000 500 1002 600")
      productA.get("stddev_price") should be(347.9605889013459d)
      productA.get("variance_price") should be(121076.57142857143d)
      productA.get("range_price") should be(992.0d)


      val productB = mongoColl.find(new BasicDBObject("id", "productb")).next()
      productB.get("acc_price").asInstanceOf[BasicDBList].toArray.toSeq should be(
        Seq("15", "1000", "1000", "1000", "1000", "1000", "1001", "50"))
      productB.get("avg_price") should be(758.25d)
      productB.get("sum_price") should be(6066.0d)
      productB.get("count") should be(NumEventsExpected)
      productB.get("first_price") should be("15")
      productB.get("last_price") should be("50")
      productB.get("max_price") should be(1001.0d)
      productB.get("min_price") should be(15.0d)
      productB.get("fulltext_price") should be("15 1000 1000 1000 1000 1000 1001 50")
      productB.get("stddev_price") should be(448.04041590655d)
      productB.get("variance_price") should be(200740.2142857143d)
      productB.get("range_price") should be(986.0d)
    }

    def checkMongoDb: Unit = {
      val mongoClientURI = MongoClientURI(s"mongodb://$Localhost:$TestMongoPort/local")
      mongoClientURI.database should be(Some("local"))
    }
  }
}
