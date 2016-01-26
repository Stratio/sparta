/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

import scala.io.Source
import scala.reflect.io.File

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ISocketORawDataIT extends SparktaATSuite {

  val NumExecutors = 4
  val policyFile = "policies/ISocket-ORawData.json"
  val CsvLines = Source.fromFile(PathToCsv).getLines().toList.map(line => line).toSeq.sortBy(_.toString)

  val parquetPath = policyDto.rawData.path

  "Sparkta" should {
    "save raw data in the storage" in {
      sparktaRunner
      checkData
    }

    def checkData(): Unit = {
      val sc = new SparkContext(s"local[$NumExecutors]", "ISocketORawDataAT")
      val sqc = SQLContext.getOrCreate(sc)
      val result = sqc.read.parquet(parquetPath)
      result.registerTempTable("rawLines")

      sqc.sql("select data from rawLines")
        .collect()
        .map(_.get(0))
        .toSeq
        .sortBy(_.toString) should be(CsvLines)
      sc.stop
    }
  }

  override def extraAfter: Unit = File(parquetPath).deleteRecursively

  override def extraBefore: Unit = {}
}
