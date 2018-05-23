/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.workflow.step

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.DistributedMonad
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.collection.mutable
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.models.OutputOptions

class MockInputStep(
                     name: String,
                     outputOptions: OutputOptions,
                     ssc: Option[StreamingContext],
                     xDSession: XDSession,
                     properties: Map[String, JSerializable]
                   ) extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties) {

  def init(): DistributedMonad[DStream] = ssc.get.queueStream(new mutable.Queue[RDD[Row]])

}
