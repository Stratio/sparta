/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
  val totalRegisters = 1000
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
