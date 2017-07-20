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
package com.stratio.sparta.plugin.input.websocket

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

class WebSocketInput(
                      name: String,
                      ssc: StreamingContext,
                      sparkSession: XDSession,
                      properties: Map[String, JSerializable]
                    ) extends Input(name, ssc, sparkSession, properties) with SLF4JLogging {

  def initStream: DStream[Row] = {
    ssc.receiverStream(new WebSocketReceiver(properties.getString("url"), storageLevel))
      .map(data => Row(data))
  }
}
