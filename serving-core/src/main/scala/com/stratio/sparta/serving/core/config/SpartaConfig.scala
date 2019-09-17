/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.config

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant.{ConfigBootstrap, ConfigCatalog, ConfigDebug, ConfigValidator, _}
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

/**
  * Helper with common operations used to create a Sparta context used to run the application.
  */
//scalastyle:off
object SpartaConfig extends SLF4JLogging {

  private var mainConfig: Option[Config] = None
  private var sparkConfig: Option[Config] = None
  private var crossdataConfig: Option[Config] = None
  private var apiConfig: Option[Config] = None
  private var oauth2Config: Option[Config] = None
  private var headersAuthConfig: Option[Config] = None
  private[core] var postgresConfig: Option[Config] = None
  private var zookeeperConfig: Option[Config] = None
  private var hdfsConfig: Option[Config] = None
  private var detailConfig: Option[Config] = None
  private var marathonConfig: Option[Config] = None
  private var sprayConfig: Option[Config] = None
  private var securityConfig: Option[Config] = None
  private var intelligenceConfig: Option[Config] = None
  private var igniteConfig: Option[Config] = None
  private var lineageConfig: Option[Config] = None
  private var s3Config: Option[Config] = None
  private var sftpConfig: Option[Config] = None
  private var bootstrapConfig: Option[Config] = None
  private var debugConfig: Option[Config] = None
  private var validatorConfig: Option[Config] = None
  private var catalogConfig: Option[Config] = None

  def getSpartaConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initMainConfig(fromConfig)
    else mainConfig.orElse(initMainConfig(fromConfig))

  def getSparkConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initSparkConfig(fromConfig)
    else sparkConfig.orElse(initSparkConfig(fromConfig))

  def getCrossdataConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initCrossdataConfig(fromConfig)
    else crossdataConfig.orElse(initCrossdataConfig(fromConfig))

  def getApiConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initApiConfig(fromConfig)
    else apiConfig.orElse(initApiConfig(fromConfig))

  def getOauth2Config(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initOauth2Config(fromConfig)
    else oauth2Config.orElse(initOauth2Config(fromConfig))

  def getAuthViaHeadersConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[AuthViaHeadersConfig] = {
    val optConfig =
      if(force) initAuthViaHeadersConfig(fromConfig)
      else headersAuthConfig.orElse(initAuthViaHeadersConfig(fromConfig))

    optConfig.map{ config =>
      AuthViaHeadersConfig(
        Try(config.getString("enabled").toBoolean).getOrElse(false),
        Try(config.getString("user")).getOrElse("USER_HEADER"),
        Try(config.getString("group")).getOrElse("GROUP_HEADER")
      )
    }
  }

  def getPostgresConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initPostgresConfig(fromConfig)
    else postgresConfig.orElse(initPostgresConfig(fromConfig))

  def getZookeeperConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initZookeeperConfig(fromConfig)
    else zookeeperConfig.orElse(initZookeeperConfig(fromConfig))

  def getHdfsConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initHdfsConfig(fromConfig)
    else hdfsConfig.orElse(initHdfsConfig(fromConfig))

  def getDetailConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initDetailConfig(fromConfig)
    else detailConfig.orElse(initDetailConfig(fromConfig))

  def getMarathonConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initMarathonConfig(fromConfig)
    else marathonConfig.orElse(initMarathonConfig(fromConfig))

  def getSprayConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initSprayConfig(fromConfig)
    else sprayConfig.orElse(initSprayConfig(fromConfig))

  def getSecurityConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initSecurityConfig(fromConfig)
    else securityConfig.orElse(initSecurityConfig(fromConfig))

  def getIntelligenceConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initIntelligenceConfig(fromConfig)
    else intelligenceConfig.orElse(initIntelligenceConfig(fromConfig))

  def getIgniteConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initIgniteConfig(fromConfig)
    else igniteConfig.orElse(initIgniteConfig(fromConfig))

  def getGovernanceConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initGovernanceConfig(fromConfig)
    else lineageConfig.orElse(initGovernanceConfig(fromConfig))

  def getS3Config(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initS3Config(fromConfig)
    else s3Config.orElse(initS3Config(fromConfig))

  def getSftpConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initSftpConfig(fromConfig)
    else sftpConfig.orElse(initSftpConfig(fromConfig))

  def getBootstrapConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initBootstrapConfig(fromConfig)
    else bootstrapConfig.orElse(initBootstrapConfig(fromConfig))

  def getDebugConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initDebugConfig(fromConfig)
    else debugConfig.orElse(initDebugConfig(fromConfig))

  def getValidatorConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initValidatorConfig(fromConfig)
    else validatorConfig.orElse(initValidatorConfig(fromConfig))

  def getCatalogConfig(fromConfig: Option[Config] = None, force: Boolean = false): Option[Config] =
    if(force) initValidatorConfig(fromConfig)
    else catalogConfig.orElse(initCatalogConfig(fromConfig))

  def daemonicAkkaConfig: Config = mainConfig match {
    case Some(mainSpartaConfig) =>
      mainSpartaConfig.withFallback(ConfigFactory.load(ConfigFactory.parseString("akka.daemonic=on")))
    case None => ConfigFactory.load(ConfigFactory.parseString("akka.daemonic=on"))
  }


  /* PRIVATE METHODS */
  private[config] def initConfig(
                                  node: String,
                                  configFactory: SpartaConfigFactory = SpartaConfigFactory()
                                ): Option[Config] = {
    log.debug(s"Loading $node configuration")
    val configResult = configFactory.getConfig(node)
    assert(configResult.isDefined, s"Fatal Error: configuration can not be loaded: $node")
    configResult
  }

  private[config] def initMainConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    mainConfig = initConfig(ConfigAppName, configFactory)
    mainConfig
  }

  private[config] def initCrossdataConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    crossdataConfig = initConfig(ConfigCrossdata, configFactory)
    crossdataConfig
  }

