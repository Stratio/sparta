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
import com.mongodb.BasicDBObject
import com.mongodb.casbah.{MongoCollection, MongoConnection}
import com.stratio.sparta.testat.SpartaATSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * Acceptance test:
 * [Input]: Socket.
 * [Output]: MongoDB.
 * [Operators]: count
 */
@RunWith(classOf[JUnitRunner])
class ISocketOMongoFiltersJsonIT extends MongoEmbedDatabase with SpartaATSuite {

  override val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data-filters-json").getPath
  override val policyFile = "policies/ISocket-OMongo-filters-json.json"

  val TestMongoPort = 60001
  var mongoProps: MongodProps = _
  var mongoConnection: MongoConnection = _

  "Sparta" should {
    "starts and executes a policy that reads from a socket and writes in mongodb with filters in operators" in {
      spartaRunner
      checkMongoData
    }

    def checkMongoData(): Unit = {

      val mongoColl = mongoConnection("csvtestfilter")("hashtagfilter")

      mongoColl.size should be(2)

      val hashtag = mongoColl.find(new BasicDBObject("hashtag", "spartaHashtag"))
      val register = hashtag.next()
      register.get("count1") should be(1)
    }
  }

  override def extraBefore: Unit = {
    mongoProps = mongoStart(TestMongoPort)
    mongoConnection = MongoConnection(Localhost, TestMongoPort)
  }

  override def extraAfter: Unit = {
    mongoConnection.close()
    mongoStop(mongoProps)
    deletePath(s"$CheckpointPath/${"ATSocketMongoFiltersJSON".toLowerCase}")
  }
}
