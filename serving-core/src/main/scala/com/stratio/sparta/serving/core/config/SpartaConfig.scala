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

package com.stratio.sparta.serving.core.config

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.{Success, Try}

/**
  * Helper with common operations used to create a Sparta context used to run the application.
  */
object SpartaConfig extends SLF4JLogging {

  var mainConfig: Option[Config] = None
  var apiConfig: Option[Config] = None

  def initConfig(node: String,
                 currentConfig: Option[Config] = None,
                 configFactory: SpartaConfigFactory = SpartaConfigFactory()): Option[Config] = {
    log.info(s"Loading $node configuration")
    val configResult = currentConfig match {
      case Some(config) => Try(config.getConfig(node)).toOption
      case None => configFactory.getConfig(node)
    }
    assert(configResult.isDefined, s"Fatal Error: configuration can not be loaded: $node")
    configResult
  }

  def initWithFallbackConfig(node: String, currentConfig: Config): Option[Config] = {
    log.info(s"Loading $node configuration")
    val mergedConfig = ConfigFactory.load().withFallback(currentConfig)
    val configResult = Try(mergedConfig.getConfig(node)).toOption
    assert(configResult.isDefined, s"Fatal Error: configuration can not be loaded: $node")
    configResult
  }

  def initMainConfig(currentConfig: Option[Config] = None,
                     configFactory: SpartaConfigFactory = SpartaConfigFactory()): Option[Config] = {
    mainConfig = initConfig(ConfigAppName, currentConfig, configFactory)
    mainConfig
  }

  def initMainWithFallbackConfig(currentConfig: Config): Option[Config] = {
    mainConfig = initWithFallbackConfig(ConfigAppName, currentConfig)
    mainConfig
  }

  def initApiConfig(configFactory: SpartaConfigFactory = SpartaConfigFactory()): Option[Config] = {
    apiConfig = initConfig(ConfigApi, mainConfig, configFactory)
    apiConfig
  }

  def getClusterConfig(executionMode: Option[String] = None): Option[Config] =
    Try {
      executionMode match {
        case Some(execMode) if execMode.nonEmpty => execMode
        case _ => getDetailConfig.get.getString(ExecutionMode)
      }
    } match {
      case Success(execMode) if execMode != ConfigLocal =>
        getOptionConfig(execMode, mainConfig.get)
      case _ =>
        log.error("Error when extracting cluster configuration")
        None
    }

  def getHdfsConfig: Option[Config] = mainConfig.flatMap(config => getOptionConfig(ConfigHdfs, config))

  def getDetailConfig: Option[Config] = mainConfig.flatMap(config => getOptionConfig(ConfigDetail, config))

  def getZookeeperConfig: Option[Config] = mainConfig.flatMap(config => getOptionConfig(ConfigZookeeper, config))

  def getSprayConfig: Option[Config] = SpartaConfigFactory().getConfig(ConfigSpray)

  def initOptionalConfig(node: String,
                         currentConfig: Option[Config] = None,
                         configFactory: SpartaConfigFactory = SpartaConfigFactory()): Option[Config] = {
    log.info(s" Loading $node configuration")
    Try(
      currentConfig match {
        case Some(config) => getOptionConfig(node, config)
        case None => configFactory.getConfig(node)
      }
    ).getOrElse(None)
  }

  def getOptionConfig(node: String, currentConfig: Config): Option[Config] =
    Try(currentConfig.getConfig(node)).toOption

  def getOptionStringConfig(node: String, currentConfig: Config): Option[String] =
    Try(currentConfig.getString(node)).toOption

  def daemonicAkkaConfig: Config = mainConfig match {
    case Some(mainSpartaConfig) =>
      mainSpartaConfig.withFallback(ConfigFactory.load(ConfigFactory.parseString("akka.daemonic=on")))
    case None => ConfigFactory.load(ConfigFactory.parseString("akka.daemonic=on"))
  }

}
