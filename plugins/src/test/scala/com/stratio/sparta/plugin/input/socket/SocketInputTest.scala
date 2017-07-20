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
package com.stratio.sparta.plugin.input.socket

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

@RunWith(classOf[JUnitRunner])
class SocketInputTest extends WordSpec with MockitoSugar{

  val sparkSession = mock[XDSession]
  val ssc = mock[StreamingContext]

  "A SocketInput" should {
    "instantiate successfully with parameters" in {
      new SocketInput("socket", ssc, sparkSession,
        Map("hostname" -> "localhost", "port" -> 9999).mapValues(_.asInstanceOf[JSerializable]))
    }
    "fail without parameters" in {
      intercept[IllegalStateException] {
        new SocketInput("socket", ssc, sparkSession, Map())
      }
    }
    "fail with bad port argument" in {
      intercept[IllegalStateException] {
        new SocketInput("socket", ssc, sparkSession, Map("hostname" -> "localhost", "port" -> "BADPORT")
          .mapValues(_.asInstanceOf[JSerializable]))
      }
    }
  }
}
