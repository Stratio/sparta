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

import com.github.simplyscala.{MongodProps, MongoEmbedDatabase}
import com.mongodb.casbah.{MongoClientURI, MongoConnection, MongoCollection}

/**
 * Acceptance test:
 *   [Input]: Socket.
 *   [Output]: MongoDB.
 *   [Operators]: sum, avg.
 * @author gschiavon
 */
class ISocketOCsvAT extends SparktaATSuite {

  val PolicyEndSleep = 60000
  val PathToPolicy = getClass.getClassLoader.getResource("policies/ISocket-OCsv.json").getPath
  val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data.csv").getPath


  before {
    zookeeperStart
    socketStart


  }

  after {

    serverSocket.close()
    zkTestServer.stop()

  }

  "Sparkta" should {
    "starts and executes a policy that reads from a socket and writes in a csv file" in {

      startSparkta
      sendPolicy(PathToPolicy)
      sendDataToSparkta(PathToCsv)
      sleep(PolicyEndSleep)

    }


  }
}
