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
package com.stratio.sparkta.sdk

import java.io.Serializable

import com.stratio.sparkta.sdk.WriteOp.WriteOp
import org.apache.spark.streaming.dstream.DStream
import org.joda.time.DateTime

abstract class Output(properties: Map[String, Serializable], val schema : Option[Map[String,WriteOp]])
  extends Parameterizable(properties) {

  if (schema.isEmpty) {
    throw new NullPointerException("schema")
  }

  /*TODO: This produces a NPE
  schema.values.toSet.diff(supportedWriteOps.toSet).toSeq match {
    case Nil =>
    case badWriteOps =>
      throw new Exception(s"The following write ops are not supported by this output: ${badWriteOps.mkString(", ")}")
  }*/

  def supportedWriteOps : Seq[WriteOp]

  def multiplexer : Boolean

  def timeBucket : String

  def granularity : String

  def granularity : String

  def dateFromGranularity(value: DateTime, granularity : String): DateTime

  def persist(stream: DStream[UpdateMetricOperation]) : Unit

  def persist(streams: Seq[DStream[UpdateMetricOperation]]) : Unit =
    streams.foreach(persist)
}


object Output {

  def dateFromGranularity(value: DateTime, granularity : String): DateTime = {
      val secondsDate = new DateTime(value).withMillisOfSecond(0)
      val minutesDate = secondsDate.withSecondOfMinute(0)
      val hourDate = minutesDate.withMinuteOfHour(0)
      val dayDate = hourDate.withHourOfDay(0)
      val monthDate = dayDate.withDayOfMonth(1)
      val yearDate = monthDate.withMonthOfYear(1)

      granularity match {
        case "minute" => minutesDate
        case "hour" => hourDate
        case "day" => dayDate
        case "month" => monthDate
        case "year" => yearDate
        case _ => secondsDate
      }
  }
}
