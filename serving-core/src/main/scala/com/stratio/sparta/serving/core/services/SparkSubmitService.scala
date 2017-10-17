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

package com.stratio.sparta.serving.core.services

import java.io.{File, Serializable => JSerializable}
import java.io.File
import java.io.{Serializable => JSerializable}
import java.nio.file.{Files, Paths}
import javax.xml.bind.DatatypeConverter

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.GraphStep
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant._
import com.stratio.sparta.serving.core.helpers.WorkflowHelper._
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.utils.{ArgumentsUtils, ClusterSparkFilesUtils, HdfsUtils}
import com.typesafe.config.Config
import org.apache.spark.security.VaultHelper._

import scala.collection.JavaConversions._
import scala.util.{Properties, Try}

class SparkSubmitService(workflow: Workflow) extends ArgumentsUtils {

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

  def extractDriverSubmit(detailConfig: Config): String = {
    val driverStorageLocation = Try(detailConfig.getString("driverLocation"))
      .getOrElse("/opt/sds/sparta/driver/sparta-driver.jar")
    if (driverLocation(driverStorageLocation) == ConfigHdfs) {
      val Hdfs = HdfsUtils()
      val Uploader = ClusterSparkFilesUtils(workflow, Hdfs)

      Uploader.uploadDriverFile(driverStorageLocation)
    } else driverStorageLocation
  }

  /**
    * Checks if we have a valid Spark home.
    */
  def validateSparkHome: String = {
    val sparkHome = extractSparkHome.notBlank
    require(sparkHome.isDefined,
      "You must set the $SPARK_HOME path in configuration or environment")
    sparkHome.get
  }

  def extractDriverArgs(zookeeperConfig: Config,
                        pluginsFiles: Seq[String],
                        detailConfig: Config): Map[String, String] = {
    val hdfsConfig = SpartaConfig.getHdfsConfig

    Map(
      "detailConfig" -> keyConfigEncoded("config", detailConfig),
      "hdfsConfig" -> keyOptionConfigEncoded("hdfs", hdfsConfig),
      "plugins" -> pluginsEncoded(pluginsFiles),
      "workflowId" -> workflow.id.get.trim,
      "zookeeperConfig" -> keyConfigEncoded("zookeeper", zookeeperConfig)
    )
  }

  def extractSubmitArgsAndSparkConf(pluginsFiles: Seq[String]): (Map[String, String], Map[String, String]) = {
    val sparkConfs = getSparkClusterConfig
    val submitArgs = getSparkSubmitArgs
    val sparkConfFromSubmitArgs = submitArgsToConf(submitArgs)

    (addJdbcDrivers(addSupervisedArgument(addKerberosArguments(submitArgsFiltered(submitArgs)))),
      addKerberosConfs(addTlsConfs(addPluginsConfs(addSparkUserConf(addAppNameConf(addCalicoNetworkConf(
        addMesosSecurityConf(addPluginsFilesToConf(sparkConfs ++ sparkConfFromSubmitArgs, pluginsFiles)))))))))
  }

  def userPluginsJars: Seq[String] = workflow.settings.global.userPluginsJars.map(userJar => userJar.jarPath.trim)

  def userPluginsFiles: Seq[File] = workflow.settings.global.userPluginsJars.filter(!_.jarPath.isEmpty)
    .map(_.jarPath).distinct.map(filePath => new File(filePath))

