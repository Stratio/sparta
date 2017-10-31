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

package com.stratio.sparta.serving.core.utils

import akka.actor.Cancellable
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.utils.AggregationTimeUtils
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._

import scala.concurrent.duration._
import scala.util.Try

trait SchedulerUtils extends SLF4JLogging {

  def scheduleOneTask(timeProperty: String, defaultTime: String)(f: ⇒ Unit): Cancellable = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val delay = Try(SpartaConfig.getDetailConfig.get.getString(timeProperty)).toOption
      .flatMap(x => if (x == "") None else Some(x)).getOrElse(defaultTime)

    log.info(s"Starting scheduler task in $timeProperty with time: $delay")
    SchedulerSystem.scheduler.scheduleOnce(AggregationTimeUtils.parseValueToMilliSeconds(delay) milli)(f)
  }

  def scheduleTask(initTimeProperty: String,
                   defaultInitTime: String,
                   intervalTimeProperty: String,
                   defaultIntervalTime: String
                  )(f: ⇒ Unit): Cancellable = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val initialDelay = Try(SpartaConfig.getDetailConfig.get.getString(initTimeProperty)).toOption
      .flatMap(x => if (x == "") None else Some(x)).getOrElse(defaultInitTime)

    val interval = Try(SpartaConfig.getDetailConfig.get.getString(intervalTimeProperty)).toOption
      .flatMap(x => if (x == "") None else Some(x)).getOrElse(defaultIntervalTime)

    log.info(s"Starting scheduler tasks with delay $initTimeProperty with time: $initialDelay and interval " +
      s"$intervalTimeProperty with time: $interval")

    SchedulerSystem.scheduler.schedule(
      AggregationTimeUtils.parseValueToMilliSeconds(initialDelay) milli,
      AggregationTimeUtils.parseValueToMilliSeconds(interval) milli)(f)
  }
}
