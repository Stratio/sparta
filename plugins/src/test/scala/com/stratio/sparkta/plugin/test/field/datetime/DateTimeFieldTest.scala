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

package com.stratio.sparkta.plugin.test.field.datetime

import java.util.Date
import com.stratio.sparkta.plugin.field.datetime.DateTimeField
import com.stratio.sparkta.sdk.TypeOp
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class DateTimeFieldTest extends WordSpecLike with Matchers {

  val dateTimeDimension: DateTimeField =
    new DateTimeField(Map("second" -> "long", "minute" -> "date", "typeOp" -> "datetime"))

  "A DateTimeDimension" should {
    "In default implementation, get 6 dimensions for a specific time" in {
      val newDate = new Date()
      val precision5s =
        dateTimeDimension.precisionValue(DateTimeField.s5Name, newDate.asInstanceOf[Any])
      val precision10s =
        dateTimeDimension.precisionValue(DateTimeField.s10Name, newDate.asInstanceOf[Any])
      val precision15s =
        dateTimeDimension.precisionValue(DateTimeField.s15Name, newDate.asInstanceOf[Any])
      val precisionSecond =
        dateTimeDimension.precisionValue(DateTimeField.SecondName, newDate.asInstanceOf[Any])
      val precisionMinute =
        dateTimeDimension.precisionValue(DateTimeField.MinuteName, newDate.asInstanceOf[Any])
      val precisionHour =
        dateTimeDimension.precisionValue(DateTimeField.HourName, newDate.asInstanceOf[Any])
      val precisionDay =
        dateTimeDimension.precisionValue(DateTimeField.DayName, newDate.asInstanceOf[Any])
      val precisionMonth =
        dateTimeDimension.precisionValue(DateTimeField.MonthName, newDate.asInstanceOf[Any])
      val precisionYear =
        dateTimeDimension.precisionValue(DateTimeField.YearName, newDate.asInstanceOf[Any])

      precision5s._1.id should be(DateTimeField.s5Name)
      precision10s._1.id should be(DateTimeField.s10Name)
      precision15s._1.id should be(DateTimeField.s15Name)
      precisionSecond._1.id should be(DateTimeField.SecondName)
      precisionMinute._1.id should be(DateTimeField.MinuteName)
      precisionHour._1.id should be(DateTimeField.HourName)
      precisionDay._1.id should be(DateTimeField.DayName)
      precisionMonth._1.id should be(DateTimeField.MonthName)
      precisionYear._1.id should be(DateTimeField.YearName)
    }

    "Each precision dimension have their output type, second must be long, minute must be date, others datetime" in {
      dateTimeDimension.precision(DateTimeField.s5Name).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precision(DateTimeField.s10Name).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precision(DateTimeField.s15Name).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precision(DateTimeField.SecondName).typeOp should be(TypeOp.Long)
      dateTimeDimension.precision(DateTimeField.MinuteName).typeOp should be(TypeOp.Date)
      dateTimeDimension.precision(DateTimeField.DayName).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precision(DateTimeField.MonthName).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precision(DateTimeField.YearName).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precision(DateTimeField.timestamp.id).typeOp should be(TypeOp.Timestamp)
    }
  }
}
