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
import com.stratio.sparta.sdk.utils.AggregationTime
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._

import scala.concurrent.duration._
import scala.util.Try

trait SchedulerUtils extends SLF4JLogging {

  def scheduleOneTask(timeProperty: String, defaultTime: String)(f: ⇒ Unit): Cancellable = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val awaitContextStop = Try(SpartaConfig.getDetailConfig.get.getString(timeProperty)).toOption
      .flatMap(x => if (x == "") None else Some(x)).getOrElse(defaultTime)

    log.info(s"Starting scheduler task, with time: $timeProperty - $awaitContextStop")
    SchedulerSystem.scheduler.scheduleOnce(AggregationTime.parseValueToMilliSeconds(awaitContextStop) milli)(f)
  }
}
