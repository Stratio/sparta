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

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.helpers.PolicyHelper._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, SubmitArgument}
import com.typesafe.config.{Config, ConfigValueFactory}

import scala.collection.JavaConversions._
import scala.util.{Failure, Properties, Success, Try}

trait SparkSubmitUtils extends PolicyConfigUtils with ArgumentsUtils {

  // Properties mapped to Spark Configuration
  val SpartaDriverClass = "com.stratio.sparta.driver.SparkDriver"
  val SubmitDeployMode = "--deploy-mode"
  val SubmitName = "--name"
  val SubmitNameConf = "spark.app.name"
  val SubmitTotalExecutorCores = "--total-executor-cores"
  val SubmitTotalExecutorCoresConf = "spark.cores.max"
  val SubmitPackages = "--packages"
  val SubmitPackagesConf = "spark.jars.packages"
  val SubmitJars = "--jars"
  val SubmitJarsConf = "spark.jars"
  val SubmitDriverJavaOptions = "--driver-java-options"
  val SubmitDriverJavaOptionsConf = "spark.driver.extraJavaOptions"
  val SubmitDriverLibraryPath = "--driver-library-path"
  val SubmitDriverLibraryPathConf = "spark.driver.extraLibraryPath"
  val SubmitDriverClassPath = "--driver-class-path"
  val SubmitDriverClassPathConf = "spark.driver.extraClassPath"
  val SubmitExecutorClassPathConf = "spark.executor.extraClassPath"
  val SubmitExcludePackages = "--exclude-packages"
  val SubmitExcludePackagesConf = "spark.jars.excludes"
  val SubmitDriverCores = "--driver-cores"
  val SubmitDriverCoresConf = "spark.driver.cores"
  val SubmitDriverMemory = "--driver-memory"
  val SubmitDriverMemoryConf = "spark.driver.memory"
  val SubmitExecutorCores = "--executor-cores"
  val SubmitExecutorCoresConf = "spark.executor.cores"
  val SubmitExecutorMemory = "--executor-memory"
  val SubmitExecutorMemoryConf = "spark.executor.memory"
  val SubmitGracefullyStopConf = "spark.streaming.stopGracefullyOnShutdown"
  val SubmitAppNameConf = "spark.app.name"
  val SubmitSparkUserConf = "spark.mesos.driverEnv.SPARK_USER"

  // Properties only available in spark-submit
  val SubmitPropertiesFile = "--properties-file"
  val SubmitRepositories = "--repositories"
  val SubmitProxyUser = "--proxy-user"
  val SubmitYarnQueue = "--queue"
  val SubmitFiles = "--files"
  val SubmitArchives = "--archives"
  val SubmitAddJars = "--addJars"
  val SubmitNumExecutors = "--num-executors"
  val SubmitPrincipal = "--principal"
  val SubmitKeyTab = "--keytab"
  val SubmitSupervise = "--supervise"

  // Spark submit arguments supported
  val SubmitArguments = Seq(SubmitDeployMode, SubmitName, SubmitPropertiesFile, SubmitTotalExecutorCores,
    SubmitPackages, SubmitRepositories, SubmitExcludePackages, SubmitJars, SubmitProxyUser, SubmitDriverJavaOptions,
    SubmitDriverLibraryPath, SubmitDriverClassPath, SubmitYarnQueue, SubmitFiles, SubmitArchives, SubmitAddJars,
    SubmitNumExecutors, SubmitDriverCores, SubmitDriverMemory, SubmitExecutorCores, SubmitExecutorMemory,
    SubmitPrincipal, SubmitKeyTab, SubmitSupervise)

  // Spark submit arguments and their spark configuration related
  val SubmitArgumentsToConfProperties = Map(
    SubmitName -> SubmitNameConf,
    SubmitTotalExecutorCores -> SubmitTotalExecutorCoresConf,
    SubmitPackages -> SubmitPackagesConf,
    SubmitExcludePackages -> SubmitExcludePackagesConf,
    SubmitJars -> SubmitJarsConf,
    SubmitDriverCores -> SubmitDriverCoresConf,
    SubmitDriverMemory -> SubmitDriverMemoryConf,
    SubmitExecutorCores -> SubmitExecutorCoresConf,
    SubmitExecutorMemory -> SubmitExecutorMemoryConf
  )

  def extractMarathonDriverSubmit(policy: PolicyModel, detailConfig: Config, hdfsConfig: Option[Config]): String =
    driverJarSubmit(policy, detailConfig, hdfsConfig, DefaultMarathonDriverURI)

  def extractDriverClusterSubmit(policy: PolicyModel, detailConfig: Config, hdfsConfig: Option[Config]): String =
    driverJarSubmit(policy, detailConfig, hdfsConfig, DefaultProvidedDriverURI)

  def extractSparkHome(clusterConfig: Config): String =
    Properties.envOrElse("SPARK_HOME", clusterConfig.getString(SparkHome)).trim

