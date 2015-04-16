/**
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
package org.apache.spark.streaming

/**
 * Created by arincon on 15/04/15.
 */
import org.apache.spark.util.ManualClock

class ClockWrapper(ssc: StreamingContext) {

  private val manualClock = ssc.scheduler.clock.asInstanceOf[ManualClock]

  def getTimeMillis: Long = manualClock.getTimeMillis()

  def setTime(timeToSet: Long) : Unit= manualClock.setTime(timeToSet)

  def advance(timeToAdd: Long) : Unit = manualClock.advance(timeToAdd)

  def waitTillTime(targetTime: Long): Long = manualClock.waitTillTime(targetTime)

}
