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

package com.stratio.sparta.plugin.input.kafka

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.spark.sql.Row
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils

class KafkaInput(properties: Map[String, JSerializable])
  extends Input(properties)
    with KafkaBase
    with SLF4JLogging {

  def setUp(ssc: StreamingContext, sparkStorageLevel: String): DStream[Row] = {
    val metaDataBrokerList = if(properties.contains("metadata.broker.list"))
      getHostPort("metadata.broker.list", DefaultHost, DefaultBrokerPort)
    else getHostPort("bootstrap.servers", DefaultHost, DefaultBrokerPort)
    val groupId = getGroupId("group.id")
    val valueSerializer = properties.getString("value.deserializer", "string")
    val topics = extractTopics

    valueSerializer match {
      case "arraybyte" =>
        KafkaUtils.createDirectStream[String, Array[Byte], StringDecoder, DefaultDecoder](
          ssc, metaDataBrokerList ++ groupId ++ getCustomProperties, topics).map(data => Row(data._2))
      case _ =>
        KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
          ssc, metaDataBrokerList ++ groupId ++ getCustomProperties, topics).map(data => Row(data._2))
    }
  }
}
