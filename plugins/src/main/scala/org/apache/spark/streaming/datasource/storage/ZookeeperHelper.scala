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

package org.apache.spark.streaming.datasource.storage

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
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
          throw new Exception("Impossible to start Zookeeper connection")
      }
    } else None
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
        throw new Exception("Impossible to start Zookeeper connection")
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