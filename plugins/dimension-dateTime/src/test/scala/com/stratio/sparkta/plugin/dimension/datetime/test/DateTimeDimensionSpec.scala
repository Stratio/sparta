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

package com.stratio.sparkta.plugin.dimension.datetime.test

import java.io
import java.util.Date
import com.stratio.sparkta.plugin.dimension.datetime.DateTimeDimension
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import com.stratio.sparkta.sdk.TypeOp

@RunWith(classOf[JUnitRunner])
class DateTimeDimensionSpec extends WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  var dateTimeBucketer: DateTimeBucketer = null
  before {
    dateTimeBucketer = new DateTimeBucketer(Map("second" -> "long", "minute" -> "date", "typeOp" -> "datetime"))
  }

  after {
    dateTimeBucketer = null
  }

  "A DateTimeBucketer" should {
    "In default implementation, get 7 buckets for a specific time" in {
      val newDate = new Date()
      val buckets = dateTimeBucketer.bucket(newDate.asInstanceOf[io.Serializable]).map(_._1.id)

      buckets.size should be(7)

      buckets should contain(DateTimeBucketer.timestamp.id)
      buckets should contain(DateTimeBucketer.SecondName)
      buckets should contain(DateTimeBucketer.MinuteName)
      buckets should contain(DateTimeBucketer.HourName)
      buckets should contain(DateTimeBucketer.DayName)
      buckets should contain(DateTimeBucketer.MonthName)
      buckets should contain(DateTimeBucketer.YearName)
    }

    "Each precision bucket have their output type, second must be long, minute must be date, others datetime" in {
      dateTimeBucketer.bucketTypes(DateTimeBucketer.SecondName).typeOp should be(TypeOp.Long)
      dateTimeBucketer.bucketTypes(DateTimeBucketer.MinuteName).typeOp should be(TypeOp.Date)
      dateTimeBucketer.bucketTypes(DateTimeBucketer.DayName).typeOp should be(TypeOp.DateTime)
      dateTimeBucketer.bucketTypes(DateTimeBucketer.MonthName).typeOp should be(TypeOp.DateTime)
      dateTimeBucketer.bucketTypes(DateTimeBucketer.YearName).typeOp should be(TypeOp.DateTime)
      dateTimeBucketer.bucketTypes(DateTimeBucketer.timestamp.id).typeOp should be(TypeOp.Timestamp)
    }
  }
}
