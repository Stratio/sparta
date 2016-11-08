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

package com.stratio.sparta.sdk.test

import java.text.SimpleDateFormat
import java.util.Date

import com.github.nscala_time.time.Imports._

import com.stratio.sparta.sdk.AggregationTime
import org.scalatest._


class AggregationTimeTest extends FlatSpec with ShouldMatchers {
  val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.ZZZ")
  val date = formatter.parseDateTime("2016-01-19 14:49:19.CET")

  "AggregationTime" should "return the date in millis rounded to 15s" in {

    val result = AggregationTime.truncateDate(date,"15s")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-19 14:49:15.+0100")
  }

  "AggregationTime" should "return the date in millis rounded to 15m" in {

    val result = AggregationTime.truncateDate(date,"15m")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-19 14:45:00.+0100")
  }

  "AggregationTime" should "return the date in millis rounded to 15h" in {

    val result = AggregationTime.truncateDate(date,"15h")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-19 10:00:00.+0100")
  }

  "AggregationTime" should "return the date in millis rounded to 15d" in {

    val result = AggregationTime.truncateDate(date,"15d")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-15 01:00:00.+0100")
  }

  "AggregationTime" should "return the date in millis rounded to seconds" in {

    val result = AggregationTime.truncateDate(date,"second")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-19 14:49:19.+0100")
  }

  "AggregationTime" should "return the date in millis rounded to minutes" in {

    val result = AggregationTime.truncateDate(date,"minute")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-19 14:49:00.+0100")
  }

  "AggregationTime with 45minute" should "return the date in millis rounded to minutes" in {

    val result = AggregationTime.truncateDate(date,"45minute")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-19 14:49:00.+0100")
  }

  "AggregationTime" should "return the date in millis rounded to hours" in {

    val result = AggregationTime.truncateDate(date,"hour")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-19 14:00:00.+0100")
  }

  "AggregationTime with 3hour" should "return the date in millis rounded to hours" in {

    val result = AggregationTime.truncateDate(date,"3hour")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-19 14:00:00.+0100")
  }

  "AggregationTime" should "return the date in millis rounded to days" in {

    val result = AggregationTime.truncateDate(date,"day")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-19 00:00:00.+0100")
  }

  "AggregationTime with 34day" should "return the date in millis rounded to days" in {

    val result = AggregationTime.truncateDate(date,"34day")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-19 00:00:00.+0100")
  }

  "AggregationTime" should "return the date in millis rounded to year" in {

    val result = AggregationTime.truncateDate(date,"year")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-01 00:00:00.+0100")
  }

  "AggregationTime with 2month" should "return the date in millis rounded to month" in {

    val result = AggregationTime.truncateDate(date,"2month")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-01 00:00:00.+0100")
  }

  "AggregationTime with 2year" should "return the date in millis rounded to year" in {

    val result = AggregationTime.truncateDate(date,"2year")

    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ").format(new Date(result)) should be("2016-01-01 00:00:00.+0100")
  }

}
