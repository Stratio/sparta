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

package com.stratio.sparkta.plugin.field.datetime.test

import java.io.{Serializable => JSerializable}
import java.util.Date

import com.stratio.sparkta.plugin.field.datetime.DateTimeField
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import com.stratio.sparkta.sdk.TypeOp

@RunWith(classOf[JUnitRunner])
class DateTimeFieldSpec extends WordSpecLike with Matchers {

  val dateTimeDimension: DateTimeField =
    new DateTimeField(Map("second" -> "long", "minute" -> "date", "typeOp" -> "datetime"))

  "A DateTimeDimension" should {
    "In default implementation, get 6 dimensions for a specific time" in {
      val newDate = new Date()
      val precisionSecond =
        dateTimeDimension.precisionValue(DateTimeField.SecondName, newDate.asInstanceOf[JSerializable])
      val precisionMinute =
        dateTimeDimension.precisionValue(DateTimeField.MinuteName, newDate.asInstanceOf[JSerializable])
      val precisionHour =
        dateTimeDimension.precisionValue(DateTimeField.HourName, newDate.asInstanceOf[JSerializable])
      val precisionDay =
        dateTimeDimension.precisionValue(DateTimeField.DayName, newDate.asInstanceOf[JSerializable])
      val precisionMonth =
        dateTimeDimension.precisionValue(DateTimeField.MonthName, newDate.asInstanceOf[JSerializable])
      val precisionYear =
        dateTimeDimension.precisionValue(DateTimeField.YearName, newDate.asInstanceOf[JSerializable])

      precisionSecond._1.id should be(DateTimeField.SecondName)
      precisionMinute._1.id should be(DateTimeField.MinuteName)
      precisionHour._1.id should be(DateTimeField.HourName)
      precisionDay._1.id should be(DateTimeField.DayName)
      precisionMonth._1.id should be(DateTimeField.MonthName)
      precisionYear._1.id should be(DateTimeField.YearName)
    }

    "Each precision dimension have their output type, second must be long, minute must be date, others datetime" in {
      dateTimeDimension.precision(DateTimeField.SecondName).typeOp should be(TypeOp.Long)
      dateTimeDimension.precision(DateTimeField.MinuteName).typeOp should be(TypeOp.Date)
      dateTimeDimension.precision(DateTimeField.DayName).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precision(DateTimeField.MonthName).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precision(DateTimeField.YearName).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precision(DateTimeField.timestamp.id).typeOp should be(TypeOp.DateTime)
    }
  }
}
