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

package com.stratio.sparkta.serving.core

import scala.util.Success
import scala.util.Try

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.Config

import com.stratio.sparkta.serving.core.constants.AppConstant

/**
 * Helper with common operations used to create a Sparkta context used to run the application.
 * @author danielcsant
 */
object SparktaConfig extends SLF4JLogging {

  var mainConfig: Option[Config] = None
  var apiConfig: Option[Config] = None
  var swaggerConfig: Option[Config] = None

  /**
   * Initializes Sparkta's base path.
   * @return the object described above.
   */
  var sparktaHome: String = {
    val system = new SparktaSystem
    val sparktaHome = system.getenv("SPARKTA_HOME").orElse({
      val sparktaHomeDefault = system.getProperty("user.dir", "./")
      log.warn("SPARKTA_HOME environment variable is not set, setting to default value")
      sparktaHomeDefault
    })
    assert(sparktaHome.isDefined, "Fatal error: sparktaHome not found.")
    log.info(s"> Setting configuration path to ${sparktaHome.get}")
    sparktaHome.get
  }

  /**
   * Initializes base configuration.
   * @param currentConfig if it is setted the function tries to load a node from a loaded config.
   * @param node with the node needed to load the configuration.
   * @return the loaded configuration.
   */
  def initConfig(node: String,
                 currentConfig: Option[Config] = None,
                 configFactory: ConfigFactory = new SparktaConfigFactory): Option[Config] = {
    log.info(s"> Loading $node configuration")
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
                     configFactory: ConfigFactory = new SparktaConfigFactory): Option[Config] = {
    mainConfig = initConfig(AppConstant.ConfigAppName, currentConfig, configFactory)
    mainConfig
  }

  def initApiConfig(configFactory: ConfigFactory = new SparktaConfigFactory): Option[Config] = {
    apiConfig = initConfig(AppConstant.ConfigApi, mainConfig, configFactory)
    apiConfig
  }

  def initSwaggerConfig(configFactory: ConfigFactory = new SparktaConfigFactory): Option[Config] = {
    swaggerConfig = initConfig(AppConstant.ConfigSwagger, mainConfig, configFactory)
    swaggerConfig
  }

  def getClusterConfig: Option[Config] = Try(getDetailConfig.get.getString(AppConstant.ExecutionMode)) match {
    case Success(executionMode) => {
      if (executionMode != AppConstant.ConfigLocal) getOptionConfig(executionMode, mainConfig.get)
      else None
    }
    case _ => None
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
   * @param currentConfig if it is setted the function tries to load a node from a loaded config.
   * @param node with the node needed to load the configuration.
   * @return the optional loaded configuration.
   */
  def initOptionalConfig(node: String,
                         currentConfig: Option[Config] = None,
                         configFactory: ConfigFactory = new SparktaConfigFactory): Option[Config] = {
    log.info(s"> Loading $node configuration")
    Try(
      currentConfig match {
        case Some(config) => getOptionConfig(node, config)
        case _ => configFactory.getConfig(node)
      }
    ).getOrElse(None)
  }

  def getOptionConfig(node: String, currentConfig: Config): Option[Config] = {
    Try(currentConfig.getConfig(node)) match {
      case Success(config) => Some(config)
      case _ => None
    }
  }

  def getOptionStringConfig(node: String, currentConfig: Config): Option[String] = {
    Try(currentConfig.getString(node)) match {
      case Success(config) => Some(config)
      case _ => None
    }
  }
}
