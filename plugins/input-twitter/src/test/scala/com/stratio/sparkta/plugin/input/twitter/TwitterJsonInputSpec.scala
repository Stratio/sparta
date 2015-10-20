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
package com.stratio.sparkta.plugin.input.twitter

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.sdk.{Event, Input}
import org.scalatest._

@RunWith(classOf[JUnitRunner])
class TwitterJsonInputSpec extends WordSpec {

  "A TwitterInput" should {

    "fail without parameters" in {
      intercept[IllegalStateException] {
        new TwitterJsonInput(Map())
      }
    }
    "fail with bad arguments argument" in {
      intercept[IllegalStateException] {
        new TwitterJsonInput(Map("hostname" -> "localhost", "port" -> "BADPORT")
        .mapValues(_.asInstanceOf[JSerializable]))
      }
    }
  }
}