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
package com.stratio.kafka.benchmark.generator.kafka

import java.util.Properties

import com.typesafe.config.Config
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}

object KafkaProducer {

  def getInstance(config: Config): Producer[String, String] = {
    val props: Properties = new Properties()
    props.put("metadata.broker.list", config.getString("brokerList"))
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    props.put("request.required.acks", "1")

    val producerConfig = new ProducerConfig(props)
    new Producer[String, String](producerConfig)
  }

  def send(producer: Producer[String, String], topic: String, message: String): Unit = {
    val keyedMessage: KeyedMessage[String, String] = new KeyedMessage[String, String](topic, message)
    producer.send(keyedMessage)
  }
}