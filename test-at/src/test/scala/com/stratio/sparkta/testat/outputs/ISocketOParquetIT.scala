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

package com.stratio.sparkta.testat.outputs

import scala.reflect.io.File

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.testat.SparktaATSuite

@RunWith(classOf[JUnitRunner])
class ISocketOParquetIT extends SparktaATSuite {

  override val policyFile = "policies/ISocket-OParquet.json"
  val parquetPath = policyDto.outputs(0).configuration("path").toString

  trait MockData {

    val NumExecutors = 4
    val expectedResults = Set(
      Set("producta", 750.0D, 6000.0D),
      Set("productb", 1000.0D, 8000.0D)
    )
  }

  "Sparkta" should {

    "save data in parquet" in new MockData {
      sparktaRunner
      val sqc = new SQLContext(new SparkContext(s"local[$NumExecutors]", "ISocketOParquetAT"))
      sqc.read.parquet(parquetPath).registerTempTable("products")
      sqc.sql("select product, avg_price, sum_price from products")
        .collect()
        .map(_.toSeq.toSet)
        .toSet should be(expectedResults)
      sqc.sparkContext.stop
    }
  }

  override def extraAfter: Unit = File(parquetPath).deleteRecursively

  override def extraBefore: Unit = {}
}
