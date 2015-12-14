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

package com.stratio.sparkta.repositories.zookeeper

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.common.config.ConfigComponent
import com.stratio.sparkta.common.ValidatingPropertyMap._
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.curator.framework.recipes.cache.{NodeCacheListener, NodeCache}
import org.apache.curator.framework.{CuratorFrameworkFactory, CuratorFramework}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.utils.CloseableUtils
import org.json4s.Formats
import org.json4s.jackson.Serialization.read

import scala.util.{Success, Failure, Try}
import scala.collection.JavaConversions._

import com.stratio.sparkta.repositories.RepositoryComponent
import ZookeeperConstants._

trait ZookeeperRepositoryComponent extends RepositoryComponent[String, Array[Byte]] with SLF4JLogging {
  self: ConfigComponent =>

  val repository = new ZookeeperRepository{}

  trait ZookeeperRepository extends Repository {

    private def curatorClient: CuratorFramework =
      ZookeeperRepository.getInstance(getConfig)

    def get(id: String): Try[Option[Array[Byte]]] =
      Try {
        Option(
          curatorClient
            .getData
            .forPath(id)
        )
      }

    def getSubRepository(id: String): Try[List[String]] =
      Try {
        curatorClient
          .getChildren
          .forPath(id).toList
      }

    def exists(id: String): Try[Boolean] =
      Try {
        Option(curatorClient
          .checkExists()
          .forPath(id)
        ).isDefined
      }
    
    def create(id: String, element: Array[Byte]): Try[Boolean] =
      Try {
        Option(
          curatorClient
            .create()
            .creatingParentsIfNeeded()
            .forPath(id, element)
        ).isDefined
      }

    def update(id: String, element: Array[Byte]): Try[Boolean] =
      Try {
        Option(
          curatorClient
            .setData()
            .forPath(id, element)
        ).isDefined
      }

    def delete(id: String): Try[Boolean] =
      Try {
        Option(
          curatorClient
            .delete()
            .forPath(id)
        ).isDefined
      }

    def getZookeeperConfig: Option[Config] = getOptionConfig(ConfigZookeeper)

    def getConfig: Map[String, Any] =
      getZookeeperConfig.fold(Map.empty[String, Any])(configAsMap)

    def start: Boolean =
      Try(
        curatorClient.start()
      ).isSuccess

    def stop: Boolean =
      Try(
        CloseableUtils.closeQuietly(curatorClient)
      ).isSuccess

    def getState: RepositoryState =
      curatorClient.getState match {
        case CuratorFrameworkState.STARTED => Started
        case CuratorFrameworkState.STOPPED => Stopped
        case CuratorFrameworkState.LATENT => NotStarted
        case _ => Unknown
      }

    def addListener[T](id: String, callback: (T, NodeCache) => Unit)
       (implicit format: Formats, m: Manifest[T]): Unit = {
      val nodeCache: NodeCache = new NodeCache(curatorClient, id)

      nodeCache.getListenable.addListener(
        new NodeCacheListener {
          override def nodeChanged(): Unit =
            Try(new String(nodeCache.getCurrentData.getData)) match {
              case Success(value) => callback(read[T](value), nodeCache)
              case Failure(e) => log.error(s"NodeCache value: ${nodeCache.getCurrentData}", e)
            }
        }
      )
      nodeCache.start()
    }
  }
}

object ZookeeperRepository {

  private var curatorClientOpt: Option[CuratorFramework] = None

  def getInstance(config: Map[String, Any]): CuratorFramework = {

    curatorClientOpt.getOrElse {
      Try {
        CuratorFrameworkFactory.builder()
          .connectString(
            config.getString(ZookeeperConnection,DefaultZookeeperConnection)
          )
          .connectionTimeoutMs(
            config.getInt(ZookeeperConnectionTimeout, DefaultZookeeperConnectionTimeout)
          )
          .sessionTimeoutMs(
            config.getInt(ZookeeperSessionTimeout, DefaultZookeeperSessionTimeout)
          )
          .retryPolicy(
            new ExponentialBackoffRetry(
              config.getInt(ZookeeperRetryInterval, DefaultZookeeperRetryInterval),
              config.getInt(ZookeeperRetryAttemps, DefaultZookeeperRetryAttemps)
            )
          )
          .build()
      } match {
        case Success(client: CuratorFramework) =>
          curatorClientOpt = Option(client)
          client
        case Failure(_: Throwable) =>
          throw ZookeeperRepositoryException("Error trying to create a new Zookeeper instance")
      }
    }
  }

  def clean: Boolean = {
    if(curatorClientOpt.isDefined)
      Try {
        CloseableUtils.closeQuietly(curatorClientOpt.get)
        curatorClientOpt = None
      }.isSuccess
    else
      true
  }

}
