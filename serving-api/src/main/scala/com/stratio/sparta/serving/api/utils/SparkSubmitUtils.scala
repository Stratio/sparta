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

import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, SubmitArgument}
import com.stratio.sparta.serving.core.utils.{HdfsUtils, PolicyConfigUtils}
import com.typesafe.config.Config

import scala.collection.JavaConversions._
import scala.util.{Failure, Properties, Success, Try}

trait SparkSubmitUtils extends PolicyConfigUtils {

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
        Map(newKey.trim -> value.trim)
      case Failure(_) =>
        log.debug(s"The key $key was not defined in config.")
        Map.empty[String, String]
    }

  def optionFromPolicyAndProperties(policyOption: Option[String],
                                    configuration: Config, configurationKey: String): String =
    policyOption.filter(_.trim.nonEmpty).getOrElse(configuration.getString(configurationKey)).trim

  def driverLocation(driverPath: String): String = {
    val begin = 0
    val end = 4

    Try(driverPath.substring(begin, end) match {
      case "hdfs" => "hdfs"
      case _ => "provided"
    }).getOrElse(DefaultDriverLocation)
  }

  def driverSubmit(policy: PolicyModel, detailConfig: Config, hdfsConfig: Option[Config]): String = {
    val driverStorageLocation = Try(optionFromPolicyAndProperties(policy.driverUri, detailConfig, DriverURI))
      .getOrElse(DefaultProvidedDriverURI)
    if(driverLocation(driverStorageLocation) == ConfigHdfs) {
      val Hdfs = HdfsUtils()
      val Uploader = ClusterSparkFilesUtils(policy, Hdfs)

      Uploader.uploadDriverFile(driverStorageLocation)
    } else driverStorageLocation
  }

  def sparkHome(clusterConfig: Config): String =
    Properties.envOrElse("SPARK_HOME", clusterConfig.getString(SparkHome)).trim

  /**
   * Checks if we have a valid Spark home.
   */
  def validateSparkHome(clusterConfig: Config): Unit = require(Try(sparkHome(clusterConfig)).isSuccess,
    "You must set the $SPARK_HOME path in configuration or environment")

  def submitArgsFromPolicy(submitArgs: Seq[SubmitArgument]): Map[String, String] =
    submitArgs.flatMap(argument => {
      if (argument.submitArgument.nonEmpty) {
        if (!SubmitArguments.contains(argument.submitArgument))
          log.warn(s"Spark submit argument added unrecognized by Sparta. \n" +
            s"Argument: ${argument.submitArgument}\nValue: ${argument.submitValue}")
        Some(argument.submitArgument.trim -> argument.submitValue.trim)
      } else None
    }).toMap

  def submitArgsFromProps(clusterConfig: Config): Map[String, String] =
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
