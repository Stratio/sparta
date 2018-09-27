/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.utils

import javax.cache.configuration.Factory
import javax.cache.expiry.{CreatedExpiryPolicy, Duration}
import javax.net.ssl.SSLContext

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.constants.SdkConstants._
import com.stratio.sparta.core.helpers.SSLHelper
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.typesafe.config.Config
import org.apache.ignite.cache.CacheMode
import org.apache.ignite.configuration.{CacheConfiguration, DataStorageConfiguration, IgniteConfiguration}
import org.apache.ignite.logger.log4j2.Log4J2Logger
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi
import org.apache.ignite.spi.discovery.DiscoverySpi
import org.apache.ignite.spi.discovery.zk.ZookeeperDiscoverySpi
import org.apache.ignite.{Ignite, IgniteCache, Ignition}

import scala.collection.JavaConversions._
import scala.util.Try

//scalastyle:off
trait SpartaIgniteUtils {

  cache: CacheDiscoverySpiComponent =>

  val config = SpartaConfig.getIgniteConfig().get

  def getIgniteConfiguration() = {
    val instanceName = Try(config.getString(AppConstant.IgniteInstanceName)).getOrElse("sparta-ignite-instance")
    val cacheName = Try(config.getString(AppConstant.IgniteCacheName)).getOrElse("sparta-cache")
    val igniteConfig = new IgniteConfiguration()

    igniteConfig.setIgniteInstanceName(instanceName)
    igniteConfig.setCacheConfiguration(cacheConfiguration(cacheName))
    igniteConfig.setDataStorageConfiguration(dataStoreConfiguration())
    igniteConfig.setDiscoverySpi(cacheDiscoverSpi.cacheDiscovery())
    igniteConfig.setGridLogger(new Log4J2Logger(this.getClass.getClassLoader.getResource("log4j2.xml")))
    igniteConfig.setCommunicationSpi(cacheDiscoverSpi.cacheComunicationSpi(config.getInt(AppConstant.IgniteCommunicationSpi), config.getInt(AppConstant.IgniteCommunicationSpiPortRange)))

    if (Try(config.getBoolean(AppConstant.IgniteSecurityEnabled)).getOrElse(false)) {
      igniteConfig.setSslContextFactory(securityConfiguration())
    }

    igniteConfig
  }

  private def cacheConfiguration[K, V](cacheName: String): CacheConfiguration[K, V] = {
    val cacheCfg = new CacheConfiguration[K, V](cacheName)
    cacheCfg.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ETERNAL))
    cacheCfg.setCacheMode(CacheMode.REPLICATED)
    cacheCfg
  }

  private def dataStoreConfiguration(): DataStorageConfiguration = {
    val dataStoreConfig = new DataStorageConfiguration()
    if (Try(config.getBoolean(AppConstant.IgnitePersistenceEnabled)).getOrElse(false)) {
      dataStoreConfig.getDefaultDataRegionConfiguration.setPersistenceEnabled(true)
      dataStoreConfig.setStoragePath(config.getString(AppConstant.IgnitePersistencePath))
      dataStoreConfig.setWalPath(config.getString(AppConstant.IgnitePersistenceWalPath))
      dataStoreConfig.setWalArchivePath(config.getString(AppConstant.IgnitePersistenceWalPath))
    }
    dataStoreConfig.getDefaultDataRegionConfiguration.setInitialSize(config.getLong(AppConstant.IgniteMemoryInitialSize) * 1024 * 1024)
    dataStoreConfig.getDefaultDataRegionConfiguration.setMaxSize(config.getLong(AppConstant.IgniteMemoryMaxSize) * 1024 * 1024)
    dataStoreConfig
  }

  private def securityConfiguration(): Factory[SSLContext] = {
    new Factory[SSLContext] {
      override def create() = SSLHelper.getSSLContextV2(true)
    }
  }
}

trait CacheDiscoverySpiComponent {

  val cacheDiscoverSpi: CacheDiscoverySpi
}

trait CacheDiscoverySpi {

  type Spi = DiscoverySpi

