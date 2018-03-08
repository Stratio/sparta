/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.union

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.collection.mutable

class UnionTransformStepStreaming(
                                name: String,
                                outputOptions: OutputOptions,
                                transformationStepsManagement: TransformationStepManagement,
                                ssc: Option[StreamingContext],
                                xDSession: XDSession,
                                properties: Map[String, JSerializable]
                              ) extends UnionTransformStep[DStream](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transform(inputData: Map[String, DistributedMonad[DStream]]): DistributedMonad[DStream] = {
    val streams = inputData.map { case (_, dStream) =>
      dStream.ds
    }.toSeq

    streams.size match {
      case 1 => streams.head
      case x if x > 1 => ssc.get.union(streams)
      case _ => ssc.get.queueStream(new mutable.Queue[RDD[Row]])
    }
  }
}
