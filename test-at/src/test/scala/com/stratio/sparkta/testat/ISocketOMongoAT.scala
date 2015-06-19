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
import com.mongodb.casbah.{MongoClientURI, MongoCollection, MongoConnection}

/**
 * Acceptance test:
 *   [Input]: Socket.
 *   [Output]: MongoDB.
 *   [Operators]: sum, avg.
 * @author arincon
 */
class ISocketOMongoAT extends MongoEmbedDatabase with SparktaATSuite {

  val PolicyEndSleep = 60000
  val TestMongoPort = 60000
  val PathToPolicy = getClass.getClassLoader.getResource("policies/ISocket-OMongo.json").getPath
  val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data.csv").getPath
  var mongoProps: MongodProps = _

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

      val result = mongoColl.find()

      val productA = result.filter(dbject => {
        dbject.get("id") == "producta"
      }).toSeq.head

      val productB = result.filter(dbject => {
        dbject.get("id") == "productb"
      }).toSeq.head

      productA.get("avg_price") should be(750.0d)
      productA.get("sum_price") should be(6000.0d)

      productB.get("avg_price") should be(1000.0d)
      productB.get("sum_price") should be(8000.0d)
    }

    def checkMongoDb: Unit = {
      val mongoClientURI = MongoClientURI(s"mongodb://$Localhost:$TestMongoPort/local")
      mongoClientURI.database should be(Some("local"))
    }
  }
}
