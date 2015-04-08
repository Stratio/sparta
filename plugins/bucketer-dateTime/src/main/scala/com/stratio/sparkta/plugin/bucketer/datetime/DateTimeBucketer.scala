
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
package com.stratio.sparkta.plugin.bucketer.datetime

import java.io.{Serializable => JSerializable}
import java.util.Date

import com.stratio.sparkta.plugin.bucketer.datetime.DateTimeBucketer._
import com.stratio.sparkta.sdk.{BucketType, Bucketer}
import org.joda.time.DateTime

/**
 * Created by ajnavarro on 9/10/14.
 */
case class DateTimeBucketer(props: Map[String, JSerializable]) extends Bucketer with JSerializable {

  def this() {
    this(Map((GRANULARITY_PROPERTY_NAME, DEFAULT_GRANULARITY)))
  }

  override val properties: Map[String, JSerializable] = props

  override val bucketTypes: Seq[BucketType] = Seq(timestamp, seconds, minutes, hours, days, months, years)

  override def bucket(value: JSerializable): Map[BucketType, JSerializable] =
    bucketTypes.map(bucketType =>
      bucketType -> DateTimeBucketer.bucket(value.asInstanceOf[Date], bucketType, properties)
    ).toMap
}

object DateTimeBucketer {

  def dateFromGranularity(value: Date, granularity : JSerializable): DateTime = {
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

  private def bucket(value: Date, bucketType: BucketType, properties: Map[String, JSerializable]): JSerializable = {

    dateFromGranularity(value , bucketType match {
      case t if t == timestamp => properties.contains(GRANULARITY_PROPERTY_NAME) match {
        case true => properties.get(GRANULARITY_PROPERTY_NAME).get
        case _ => DEFAULT_GRANULARITY
      }
      case _ => bucketType.id
    }).toDate.asInstanceOf[JSerializable]
  }

  private final val DEFAULT_GRANULARITY = "second".asInstanceOf[JSerializable]
  private final val GRANULARITY_PROPERTY_NAME = "granularity"
  val seconds = new BucketType("second")
  val minutes = new BucketType("minute")
  val hours = new BucketType("hour")
  val days = new BucketType("day")
  val months = new BucketType("month")
  val years = new BucketType("year")
  val timestamp = Bucketer.timestamp
}