  /**
   * Checks if we have a valid Spark home.
   */
  def validateSparkHome(clusterConfig: Config): String = {
    val sparkHome = Try(extractSparkHome(clusterConfig))
    require(sparkHome.isSuccess,
      "You must set the $SPARK_HOME path in configuration or environment")
    sparkHome.get
  }

  def extractDriverArguments(policy: PolicyModel,
                             driverFile: String,
                             clusterConfig: Config,
                             zookeeperConfig: Config,
                             executionMode: String,
                             pluginsFiles: Seq[String]): Map[String, String] = {
    val driverLocationKey = driverLocation(driverFile)
    val driverLocationConfig = SpartaConfig.initOptionalConfig(driverLocationKey, SpartaConfig.mainConfig)
    val detailConfig = DetailConfig.withValue("executionMode", ConfigValueFactory.fromAnyRef(executionMode))

    Map(
      "clusterConfig" -> keyOptionConfigEncoded(executionMode, Option(clusterConfig)),
      "detailConfig" -> keyConfigEncoded("config", detailConfig),
      "plugins" -> pluginsEncoded(pluginsFiles),
      "policyId" -> policy.id.get.trim,
      "storageConfig" -> keyOptionConfigEncoded(driverLocationKey, driverLocationConfig),
      "zookeeperConfig" -> keyConfigEncoded("zookeeper", zookeeperConfig)
    )
  }

  def extractSubmitArgumentsAndSparkConf(policy: PolicyModel,
                                         clusterConfig: Config,
                                         pluginsFiles: Seq[String]): (Map[String, String], Map[String, String]) = {
    val sparkConfFromProps = getSparkConfFromProps(clusterConfig)
    val sparkConfFromPolicy = getSparkConfigFromPolicy(policy)
    val submitArgumentsFromProps = submitArgsFromProps(clusterConfig)
    val sparkConfFromSubmitArgumentsProps = submitArgsToConf(submitArgumentsFromProps)
    val submitArgumentsFromPolicy = submitArgsFromPolicy(policy.sparkSubmitArguments)
    val sparkConfFromSubmitArgumentsPolicy = submitArgsToConf(submitArgumentsFromPolicy)

    (addSupervisedArgument(addKerberosArguments(
      submitArgsFiltered(submitArgumentsFromProps) ++ submitArgsFiltered(submitArgumentsFromPolicy),
      policy.sparkKerberos)),
      addPluginsConfs(
        addSparkUserConf(
          addAppNameConf(
            addGracefulStopConf(
              addPluginsFilesToConf(
                sparkConfFromSubmitArgumentsProps ++ sparkConfFromProps ++ sparkConfFromSubmitArgumentsPolicy
                  ++ sparkConfFromPolicy, pluginsFiles
              ), gracefulStop(policy)
            ), policy.name
          ), policy.sparkUser
        ), policy
      )
    )
  }

  /** Protected Methods **/

  protected def addSparkUserConf(sparkConfs: Map[String, String], sparkUser: Option[String]): Map[String, String] = {
    if (!sparkConfs.contains(SubmitSparkUserConf) && sparkUser.isDefined) {
      sparkConfs ++ Map(SubmitSparkUserConf -> sparkUser.get)
    } else sparkConfs
  }

  protected def driverJarSubmit(policy: PolicyModel,
                                detailConfig: Config,
                                hdfsConfig: Option[Config],
                                defaultValue: String
                               ): String = {
    val driverStorageLocation = Try(optionFromPolicyAndProperties(policy.driverUri, detailConfig, DriverURI))
      .getOrElse(defaultValue)
    if (driverLocation(driverStorageLocation) == ConfigHdfs) {
      val Hdfs = HdfsUtils()
      val Uploader = ClusterSparkFilesUtils(policy, Hdfs)

      Uploader.uploadDriverFile(driverStorageLocation)
    } else driverStorageLocation
  }

  protected def addPluginsConfs(sparkConfs: Map[String, String], policy: PolicyModel): Map[String, String] = {
    val inputConfs = policy.input.fold(Map.empty[String, String]) { input =>
      getSparkConfigs(Seq(input), Input.SparkSubmitConfigurationMethod, Input.ClassSuffix)
    }
    val outputsConfs = getSparkConfigs(policy.outputs, Output.SparkSubmitConfigurationMethod, Output.ClassSuffix)

    sparkConfs ++ inputConfs ++ outputsConfs
  }

  protected def addAppNameConf(sparkConfs: Map[String, String], name: String): Map[String, String] = {
    if (!sparkConfs.contains(SubmitAppNameConf)) {
      val format = DateTimeFormat.forPattern("yyyy/MM/dd-hh:mm:ss")
      sparkConfs ++ Map(SubmitAppNameConf -> s"$name-${format.print(DateTime.now)}")
    } else sparkConfs
  }

  protected def driverLocation(driverPath: String): String = {
    val begin = 0
    val end = 4

    Try(driverPath.substring(begin, end) match {
      case "hdfs" => "hdfs"
      case _ => "provided"
    }).getOrElse(DefaultDriverLocation)
  }

