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
package org.apache.spark.streaming.datasource

import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.datasource.config.ConfigParameters._
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.BeforeAndAfter

private[datasource] trait TemporalDataSuite extends DatasourceSuite
  with BeforeAndAfter {

  val conf = new SparkConf()
    .setAppName("datasource-receiver-example")
    .setIfMissing("spark.master", "local[*]")
  var sc: SparkContext = null
  var ssc: StreamingContext = null
  var sparkSession: SparkSession = null
  val tableName = "tableName"
  val datasourceParams = Map(
    StorageLevelKey -> "MEMORY_ONLY",
    RememberDuration -> "20000"
  )
  val schema = new StructType(Array(
    StructField("id", StringType, nullable = true),
    StructField("idInt", IntegerType, nullable = true)
  ))
  val totalRegisters = 10000
  val registers = for (a <- 1 to totalRegisters) yield Row(a.toString, a)

  after {
    if (ssc != null) {
      ssc.stop()
      ssc = null
    }
    if (sc != null) {
      sc.stop()
      sc = null
    }
    if (sparkSession != null) {
      sparkSession.stop()
      sparkSession = null
    }
  }
}
