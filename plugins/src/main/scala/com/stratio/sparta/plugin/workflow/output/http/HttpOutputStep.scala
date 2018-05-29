/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.http

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.enumerations.PostType
import com.stratio.sparta.sdk.models.{ErrorValidations, WorkflowValidationMessage}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.enumerators.{OutputFormatEnum, SaveModeEnum}
import com.stratio.sparta.sdk.workflow.step.OutputStep
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.crossdata.XDSession

import scala.util.Try
import scalaj.http._

class HttpOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) {

  val MaxReadTimeout = 5000
  val MaxConnTimeout = 1000

  val urlRegex = "\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".r

  require(properties.getString("url", None).isDefined, "url must be provided")

  val url = properties.getString("url")
  val delimiter = properties.getString("delimiter", ",")
  val readTimeout = Try(properties.getInt("readTimeout")).getOrElse(MaxReadTimeout)
  val connTimeout = Try(properties.getInt("connTimeout")).getOrElse(MaxConnTimeout)
  val outputFormat = OutputFormatEnum.withName(properties.getString("outputFormat", "json").toUpperCase)
  val postType = PostType.withName(properties.getString("postType", "body").toUpperCase)
  val parameterName = properties.getString("parameterName", "")
  val contentType = if (outputFormat == OutputFormatEnum.ROW) "text/plain" else "application/json"


  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (urlRegex.findFirstIn(url).isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"url must be valid and start with http(s)://", name)
      )

    validation
  }

  override def supportedSaveModes: Seq[SaveModeEnum.Value] = Seq(SaveModeEnum.Append)

  /**
    * Save function that implements the plugins.
    *
    * @param dataFrame The dataFrame to save
    * @param saveMode  The sparta save mode selected
    * @param options   Options to save the data (partitionBy, primaryKey ... )
    */
  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {

    validateSaveMode(saveMode)
    executor(dataFrame)
  }

  private def executor(dataFrame: DataFrame): Unit = {
    outputFormat match {
      case OutputFormatEnum.ROW => dataFrame.rdd.foreachPartition(partition =>
        partition.foreach(row => sendData(row.mkString(delimiter))))
      case _ => dataFrame.toJSON.foreachPartition(partition =>
        partition.foreach(row => sendData(row)))
    }
  }

  def sendData(formattedData: String): HttpResponse[String] = {
    postType match {
      case PostType.PARAMETER => {
        Http(url).param(parameterName,formattedData).asString
      }
      case PostType.BODY => {
        Http(url).postData(formattedData)
          .header("content-type", contentType)
          .header("Charset", "UTF-8")
          .option(HttpOptions.readTimeout(readTimeout)).asString
      }
    }
  }
}