  protected def optionFromPolicyAndProperties(policyOption: Option[String],
                                              configuration: Config, configurationKey: String): String =
    policyOption.filter(_.trim.nonEmpty).getOrElse(configuration.getString(configurationKey)).trim

  protected def submitArgsFromPolicy(submitArgs: Seq[SubmitArgument]): Map[String, String] =
    submitArgs.flatMap(argument => {
      if (argument.submitArgument.nonEmpty) {
        if (!SubmitArguments.contains(argument.submitArgument))
          log.warn(s"Spark submit argument added unrecognized by Sparta.\t" +
            s"Argument: ${argument.submitArgument}\tValue: ${argument.submitValue}")
        Some(argument.submitArgument.trim -> argument.submitValue.trim)
      } else None
    }).toMap

  protected def submitArgsFromProps(clusterConfig: Config): Map[String, String] =
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
      toMap(DriverCores, SubmitDriverCores, clusterConfig) ++
      toMap(DriverMemory, SubmitDriverMemory, clusterConfig) ++
      toMap(ExecutorCores, SubmitExecutorCores, clusterConfig) ++
      toMap(ExecutorMemory, SubmitExecutorMemory, clusterConfig) ++
      // Yarn only
      toMap(YarnQueue, SubmitYarnQueue, clusterConfig) ++
      toMap(Files, SubmitFiles, clusterConfig) ++
      toMap(Archives, SubmitArchives, clusterConfig) ++
      toMap(AddJars, SubmitAddJars, clusterConfig) ++
      toMap(NumExecutors, SubmitNumExecutors, clusterConfig) ++
      toMap(Supervise, SubmitSupervise, clusterConfig)

  protected def toMap(key: String, newKey: String, config: Config): Map[String, String] =
    Try(config.getString(key)) match {
      case Success(value) =>
        Map(newKey.trim -> value.trim)
      case Failure(_) =>
        log.debug(s"The key $key was not defined in config.")
        Map.empty[String, String]
    }

  protected def submitArgsToConf(submitArgs: Map[String, String]): Map[String, String] =
    submitArgs.flatMap { case (argument, value) =>
      SubmitArgumentsToConfProperties.find { case (submitArgument, confProp) => submitArgument == argument }
        .map { case (submitArgument, confProp) => confProp -> value }
    }

  protected def submitArgsFiltered(submitArgs: Map[String, String]): Map[String, String] =
    submitArgs.filter { case (argument, value) => !SubmitArgumentsToConfProperties.contains(argument) }

  protected def addPluginsFilesToConf(sparkConfs: Map[String, String], pluginsFiles: Seq[String])
  : Map[String, String] = {
    if (pluginsFiles.exists(_.trim.nonEmpty)) {
      val confWithJars = addPropValueToConf(pluginsFiles.mkString(","), SubmitJarsConf, sparkConfs)
      val pluginsFiltered = pluginsFiles.filter(file => !file.startsWith("hdfs") && !file.startsWith("http"))

      if (pluginsFiltered.nonEmpty) {
        val plugins = pluginsFiltered.mkString(",")
        val confWithDriverClassPath = addPropValueToConf(plugins, SubmitDriverClassPathConf, confWithJars)

        addPropValueToConf(plugins, SubmitExecutorClassPathConf, confWithDriverClassPath)
      } else confWithJars
    } else sparkConfs
  }

  protected def addPropValueToConf(pluginsFiles: String,
                                   sparkConfKey: String,
                                   sparkConfs: Map[String, String]): Map[String, String] =
    if (sparkConfs.contains(sparkConfKey))
      sparkConfs.map { case (confKey, value) =>
        if (confKey == sparkConfKey) confKey -> s"$value,$pluginsFiles"
        else confKey -> value
      }
    else sparkConfs ++ Map(sparkConfKey -> pluginsFiles)

  def addGracefulStopConf(sparkConfs: Map[String, String], gracefullyStop: Option[Boolean]): Map[String, String] =
    gracefullyStop.fold(sparkConfs) { gStop => sparkConfs ++ Map(SubmitGracefullyStopConf -> gStop.toString) }

  protected def addKerberosArguments(submitArgs: Map[String, String],
                                     sparkKerberos: Option[Boolean]): Map[String, String] =
    (sparkKerberos, HdfsUtils.getPrincipalName, HdfsUtils.getKeyTabPath) match {
      case (Some(kerberosEnable), Some(principalName), Some(keyTabPath)) if kerberosEnable =>
        log.info(s"Launching Spark Submit with Kerberos security, adding principal and keyTab arguments... \n\t")
        submitArgs ++ Map(SubmitPrincipal -> principalName, SubmitKeyTab -> keyTabPath)
      case _ =>
        submitArgs
    }

  protected def addSupervisedArgument(submitArgs: Map[String, String]): Map[String, String] =
    submitArgs.flatMap { case (argumentKey, value) =>
      if (argumentKey == SubmitSupervise)
        if (value == "true") Some(SubmitSupervise -> "") else None
      else Some(argumentKey -> value)
    }
}
