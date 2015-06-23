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

import scala.io.Source
import scala.reflect.io.File

class ISocketORawDataAT extends SparktaATSuite {

  val PolicyEndSleep = 30000
  val NumExecutors = 4
  val Policy = getClass.getClassLoader.getResource("policies/ISocket-ORawData.json")
  val PathToPolicy = Policy.getPath
  val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data.csv").getPath

  val CsvLines = Source.fromFile(PathToCsv).getLines().toList.map(line => line).toSeq.sortBy(_.toString)

  implicit val formats = DefaultFormats + new JsoneyStringSerializer()
  val parquetPath = parse(Policy.openStream()).extract[AggregationPoliciesDto].rawData.path

  before {
    zookeeperStart
    socketStart
  }

  after {
    serverSocket.close()
    zkTestServer.stop()
    File(parquetPath).deleteRecursively
  }

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
      val result = sqc.read.parquet(parquetPath)
      result.registerTempTable("rawLines")

      sqc.sql("select data from rawLines")
        .collect()
        .map(_.get(0))
        .toSeq
        .sortBy(_.toString) should be (CsvLines)
    }
  }
}
