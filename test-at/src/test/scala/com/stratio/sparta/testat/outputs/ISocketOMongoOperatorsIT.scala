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
import com.mongodb.casbah.{MongoClientURI, MongoCollection, MongoConnection}
import com.mongodb.{BasicDBList, BasicDBObject}
import com.stratio.sparta.testat.SpartaATSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._

import com.stratio.sparta.testat.SpartaATSuite

/**
 * Acceptance test:
 * [Input]: Socket.
 * [Output]: MongoDB.
 * [Operators]: accumulator, avg, count, firsValue, fullText, lastValue, max,
 * median, min, range, stddev, sum, variance.
 */
@RunWith(classOf[JUnitRunner])
class ISocketOMongoOperatorsIT extends MongoEmbedDatabase with SpartaATSuite {

  override val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data-operators.csv").getPath
  override val policyFile = "policies/ISocket-OMongo-operators.json"

  val TestMongoPort = 60000
  var mongoProps: MongodProps = _
  var mongoConnection: MongoConnection = _
  val NumEventsExpected = 8

  "Sparta" should {
    "starts and executes a policy that reads from a socket and writes in mongodb" in {
      spartaRunner
      checkMongoData("testCubeWithTime")
      checkMongoData("testCubeWithoutTime")
    }

    def checkMongoData(tableName: String): Unit = {
      val mongoColl: MongoCollection = mongoConnection("csvtest")(tableName)
      mongoColl.size should be(2)

      val productA = mongoColl.find(new BasicDBObject("product", "producta")).next()
      productA.get("acc_price").asInstanceOf[BasicDBList].toArray.toSeq should be(
        Seq("10", "500", "1000", "500", "1000", "500", "1002", "600"))
      productA.get("avg_price") should be(639.0d)
      productA.get("avg_associative_price").asInstanceOf[BasicDBObject].get("mean") should be(639.0d)
      productA.get("sum_price") should be(5112.0d)
      productA.get("count_price") should be(NumEventsExpected)
      productA.get("first_price") should be("10")
      productA.get("last_price") should be("600")
      productA.get("max_price") should be(1002.0d)
      productA.get("min_price") should be(10.0d)
      productA.get("mode_price").asInstanceOf[BasicDBList].toArray should be(Seq("500").toArray)
      productA.get("fulltext_price") should be("10 500 1000 500 1000 500 1002 600")
      productA.get("stddev_price") should be(347.9605889013459d)
      productA.get("variance_price") should be(121076.57142857143d)
      productA.get("range_price") should be(992.0d)
      mapAsScalaMap(productA.get("entityCount_text").asInstanceOf[BasicDBObject].toMap) should be(
        Map("hola" -> 16L, "holo" -> 8L))
      productA.get("totalEntity_text") should be(24)


      val productB = mongoColl.find(new BasicDBObject("product", "productb")).next()
      productB.get("acc_price").asInstanceOf[BasicDBList].toArray.toSeq should be(
        Seq("15", "1000", "1000", "1000", "1000", "1000", "1001", "50"))
      productB.get("avg_price") should be(758.25d)
      productA.get("avg_associative_price").asInstanceOf[BasicDBObject].get("mean") should be(639.0d)
      productB.get("sum_price") should be(6066.0d)
      productB.get("count_price") should be(NumEventsExpected)
      productB.get("first_price") should be("15")
      productB.get("last_price") should be("50")
      productB.get("max_price") should be(1001.0d)
      productB.get("min_price") should be(15.0d)
      productB.get("mode_price").asInstanceOf[BasicDBList].toArray should be(Seq("1000").toArray)
      productB.get("fulltext_price") should be("15 1000 1000 1000 1000 1000 1001 50")
      productB.get("stddev_price") should be(448.04041590655d)
      productB.get("variance_price") should be(200740.2142857143d)
      productB.get("range_price") should be(986.0d)
      mapAsScalaMap(productB.get("entityCount_text").asInstanceOf[BasicDBObject].toMap) should be(
        Map("hola" -> 16L, "holo" -> 8L))
      productB.get("totalEntity_text") should be(24)
    }
  }

  override def extraBefore: Unit = {
    mongoProps = mongoStart(TestMongoPort)
    mongoConnection = MongoConnection(Localhost, TestMongoPort)
  }

  override def extraAfter: Unit = {
    mongoConnection.close()
    mongoStop(mongoProps)
    deletePath(s"$CheckpointPath/${"ATSocketMongo".toLowerCase}")
  }
}
