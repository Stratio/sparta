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

package com.stratio.sparta.driver

import akka.actor.{ActorSystem, Props}
import com.google.common.io.BaseEncoding
import com.stratio.sparta.driver.actor.MarathonAppActor
import com.stratio.sparta.driver.actor.MarathonAppActor.StartApp
import com.stratio.sparta.driver.exception.DriverException
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.utils.PluginsFilesUtils
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success, Try}

object MarathonDriver extends PluginsFilesUtils {

  val NumberOfArguments = 3
  val PolicyIdIndex = 0
  val ZookeeperConfigurationIndex = 1
  val DetailConfigurationIndex = 2

  def main(args: Array[String]): Unit = {
    assert(args.length == NumberOfArguments,
      s"Invalid number of arguments: ${args.length}, args: $args, expected: $NumberOfArguments")
    Try {
      val policyId = args(PolicyIdIndex)
      val zookeeperConf = new String(BaseEncoding.base64().decode(args(ZookeeperConfigurationIndex)))
      val detailConf = new String(BaseEncoding.base64().decode(args(DetailConfigurationIndex)))
      initSpartaConfig(zookeeperConf, detailConf)
      val curatorInstance = CuratorFactoryHolder.getInstance()
      val system = ActorSystem(policyId)
      val marathonAppActor =
        system.actorOf(Props(new MarathonAppActor(curatorInstance)), AkkaConstant.MarathonAppActorName)

      marathonAppActor ! StartApp(policyId)
    } match {
      case Success(_) =>
        log.info("Initiated Marathon App environment")
      case Failure(driverException: DriverException) =>
        log.error(driverException.msg, driverException.getCause)
        throw driverException
      case Failure(exception) =>
        log.error(s"Error initiating Marathon App environment: ${exception.getLocalizedMessage}", exception)
        throw exception
    }
  }

  def initSpartaConfig(zKConfig: String, detailConf: String): Unit = {
    val configStr = s"${detailConf.stripPrefix("{").stripSuffix("}")}" +
      s"\n${zKConfig.stripPrefix("{").stripSuffix("}")}"
    log.info(s"Parsed config: sparta { $configStr }")
    val composedStr = s" sparta { $configStr } "
    SpartaConfig.initMainWithFallbackConfig(ConfigFactory.parseString(composedStr))
  }
}
