/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.lite.batch

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.lite.batch.models.ResultBatchData
import com.stratio.sparta.sdk.lite.validation.Validator
import org.apache.spark.sql.SparkSession

abstract class LiteCustomBatchInput(
                                     @transient val sparkSession: SparkSession,
                                     properties: Map[String, String]
                                   ) extends Validator with SLF4JLogging {

  def init(): ResultBatchData

}
