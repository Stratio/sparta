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
import com.stratio.sparta.serving.core.constants.AppConstant
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.{Failure, Success, Try}

/**
 * Helper with common operations used to create a Sparta context used to run the application.
 */
object SpartaConfig extends SLF4JLogging {

  var mainConfig: Option[Config] = None
  var apiConfig: Option[Config] = None
  var swaggerConfig: Option[Config] = None
  var sprayConfig: Option[Config] = None

  /**
   * Initializes base configuration.
   *
   * @param currentConfig if it is setted the function tries to load a node from a loaded config.
   * @param node          with the node needed to load the configuration.
   * @return the loaded configuration.
   */
  def initConfig(node: String,
                 currentConfig: Option[Config] = None,
                 configFactory: ConfigFactory = new SpartaConfigFactory): Option[Config] = {
    log.info(s"Loading $node configuration")
    val configResult = currentConfig match {
      case Some(config) => Try(config.getConfig(node)) match {
        case Success(config) => Some(config)
        case _ => None
      }
      case _ => configFactory.getConfig(node)
    }
    assert(configResult.isDefined, s"Fatal Error: configuration can not be loaded: $node")
    configResult
  }

  def initMainConfig(currentConfig: Option[Config] = None,
                     configFactory: ConfigFactory = new SpartaConfigFactory): Option[Config] = {
    mainConfig = initConfig(AppConstant.ConfigAppName, currentConfig, configFactory)
    mainConfig
  }

  def initApiConfig(configFactory: ConfigFactory = new SpartaConfigFactory): Option[Config] = {
    apiConfig = initConfig(AppConstant.ConfigApi, mainConfig, configFactory)
    apiConfig
  }

  def initSwaggerConfig(configFactory: ConfigFactory = new SpartaConfigFactory): Option[Config] = {
    swaggerConfig = initConfig(AppConstant.ConfigSwagger, mainConfig, configFactory)
    swaggerConfig
  }

  def initSprayConfig(configFactory: ConfigFactory = new SpartaConfigFactory): Option[Config] = {
    sprayConfig = configFactory.getConfig(AppConstant.ConfigSpray)
    sprayConfig
  }

  def getClusterConfig(executionMode: Option[String] = None): Option[Config] =
    Try {
      executionMode match {
        case Some(execMode) if execMode.nonEmpty => execMode
        case _ => getDetailConfig.get.getString(AppConstant.ExecutionMode)
      }
    } match {
      case Success(execMode) =>
        if (execMode != AppConstant.ConfigLocal) getOptionConfig(execMode, mainConfig.get)
        else None
      case Failure(exception) =>
        log.error("Error when extracting cluster configuration. ", exception)
        None
    }

  def getHdfsConfig: Option[Config] = mainConfig match {
    case Some(config) => getOptionConfig(AppConstant.ConfigHdfs, config)
    case None => None
  }

  def getDetailConfig: Option[Config] = mainConfig match {
    case Some(config) => getOptionConfig(AppConstant.ConfigDetail, config)
    case None => None
  }

  def getZookeeperConfig: Option[Config] = mainConfig match {
    case Some(config) => getOptionConfig(AppConstant.ConfigZookeeper, config)
    case None => None
  }

  /**
   * Initializes base configuration.
   *
   * @param currentConfig if it is setted the function tries to load a node from a loaded config.
   * @param node          with the node needed to load the configuration.
   * @return the optional loaded configuration.
   */
  def initOptionalConfig(node: String,
                         currentConfig: Option[Config] = None,
                         configFactory: ConfigFactory = new SpartaConfigFactory): Option[Config] = {
    log.info(s"> Loading $node configuration")
    Try(
      currentConfig match {
        case Some(config) => getOptionConfig(node, config)
        case _ => configFactory.getConfig(node)
      }
    ).getOrElse(None)
  }

  def getOptionConfig(node: String, currentConfig: Config): Option[Config] =
    Try(currentConfig.getConfig(node)) match {
      case Success(config) => Some(config)
      case _ => None
    }

  def getOptionStringConfig(node: String, currentConfig: Config): Option[String] =
    Try(currentConfig.getString(node)) match {
      case Success(config) => Some(config)
      case _ => None
    }

  def isHttpsEnabled(): Boolean =
    SpartaConfig.sprayConfig match {
      case Some(config) =>
        Try(config.getValue("ssl-encryption")) match {
          case Success(value) =>
            if ("on".equals(value.unwrapped())) true else false
          case Failure(exception) => false
        }
      case None => false
    }

  def daemonicAkkaConfig: Config = mainConfig match {
    case Some(mainSpartaConfig) =>
      mainSpartaConfig.withFallback(ConfigFactory.load(ConfigFactory.parseString("akka.daemonic=on")))
    case None => ConfigFactory.load(ConfigFactory.parseString("akka.daemonic=on"))
  }
}
