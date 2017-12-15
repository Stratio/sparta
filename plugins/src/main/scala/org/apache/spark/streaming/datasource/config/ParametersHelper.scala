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

package org.apache.spark.streaming.datasource.config

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.datasource.config.ConfigParameters._
import org.apache.spark.streaming.datasource.models.OffsetLocation
import org.apache.spark.streaming.datasource.models.OffsetLocation.OffsetLocation

import scala.util.Try

object ParametersHelper {

  /**
   * Spark properties
   */
  def getRememberDuration(params: Map[String, String]): Option[Long] =
    Try(params.get(RememberDuration).map(_.toLong)).getOrElse(None)

  def getStorageLevel(params: Map[String, String]): StorageLevel =
    StorageLevel.fromString(Try(params.getOrElse(StorageLevelKey, DefaultStorageLevel))
      .getOrElse(DefaultStorageLevel))

  def getOffsetLocation(params: Map[String, String]): OffsetLocation =
    Try(OffsetLocation.withName(params.getOrElse(OffsetsLocation, DefaultOffsetLocation)))
      .getOrElse(DefaultOffsetLocationEnum)

  def getZookeeperParams(params: Map[String, String]): Map[String, String] =
    params.filter(_._1.contains("zookeeper"))

  def getZookeeperConnection(params: Map[String, String]): String =
    params.getOrElse(ZookeeperConnection,
      throw new Exception("It is mandatory to specify at least one zookeeper connection parameter"))

  def getZookeeperPath(params: Map[String, String]): String =
    params.getOrElse(ZookeeperPath, DefaultZookeeperPath)

  def getResetOffsetOnStart(params: Map[String, String]): Boolean =
    Try(params.getOrElse(ResetOffsetOnStart, DefaultResetOffsetOnStart).toBoolean).getOrElse(false)

  def getIgnoreStartedStatus(params: Map[String, String]): Boolean =
    Try(params.getOrElse(IgnoreStartedStatus, DefaultIgnoreStartedStatus).toBoolean).getOrElse(false)
}
