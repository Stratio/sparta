/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
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

package com.stratio.sparkta.plugin.dimension.datetime

import java.io.{Serializable => JSerializable}
import java.util.Date

import akka.event.slf4j.SLF4JLogging
import org.joda.time.DateTime

import DateTimeDimension._
import com.stratio.sparkta.sdk._

case class DateTimeDimension(props: Map[String, JSerializable]) extends Bucketer with JSerializable with SLF4JLogging {

  def this() {
    this(Map((GRANULARITY_PROPERTY_NAME, DEFAULT_GRANULARITY)))
  }

  override val properties: Map[String, JSerializable] = props

  override val bucketTypes: Seq[BucketType] = Seq(timestamp, seconds, minutes, hours, days, months, years)

  @throws(classOf[ClassCastException])
  override def bucket(value: JSerializable): Map[BucketType, JSerializable] =
    try {
      bucketTypes.map(bucketType =>
        bucketType -> DateTimeDimension.bucket(value.asInstanceOf[Date], bucketType, properties)
      ).toMap
    }
    catch {
      case cce: ClassCastException => log.error("Error parsing " + value + " ."); throw cce;
    }
}

object DateTimeDimension {

  private final val DEFAULT_GRANULARITY = "second"
  private final val GRANULARITY_PROPERTY_NAME = "granularity"
  val seconds = new BucketType("second")
  val minutes = new BucketType("minute")
  val hours = new BucketType("hour")
  val days = new BucketType("day")
  val months = new BucketType("month")
  val years = new BucketType("year")
  val timestamp = Bucketer.timestamp

  private def bucket(value: Date, bucketType: BucketType, properties: Map[String, JSerializable]): JSerializable = {
    DateOperations.dateFromGranularity(new DateTime(value), bucketType match {
      case t if t == timestamp => properties.contains(GRANULARITY_PROPERTY_NAME) match {
        case true => properties.get(GRANULARITY_PROPERTY_NAME).get.toString
        case _ => DEFAULT_GRANULARITY
      }
      case _ => bucketType.id
    }).asInstanceOf[JSerializable]
  }
}
