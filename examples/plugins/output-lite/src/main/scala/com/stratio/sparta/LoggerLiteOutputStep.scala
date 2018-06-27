/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta

import org.apache.spark.rdd.RDD
import com.stratio.sparta.sdk.lite.common._
import com.stratio.sparta.sdk.lite.common
import scala.util.Try
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

class LoggerLiteOutputStep(
                              sparkSession: SparkSession,
                              properties: Map[String, String]
                            )
  extends LiteCustomOutput(sparkSession, properties) {

  override def save(data: DataFrame, saveMode: String, saveOptions: Map[String, String]): Unit = {
    data.foreach{ row =>
      println(row.mkString(","))
    }
  }
}
