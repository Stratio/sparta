/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource.storage

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.utils.CloseableUtils
import org.apache.spark.streaming.datasource.config.ParametersHelper._
import org.apache.spark.streaming.datasource.models._
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.Serialization._
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

object ZookeeperHelper extends SLF4JLogging {

  private var curatorFramework: Option[CuratorFramework] = None
  private val DefaultZKConnectionTimeout = 15000
  private val DefaultZKSessionTimeout = 60000
  private val DefaultZKRetryAttemps = 5
  private val DefaultZKRetryInterval = 10000

  implicit val json4sJacksonFormats: Formats =
    DefaultFormats +
      new JsoneyStringSerializer() +
      new EnumNameSerializer(OffsetLocation) +
      new EnumNameSerializer(OffsetOperator) +
      new EnumNameSerializer(OrderOperator)

  def resetInstance(): Unit = {
    if (curatorFramework.isDefined) {
      CloseableUtils.closeQuietly(curatorFramework.get)
      curatorFramework = None
    }
  }

  def setNotStarted(zookeeperParams: Map[String, String]): Unit =
    curatorFramework.foreach( _ =>
      getOffsets(zookeeperParams).foreach(status => setOffsets(zookeeperParams, status.copy(started = false))))

  def existsPath(zookeeperParams: Map[String, String], path: String): Boolean = {
    if (curatorFramework.isEmpty) {
      getInstance(zookeeperParams)
    }

    curatorFramework match {
      case Some(curator) => Option(curator.checkExists().forPath(path)).isDefined
      case None => false
    }
  }

  def getOffsets(zookeeperParams: Map[String, String]): Option[StatusOffset] = {
    val path = getZookeeperPath(zookeeperParams)
    if (existsPath(zookeeperParams, path)) {
      if (curatorFramework.isEmpty) {
        getInstance(zookeeperParams)
      }
      curatorFramework match {
        case Some(fk) =>
          Option(read[StatusOffset](new Predef.String(fk.getData.forPath(path))))
        case None =>
          throw new Exception("Exception while getting stored offsets: no available connection to Zookeeper")
      }
    } else None
  }

  def resetOffsets(zookeeperParams: Map[String, String]): Unit = {
    val path = getZookeeperPath(zookeeperParams)
    if (existsPath(zookeeperParams, path)) {
      if (curatorFramework.isEmpty) {
        getInstance(zookeeperParams)
      }
      curatorFramework match {
        case Some(fk) =>
          fk.delete().deletingChildrenIfNeeded().forPath(path)
        case None =>
          throw new Exception("Exception while resetting stored offsets: no available connection to Zookeeper")
      }
    }
  }

  def setOffsets(zookeeperParams: Map[String, String], statusOffset: StatusOffset): Unit = {
    val path = getZookeeperPath(zookeeperParams)
    if (curatorFramework.isEmpty) {
      getInstance(zookeeperParams)
    }
    curatorFramework match {
      case Some(fk) =>
        if (getOffsets(zookeeperParams).isEmpty)
          fk.create().creatingParentsIfNeeded().forPath(path, write(statusOffset).getBytes)
        else fk.setData().forPath(path, write(statusOffset).getBytes)
      case None =>
        throw new Exception("Exception while setting stored offsets: no available connection to Zookeeper")
    }
  }

  private def getInstance(zookeeperParams: Map[String, String]): CuratorFramework = {
    curatorFramework match {
      case None =>
        val defaultConnectionString = getZookeeperConnection(zookeeperParams)
        val connectionTimeout = DefaultZKConnectionTimeout
        val sessionTimeout = DefaultZKSessionTimeout
        val retryAttempts = DefaultZKRetryAttemps
        val retryInterval = DefaultZKRetryInterval

        Try {
          curatorFramework = Option(CuratorFrameworkFactory.builder()
            .connectString(defaultConnectionString)
            .connectionTimeoutMs(connectionTimeout)
            .sessionTimeoutMs(sessionTimeout)
            .retryPolicy(new ExponentialBackoffRetry(retryInterval, retryAttempts))
            .build())
          curatorFramework.get.start()
          log.info(s"Zookeeper connection in datasource receiver to $defaultConnectionString was STARTED.")
          curatorFramework.get
        } match {
          case Success(curatorFk) =>
            curatorFk
          case Failure(e) =>
            log.error("Impossible to start Zookeeper connection", e)
            throw e
        }
      case Some(curatorFk) =>
        curatorFk
    }
  }
}