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

/**
 * Created by ajnavarro on 9/10/14.
 */
@RunWith(classOf[JUnitRunner])
class DateTimeDimensionSpec extends WordSpecLike with Matchers {

  val NumExpectedBuckets = 7

  "A DateTimeDimension" should {
    "In default implementation, get 5 buckets for a specific time" in {
      val dateTimeDimension = new DateTimeDimension()
      val newDate = new Date()
      val buckets = dateTimeDimension.bucket(newDate.asInstanceOf[io.Serializable])

      buckets.size should be(NumExpectedBuckets)

      buckets.keys should contain(DateTimeDimension.timestamp)
      buckets.keys should contain(DateTimeDimension.seconds)
      buckets.keys should contain(DateTimeDimension.minutes)
      buckets.keys should contain(DateTimeDimension.hours)
      buckets.keys should contain(DateTimeDimension.days)
      buckets.keys should contain(DateTimeDimension.months)
      buckets.keys should contain(DateTimeDimension.years)
    }
  }
}
