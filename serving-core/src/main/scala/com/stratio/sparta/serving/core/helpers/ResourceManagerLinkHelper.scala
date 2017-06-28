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

package com.stratio.sparta.serving.core.helpers

import java.net.Socket

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant

import scala.util._

object ResourceManagerLinkHelper extends SLF4JLogging {

  def getLink(executionMode: String,
              sparkMaster: String,
              monitoringLink: Option[String] = None,
              withCheck: Boolean = true): Option[String] = {
    val (host: String, port: Int) = (monitoringLink, executionMode) match {
      case (None, AppConstant.ConfigMesos) | (None, AppConstant.ConfigMarathon) => mesosLink(sparkMaster)
      case (None, AppConstant.ConfigLocal) => localLink
      case (Some(uri), _) => userLink(uri)
      case _ => throw new IllegalArgumentException(s"Wrong value in property executionMode: $executionMode")
    }

    if (withCheck) checkConnectivity(host, port)
    else monitoringLink.orElse(Option(s"http://$host:$port"))
  }

  def checkConnectivity(host: String, port: Int, monitoringLink: Option[String] = None): Option[String] = {
    Try {
      new Socket(host, port)
    } match {
      case Success(socket) =>
        if (socket.isConnected) {
          socket.close()
          monitoringLink.orElse(Option(s"http://$host:$port"))
        } else {
          log.debug(s"Cannot connect to http://$host:$port")
          socket.close()
          monitoringLink
        }
      case Failure(_) =>
        log.debug(s"Cannot connect to http://$host:$port")
        monitoringLink
    }
  }

  private def mesosLink(sparkMaster: String) = {
    val host = sparkMaster.replace("mesos://", "").replaceAll(":\\d+", "")
    val port = 5050
    (host, port)
  }

  private def localLink = {
    val localhostName = java.net.InetAddress.getLocalHost.getHostName
    (localhostName, 4040)
  }

  private def userLink(uri: String) = {
    val host = uri.replace("http://", "").replace("https://", "").replaceAll(":\\d+", "")
    val port = uri.split(":").lastOption.getOrElse("4040").toInt
    (host, port)
  }
}