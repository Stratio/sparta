/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.plugin.output.http

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.ValidatingPropertyMap._
import com.stratio.sparta.sdk._
import org.apache.spark.Logging
import org.apache.spark.sql._

import scala.util.Try
import scalaj.http._


/**
  * This output send all AggregateOperations or DataFrames information through a REST operation.
  *
  * @param keyName
  * @param properties
  * @param schemas
  */
class HttpOutput(keyName: String,
                 version: Option[Int],
                 properties: Map[String, JSerializable],
                 schemas: Seq[TableSchema])
  extends Output(keyName, version, properties, schemas) with Logging {

  val MaxReadTimeout = 5000

  val MaxConnTimeout = 1000

  require(properties.getString("url", None).isDefined, "url must be provided")
  val url = properties.getString("url")

  val delimiter = properties.getString("delimiter", ",")

  val readTimeout = Try(properties.getInt("readTimeout")).getOrElse(MaxReadTimeout)

  val connTimeout = Try(properties.getInt("connTimeout")).getOrElse(MaxConnTimeout)

  val outputFormat = OutputFormat.withName(properties.getString("outputFormat", "json").toUpperCase)

  val postType = PostType.withName(properties.getString("postType", "body").toUpperCase)

  val parameterName = properties.getString("parameterName", "")

  val contentType = if (outputFormat == OutputFormat.ROW) "text/plain" else "application/json"

  override def supportedSaveModes: Seq[SaveModeEnum.Value] = Seq(SaveModeEnum.Append)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {

    validateSaveMode(saveMode)
    executor(dataFrame)
  }

  private def executor(dataFrame: DataFrame): Unit = {
    outputFormat match {
      case OutputFormat.ROW => dataFrame.rdd.foreachPartition(partition =>
        partition.foreach(row => sendData(row.mkString(delimiter))))
      case _ => dataFrame.toJSON.foreachPartition(partition =>
        partition.foreach(row => sendData(row)))
    }
  }

  def sendData(formattedData: String): HttpResponse[String] = {
    postType match {
      case PostType.PARAMETER => Http(url)
        .postForm(Seq(parameterName -> formattedData)).asString
      case PostType.BODY => Http(url).postData(formattedData)
        .header("content-type", contentType)
        .header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(readTimeout)).asString
    }
  }
}
