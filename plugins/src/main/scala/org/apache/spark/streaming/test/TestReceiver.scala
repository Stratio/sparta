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

package org.apache.spark.streaming.test

import akka.event.slf4j.SLF4JLogging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

import scala.util.Random

class TestReceiver(event: String, random: Boolean, maxNumber: Int, storageLevel: StorageLevel)
  extends Receiver[String](storageLevel) with SLF4JLogging {

  def onStart() {
    log.info("Starting test Receiver")
    // Start the thread that receives data over a connection
    new Thread("Test Receiver") {
      override def run() {
        receive()
      }
    }.start()
  }

  def onStop(): Unit = {
    log.info("Stopping test Receiver")
  }

  private def receive() {
    try {
      while (!isStopped) {
        store(if(random) Random.nextInt(maxNumber).toString else event)
      }
      // Restart in an attempt to connect again when server is active again
      restart("Restarting test Receiver")
    } catch {
      case t: Throwable =>
        // restart if there is any other error
        restart("Error receiving data", t)
    }
  }
}
