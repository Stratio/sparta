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
package com.stratio.sparta.plugin.input.flume

import java.io.Serializable
import java.net.InetSocketAddress

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.flume.FlumeUtils

class FlumeInput(
                  name: String,
                  ssc: StreamingContext,
                  sparkSession: XDSession,
                  properties: Map[String, Serializable]
                ) extends Input(name, ssc, sparkSession, properties) with SLF4JLogging {

  val DEFAULT_FLUME_PORT = 11999
  val DEFAULT_ENABLE_DECOMPRESSION = false
  val DEFAULT_MAXBATCHSIZE = 1000
  val DEFAULT_PARALLELISM = 5

  def initStream: DStream[Row] = {

    if (properties.getString("type").equalsIgnoreCase("pull")) {
      FlumeUtils.createPollingStream(
        ssc,
        getAddresses,
        storageLevel,
        maxBatchSize,
        parallelism
      ).map(data => Row(data.event.getBody.array))
    } else {
      // push
      FlumeUtils.createStream(
        ssc, properties.getString("hostname"),
        properties.getString("port").toInt,
        storageLevel,
        enableDecompression
      ).map(data => Row(data.event.getBody.array))
    }
  }

  private def getAddresses: Seq[InetSocketAddress] =
    properties.getMapFromArrayOfValues("addresses")
      .map(values => (values.get("host"), values.get("port")))
      .map {
        case (Some(address), None) =>
          new InetSocketAddress(address, DEFAULT_FLUME_PORT)
        case (Some(address), Some(port)) =>
          new InetSocketAddress(address, port.toInt)
        case _ =>
          throw new IllegalStateException(s"Invalid configuration value for addresses : ${properties.get("addresses")}")
      }

  private def enableDecompression: Boolean =
    properties.hasKey("enableDecompression") match {
      case true => properties.getBoolean("enableDecompression")
      case false => DEFAULT_ENABLE_DECOMPRESSION
    }

  private def parallelism: Int = {
    properties.hasKey("parallelism") match {
      case true => properties.getString("parallelism").toInt
      case false => DEFAULT_PARALLELISM
    }
  }

  private def maxBatchSize: Int =
    properties.hasKey("maxBatchSize") match {
      case true => properties.getString("maxBatchSize").toInt
      case false => DEFAULT_MAXBATCHSIZE
    }
}

