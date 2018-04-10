/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.workflow.step

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.Parameterizable
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext

import scala.util.Try


abstract class InputStep[Underlying[Row]](
                          val name: String,
                          val outputOptions: OutputOptions,
                          @transient private[sparta] val ssc: Option[StreamingContext],
                          @transient private[sparta] val sparkSession: XDSession,
                          properties: Map[String, JSerializable]
                        ) extends Parameterizable(properties) with GraphStep {

  /* GLOBAL VARIABLES */

  override lazy val customKey = "inputOptions"
  override lazy val customPropertyKey = "inputOptionsKey"
  override lazy val customPropertyValue = "inputOptionsValue"

  lazy val StorageDefaultValue = "MEMORY_ONLY"
  lazy val DefaultRawDataField = "raw"
  lazy val DefaultRawDataType = "string"
  lazy val DefaultSchema: StructType = StructType(Seq(StructField(DefaultRawDataField, StringType)))
  lazy val storageLevel: StorageLevel = {
    val storageLevel = properties.getString("storageLevel", StorageDefaultValue)
    StorageLevel.fromString(storageLevel)
  }

  def initWithSchema(): (DistributedMonad[Underlying], Option[StructType]) = {
    val monad = init()

    (monad, None)
  }

  /* METHODS TO IMPLEMENT */

  /**
    * Create and initialize stream using the Spark Streaming Context.
    *
    * @return The DStream created with spark rows
    */
  def init(): DistributedMonad[Underlying]

}

object InputStep {

  val StepType = "input"

}