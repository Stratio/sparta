/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta

import org.apache.spark.sql._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import com.stratio.sparta.sdk.lite.xd.batch._
import com.stratio.sparta.sdk.lite.batch.models._

import scala.util.{Failure, Success, Try}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types._

import scala.util.Try

class GeneratorLiteInputStepBatch(
                                   xdSession: XDSession,
                                   properties: Map[String, String]
                                 )
  extends LiteCustomXDBatchInput(xdSession, properties) {

  lazy val stringSchema = StructType(Seq(StructField("raw", StringType)))

  override def init(): ResultBatchData = {
    val register = Seq(new GenericRowWithSchema(Array("test-data"), stringSchema).asInstanceOf[Row])
    val defaultRDD = xdSession.sparkContext.parallelize(register)

    ResultBatchData(defaultRDD, Option(stringSchema))
  }
}
