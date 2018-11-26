/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.sftp

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step._
import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.serving.core.models.enumerators.SftpFileTypeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.util.Try

class SFTPInputStepBatch(
                          name: String,
                          outputOptions: OutputOptions,
                          ssc: Option[StreamingContext],
                          xDSession: XDSession,
                          properties: Map[String, JSerializable]
                        )
  extends InputStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val path = properties.getString("path", None)
  lazy val host = properties.getString("host", None)
  lazy val port = properties.getInt("port", None)
  lazy val username = properties.getString("username", None)
  lazy val fileType = properties.getString(key = "fileType", None)
  lazy val delimiter = properties.getString("port", ",")
  lazy val rowTag = properties.getString("rowTag", None).notBlank
  lazy val tlsEnable = Try(properties.getBoolean("tlsEnabled")).getOrElse(false)

  lazy val sparkConf = xDSession.conf.getAll
  lazy val pemOption = if(tlsEnable) {
    SecurityHelper.getPemUri(sparkConf).fold(Map.empty[String, String]) { pemUri => Map("pem" -> pemUri) }
  } else Map.empty[String, String]

  //Dummy function on batch inputs that generates DataSets with schema
  def init(): DistributedMonad[RDD] = {
    throw new Exception("Not used on inputs that generates DataSets with schema")
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (path.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage("The path value is not defined.", name)
      )

    if (host.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage("The host value is not defined.", name)
      )

    if (port.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage("The port value is not defined.", name)
      )

    if (port.nonEmpty && (port.get <= 0 || port.get >= 65535))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage("The port value is not valid.", name)
      )

    if (username.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage("The username for the sftp server is not defined.", name)
      )

    if (fileType.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage("The input file type is not chosen.", name)
      )

    if (fileType.get == SftpFileTypeEnum.xml.toString && rowTag.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The user should specify a row tag of the" +
          s" xml files to treat as a row", name)
      )

    if (fileType.get == SftpFileTypeEnum.csv.toString && delimiter.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage("The delimiter is not defined", name)
      )

    if (debugOptions.isDefined && !validDebuggingOptions)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"$errorDebugValidation", name)
      )

    validation
  }

  override def initWithSchema(): (DistributedMonad[RDD], Option[StructType]) = {

    val commonOptions = Map(
      "host" -> properties.getString("host", None),
      "port" -> properties.getString("port", None),
      "username" -> properties.getString("username", None),
      "fileType" -> properties.getString(key = "fileType", None),
      "password" -> properties.getString(key = "password", None))
      .filter { case (key, value) => value.isDefined }.map { case (key, optValue) => (key, optValue.get) } ++ pemOption

    val dataframeReader = xDSession.read.format("com.springml.spark.sftp")

    val dataFrameReaderWithCustomOptions = dataframeReader.options(propertiesWithCustom.mapValues(_.toString) ++ commonOptions)

    val df = dataFrameReaderWithCustomOptions.load(properties.getString("path"))

    (df.rdd, Option(df.schema))

  }
}