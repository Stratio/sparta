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

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.mongodb.casbah.{MongoClientURI, MongoCollection, MongoConnection}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.testat.SparktaATSuite

/**
 * Acceptance test:
 * [Input]: Socket.
 * [Output]: MongoDB.
 * [Operators]: sum, avg.
 * @author arincon
 */
@RunWith(classOf[JUnitRunner])
class ISocketOMongoIT extends MongoEmbedDatabase with SparktaATSuite {

  override val PolicyEndSleep = 60000
  val TestMongoPort = 60000
  val DatabaseName = "csvtest"
  val CollectionName = "product_minute"
  override val policyFile = "policies/ISocket-OMongo.json"
  var mongoProps: MongodProps = _

  "Sparkta" should {
    "starts and executes a policy that reads from a socket and writes in mongodb" in {
      sparktaRunner
      checkMongoData
    }

    def checkMongoData(): Unit = {
      val mongoConnection = getMongoConnection()

      mongoConnection.size should be(2)
      getMongoConnection().find().map(dbObject => {
        dbObject.get("id") match {
          case "producta" => {
            dbObject.get("avg_price") should be(750.0d)
            dbObject.get("sum_price") should be(6000.0d)
          }
          case "productb" => {
            dbObject.get("avg_price") should be(1000.0d)
            dbObject.get("sum_price") should be(8000.0d)
          }
          case _ => require(false)
        }
      })
    }

    def getMongoConnection(): MongoCollection = {
      MongoConnection(Localhost, TestMongoPort)(DatabaseName)(CollectionName)
    }

    def checkMongoDb: Unit = {
      val mongoClientURI = MongoClientURI(s"mongodb://$Localhost:$TestMongoPort/local")
      mongoClientURI.database should be(Some("local"))
    }
  }

  override def extraBefore: Unit = mongoProps = mongoStart(TestMongoPort)

  override def extraAfter: Unit = mongoStop(mongoProps)
}
