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

package com.stratio.sparta.serving.api.utils

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.utils.ClusterSparkFilesUtils
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, SubmitArgument}
import com.stratio.sparta.serving.core.utils.HdfsUtils
import com.typesafe.config.Config

import scala.collection.JavaConversions._
import scala.util.{Failure, Properties, Success, Try}

trait SparkSubmitUtils extends SLF4JLogging {

  val SubmitDeployMode = "--deploy-mode"
  val SubmitName = "--name"
  val SubmitPropertiesFile = "--properties-file"
  val SubmitTotalExecutorCores = "--total-executor-cores"
  val SubmitPackages = "--packages"
  val SubmitRepositories = "--repositories"
  val SubmitExcludePackages = "--exclude-packages"
  val SubmitJars = "--jars"
  val SubmitProxyUser = "--proxy-user"
  val SubmitDriverJavaOptions = "--driver-java-options"
  val SubmitDriverLibraryPath = "--driver-library-path"
  val SubmitDriverClassPath = "--driver-class-path"

  // Yarn only
  val SubmitYarnQueue = "--queue"
  val SubmitFiles = "--files"
  val SubmitArchives = "--archives"
  val SubmitAddJars = "--addJars"
  val SubmitNumExecutors = "--num-executors"
  val SubmitDriverCores = "--driver-cores"
  val SubmitDriverMemory = "--driver-memory"
  val SubmitExecutorCores = "--executor-cores"
  val SubmitExecutorMemory = "--executor-memory"

  //Kerberos
  val SubmitPrincipal = "--principal"
  val SubmitKeyTab = "--keytab"

  //standAlone and Mesos
  val SubmitSupervise = "--supervise"

  val SubmitArguments = Seq(SubmitDeployMode, SubmitName, SubmitPropertiesFile, SubmitTotalExecutorCores,
    SubmitPackages, SubmitRepositories, SubmitExcludePackages, SubmitJars, SubmitProxyUser, SubmitDriverJavaOptions,
    SubmitDriverLibraryPath, SubmitDriverClassPath, SubmitYarnQueue, SubmitFiles, SubmitArchives, SubmitAddJars,
    SubmitNumExecutors, SubmitDriverCores, SubmitDriverMemory, SubmitExecutorCores, SubmitExecutorMemory,
    SubmitPrincipal, SubmitKeyTab, SubmitSupervise)

  def sparkConf(clusterConfig: Config): Seq[(String, String)] =
    clusterConfig.entrySet()
      .filter(_.getKey.startsWith("spark.")).toSeq
      .map(e => (e.getKey, e.getValue.unwrapped.toString))

  def toMap(key: String, newKey: String, config: Config): Map[String, String] =
    Try(config.getString(key)) match {
      case Success(value) =>
        Map(newKey -> value)
      case Failure(_) =>
        log.debug(s"The key $key was not defined in config.")
        Map.empty[String, String]
    }

  def driverSubmit(policy: PolicyModel, detailConfig: Config, hdfsConfig: Option[Config]): String =
    Try(policy.driverLocation.getOrElse(detailConfig.getString(DriverLocation)))
      .getOrElse(DefaultDriverLocation) match {
      case location if location == "hdfs" =>
        val driverFolder = hdfsConfig match {
          case Some(config) => Try(config.getString(DriverFolder)).getOrElse(DefaultDriverFolder)
          case None => DefaultDriverFolder
        }
        val Hdfs = HdfsUtils()
        val Uploader = ClusterSparkFilesUtils(policy, Hdfs)
        val BasePath = Try(policy.driverUri.getOrElse(detailConfig.getString(DriverURI)))
          .getOrElse(s"/user/${Hdfs.userName}/$ConfigAppName") + s"/${policy.id.get.trim}"
        val DriverJarPath = s"$BasePath/$driverFolder/"

        Uploader.uploadDriverFile(DriverJarPath)
      case _ =>
        policy.driverUri.getOrElse(detailConfig.getString(DriverURI))
    }

  def pluginsSubmit(policy: PolicyModel, detailConfig: Config): Seq[String] =
    Try(policy.pluginsLocation.getOrElse(detailConfig.getString(PluginsLocation)))
      .getOrElse(DefaultPluginsLocation) match {
      case location if location == "local" => policy.userPluginsJars.map(userJar => userJar.jarPath)
      case _ => Seq.empty[String]
    }

  def sparkHome(clusterConfig: Config): String =
    Properties.envOrElse("SPARK_HOME", clusterConfig.getString(SparkHome))

  /**
   * Checks if we have a valid Spark home.
   */
  def validateSparkHome(clusterConfig: Config): Unit = require(Try(sparkHome(clusterConfig)).isSuccess,
    "You must set the $SPARK_HOME path in configuration or environment")

  /**
   * Checks if supervise param is set when execution mode is standalone or mesos
   *
   * @return The result of checks as boolean value
   */
  def isSupervised(policy: PolicyModel, detailConfig: Config, clusterConfig: Config): Boolean = {
    val executionMode = policy.executionMode match {
      case Some(mode) if mode.nonEmpty => mode
      case _ => detailConfig.getString(ExecutionMode)
    }
    if (executionMode == ConfigStandAlone || executionMode == ConfigMesos) {
      Try(clusterConfig.getBoolean(Supervise)).getOrElse(false)
    } else false
  }

  def submitArgumentsFromPolicy(submitArgs: Seq[SubmitArgument]): Map[String, String] =
    submitArgs.flatMap(argument => {
      if (argument.submitArgument.nonEmpty) {
        if (!SubmitArguments.contains(argument.submitArgument))
          log.warn(s"Spark submit argument added unrecognized by Sparta. \n" +
            s"Argument: ${argument.submitArgument}\nValue: ${argument.submitValue}")
        Some(argument.submitArgument -> argument.submitValue)
      } else None
    }).toMap

  def submitArgumentsFromProperties(clusterConfig: Config): Map[String, String] =
    toMap(DeployMode, SubmitDeployMode, clusterConfig) ++
      toMap(Name, SubmitName, clusterConfig) ++
      toMap(PropertiesFile, SubmitPropertiesFile, clusterConfig) ++
      toMap(TotalExecutorCores, SubmitTotalExecutorCores, clusterConfig) ++
      toMap(Packages, SubmitPackages, clusterConfig) ++
      toMap(Repositories, SubmitRepositories, clusterConfig) ++
      toMap(ExcludePackages, SubmitExcludePackages, clusterConfig) ++
      toMap(Jars, SubmitJars, clusterConfig) ++
      toMap(ProxyUser, SubmitProxyUser, clusterConfig) ++
      toMap(DriverJavaOptions, SubmitDriverJavaOptions, clusterConfig) ++
      toMap(DriverLibraryPath, SubmitDriverLibraryPath, clusterConfig) ++
      toMap(DriverClassPath, SubmitDriverClassPath, clusterConfig) ++
      // Yarn only
      toMap(YarnQueue, SubmitYarnQueue, clusterConfig) ++
      toMap(Files, SubmitFiles, clusterConfig) ++
      toMap(Archives, SubmitArchives, clusterConfig) ++
      toMap(AddJars, SubmitAddJars, clusterConfig) ++
      toMap(NumExecutors, SubmitNumExecutors, clusterConfig) ++
      toMap(DriverCores, SubmitDriverCores, clusterConfig) ++
      toMap(DriverMemory, SubmitDriverMemory, clusterConfig) ++
      toMap(ExecutorCores, SubmitExecutorCores, clusterConfig) ++
      toMap(ExecutorMemory, SubmitExecutorMemory, clusterConfig)
}
