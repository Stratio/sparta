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

import com.rabbitmq.client.QueueingConsumer.Delivery
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class MessageHandlerTest extends WordSpec with Matchers with MockitoSugar {

  val message = "This is the message for testing"
  "RabbitMQ MessageHandler Factory " should {

    "Get correct handler for string " in {
      val result = MessageHandler("string")
      result should matchPattern { case StringMessageHandler => }
    }

    "Get correct handler for arraybyte " in {
      val result = MessageHandler("arraybyte")
      result should matchPattern { case ByteArrayMessageHandler => }
    }

    "Get correct handler for empty input " in {
      val result = MessageHandler("")
      result should matchPattern { case StringMessageHandler => }
    }

    "Get correct handler for bad input " in {
      val result = MessageHandler("badInput")
      result should matchPattern { case StringMessageHandler => }
    }
  }

  "StringMessageHandler " should {
    "Handle strings" in {
      val delivery = mock[Delivery]
      when(delivery.getBody).thenReturn(message.getBytes)
      val result = StringMessageHandler.handler(delivery)
      verify(delivery, times(1)).getBody
      result.getString(0) shouldBe message
    }
  }

  "ByteArrayMessageHandler " should {
    "Handle bytes" in {
      val delivery = mock[Delivery]
      when(delivery.getBody).thenReturn(message.getBytes)
      val result = ByteArrayMessageHandler.handler(delivery)
      verify(delivery, times(1)).getBody
      result.getAs[Array[Byte]](0) shouldBe message.getBytes
    }

  }

}
