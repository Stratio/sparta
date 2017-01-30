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

package com.stratio.sparta.serving.core.utils

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.policy.PolicyModel
import com.typesafe.config.Config

import scala.util.{Failure, Success, Try}

trait PolicyConfigUtils extends SLF4JLogging {

  val DetailConfig = SpartaConfig.getDetailConfig.getOrElse {
    val message = "Impossible to extract Detail Configuration"
    log.error(message)
    throw new RuntimeException(message)
  }

  def isLocal(policy: PolicyModel): Boolean =
    policy.executionMode match {
      case Some(executionMode) if executionMode.nonEmpty => executionMode.equalsIgnoreCase(ConfigLocal)
      case _ => DetailConfig.getString(ExecutionMode).equalsIgnoreCase(ConfigLocal)
    }

  def isCluster(policy: PolicyModel, clusterConfig: Config): Boolean =
    policy.sparkConf.find(sparkProp =>
      sparkProp.sparkConfKey == DeployMode && sparkProp.sparkConfValue == ClusterValue) match {
      case Some(mode) => true
      case _ => Try(clusterConfig.getString(DeployMode)) match {
        case Success(mode) => mode == ClusterValue
        case Failure(e) => false
      }
    }

  def getDetailExecutionMode(policy: PolicyModel, clusterConfig: Config): String =
    if (isLocal(policy)) LocalValue
    else {
      val execMode = executionMode(policy)
      if (isCluster(policy, clusterConfig)) s"$execMode-$ClusterValue"
      else s"$execMode-$ClientValue"
    }

  def pluginsJars(policy: PolicyModel): Seq[String] =
    policy.userPluginsJars.map(userJar => userJar.jarPath.trim)

  def isSupervised(policy: PolicyModel, clusterConfig: Config): Boolean = {
    val mode = executionMode(policy)
    if (mode == ConfigStandAlone || mode == ConfigMesos) {
      Try(clusterConfig.getBoolean(Supervise)).getOrElse(false)
    } else false
  }

  def gracefulStop(policy: PolicyModel): Boolean =
    Try(policy.stopGracefully.getOrElse(DetailConfig.getBoolean(ConfigStopGracefully)))
      .getOrElse(DefaultStopGracefully)

  def driverLocation(policy: PolicyModel): String =
    Try(policy.driverLocation.getOrElse(DetailConfig.getString(DriverLocation)))
      .getOrElse(DefaultDriverLocation)

  def executionMode(policy: PolicyModel): String = policy.executionMode match {
    case Some(mode) if mode.nonEmpty => mode
    case _ => DetailConfig.getString(ExecutionMode)
  }
}
