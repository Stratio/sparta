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
import java.lang.reflect.Method
import java.net.{URL, URLClassLoader}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.serving.core.models.CommonCubeModel

object OperationsHelper {

  /**
    * Given a value transform this value to a milliseconds
    * @param  value with the path of the policy.
    */
  //scalastyle:off
  def parseValueToMilliSeconds(value: String): Long = {
    val Prefix: Seq[String] = Seq("[1-9][0-9]*s", "[1-9][0-9]*m", "[1-9][0-9]*h", "[1-9][0-9]*d", "[1-9][0-9]*y",
      "[1-9][0-9]*M", "second", "minute", "hour", "day", "month", "year")

    val prefix = for {
      prefix <- Prefix
      if (value.matches(prefix))
    } yield (prefix)

    prefix.headOption match {
      case Some("[1-9][0-9]*s") => value.replace("s", "").toLong * 1000
      case Some("[1-9][0-9]*m") => value.replace("m", "").toLong * 60000
      case Some("[1-9][0-9]*h") => value.replace("h", "").toLong * 3600000
      case Some("[1-9][0-9]*d") => value.replace("d", "").toLong * 86400000
      case Some("[1-9][0-9]*M") => value.replace("M", "").toLong * 2628000000L
      case Some("[1-9][0-9]*y") => value.replace("y", "").toLong * 31557600000L
      case Some(value) => parsePrecisionToMillis(value)
      case _ => value.toLong
    }
  }

  private def parsePrecisionToMillis(value: String): Long = {
    value match {
      case "second" => 1000
      case "minute" => 60000
      case "hour"   => 3600000
      case "day"    => 86400000
      case "month"  => 2628000000L
      case "year"   => 31557600000L
    }
  }

  /**
    * Given a cube check several conditions in order to have a well configured policy
    * @param  cube Cube from the policy
    * @return A sequence with the errors
    */
  def checkCubeValues(cube: CommonCubeModel, sparkStreamingWindow: Long): Unit = {}

  /**
    * Given the checkpointInterval and sparkStreamingWindow this function check that de checkpointInterval
    * is at least 5 times greater than the sparkStreamingWindow and also multiples between each other
    * @param  sparkStreamingWindow Value of the spark streaming window
    * @param  checkpointInterval Interval to execute the checkpoint.
    */
  def checkValidCheckpointInterval(sparkStreamingWindow: Long, checkpointInterval: String): Boolean = {
    val interval = parseValueToMilliSeconds(checkpointInterval)
    interval >= sparkStreamingWindow * 5 && interval % sparkStreamingWindow == 0
  }
  /**
    * ComputeLast value has to be greater than the precision in order to prevent data loss
    * @param  computeLast Time of the data before expires.
    * @param  precision Precision of time to aggregate data.
    */
  def checkValidComputeLastValue(computeLast: String, precision: String): Boolean =
    parseValueToMilliSeconds(computeLast) > parseValueToMilliSeconds(precision)
}
