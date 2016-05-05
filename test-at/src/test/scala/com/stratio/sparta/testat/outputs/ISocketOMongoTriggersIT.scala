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
package com.stratio.sparta.testat.outputs

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.mongodb.casbah.{MongoCollection, MongoConnection}
import com.mongodb.{BasicDBList, BasicDBObject}
import com.stratio.sparta.testat.SpartaATSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._

/**
 * Acceptance test:
 * [Input]: Socket.
 * [Output]: MongoDB.
 * [Operators]: count
 * [Triggers]:
 *  "select product from testCubeWithTime",
 *  "select * from stream",
 *  "select product from testCubeWithTime where count > 8"
 */
@RunWith(classOf[JUnitRunner])
class ISocketOMongoTriggersIT extends MongoEmbedDatabase with SpartaATSuite {

  override val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data-triggers.csv").getPath
  override val policyFile = "policies/ISocket-OMongo-Triggers.json"

  val TestMongoPort = 60000
  var mongoProps: MongodProps = _
  var mongoConnection: MongoConnection = _
  val InputNumEventsExpected = 17

  "Sparta" should {
    "starts and executes a policy that reads from a socket and writes in mongodb" in {
      spartaRunner
      checkMongoCubeData("testCubeWithTime")
      checkMongoStreamTriggerData("tableselect")
      checkMongoCubeTriggerData("tableproject")
      checkMongoCubeTriggerWhereData("tablewhere")
    }

    def checkMongoCubeData(tableName: String): Unit = {
      val mongoColl: MongoCollection = mongoConnection("streamtest")(tableName)
      mongoColl.size should be(2)

      val productA = mongoColl.find(new BasicDBObject("product", "producta")).next()
      productA.get("count") should be(8)

      val productB = mongoColl.find(new BasicDBObject("product", "productb")).next()
      productB.get("count") should be(9)
    }

    def checkMongoStreamTriggerData(tableName: String): Unit = {
      val mongoColl: MongoCollection = mongoConnection("streamtest")(tableName)
      mongoColl.size should be(InputNumEventsExpected)

      val productA = mongoColl.find(new BasicDBObject("product", "producta"))
      productA.size should be(8)

      val productB = mongoColl.find(new BasicDBObject("product", "productb"))
      productB.size should be(9)
    }

    def checkMongoCubeTriggerData(tableName: String): Unit = {
      val mongoColl: MongoCollection = mongoConnection("streamtest")(tableName)
      mongoColl.size should be(2)

      val productA = mongoColl.find(new BasicDBObject("product", "producta"))
      productA.size should be(1)

      val productB = mongoColl.find(new BasicDBObject("product", "productb"))
      productB.size should be(1)
    }

    def checkMongoCubeTriggerWhereData(tableName: String): Unit = {
      val mongoColl: MongoCollection = mongoConnection("streamtest")(tableName)
      mongoColl.size should be(1)

      val productA = mongoColl.find(new BasicDBObject("product", "producta"))
      productA.size should be(0)

      val productB = mongoColl.find(new BasicDBObject("product", "productb"))
      productB.size should be(1)
    }
  }

  override def extraBefore: Unit = {
    mongoProps = mongoStart(TestMongoPort)
    mongoConnection = MongoConnection(Localhost, TestMongoPort)
  }

  override def extraAfter: Unit = {
    mongoConnection.close()
    mongoStop(mongoProps)
    deletePath(s"$CheckpointPath/${"ATSocketMongoTriggers".toLowerCase}")
  }
}
