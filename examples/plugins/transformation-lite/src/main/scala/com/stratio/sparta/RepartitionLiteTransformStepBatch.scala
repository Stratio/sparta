/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta

import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import scala.util.{Failure, Success, Try}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.lite.batch._
import com.stratio.sparta.sdk.lite.batch.models._
import org.apache.spark.sql._

class RepartitionLiteTransformStepBatch(
                                         sparkSession: SparkSession,
                                         properties: Map[String, String]
                                       ) extends LiteCustomBatchTransform(sparkSession, properties) {

  override def transform(inputData: Map[String, ResultBatchData]): OutputBatchTransformData = {
    val inputStream = inputData.head._2.data

    OutputBatchTransformData(inputStream.repartition(5))
  }
}