  private[config] def initApiConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    apiConfig = initConfig(ConfigApi, configFactory)
    apiConfig
  }

  private[config] def initSparkConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    sparkConfig = initConfig(ConfigSpark, configFactory)
    sparkConfig
  }

  private[config] def initOauth2Config(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    oauth2Config = initConfig(ConfigOauth2, configFactory)
    oauth2Config
  }

  private[config] def initAuthViaHeadersConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    headersAuthConfig = initConfig(ConfigAuthViaHeaders, configFactory)
    headersAuthConfig
  }

  private[config] def initPostgresConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    postgresConfig = initConfig(ConfigPostgres, configFactory)
    postgresConfig
  }

  private[config] def initZookeeperConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    zookeeperConfig = initConfig(ConfigZookeeper, configFactory)
    zookeeperConfig
  }

  private[config] def initHdfsConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    hdfsConfig = initConfig(ConfigHdfs, configFactory)
    hdfsConfig
  }

  private[config] def initDetailConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    detailConfig = initConfig(ConfigDetail, configFactory)
    detailConfig
  }

  private[config] def initMarathonConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    marathonConfig = initConfig(ConfigMarathon, configFactory)
    marathonConfig
  }

  private[config] def initSprayConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    sprayConfig = initConfig(ConfigSpray, configFactory)
    sprayConfig
  }

  private[config] def initSecurityConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    securityConfig = initConfig(ConfigSecurity, configFactory)
    securityConfig
  }

  private[config] def initIntelligenceConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    intelligenceConfig = initConfig(ConfigIntelligence, configFactory)
    intelligenceConfig
  }

  private[config] def initIgniteConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    igniteConfig = initConfig(ConfigIgnite, configFactory)
    igniteConfig
  }

  private[config] def initGovernanceConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    lineageConfig = initConfig(ConfigGovernance, configFactory)
    lineageConfig
  }

  private[config] def initS3Config(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    s3Config = initConfig(ConfigS3, configFactory)
    s3Config
  }

  private[config] def initSftpConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    sftpConfig = initConfig(ConfigSftp, configFactory)
    sftpConfig
  }

  private[config] def initBootstrapConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    bootstrapConfig = initConfig(ConfigBootstrap, configFactory)
    bootstrapConfig
  }

  private[config] def initDebugConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    debugConfig = initConfig(ConfigDebug, configFactory)
    debugConfig
  }

  private[config] def initValidatorConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    validatorConfig = initConfig(ConfigValidator, configFactory)
    validatorConfig
  }

  private[config] def initCatalogConfig(fromConfig: Option[Config] = None): Option[Config] = {
    val configFactory = SpartaConfigFactory(fromConfig)
    catalogConfig = initConfig(ConfigCatalog, configFactory)
    catalogConfig
  }
}