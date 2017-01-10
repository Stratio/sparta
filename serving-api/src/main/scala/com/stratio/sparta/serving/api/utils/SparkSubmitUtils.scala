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
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, SubmitArgument}
import com.typesafe.config.Config

import scala.collection.JavaConversions._
import scala.util.{Failure, Properties, Success, Try}

trait SparkSubmitUtils extends SLF4JLogging {

  val ClusterConfig : Config

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

  def sparkConf: Seq[(String, String)] =
    ClusterConfig.entrySet()
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

  def sparkHome: String = Properties.envOrElse("SPARK_HOME", ClusterConfig.getString(AppConstant.SparkHome))

  /**
   * Checks if we have a valid Spark home.
   */
  def validateSparkHome(): Unit = require(Try(sparkHome).isSuccess,
    "You must set the $SPARK_HOME path in configuration or environment")

  /**
   * Checks if supervise param is set when execution mode is standalone or mesos
   *
   * @return The result of checks as boolean value
   */
  def isSupervised(policy: PolicyModel, detailConfig : Config): Boolean = {
    val executionMode = policy.executionMode match {
      case Some(mode) if mode.nonEmpty => mode
      case _ => detailConfig.getString(AppConstant.ExecutionMode)
    }
    if (executionMode == AppConstant.ConfigStandAlone || executionMode == AppConstant.ConfigMesos) {
      Try(ClusterConfig.getBoolean(AppConstant.Supervise)).getOrElse(false)
    } else false
  }

  def submitArgumentsFromPolicy(submitArgs: Seq[SubmitArgument]): Map[String, String] =
    submitArgs.map(argument => {
      if(!SubmitArguments.contains(argument.submitArgument))
        log.warn(s"Spark submit argument added unrecognized by Sparta. \n" +
          s"Argument: ${argument.submitArgument}\nValue: ${argument.submitValue}")
      argument.submitArgument -> argument.submitValue
    }).toMap

  def submitArgumentsFromProperties: Map[String, String] =
    toMap(AppConstant.DeployMode, SubmitDeployMode, ClusterConfig) ++
      toMap(AppConstant.Name, SubmitName, ClusterConfig) ++
      toMap(AppConstant.PropertiesFile, SubmitPropertiesFile, ClusterConfig) ++
      toMap(AppConstant.TotalExecutorCores, SubmitTotalExecutorCores, ClusterConfig) ++
      toMap(AppConstant.Packages, SubmitPackages, ClusterConfig) ++
      toMap(AppConstant.Repositories, SubmitRepositories, ClusterConfig) ++
      toMap(AppConstant.ExcludePackages, SubmitExcludePackages, ClusterConfig) ++
      toMap(AppConstant.Jars, SubmitJars, ClusterConfig) ++
      toMap(AppConstant.ProxyUser, SubmitProxyUser, ClusterConfig) ++
      toMap(AppConstant.DriverJavaOptions, SubmitDriverJavaOptions, ClusterConfig) ++
      toMap(AppConstant.DriverLibraryPath, SubmitDriverLibraryPath, ClusterConfig) ++
      toMap(AppConstant.DriverClassPath, SubmitDriverClassPath, ClusterConfig) ++
      // Yarn only
      toMap(AppConstant.YarnQueue, SubmitYarnQueue, ClusterConfig) ++
      toMap(AppConstant.Files, SubmitFiles, ClusterConfig) ++
      toMap(AppConstant.Archives, SubmitArchives, ClusterConfig) ++
      toMap(AppConstant.AddJars, SubmitAddJars, ClusterConfig) ++
      toMap(AppConstant.NumExecutors, SubmitNumExecutors, ClusterConfig) ++
      toMap(AppConstant.DriverCores, SubmitDriverCores, ClusterConfig) ++
      toMap(AppConstant.DriverMemory, SubmitDriverMemory, ClusterConfig) ++
      toMap(AppConstant.ExecutorCores, SubmitExecutorCores, ClusterConfig) ++
      toMap(AppConstant.ExecutorMemory, SubmitExecutorMemory, ClusterConfig)
}
