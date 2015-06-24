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
import com.stratio.sparkta.testat.SparktaATSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * Acceptance test:
 * [Input]: Socket.
 * [Output]: MongoDB.
 * [Operators]: sum, avg.
 * @author gschiavon
 */
@RunWith(classOf[JUnitRunner])
class ISocketOMongoDetectorIT extends MongoEmbedDatabase with SparktaATSuite {

  val TestMongoPort = 60000
  val policyFile = "policies/ISocket-OMongo-Detector.json"
  override val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-internal-data.csv").getPath
  var mongoProps: MongodProps = _
  val DatabaseName = "csvtest"
  val CollectionMaxMinOdometer = "asset_company_root_ou_vehicle_path_id_minute"
  val CollectionRpmAvg = "company_root_precision3_ou_vehicle_minute"

  "Sparkta" should {
    "starts and executes a policy that reads from a socket and writes in mongodb" in {
      sparktaRunner
      checkMongoData
    }

    def checkMongoData(): Unit = {
      val mongoConnectionMaxMinOdometer = getMongoConnection(CollectionMaxMinOdometer)
      val mongoConnectionRpmAvg = getMongoConnection(CollectionRpmAvg)

      if (mongoConnectionRpmAvg.size > 0) {
        mongoConnectionRpmAvg.map(dbObject => {
          val id = dbObject.get("id")
          id match {
            case "3.0_1510_List(37.265625, -3.515625)" =>
              dbObject.get("avg_rpm_event_avg") should be(26.666666666666668)
            case "2.0_3_List(37.265625, -6.328125)" =>
              dbObject.get("avg_rpm_event_avg") should be(14.0)
            case _ => require(false)
          }
        })
      }

      if (mongoConnectionMaxMinOdometer.size > 0) {
        mongoConnectionMaxMinOdometer.map(dbObject => {
          dbObject.get("id") match {
            case "3.0_1510_356363056643879_356363056643879-14" => {
              dbObject.get("max_odometer") should be(8004334.0d)
              dbObject.get("min_odometer") should be(1004334.0d)
            }
            case "2.0_3_356363051321497_356363051321497-13" => {
              dbObject.get("max_odometer") should be(9917036.0d)
              dbObject.get("min_odometer") should be(3000216.0d)
            }
            case _ => require(false)
          }
        })
      }
    }

    def getMongoConnection(CollectionName: String): MongoCollection = {
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