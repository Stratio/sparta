/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorsManagement, OutputOptions, OutputStep, TransformStep}
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.functions._
import com.stratio.sparta.sdk.workflow.step._

import scala.util.{Failure, Success, Try}
import org.apache.spark.sql._

import scala.util.Try

class RepartitionCustomTransformStepStream(name: String,
                                           outputOptions: OutputOptions,
                                           transformationStepsManagement: TransformationStepManagement,
                                           ssc: Option[StreamingContext],
                                           xDSession: XDSession,
                                           properties: Map[String, JSerializable])
  extends TransformStep[DStream](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) with SLF4JLogging {

  lazy val partitions = Try(propertiesWithCustom.getInt("partitions")).getOrElse(
    throw new Exception("Property partitions is mandatory"))


  override def transform(inputData: Map[String, DistributedMonad[DStream]]): DistributedMonad[DStream] =
    applyHeadTransform(inputData) { (_, inputDistributedMonad) =>
      val inputStream = inputDistributedMonad.ds
      inputStream.repartition(partitions)
    }
}
