/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparkta.sdk

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ConnectionConfSpec extends WordSpec with Matchers {

  "getConnectionConf" should {
    "return a connection chain" in {
      val conn = """[{"host":"localhost","port":"60000"},{"host":"localhost","port":"60000"}]"""
      val validating: ValidatingPropertyMap[String, JsoneyString] =
        new ValidatingPropertyMap[String, JsoneyString](Map("hosts" -> JsoneyString(conn)))
      validating.getConnectionConfs("hosts") should be("localhost:60000,localhost:60000")
    }
  }


}
