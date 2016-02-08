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

package com.stratio.sparkta.serving.core.helpers

import java.io.File

import com.stratio.sparkta.sdk.DimensionType
import com.stratio.sparkta.serving.core.models._
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{WordSpec, Matchers}

import scala.collection.mutable


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

  }
  "OperationsHelper and checkValidCheckpointInterval " should {

    "parseValueToMilliSeconds should return true because interval is 5 times greater than Spark Streaming Window" in {
      val res = OperationsHelper.checkValidCheckpointInterval(2000,"10000")
      res should be(true)
    }

    "parseValueToMilliSeconds should return true because interval is more than 5 times greater" +
      " than Spark Streaming Window" in {
      val res = OperationsHelper.checkValidCheckpointInterval(2000,"20000")
      res should be(true)
    }

    "parseValueToMilliSeconds should return false because interval is less than 5 times than Spark Streaming Window" in {
      val res = OperationsHelper.checkValidCheckpointInterval(2000,"6000")
      res should be(false)
    }
  }

}