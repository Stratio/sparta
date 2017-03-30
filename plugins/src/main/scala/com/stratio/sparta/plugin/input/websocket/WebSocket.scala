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

import akka.event.slf4j.SLF4JLogging
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.ws.{WebSocketByteListener, WebSocketTextListener, WebSocketUpgradeHandler, WebSocket => WS}

class WebSocket(client: AsyncHttpClient) extends SLF4JLogging {
  self =>

  type OnTextMessageHandler = String => Unit
  type OnBinaryMessageHandler = Array[Byte] => Unit
  type OnWebSocketOperationHandler = WebSocket => Unit
  type OnErrorHandler = Throwable => Unit

  private var ws : Option[WS] = None
  private var textMessageHandlers = scala.collection.mutable.ListBuffer.empty[OnTextMessageHandler]
  private var binaryMessageHandlers = scala.collection.mutable.ListBuffer.empty[OnBinaryMessageHandler]
  private var openHandlers = scala.collection.mutable.ListBuffer.empty[OnWebSocketOperationHandler]
  private var closeHandlers = scala.collection.mutable.ListBuffer.empty[OnWebSocketOperationHandler]
  private var errorHandlers = scala.collection.mutable.ListBuffer.empty[OnErrorHandler]

  def open(url: String): WebSocket = {
    require(url.startsWith("ws://") || url.startsWith("wss://"), "Only ws and wss schemes are supported")
    if (client.isClosed)
      throw new IllegalStateException(
        "Client is closed, please create a new WebSocket instance by calling WebSocket()")

    val handler = new WebSocketUpgradeHandler.Builder().addWebSocketListener(internalWebSocketListener).build()
    ws = Option(client.prepareGet(url).execute(handler).get())

    openHandlers foreach (_ (self))
    this
  }

  protected def internalWebSocketListener = {
    new WebSocketListener {
      def onError(t: Throwable) {
        errorHandlers foreach (_ (t))
      }

      def onMessage(message: String) {
        textMessageHandlers foreach (_ (message))
      }

      def onMessage(message: Array[Byte]) {
        binaryMessageHandlers foreach (_ (message))
      }

      def onClose(ws: WS) {
        closeHandlers foreach (_ (self))
      }

      def onOpen(ws: WS) {
        // onOpen handlers are called from open() after the WebSocket has been initialized
      }

      def onFragment(fragment: String, last: Boolean) {
        log.debug("Fragments not supported in WebSocket")
      }

      def onFragment(fragment: Array[Byte], last: Boolean) {
        log.debug("Fragments not supported in WebSocket")
      }
    }
  }

  def onTextMessage(handler: OnTextMessageHandler): WebSocket = {
    textMessageHandlers += handler
    this
  }

  def onBinaryMessage(handler: OnBinaryMessageHandler): WebSocket = {
    binaryMessageHandlers += handler
    this
  }

  def onOpen(handler: OnWebSocketOperationHandler): WebSocket = {
    openHandlers += handler
    this
  }

  def onClose(handler: OnWebSocketOperationHandler): WebSocket = {
    closeHandlers += handler
    this
  }

  def onError(handler: OnErrorHandler): WebSocket = {
    errorHandlers += handler
    this
  }

  def sendText(message: String): WebSocket = {
    ws match {
      case Some(s) if s.isOpen => s.sendMessage(message)
      case _ => throw new IllegalStateException("WebSocket is closed, use WebSocket.open(String) to reconnect)")
    }
    this
  }

  def send(message: Array[Byte]): WebSocket = {
    ws match {
      case Some(s) if s.isOpen => s.sendMessage(message)
      case _ => throw new IllegalStateException("WebSocket is closed, use WebSocket.open(String) to reconnect)")
    }
    this
  }

  def close(): WebSocket = {
    ws foreach {
      _.close()
    }
    this
  }

  def shutdown() {
    client.close()
  }
}

object WebSocket {

  def apply(): WebSocket = new WebSocket(new AsyncHttpClient())
}

trait WebSocketListener extends WebSocketByteListener with WebSocketTextListener
