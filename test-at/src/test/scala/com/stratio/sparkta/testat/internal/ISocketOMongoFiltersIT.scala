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

package com.stratio.sparkta.testat.internal

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.mongodb.casbah.{MongoClientURI, MongoCollection, MongoConnection}
import com.mongodb.{BasicDBList, BasicDBObject}
import com.stratio.sparkta.testat.SparktaATSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._

/**
 * Acceptance test:
 * [Input]: Socket.
 * [Output]: MongoDB.
 * [Operators]: count
 */
@RunWith(classOf[JUnitRunner])
class ISocketOMongoFiltersIT extends MongoEmbedDatabase with SparktaATSuite {

  override val PolicyEndSleep = 60000
  override val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data-filters.csv").getPath
  override val policyFile = "policies/ISocket-OMongo-filters.json"
  val TestMongoPort = 60000
  var mongoProps: MongodProps = _

  val NumEventsExpected: Int = 8

  "Sparkta" should {
    "starts and executes a policy that reads from a socket and writes in mongodb with filters in operators" in {
      sparktaRunner
      checkMongoData
    }

    def checkMongoData(): Unit = {
      val mongoColl: MongoCollection =
        MongoConnection(Localhost, TestMongoPort)("csvtest")("id_hashtag_location_province_time")

      val hashtag = mongoColl.find(new BasicDBObject("hashtag", "sparktaHashtag")).next()
      hashtag.get("count1") should be(1)

      mongoColl.size should be(2)

      val mongoColl2: MongoCollection =
        MongoConnection(Localhost, TestMongoPort)("csvtest")("id_idTweet_location_province_text_time")

      val favorited = mongoColl2.find(new BasicDBObject("idTweet", "1")).next()
      favorited.get("sum1") should be(2)

      mongoColl2.size should be(1)

    }

    def checkMongoDb: Unit = {
      val mongoClientURI = MongoClientURI(s"mongodb://$Localhost:$TestMongoPort/local")
      mongoClientURI.database should be(Some("local"))
    }
  }

  override def extraBefore: Unit = mongoProps = mongoStart(TestMongoPort)

  override def extraAfter: Unit = mongoStop(mongoProps)
}
