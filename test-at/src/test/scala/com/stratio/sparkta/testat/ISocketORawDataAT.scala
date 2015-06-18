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

import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.reflect.io.File

class ISocketORawDataAT extends SparktaATSuite {

  val PolicyEndSleep = 30000
  val NumExecutors = 4
  val Policy = getClass.getClassLoader.getResource("policies/ISocket-ORawData.json")
  val PathToPolicy = Policy.getPath
  val PathToCsv = getClass.getClassLoader.getResource("fixtures/ISocket-ORawData.csv").getPath

  implicit val formats = DefaultFormats + new JsoneyStringSerializer()
  val parquetPath = parse(Policy.openStream()).extract[AggregationPoliciesDto].rawDataParquetPath

  before {
    zookeeperStart
    socketStart
  }

  after {
    serverSocket.close()
    zkTestServer.stop()
    File(parquetPath).deleteRecursively
  }

  val Total = 16

  "Sparkta" should {

    "save raw data in the storage" in {
      startSparkta

      sendPolicy(PathToPolicy)
      sendDataToSparkta(PathToCsv)
      Thread.sleep(PolicyEndSleep)
      SparkContextFactory.destroySparkContext
      checkData
    }

    def checkData(): Unit = {
      val sc = new SparkContext(s"local[$NumExecutors]", "ISocketORawDataAT")
      val sqc = new SQLContext(sc)
      val result = sqc.parquetFile(parquetPath).toDF
      //FIXME: Currently we are only capable of check the count
      result.count should be(Total)
    }
  }
}