  def cacheDiscovery(): Spi

  def cacheComunicationSpi(port: Int, range: Int = 0): TcpCommunicationSpi
}

trait ZkDiscoverySpiComponent extends CacheDiscoverySpiComponent {

  val cacheDiscoverSpi: CacheDiscoverySpi = new ZkDiscoverySpiImpl {
    val config: Config = SpartaConfig.getZookeeperConfig().get
  }

  trait ZkDiscoverySpiImpl extends CacheDiscoverySpi {

    val config: Config

    override def cacheDiscovery(): Spi = {
      val zkSpi = new ZookeeperDiscoverySpi()
      zkSpi.setZkConnectionString(Try(config.getString(ZKConnection)).getOrElse(DefaultZKConnection))
      zkSpi.setSessionTimeout(Try(config.getLong(ZKSessionTimeout)).getOrElse(DefaultZKSessionTimeout))
      zkSpi.setZkRootPath(AppConstant.IgniteDiscoveryZkPath)
      zkSpi.setJoinTimeout(Try(config.getLong(ZKRetryInterval)).getOrElse(DefaultZKRetryInterval))
      zkSpi.setClientReconnectDisabled(false)
      zkSpi
    }

    override def cacheComunicationSpi(port: Int, range: Int = 0): TcpCommunicationSpi = {
      val tcpSpi = new TcpCommunicationSpi()
      tcpSpi.setLocalPort(port)
      tcpSpi.setLocalPortRange(range)
      tcpSpi
    }
  }

}

object SpartaIgnite extends SpartaIgniteUtils with ZkDiscoverySpiComponent with SLF4JLogging {

  private var igniteSparta: Option[Ignite] = None

  private def stopOrphanedNodes(ignite: Ignite): Unit = {
    if (Try(SpartaConfig.getIgniteConfig().get.getBoolean(AppConstant.IgniteClusterEnabled)).getOrElse(false)) {
      val cluster = ignite.cluster
      val nodes = cluster.nodes().toSeq
      nodes.foreach(node => log.debug(s"Ignite cluster node detected with Id ${node.id()} and Address ${node.addresses().toSeq.mkString(",")}"))
      val nodesToStop = nodes.flatMap { node =>
        if (!cluster.pingNode(node.id()))
          Option(node.id())
        else None
      }
      if (nodesToStop.nonEmpty) {
        log.info(s"Stopping lost ignite nodes: ${nodesToStop.mkString(",")}")
        cluster.stopNodes(nodesToStop)
        log.info(s"Ignite nodes stopped :${nodesToStop.mkString(",")}")
      }
    }
  }

  def closeIgniteConnection() = {
    igniteSparta.foreach(_.close())
    igniteSparta = None
  }

  def getCache[K, V](name: String): IgniteCache[K, V] = synchronized {
    val igniteInstance = igniteSparta match {
      case Some(instance) =>
        stopOrphanedNodes(instance)
        instance
      case None =>
        val ignite = Ignition.start(getIgniteConfiguration())
        if (Try(SpartaConfig.getIgniteConfig().get.getBoolean(AppConstant.IgniteClusterEnabled)).getOrElse(false)) {
          stopOrphanedNodes(ignite)
          log.info("Starting Ignite cluster instance")
          val cluster = ignite.cluster()
          if (!cluster.active()) cluster.active(true)
          stopOrphanedNodes(ignite)
        } else log.info("Starting Ignite local instance")
        log.info(s"Ignite instance started ${ignite.name()}")
        igniteSparta = Option(ignite)
        ignite
    }
    val cachesCreated = igniteInstance.cacheNames().toSeq
    log.info(s"Caches created in Ignite: ${cachesCreated.mkString(",")}")

    if (cachesCreated.contains(name)) {
      val obtainedCache = igniteInstance.getOrCreateCache[K, V](name)
      log.info(s"Obtained Ignite cache $name")
      obtainedCache
    } else {
      val createdCache = igniteInstance.createCache[K, V](name)
      log.info(s"Created Ignite cache $name")
      createdCache
    }
  }
}