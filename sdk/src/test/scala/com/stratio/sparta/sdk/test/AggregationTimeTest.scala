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

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.sdk.AggregationTime
import org.scalatest._


class AggregationTimeTest extends FlatSpec with ShouldMatchers {
  val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.ZZZ")
  val date = formatter.parseDateTime("2016-01-19 14:49:19.CET")

  "AggregationTime" should "return the date in millis rounded to 15s" in {

    val result = AggregationTime.truncateDate(date,"15s")
    result should be(1453211355000L)
  }

  "AggregationTime" should "return the date in millis rounded to 15m" in {

    val result = AggregationTime.truncateDate(date,"15m")
    result should be(1453211100000L)
  }

  "AggregationTime" should "return the date in millis rounded to 15h" in {

    val result = AggregationTime.truncateDate(date,"15h")
    result should be(1453194000000L)
  }

  "AggregationTime" should "return the date in millis rounded to 15d" in {

    val result = AggregationTime.truncateDate(date,"15d")
    result should be(1452816000000L)
  }

  "AggregationTime" should "return the date in millis rounded to seconds" in {

    val result = AggregationTime.truncateDate(date,"second")
    result should be(1453211359000L)
  }

  "AggregationTime" should "return the date in millis rounded to minutes" in {

    val result = AggregationTime.truncateDate(date,"minute")
    result should be(1453211340000L)
  }

  "AggregationTime" should "return the date in millis rounded to hours" in {

    val result = AggregationTime.truncateDate(date,"hour")
    result should be(1453208400000L)
  }

  "AggregationTime" should "return the date in millis rounded to days" in {

    val result = AggregationTime.truncateDate(date,"day")
    result should be(1453158000000L)
  }

  "AggregationTime" should "return the date in millis rounded to year" in {

    val result = AggregationTime.truncateDate(date,"year")
    result should be(1451602800000L)
  }

}
