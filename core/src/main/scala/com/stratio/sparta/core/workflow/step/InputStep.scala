/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.step

import java.io.{Serializable => JSerializable}

import com.google.common.io.Files
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.enumerators.InputFormatEnum
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.core.properties.{JsoneyStringSerializer, Parameterizable}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
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

  lazy val (debugPath, debugQuery, debugUserProvidedExample, debugFileUploaded):
    (Option[String], Option[String],Option[String], Option[String]) =
    debugOptions.map(debugOpt =>
      (debugOpt.path.notBlank, debugOpt.query.notBlank,
        debugOpt.userProvidedExample.notBlank, debugOpt.fileUploaded.notBlank))
      .getOrElse((None, None, None, None))

  def getFileExtension(path: Option[String]): Option[String] = path.map(Files.getFileExtension)

  def getSerializerForInput(path: Option[String]): Option[InputFormatEnum.Value] =
    getFileExtension(path) match{
      case Some(extension) =>  Try(InputFormatEnum.withName(extension.toUpperCase)).map(Some(_)).getOrElse(None)
      case _ => None
    }

  lazy val validDebuggingOptions = debugPath.isDefined || debugQuery.isDefined ||
    debugUserProvidedExample.isDefined || debugFileUploaded.isDefined

  val errorDebugValidation = "Either an input path or an uploaded file path or a user-defined example " +
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