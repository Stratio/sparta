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

package com.stratio.sparta.serving.core.helpers

object OperationsHelper {

  /**
    * Given a value transform this value to a milliseconds
    * @param  value with the path of the policy.
    */
  //scalastyle:off
  def parseValueToMilliSeconds(value: String): Long = {
    val Prefix: Seq[String] = Seq("[1-9][0-9]*ms","[1-9][0-9]*s", "[1-9][0-9]*m", "[1-9][0-9]*h", "[1-9][0-9]*d",
      "[1-9][0-9]*y",
      "[1-9][0-9]*M", "second", "minute", "hour", "day", "month", "year")

    val prefix = for {
      prefix <- Prefix
      if (value.matches(prefix))
    } yield (prefix)

    prefix.headOption match {
      case Some("[1-9][0-9]*ms") => value.replace("ms", "").toLong
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
  //scalastyle:on

  private def parsePrecisionToMillis(value: String): Long = {
    value match {
      case "second" => 1000
      case "minute" => 60000
      case "hour"   => 3600000
      case "day"    => 86400000
      case "month"  => 2628000000L
      case "year"   => 31557600000L
      case _ => value.toLong
    }
  }

}
