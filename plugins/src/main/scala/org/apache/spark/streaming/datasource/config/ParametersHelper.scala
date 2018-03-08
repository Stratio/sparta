/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
