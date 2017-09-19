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
package com.stratio.sparta.serving.core.exception

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class ServingExceptionTest extends WordSpec with Matchers {

  "A ServingException" should {
    "create an exception with message" in {
      ServerException.create("message").getMessage should be("message")
    }
    "create an exception with message and a cause" in {
      val cause = new IllegalArgumentException("any exception")
      val exception = ServerException.create("message", cause)
      exception.getMessage should be("message")
      exception.getCause should be theSameInstanceAs(cause)
    }
  }
}
