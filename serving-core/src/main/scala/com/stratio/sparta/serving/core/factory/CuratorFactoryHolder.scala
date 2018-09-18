/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.factory

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.constants.SdkConstants._
import com.stratio.sparta.serving.core.config.SpartaConfig
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

  def setInstance(curatorInstance: CuratorFramework) : Unit = {
    resetInstance()
    curatorFramework = Option(curatorInstance)
  }

  /**
    * Gets a new instance of a CuratorFramework if it was not created before.
    * @return a singleton instance of CuratorFramework.
    */
  def getInstance(config: Option[Config] = SpartaConfig.getZookeeperConfig()): CuratorFramework = {
    curatorFramework match {
      case None =>
        val defaultConnectionString = getStringConfigValue(config, ZKConnection, DefaultZKConnection)
        val connectionTimeout = getIntConfigValue(config, ZKConnectionTimeout, DefaultZKConnectionTimeout)
        val sessionTimeout = getIntConfigValue(config, ZKSessionTimeout, DefaultZKSessionTimeout)
        val retryAttempts = getIntConfigValue(config, ZKRetryAttemps, DefaultZKRetryAttemps)
        val retryInterval = getIntConfigValue(config, ZKRetryInterval, DefaultZKRetryInterval)

        Try {
          curatorFramework = Some(CuratorFrameworkFactory.builder()
            .connectString(defaultConnectionString)
            .connectionTimeoutMs(connectionTimeout)
            .sessionTimeoutMs(sessionTimeout)
            .retryPolicy(new ExponentialBackoffRetry(retryInterval, retryAttempts))
            .build())

          curatorFramework.foreach(_.start())
          log.info(s"Curator connection created correctly for Zookeeper cluster $defaultConnectionString")
          curatorFramework.getOrElse(throw new Exception("Curator connection not created"))
        } match {
          case Success(curatorFk) => curatorFk
          case Failure(e) => log.error("Unable to establish a connection with the specified Zookeeper", e); throw e
        }
      case Some(curatorFk) =>
        Try(curatorFk.getZookeeperClient.getZooKeeper.getState.isConnected) match {
          case Success(_) =>
            curatorFk
          case Failure(e) =>
            log.error("Curator connection disconnected. Reconnecting it!", e)
            resetInstance()
            getInstance()
        }
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

  def existsPath(path: String): Boolean = Try(getInstance()) match {
    case Success(curator) => Try(Option(curator.checkExists().forPath(path))).toOption.flatten.isDefined
    case Failure(e) => throw e
  }

  protected def getPathValue[U](configKey: String, config: Config, typeToReturn: Class[U]): U =
    config.getAnyRef(configKey).asInstanceOf[U]

  protected def getStringConfigValue(config: Option[Config], key: String, default: String): String =
    Try(getPathValue(key, config.get, classOf[String])).getOrElse(default)

  protected def getIntConfigValue(config: Option[Config], key: String, default: Int): Int =
    Try(getPathValue(key, config.get, classOf[Int])).getOrElse(default)
}