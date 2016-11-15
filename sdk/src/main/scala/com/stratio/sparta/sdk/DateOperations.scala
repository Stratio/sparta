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

package com.stratio.sparta.sdk

import java.sql.Timestamp
import java.util.Date

import com.github.nscala_time.time.Imports._

object DateOperations {

  def millisToTimeStamp(date: Long): Timestamp = new Timestamp(date)

  def getMillisFromSerializable(date: Any): Long = date match {
    case value if value.isInstanceOf[Timestamp] || value.isInstanceOf[Date]
      || value.isInstanceOf[DateTime] => getMillisFromDateTime(date)
    case value if value.isInstanceOf[Long] => value.asInstanceOf[Long]
    case value if value.isInstanceOf[String] => value.asInstanceOf[String].toLong
    case _ => new DateTime().getMillis
  }

  def getMillisFromDateTime(value: Any): Long = value match {
    case value if value.isInstanceOf[Timestamp] => value.asInstanceOf[Timestamp].getTime
    case value if value.isInstanceOf[Date] => value.asInstanceOf[Date].getTime
    case value if value.isInstanceOf[DateTime] => value.asInstanceOf[DateTime].getMillis
  }

  def subPath(granularity: String, datePattern: Option[String]): String = {
    val suffix = AggregationTime.truncateDate(DateTime.now, granularity)
    if (!datePattern.isDefined || suffix.equals(0L)) s"/$suffix"
    else s"/${DateTimeFormat.forPattern(datePattern.get).print(new DateTime(suffix))}/$suffix"
  }

  def roundDateTime(t: DateTime, d: Duration): DateTime =
    t minus (t.getMillis - (t.getMillis.toDouble / d.getMillis).round * d.getMillis)
}
