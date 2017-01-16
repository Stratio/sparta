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

package com.stratio.sparta.plugin.input.rabbitmq.handler

import java.io.{Serializable => JSerializable}

import com.rabbitmq.client.QueueingConsumer.Delivery
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.Row

sealed abstract class MessageHandler {
  def handler: Delivery => Row
}

object MessageHandler {
  val KeyDeserializer = "key.deserializer"
  val DefaultSerializer = "string"
  val ArraySerializerKey = "arraybyte"

  def apply(properties: Map[String, JSerializable]): MessageHandler = {
    apply(properties.getString(KeyDeserializer, DefaultSerializer))
  }

  def apply(handlerType: String): MessageHandler = handlerType match {
    case ArraySerializerKey => ByteArrayMessageHandler
    case _ => StringMessageHandler
  }

  case object StringMessageHandler extends MessageHandler {
    override def handler: (Delivery) => Row = (rawMessage: Delivery) => Row(new Predef.String(rawMessage.getBody))
  }

  case object ByteArrayMessageHandler extends MessageHandler {
    override def handler: (Delivery) => Row = (rawMessage: Delivery) => Row(rawMessage.getBody)
  }
}