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

object ZookeeperConstants {

  val ZookeeperConnection = "connectionString"
  val DefaultZookeeperConnection = "localhost:2181"
  val ZookeeperConnectionTimeout = "connectionTimeout"
  val DefaultZookeeperConnectionTimeout = 15000
  val ZookeeperSessionTimeout = "sessionTimeout"
  val DefaultZookeeperSessionTimeout = 60000
  val ZookeeperRetryAttemps = "retryAttempts"
  val DefaultZookeeperRetryAttemps = 5
  val ZookeeperRetryInterval = "retryInterval"
  val DefaultZookeeperRetryInterval = 10000
  val ConfigZookeeper = "zk"

}