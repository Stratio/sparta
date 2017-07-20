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
  val SparkConsumerPropertiesKeys =
    List(StorageLevelKey, RememberDuration, OffsetsLocation, ZookeeperConnection, ZookeeperPath)

  /**
   * Configuration Defaults
   */
  val DefaultStorageLevel = "MEMORY_ONLY"
  val DefaultMinRememberDuration = "60s"
  val DefaultOffsetLocation = "MEMORY"
  val DefaultOffsetLocationEnum = OffsetLocation.MEMORY
  val DefaultZookeeperPath = "/stratio/sparta/crossdata/offsets"
}
