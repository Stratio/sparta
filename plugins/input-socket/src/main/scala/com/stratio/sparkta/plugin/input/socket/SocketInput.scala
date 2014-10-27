/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.plugin.input.socket

import com.stratio.sparkta.sdk.Input._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{Event, Input}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

/**
 * Created by ajnavarro on 22/10/14.
 */
class SocketInput(properties: Map[String, Serializable]) extends Input(properties) {

  override def setUp(ssc: StreamingContext): DStream[Event] = {
    ssc.socketTextStream(
      properties.getString("hostname"),
      properties.getString("port").toInt,
      StorageLevel.fromString(properties.getString("storageLevel")))
      .map(data => new Event(Map(RAW_DATA_KEY -> data.getBytes("UTF-8").asInstanceOf[Serializable])))
  }
}
