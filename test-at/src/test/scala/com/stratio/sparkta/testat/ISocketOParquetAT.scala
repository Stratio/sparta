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

import scala.reflect.io.File

import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.reflect.io.File

class ISocketOParquetAT extends SparktaATSuite {

  val PolicyEndSleep = 30000
  val NumExecutors = 4
  val Policy = getClass.getClassLoader.getResource("policies/ISocket-OParquet.json")
  val PathToPolicy = Policy.getPath
  val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data.csv").getPath

  implicit val formats = DefaultFormats + new JsoneyStringSerializer()
  val parquetPath = parse(Policy.openStream()).extract[AggregationPoliciesDto].outputs(0).configuration("path").toString
  val expectedResults = Set(("producta", 750.0D, 6000.0D),
    ("productb", 1000.0D, 8000.0D),
    ("producta", 750.0D, 6000.0D),
    ("productb", 1000.0D, 8000.0D))

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

    "save data in parquet" in {
      startSparkta

      sendPolicy(PathToPolicy)
      sendDataToSparkta(PathToCsv)
      Thread.sleep(PolicyEndSleep)
      //FIXME: We need to change this when we can stop gracefully Sparkta
      SparkContextFactory.destroySparkContext
      checkData
    }

    def checkData(): Unit = {
      val sc = new SparkContext(s"local[$NumExecutors]", "ISocketOParquetAT")
      val sqc = new SQLContext(sc)
      val result = sqc.parquetFile(parquetPath).toDF
      val elementsAsSeq = result.select("product", "avg_price", "sum_price").map(
        row => (row.getString(0), row.getDouble(1), row.getDouble(2))).collect.toSet
      elementsAsSeq should be(expectedResults)
    }
  }
}