  def getSparkLocalConfig: Map[String, String] =
    Map(
      SubmitNameConf -> Option("SPARTA"),
      SubmitMasterConf -> Option(workflow.settings.sparkSettings.master),
      SubmitDriverMemoryConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.driverMemory,
      SubmitDriverCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.driverCores,
      SubmitExecutorMemoryConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.executorMemory,
      SubmitExecutorCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.executorCores,
      SubmitBinaryStringConf -> workflow.settings.sparkSettings.sparkConf.parquetBinaryAsString.map(_.toString),
      SubmitLocalityWaitConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.localityWait,
      SubmitTaskMaxFailuresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.taskMaxFailures,
      SubmitBackPressureEnableConf -> workflow.settings.streamingSettings.backpressure.map(_.toString),
      SubmitBackPressureInitialRateConf -> workflow.settings.streamingSettings.backpressureInitialRate
    ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> value)) }.flatten.toMap ++ getUserSparkConfig

  /** Private Methods **/

  //scalastyle:off
  private[sparta] def getSparkClusterConfig: Map[String, String] = {
    Map(
      SubmitCoarseConf -> workflow.settings.sparkSettings.sparkConf.coarse.map(_.toString),
      SubmitGracefullyStopConf -> workflow.settings.sparkSettings.sparkConf.stopGracefully.map(_.toString),
      SubmitGracefullyStopTimeoutConf -> workflow.settings.sparkSettings.sparkConf.stopGracefulTimeout,
      SubmitBinaryStringConf -> workflow.settings.sparkSettings.sparkConf.parquetBinaryAsString.map(_.toString),
      SubmitTotalExecutorCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.coresMax,
      SubmitExecutorMemoryConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.executorMemory,
      SubmitExecutorCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.executorCores,
      SubmitDriverCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.driverCores,
      SubmitDriverMemoryConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.driverMemory,
      SubmitExtraCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.mesosExtraCores,
      SubmitLocalityWaitConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.localityWait,
      SubmitLocalDirConf -> workflow.settings.sparkSettings.sparkConf.sparkLocalDir,
      SubmitTaskMaxFailuresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.taskMaxFailures,
      SubmitBackPressureEnableConf -> workflow.settings.streamingSettings.backpressure.map(_.toString),
      SubmitBackPressureInitialRateConf -> workflow.settings.streamingSettings.backpressureInitialRate,
      SubmitExecutorExtraJavaOptionsConf -> workflow.settings.sparkSettings.sparkConf.executorExtraJavaOptions,
      SubmitMemoryFractionConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.sparkMemoryFraction,
      SubmitExecutorDockerImageConf -> workflow.settings.sparkSettings.sparkConf.sparkDockerConf.executorDockerImage,
      SubmitExecutorDockerVolumeConf -> workflow.settings.sparkSettings.sparkConf.sparkDockerConf.executorDockerVolumes,
      SubmitExecutorDockerForcePullConf -> workflow.settings.sparkSettings.sparkConf.sparkDockerConf.executorForcePullImage.map(_.toString),
      SubmitMesosNativeLibConf -> workflow.settings.sparkSettings.sparkConf.sparkMesosConf.mesosNativeJavaLibrary,
      SubmitExecutorHomeConf -> Option("/opt/spark/dist"),
      SubmitHdfsUriConf -> workflow.settings.sparkSettings.sparkConf.sparkMesosConf.mesosHDFSConfURI
    ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> value)) }.flatten.toMap ++ getUserSparkConfig
  }

  //scalastyle:on

  private[sparta] def getUserSparkConfig: Map[String, String] =
    workflow.settings.sparkSettings.sparkConf.userSparkConf.flatMap { sparkProperty =>
      if (sparkProperty.sparkConfKey.isEmpty || sparkProperty.sparkConfValue.isEmpty)
        None
      else Option((sparkProperty.sparkConfKey, sparkProperty.sparkConfValue))
    }.toMap

  private[sparta] def getSparkSubmitArgs: Map[String, String] = {
    Map(
      SubmitDeployMode -> workflow.settings.sparkSettings.submitArguments.deployMode,
      SubmitSupervise -> workflow.settings.sparkSettings.submitArguments.supervise.map(_.toString),
      SubmitJars -> workflow.settings.sparkSettings.submitArguments.jars,
      SubmitPropertiesFile -> workflow.settings.sparkSettings.submitArguments.propertiesFile,
      SubmitPackages -> workflow.settings.sparkSettings.submitArguments.packages,
      SubmitExcludePackages -> workflow.settings.sparkSettings.submitArguments.excludePackages,
      SubmitRepositories -> workflow.settings.sparkSettings.submitArguments.repositories,
      SubmitProxyUser -> workflow.settings.sparkSettings.submitArguments.proxyUser,
      SubmitDriverJavaOptions -> workflow.settings.sparkSettings.submitArguments.driverJavaOptions,
      SubmitDriverLibraryPath -> workflow.settings.sparkSettings.submitArguments.driverLibraryPath,
      SubmitDriverClassPath -> workflow.settings.sparkSettings.submitArguments.driverClassPath
    ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> value)) }.flatten.toMap ++ userSubmitArgsFromWorkflow
  }

  private[sparta] def extractSparkHome: Option[String] =
    Properties.envOrNone("SPARK_HOME").notBlank.orElse(Option("/opt/spark/dist"))

  private[sparta] def addSparkUserConf(sparkConfs: Map[String, String]): Map[String, String] =
    if (!sparkConfs.contains(SubmitSparkUserConf) &&
      workflow.settings.sparkSettings.sparkConf.sparkUser.notBlank.isDefined) {
      sparkConfs ++ Map(SubmitSparkUserConf -> workflow.settings.sparkSettings.sparkConf.sparkUser.get)
    } else sparkConfs

  private[sparta] def addCalicoNetworkConf(sparkConfs: Map[String, String]): Map[String, String] =
    Properties.envOrNone(CalicoNetworkEnv).notBlank match {
      case Some(calicoNetwork) =>
        sparkConfs ++ Map(
          SubmitDriverCalicoNetworkConf -> calicoNetwork,
          SubmitExecutorCalicoNetworkConf -> calicoNetwork
        )
      case _ => sparkConfs
    }

  private[sparta] def addMesosSecurityConf(sparkConfs: Map[String, String]): Map[String, String] = {
    val mesosOptions = Map(
      SubmitMesosPrincipalConf -> Properties.envOrNone(MesosPrincipalEnv).notBlank,
      SubmitMesosSecretConf -> Properties.envOrNone(MesosSecretEnv).notBlank,
      SubmitMesosRoleConf -> Properties.envOrNone(MesosRoleEnv).notBlank
    )

    mesosOptions.flatMap { case (k, v) => v.notBlank.map(value => Option(k -> value)) }.flatten.toMap ++ sparkConfs
  }

  private[sparta] def addPluginsConfs(sparkConfs: Map[String, String]): Map[String, String] = {
    val sparkConfsReflection = getSparkConfsReflec(workflow.pipelineGraph.nodes, GraphStep.SparkSubmitConfMethod)

    sparkConfs ++ sparkConfsReflection
  }

  private[sparta] def addKerberosConfs(sparkConfs: Map[String, String]): Map[String, String] = {
    (workflow.settings.sparkSettings.sparkKerberos,
      HdfsUtils.getPrincipalName.notBlank,
      HdfsUtils.getKeyTabPath.notBlank) match {
      case (true, Some(principalName), Some(keyTabPath)) =>
        val keyTabBase64 = DatatypeConverter.printBase64Binary(Files.readAllBytes(Paths.get(keyTabPath)))
        log.info(s"Launching Spark Submit with Kerberos security, adding principal and keyTab configurations")
        sparkConfs ++ Map(
          "spark.hadoop.yarn.resourcemanager.principal" -> principalName,
          "spark.yarn.principal" -> principalName,
          "spark.mesos.kerberos.keytabBase64" -> keyTabBase64
        )
      case _ =>
        sparkConfs
    }
  }

  private[sparta] def addTlsConfs(sparkConfs: Map[String, String]): Map[String, String] = {
    val tlsEnable = workflow.settings.sparkSettings.sparkDataStoreTls
    val tlsOptions = {
      if (tlsEnable) {
        val useDynamicAuthentication = Try {
          scala.util.Properties.envOrElse("USE_DYNAMIC_AUTHENTICATION", "false").toBoolean
        }.getOrElse(false)
        val vaultHost = scala.util.Properties.envOrNone("VAULT_HOSTS").notBlank
        val vaultPort = scala.util.Properties.envOrNone("VAULT_PORT").notBlank
        val vaultToken = scala.util.Properties.envOrNone("VAULT_TOKEN").notBlank
        val appName = scala.util.Properties.envOrNone("MARATHON_APP_LABEL_DCOS_SERVICE_NAME")
          .notBlank
          .orElse(scala.util.Properties.envOrNone("TENANT_NAME").notBlank)

        (vaultHost, vaultPort, appName) match {
          case (Some(host), Some(port), Some(name)) =>
            Seq(
              ("spark.mesos.driverEnv.SPARK_DATASTORE_SSL_ENABLE", "true"),
              ("spark.mesos.driverEnv.VAULT_HOST", host),
              ("spark.mesos.driverEnv.VAULT_PORT", port),
              ("spark.mesos.driverEnv.VAULT_PROTOCOL", "https"),
              ("spark.mesos.driverEnv.APP_NAME", name),
              ("spark.mesos.driverEnv.CA_NAME", "ca"),
              ("spark.executorEnv.SPARK_DATASTORE_SSL_ENABLE", "true"),
              ("spark.executorEnv.VAULT_HOST", host),
              ("spark.executorEnv.VAULT_PORT", port),
              ("spark.executorEnv.VAULT_PROTOCOL", "https"),
              ("spark.executorEnv.APP_NAME", name),
              ("spark.executorEnv.CA_NAME", "ca"),
              ("spark.secret.vault.host", host),
              ("spark.secret.vault.hosts", host),
              ("spark.secret.vault.port", port),
              ("spark.secret.vault.protocol", "https")
            ) ++ {
              if (vaultToken.isDefined && !useDynamicAuthentication) {
                val tempToken = getTemporalToken(s"https://$host:$port", vaultToken.get)
                Seq(
                  ("spark.mesos.driverEnv.VAULT_TEMP_TOKEN", tempToken)
                  //("spark.secret.vault.tempToken", tempToken)
                )
              } else Seq.empty[(String, String)]
            }
          case _ =>
            log.warn("TLS is enabled but the properties are wrong")
            Seq.empty[(String, String)]
        }
      } else Seq.empty[(String, String)]
    }.toMap

    sparkConfs ++ tlsOptions
  }

  private[sparta] def addAppNameConf(sparkConfs: Map[String, String]): Map[String, String] = {
    if (!sparkConfs.contains(SubmitAppNameConf)) {
      sparkConfs ++ Map(SubmitAppNameConf -> s"${workflow.name}")
    } else sparkConfs
  }

  private[sparta] def driverLocation(driverPath: String): String = {
    val begin = 0
    val end = 4

    Try {
      driverPath.substring(begin, end) match {
        case "hdfs" => "hdfs"
        case _ => "provided"
      }
    }.getOrElse(DefaultDriverLocation)
  }

  private[sparta] def userSubmitArgsFromWorkflow: Map[String, String] =
    workflow.settings.sparkSettings.submitArguments.userArguments.flatMap(argument => {
      if (argument.submitArgument.nonEmpty) {
        if (!SubmitArguments.contains(argument.submitArgument))
          log.warn(s"Spark submit argument added unrecognized by Sparta.\t" +
            s"Argument: ${argument.submitArgument}\tValue: ${argument.submitValue}")
        Some(argument.submitArgument.trim -> argument.submitValue.trim)
      } else None
    }).toMap

  private[sparta] def submitArgsToConf(submitArgs: Map[String, String]): Map[String, String] =
    submitArgs.flatMap { case (argument, value) =>
      SubmitArgumentsToConfProperties.find { case (submitArgument, confProp) => submitArgument == argument }
        .map { case (_, confProp) => confProp -> value }
    }

  private[sparta] def submitArgsFiltered(submitArgs: Map[String, String]): Map[String, String] =
    submitArgs.filter { case (argument, _) => !SubmitArgumentsToConfProperties.contains(argument) }

  private[sparta] def addPluginsFilesToConf(sparkConfs: Map[String, String], pluginsFiles: Seq[String])
  : Map[String, String] =
    if (pluginsFiles.exists(_.trim.nonEmpty)) {
      val confWithJars = addPropValueToConf(pluginsFiles.mkString(","), SubmitJarsConf, sparkConfs)
      val pluginsFiltered = pluginsFiles.filter(file => !file.startsWith("hdfs") && !file.startsWith("http"))

      if (pluginsFiltered.nonEmpty) {
        val plugins = pluginsFiltered.mkString(",")
        val confWithDriverClassPath = addPropValueToConf(plugins, SubmitDriverClassPathConf, confWithJars)

        addPropValueToConf(plugins, SubmitExecutorClassPathConf, confWithDriverClassPath)
      } else confWithJars
    } else sparkConfs

  private[sparta] def addPropValueToConf(pluginsFiles: String,
                                         sparkConfKey: String,
                                         sparkConfs: Map[String, String]): Map[String, String] =
    if (sparkConfs.contains(sparkConfKey))
      sparkConfs.map { case (confKey, value) =>
        if (confKey == sparkConfKey) confKey -> s"$value,$pluginsFiles"
        else confKey -> value
      }
    else sparkConfs ++ Map(sparkConfKey -> pluginsFiles)

  private[sparta] def addKerberosArguments(submitArgs: Map[String, String]): Map[String, String] =
    (workflow.settings.sparkSettings.sparkKerberos,
      HdfsUtils.getPrincipalName.notBlank,
      HdfsUtils.getKeyTabPath.notBlank) match {
      case (true, Some(principalName), Some(keyTabPath)) =>
        log.info(s"Launching Spark Submit with Kerberos security, adding principal and keyTab arguments")
        submitArgs ++ Map(SubmitPrincipal -> principalName, SubmitKeyTab -> keyTabPath)
      case _ =>
        submitArgs
    }

  private[sparta] def addSupervisedArgument(submitArgs: Map[String, String]): Map[String, String] =
    submitArgs.flatMap { case (argumentKey, value) =>
      if (argumentKey == SubmitSupervise)
        if (value == "true") Some(SubmitSupervise -> "") else None
      else Some(argumentKey -> value)
    }

  private[sparta] def addJdbcDrivers(submitArgs: Map[String, String]): Map[String, String] = {
    val jdbcDrivers = new File("/jdbc-drivers")
    if (jdbcDrivers.exists && jdbcDrivers.isDirectory) {
      val jdbcFiles = jdbcDrivers.listFiles()
        .filter(file => file.isFile && file.getName.endsWith("jar"))
        .map(file => file.getAbsolutePath)
      if (jdbcFiles.isEmpty) submitArgs
      else Map(SubmitDriverClassPath -> jdbcFiles.mkString(":")) ++ submitArgs
    } else submitArgs
  }
}