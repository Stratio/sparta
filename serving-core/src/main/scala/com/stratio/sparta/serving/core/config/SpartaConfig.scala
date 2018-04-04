/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
  var sparkConfig: Option[Config] = None
  var crossdataConfig: Option[Config] = None
  var apiConfig: Option[Config] = None
  var oauth2Config: Option[Config] = None

  def initConfig(node: String,
                 currentConfig: Option[Config] = None,
                 configFactory: SpartaConfigFactory = SpartaConfigFactory()): Option[Config] = {
    log.debug(s"Loading $node configuration")
    val configResult = currentConfig match {
      case Some(config) => Try(config.getConfig(node)).toOption
      case None => configFactory.getConfig(node)
    }
    assert(configResult.isDefined, s"Fatal Error: configuration can not be loaded: $node")
    configResult
  }

  def initWithFallbackConfig(node: String, currentConfig: Config): Option[Config] = {
    log.debug(s"Loading $node configuration")
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

  def initSparkConfig(currentConfig: Option[Config] = None,
                     configFactory: SpartaConfigFactory = SpartaConfigFactory()): Option[Config] = {
    sparkConfig = initConfig(ConfigSpark, currentConfig, configFactory)
    sparkConfig
  }

  def initCrossdataConfig(currentConfig: Option[Config] = None,
                      configFactory: SpartaConfigFactory = SpartaConfigFactory()): Option[Config] = {
    crossdataConfig = initConfig(ConfigCrossdata, currentConfig, configFactory)
    crossdataConfig
  }

  def initMainWithFallbackConfig(currentConfig: Config): Option[Config] = {
    mainConfig = initWithFallbackConfig(ConfigAppName, currentConfig)
    mainConfig
  }

  def initApiConfig(configFactory: SpartaConfigFactory = SpartaConfigFactory()): Option[Config] = {
    apiConfig = initConfig(ConfigApi, mainConfig, configFactory)
    apiConfig
  }

  def initOauth2Config(configFactory: SpartaConfigFactory = SpartaConfigFactory()): Option[Config] = {
    oauth2Config = initConfig(ConfigOauth2)
    oauth2Config
  }

  def getHdfsConfig: Option[Config] = mainConfig.flatMap(config => getOptionConfig(ConfigHdfs, config))

  def getDetailConfig: Option[Config] = mainConfig.flatMap(config => getOptionConfig(ConfigDetail, config))

  def getZookeeperConfig: Option[Config] = mainConfig.flatMap(config => getOptionConfig(ConfigZookeeper, config))

  def getMarathonConfig: Option[Config] = mainConfig.flatMap(config => getOptionConfig(ConfigMarathon,config))

  def getOauth2Config: Option[Config] = oauth2Config match {
    case Some(config) => Some(config)
    case None =>
      oauth2Config = initOauth2Config()
      oauth2Config
  }

  def getSprayConfig: Option[Config] = SpartaConfigFactory().getConfig(ConfigSpray)

  def initOptionalConfig(node: String,
                         currentConfig: Option[Config] = None,
                         configFactory: SpartaConfigFactory = SpartaConfigFactory()): Option[Config] = {
    log.debug(s" Loading $node configuration")
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
