/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.workflow.step

import java.io.{Serializable => JSerializable}

import com.google.common.io.Files
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.enumerators.InputFormatEnum
import com.stratio.sparta.sdk.models.OutputOptions
import com.stratio.sparta.sdk.properties.{JsoneyStringSerializer, Parameterizable}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization._
import org.json4s.{DefaultFormats, Formats}

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


  lazy val debugOptions: Option[DebugOptions] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    properties.getString("debugOptions", None).map{ debug =>
      read[DebugOptions](debug)
    }
  }

  lazy val (debugPath, debugQuery, debugUserProvidedExample): (Option[String], Option[String],Option[String]) =
    debugOptions.map(debugOpt =>
      (debugOpt.path.notBlank, debugOpt.query.notBlank, debugOpt.userProvidedExample.notBlank))
      .getOrElse((None, None,None))

  lazy val fileExtension: Option[String] = debugPath.map(Files.getFileExtension)

  lazy val serializerForInput: Option[InputFormatEnum.Value] =
    if (fileExtension.isDefined)
      Try(InputFormatEnum.withName(fileExtension.get.toUpperCase)).map(Some(_)).getOrElse(None)
    else None

  lazy val validDebuggingOptions = debugPath.isDefined || debugQuery.isDefined || debugUserProvidedExample.isDefined

  val errorDebugValidation = "Either an input path or a user-defined example " +
    "or a valid query to execute must be provided inside the debugging options"

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