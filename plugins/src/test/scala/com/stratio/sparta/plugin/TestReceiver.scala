/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin

import akka.event.slf4j.SLF4JLogging
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

class TestReceiver(event: GenericRowWithSchema, maxEvents: Int, storageLevel: StorageLevel)
  extends Receiver[GenericRowWithSchema](storageLevel) with SLF4JLogging {

  private var eventsGenerated = 0L

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
      while (!isStopped && eventsGenerated <= maxEvents) {
        store(event)
        eventsGenerated += 1
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