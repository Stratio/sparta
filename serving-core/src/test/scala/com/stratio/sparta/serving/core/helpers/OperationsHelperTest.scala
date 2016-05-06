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
/**
  * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparta.serving.core.helpers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}


@RunWith(classOf[JUnitRunner])
class OperationsHelperTest extends WordSpec with Matchers with MockitoSugar {

  "OperationsHelper parseValueToMilliSeconds" should {

    "parseValueToMilliSeconds should return 1 second when precision is second" in {
      val res = OperationsHelper.parseValueToMilliSeconds("second")
      res should be(1000)
    }

    "parseValueToMilliSeconds should return 1 second" in {
      val res = OperationsHelper.parseValueToMilliSeconds("1s")
      res should be(1000)
    }

    "parseValueToMilliSeconds should return 1 second when precision is minute" in {
      val res = OperationsHelper.parseValueToMilliSeconds("minute")
      res should be(60000)
    }

    "parseValueToMilliSeconds should return a value in seconds of 1 minute" in {
      val res = OperationsHelper.parseValueToMilliSeconds("1m")
      res should be(60000)
    }

    "parseValueToMilliSeconds should return a value in seconds of 1 hour" in {
      val res = OperationsHelper.parseValueToMilliSeconds("1h")
      res should be(3600000)
    }

    "parseValueToMilliSeconds should return 1 second when precision is hour" in {
      val res = OperationsHelper.parseValueToMilliSeconds("hour")
      res should be(3600000)
    }

    "parseValueToMilliSeconds should return 1 second when precision is day" in {
      val res = OperationsHelper.parseValueToMilliSeconds("day")
      res should be(86400000)
    }
    "parseValueToMilliSeconds should return a value in seconds of 1 day" in {
      val res = OperationsHelper.parseValueToMilliSeconds("1d")
      res should be(86400000)
    }

    "parseValueToMilliSeconds should return a value in seconds of 1 month" in {
      val res = OperationsHelper.parseValueToMilliSeconds("1M")
      res should be(2628000000L)
    }

    "parseValueToMilliSeconds should return a value in seconds of year" in {
      val res = OperationsHelper.parseValueToMilliSeconds("year")
      res should be(31557600000L)
    }

    "parseValueToMilliSeconds should return a value in seconds of 1 year" in {
      val res = OperationsHelper.parseValueToMilliSeconds("1y")
      res should be(31557600000L)
    }

    "parseValueToMilliSeconds should return 30 second" in {
      val res = OperationsHelper.parseValueToMilliSeconds("30s")
      res should be(30000)
    }

    "parseValueToMilliSeconds should return 30000 second" in {
      val res = OperationsHelper.parseValueToMilliSeconds("30000")
      res should be(30000)
    }

    "parseValueToMilliSeconds should return 2 days" in {
      val res = OperationsHelper.parseValueToMilliSeconds("2d")
      res should be(172800000)
    }

    "parseValueToMilliSeconds should return 6 milliseconds" in {
      val res = OperationsHelper.parseValueToMilliSeconds("6ms")
      res should be(6)
    }

    "parseValueToMilliSeconds should return 6 seconds" in {
      val res = OperationsHelper.parseValueToMilliSeconds("6000ms")
      res should be(6000)
    }

    "parseValueToMilliSeconds should return 6500 milliseconds" in {
      val res = OperationsHelper.parseValueToMilliSeconds("6500")
      res should be(6500)
    }

  }

}