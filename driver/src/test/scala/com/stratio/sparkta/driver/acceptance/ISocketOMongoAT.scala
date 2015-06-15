package com.stratio.sparkta.driver.acceptance

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.mongodb.casbah.{MongoClientURI, MongoCollection, MongoConnection}

class ISocketOMongoAT extends MongoEmbedDatabase with SparktaATSuite {

  val TestMongoPort = 60000
  val PathToPolicy = getClass.getClassLoader.getResource("policies/ISocket-OMongo.json").getPath
  val PathToCsv = getClass.getClassLoader.getResource("fixtures/ISocket-OMongo.csv").getPath
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


  "Sparkta should" should {

    "start in 3 seconds on 9090" in {
      checkMongoDb
      startSparkta
      sendPolicy(PathToPolicy)
      sendDataToSparkta(PathToCsv)
      Thread.sleep(30000)
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
