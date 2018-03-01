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


package com.stratio.sparta.serving.core.factory

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.typesafe.config.Config
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.utils.CloseableUtils

import scala.util.{Failure, Success, Try}

/**
  * Customized factory that encapsulates the real CuratorFrameworkFactory and creates a singleton instance of it.
  */
object CuratorFactoryHolder extends SLF4JLogging {

  private var curatorFramework: Option[CuratorFramework] = None
  final val ZKConfigPrefix = AppConstant.ConfigZookeeper

  def setInstance(curatorInstance: CuratorFramework) : Unit = {
    resetInstance()
    curatorFramework = Option(curatorInstance)
  }

  /**
    * Gets a new instance of a CuratorFramework if it was not created before.
    * @return a singleton instance of CuratorFramework.
    */
  def getInstance(config: Option[Config] = SpartaConfig.getZookeeperConfig): CuratorFramework = {
    curatorFramework match {
      case None => {
        var defaultConnectionString = AppConstant.DefaultZKConnection
        var connectionTimeout = AppConstant.DefaultZKConnectionTimeout
        var sessionTimeout = AppConstant.DefaultZKSessionTimeout
        var retryAttempts = AppConstant.DefaultZKRetryAttemps
        var retryInterval = AppConstant.DefaultZKRetryInterval
        var pathZookeeper = AppConstant.DefaultZKPath

        Try(config.foreach(zkConfig => {
          defaultConnectionString = getStringConfigValue(zkConfig, AppConstant.ZKConnection)
          connectionTimeout = getIntConfigValue(zkConfig, AppConstant.ZKConnectionTimeout)
          sessionTimeout = getIntConfigValue(zkConfig, AppConstant.ZKSessionTimeout)
          retryAttempts = getIntConfigValue(zkConfig, AppConstant.ZKRetryAttemps)
          retryInterval = getIntConfigValue(zkConfig, AppConstant.ZKRetryInterval)
          pathZookeeper = getStringConfigValue(zkConfig, AppConstant.DefaultZKPath)
        }))

        Try {

          curatorFramework = Some(CuratorFrameworkFactory.builder()
            .connectString(defaultConnectionString)
            .connectionTimeoutMs(connectionTimeout)
            .sessionTimeoutMs(sessionTimeout)
            //.namespace(pathZookeeper)
            .retryPolicy(new ExponentialBackoffRetry(retryInterval, retryAttempts)
            ).build())


          curatorFramework.get.start()
          log.info(s"Curator instance created correctly for Zookeeper cluster $defaultConnectionString")
          curatorFramework.get
        } match {
          case Success(curatorFk) => curatorFk
          case Failure(e) => log.error("Unable to establish a connection with the specified Zookeeper", e); throw e
        }
      }
      case Some(curatorFk) => curatorFk
    }
  }

  /**
    * Resets the current instance of the curatorFramework.
    */
  def resetInstance(): Unit = {
    if (curatorFramework.isDefined) {
      CloseableUtils.closeQuietly(curatorFramework.get)
      curatorFramework = None
    }
  }

  def existsPath(path: String): Boolean = curatorFramework match {
    case Some(curator) => Option(curator.checkExists().forPath(path)).isDefined
    case None => false
  }

  /**
    * Tries to instantiate a configuration value depending of its type.
    * @param configKey with the name of the property instead of configuration file.
    * @param config with the global configuration.
    * @param defaultValue if there is not configuration or any error appears a default value must be set.
    * @tparam U generic type of the value.
    * @return the parsed value of the configuration.
    */
  protected def getPathValue[U](configKey: String, config: Config, defaultValue: Class[U]): U =
    config.getAnyRef(configKey).asInstanceOf[U]

  protected def getStringConfigValue(config: Config, key: String): String = getPathValue(key, config, classOf[String])

  protected def getIntConfigValue(config: Config, key: String): Int = getPathValue(key, config, classOf[Int])
}