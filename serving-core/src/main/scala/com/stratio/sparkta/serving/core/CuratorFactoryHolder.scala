
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

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.Config
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

/**
 * Customized factory that encapsulates the real CuratorFrameworkFactory and creates a singleton instance of it.
 * @author anistal
 */
object CuratorFactoryHolder extends SLF4JLogging {

  private var curatorFramework: Option[CuratorFramework] = None

  val ZKConfigPrefix     =   "zk"

  /**
   * Gets a new instance of a CuratorFramework if it was not created before.
   * @param config with the ZK configuration.
   * @return a singleton instance of CuratorFramework.
   */
  def getInstance(config: Config): Option[CuratorFramework] = {
    curatorFramework match {
      case None =>  {
        val defaultConnectionString = getPathValue("connectionString", config, classOf[String])
        val connectionTimeout = getPathValue("connectionTimeout", config, classOf[Int])
        val sessionTimeout = getPathValue("sessionTimeout", config, classOf[Int])
        val retryAttempts = getPathValue("retryAttempts", config, classOf[Int])
        val retryInterval = getPathValue("retryInterval", config, classOf[Int])

        curatorFramework = Some(CuratorFrameworkFactory.builder()
          .connectString(defaultConnectionString)
          .connectionTimeoutMs(connectionTimeout)
          .sessionTimeoutMs(sessionTimeout)
          .retryPolicy(new ExponentialBackoffRetry(retryInterval, retryAttempts)
        ).build())

        curatorFramework.get.start()
        log.info(s"> ZK connection to $defaultConnectionString was successful.")
        curatorFramework
      }
      case _ => curatorFramework
    }
  }

  /**
   * Resets the current instance of the curatorFramework.
   */
  def resetInstance(): Unit = curatorFramework = None

  /**
   * Tries to instantiate a configuration value depending of its type.
   * @param configKey with the name of the property instead of configuration file.
   * @param config with the global configuration.
   * @param defaultValue if there is not configuration or any error appears a default value must be set.
   * @tparam U generic type of the value.
   * @return the parsed value of the configuration.
   */
  protected def getPathValue[U](configKey: String, config: Config, defaultValue: Class[U]): U =
    config.getConfig(ZKConfigPrefix).getAnyRef(configKey).asInstanceOf[U]
}