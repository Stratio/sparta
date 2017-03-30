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
package com.stratio.sparta.plugin.input.websocket

import org.apache.spark.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver


class WebSocketReceiver(url: String, storageLevel: StorageLevel)
  extends Receiver[String](storageLevel) with Logging {


  private var webSocket: Option[WebSocket] = None

  def onStart() {
    try {
      logInfo("Connecting to WebSocket: " + url)
      val newWebSocket = WebSocket().open(url)
        .onTextMessage({ msg: String => store(msg) })
        .onBinaryMessage({ msg: Array[Byte] => store(new Predef.String(msg)) })
      setWebSocket(Option(newWebSocket))
      logInfo("Connected to: WebSocket" + url)
    } catch {
      case e: Exception => restart("Error starting WebSocket stream", e)
    }
  }

  def onStop() {
    setWebSocket()
    logInfo("WebSocket receiver stopped")
  }

  private def setWebSocket(newWebSocket: Option[WebSocket] = None) = synchronized {
    if (webSocket.isDefined)
      webSocket.get.shutdown()
    webSocket = newWebSocket
  }

}
