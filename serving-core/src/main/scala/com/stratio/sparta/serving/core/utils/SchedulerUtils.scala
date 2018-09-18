/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import akka.actor.{ActorRef, Cancellable}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.AggregationTimeHelper
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._

import scala.concurrent.duration._
import scala.util.Try

trait SchedulerUtils extends SLF4JLogging {

  def scheduleOneTask(timeProperty: String, defaultTime: String)(f: ⇒ Unit): Cancellable = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val delay = Try(SpartaConfig.getDetailConfig().get.getString(timeProperty)).toOption
      .flatMap(x => if (x == "") None else Some(x)).getOrElse(defaultTime)

    log.info(s"Starting scheduler task in $timeProperty with time: $delay")
    SchedulerSystem.scheduler.scheduleOnce(AggregationTimeHelper.parseValueToMilliSeconds(delay) milli)(f)
  }

  def scheduleTask(initTimeProperty: String,
                   defaultInitTime: String,
                   intervalTimeProperty: String,
                   defaultIntervalTime: String
                  )(f: ⇒ Unit): Cancellable = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val initialDelay = Try(SpartaConfig.getDetailConfig().get.getString(initTimeProperty)).toOption
      .flatMap(x => if (x == "") None else Some(x)).getOrElse(defaultInitTime)

    val interval = Try(SpartaConfig.getDetailConfig().get.getString(intervalTimeProperty)).toOption
      .flatMap(x => if (x == "") None else Some(x)).getOrElse(defaultIntervalTime)

    log.info(s"Starting scheduler tasks with delay $initTimeProperty with time: $initialDelay and interval " +
      s"$intervalTimeProperty with time: $interval")

    SchedulerSystem.scheduler.schedule(
      AggregationTimeHelper.parseValueToMilliSeconds(initialDelay) milli,
      AggregationTimeHelper.parseValueToMilliSeconds(interval) milli)(f)
  }

  def scheduleMsg(initTimeProperty: String,
                  defaultInitTime: String,
                  intervalTimeProperty: String,
                  defaultIntervalTime: String,
                  receiverActor: ActorRef,
                  msg: Any
                 ): Cancellable = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val initialDelay = Try(SpartaConfig.getDetailConfig().get.getString(initTimeProperty)).toOption
      .flatMap(x => if (x == "") None else Some(x)).getOrElse(defaultInitTime)

    val interval = Try(SpartaConfig.getDetailConfig().get.getString(intervalTimeProperty)).toOption
      .flatMap(x => if (x == "") None else Some(x)).getOrElse(defaultIntervalTime)

    log.info(s"Starting scheduling of msg reception with delay " +
      s"$initTimeProperty with time: $initialDelay and interval " +
      s"$intervalTimeProperty with time: $interval")

    SchedulerSystem.scheduler.schedule(
      AggregationTimeHelper.parseValueToMilliSeconds(initialDelay) milli, //Initial delay
      AggregationTimeHelper.parseValueToMilliSeconds(interval) milli, //Interval
      receiverActor,
      msg)
  }
}
