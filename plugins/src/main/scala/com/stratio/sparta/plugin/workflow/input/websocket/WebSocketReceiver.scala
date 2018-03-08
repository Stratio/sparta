/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.websocket

import akka.event.slf4j.SLF4JLogging
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.StructType
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

class WebSocketReceiver(url: String, storageLevel: StorageLevel, outputSchema: StructType)
  extends Receiver[Row](storageLevel) with SLF4JLogging {


  private var webSocket: Option[WebSocket] = None

  def onStart() {
    try {
      log.info("Connecting to WebSocket: " + url)
      val newWebSocket = WebSocket().open(url)
        .onTextMessage { msg: String =>
          store(new GenericRowWithSchema(Array(msg), outputSchema))
        }
        .onBinaryMessage { msg: Array[Byte] =>
          store(new GenericRowWithSchema(Array(new Predef.String(msg)), outputSchema))
        }
      setWebSocket(Option(newWebSocket))
      log.info("Connected to: WebSocket" + url)
    } catch {
      case e: Exception => restart("Error starting WebSocket stream", e)
    }
  }

  def onStop() {
    setWebSocket()
    log.info("WebSocket receiver stopped")
  }

  private def setWebSocket(newWebSocket: Option[WebSocket] = None) = synchronized {
    if (webSocket.isDefined)
      webSocket.get.shutdown()
    webSocket = newWebSocket
  }

}
