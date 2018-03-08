/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource.config

import org.apache.spark.streaming.datasource.models.OffsetLocation

object ConfigParameters {

  /**
   * Spark Consumer properties
   */
  val RememberDuration = "rememberDuration"
  val StorageLevelKey = "storageLevel"
  val OffsetsLocation = "offsetLocation"
  val ZookeeperConnection = "zookeeperConnection"
  val ZookeeperPath = "zookeeperPath"
  val ResetOffsetOnStart = "resetOffsetOnStart"
  val IgnoreStartedStatus = "ignoreStartedStatus"
  val SparkConsumerPropertiesKeys =
    List(StorageLevelKey, RememberDuration, OffsetsLocation, ZookeeperConnection, ZookeeperPath,
      ResetOffsetOnStart, IgnoreStartedStatus)

  /**
   * Configuration Defaults
   */
  val DefaultStorageLevel = "MEMORY_ONLY"
  val DefaultMinRememberDuration = "60s"
  val DefaultOffsetLocation = "MEMORY"
  val DefaultOffsetLocationEnum = OffsetLocation.MEMORY
  val DefaultZookeeperPath = "/stratio/sparta/crossdata/offsets"
  val DefaultResetOffsetOnStart = "false"
  val DefaultIgnoreStartedStatus = "false"
}
