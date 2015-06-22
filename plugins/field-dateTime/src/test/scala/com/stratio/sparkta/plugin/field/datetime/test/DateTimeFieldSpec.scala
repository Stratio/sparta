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

import java.io
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
    "In default implementation, get 7 dimensions for a specific time" in {
      val newDate = new Date()
      val precisions = dateTimeDimension.dimensionValues(newDate.asInstanceOf[io.Serializable]).map(_._1.id)

      precisions.size should be(7)

      precisions should contain(DateTimeField.timestamp.id)
      precisions should contain(DateTimeField.SecondName)
      precisions should contain(DateTimeField.MinuteName)
      precisions should contain(DateTimeField.HourName)
      precisions should contain(DateTimeField.DayName)
      precisions should contain(DateTimeField.MonthName)
      precisions should contain(DateTimeField.YearName)
    }

    "Each precision dimension have their output type, second must be long, minute must be date, others datetime" in {
      dateTimeDimension.precisions(DateTimeField.SecondName).typeOp should be(TypeOp.Long)
      dateTimeDimension.precisions(DateTimeField.MinuteName).typeOp should be(TypeOp.Date)
      dateTimeDimension.precisions(DateTimeField.DayName).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precisions(DateTimeField.MonthName).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precisions(DateTimeField.YearName).typeOp should be(TypeOp.DateTime)
      dateTimeDimension.precisions(DateTimeField.timestamp.id).typeOp should be(TypeOp.Timestamp)
    }
  }
}
