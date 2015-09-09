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

package com.stratio.sparkta.plugin.input.websocket

import org.apache.spark.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

import scalawebsocket.WebSocket

class WebSocketReceiver(url: String, storageLevel: StorageLevel)
  extends Receiver[String](storageLevel) with Logging {


  @volatile private var webSocket: WebSocket = _

  def onStart() {
    try {
      logInfo("Connecting to WebSocket: " + url)
      val newWebSocket = WebSocket().open(url).onTextMessage({ msg: String => store(msg) })
      setWebSocket(newWebSocket)
      logInfo("Connected to: WebSocket" + url)
    } catch {
      case e: Exception => restart("Error starting WebSocket stream", e)
    }
  }

  def onStop() {
    setWebSocket(null)
    logInfo("WebSocket receiver stopped")
  }

  private def setWebSocket(newWebSocket: WebSocket) = synchronized {
    if (webSocket != null) {
      webSocket.shutdown()
    }
    webSocket = newWebSocket
  }

}
