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


import java.nio.file.{Files, Paths}
import javax.xml.bind.DatatypeConverter

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.GraphStep
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant._
import com.stratio.sparta.serving.core.helpers.WorkflowHelper._
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.utils.ArgumentsUtils
import com.typesafe.config.Config
import org.apache.spark.security.VaultHelper._
import SparkSubmitService._
import com.stratio.sparta.serving.core.helpers.JarsHelper

import scala.util.{Properties, Try}

class SparkSubmitService(workflow: Workflow) extends ArgumentsUtils {

  lazy val hdfsConfig: Option[Config] = SpartaConfig.getHdfsConfig
  lazy val hdfsFilesService = HdfsFilesService()

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
    val driverStorageLocation = Try(detailConfig.getString(AppConstant.DriverLocation))
      .getOrElse(AppConstant.DefaultMarathonDriverURI)
    if (driverLocation(driverStorageLocation) == ConfigHdfs)
      hdfsFilesService.uploadDriverFile(driverStorageLocation)
    else driverStorageLocation
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

    (
      addSupervisedArgument(
        addKerberosArguments(
          submitArgsFiltered(submitArgs))),
      addExecutorLogConf(
        addKerberosConfs(
          addTlsConfs(
            addPluginsConfs(
              addSparkUserConf(
                addAppNameConf(
                  addCalicoNetworkConf(
                    addNginxPrefixConf(
                      addPluginsFilesToConf(
                        addMesosSecurityConf(sparkConfs ++ sparkConfFromSubmitArgs),
                        pluginsFiles
                      )))))))))
    )
  }

  def userPluginsJars: Seq[String] = {
    val uploadedPlugins = if (workflow.settings.global.addAllUploadedPlugins)
      Try {
        hdfsFilesService.browsePlugins.flatMap { fileStatus =>
          if (fileStatus.isFile && fileStatus.getPath.getName.endsWith(".jar"))
            Option(fileStatus.getPath.toUri.toString)
          else None
        }
      }.getOrElse(Seq.empty[String])
    else Seq.empty[String]
    val userPlugins = workflow.settings.global.userPluginsJars.map(userJar => userJar.jarPath.toString.trim)

    uploadedPlugins ++ userPlugins
  }

  def getSparkLocalConfig: Map[String, String] =
    Map(
      SubmitNameConf -> Option("SPARTA"),
      SubmitMasterConf -> Option(workflow.settings.sparkSettings.master.toString),
      SubmitDriverMemoryConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.driverMemory.notBlank,
      SubmitDriverCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.driverCores.notBlank,
      SubmitExecutorMemoryConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.executorMemory.notBlank,
      SubmitExecutorCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.executorCores.notBlank,
      SubmitBinaryStringConf -> workflow.settings.sparkSettings.sparkConf.parquetBinaryAsString.map(_.toString),
      SubmitLogStagesProgressConf -> workflow.settings.sparkSettings.sparkConf.logStagesProgress.map(_.toString),
      SubmitLocalityWaitConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.localityWait.notBlank,
      SubmitTaskMaxFailuresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.
        taskMaxFailures.notBlank,
      SubmitBackPressureEnableConf -> workflow.settings.streamingSettings.backpressure.map(_.toString),
      SubmitBackPressureInitialRateConf -> workflow.settings.streamingSettings.backpressureInitialRate.notBlank
    ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> value)) }.flatten.toMap ++ getUserSparkConfig

  /** Private Methods **/

  private[core] def getSparkClusterConfig: Map[String, String] = {
    Map(
      SubmitCoarseConf -> workflow.settings.sparkSettings.sparkConf.coarse.map(_.toString),
      SubmitGracefullyStopConf -> workflow.settings.streamingSettings.stopGracefully.map(_.toString),
      SubmitGracefullyStopTimeoutConf -> workflow.settings.streamingSettings.stopGracefulTimeout.notBlank,
      SubmitBinaryStringConf -> workflow.settings.sparkSettings.sparkConf.parquetBinaryAsString.map(_.toString),
      SubmitLogStagesProgressConf -> workflow.settings.sparkSettings.sparkConf.logStagesProgress.map(_.toString),
      SubmitTotalExecutorCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.coresMax.notBlank,
      SubmitExecutorMemoryConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.executorMemory.notBlank,
      SubmitExecutorCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.executorCores.notBlank,
      SubmitDriverCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.driverCores.notBlank,
      SubmitDriverMemoryConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.driverMemory.notBlank,
      SubmitExtraCoresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.mesosExtraCores.notBlank,
      SubmitLocalityWaitConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.localityWait.notBlank,
      SubmitLocalDirConf -> workflow.settings.sparkSettings.sparkConf.sparkLocalDir.notBlank,
      SubmitTaskMaxFailuresConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.
        taskMaxFailures.notBlank,
      SubmitSqlCaseSensitiveConf -> workflow.settings.sparkSettings.sparkConf.sparkSqlCaseSensitive.map(_.toString),
      SubmitBackPressureEnableConf -> workflow.settings.streamingSettings.backpressure.map(_.toString),
      SubmitBackPressureInitialRateConf -> workflow.settings.streamingSettings.backpressureInitialRate.notBlank,
      SubmitExecutorExtraJavaOptionsConf -> workflow.settings.sparkSettings.sparkConf.executorExtraJavaOptions.notBlank,
      SubmitMemoryFractionConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.
        sparkMemoryFraction.notBlank,
      SubmitExecutorDockerImageConf -> Option("qa.stratio.com/stratio/stratio-spark:2.2.0.4"),
      SubmitExecutorDockerVolumeConf -> Option("/opt/mesosphere/packages/:/opt/mesosphere/packages/:ro," +
        "/opt/mesosphere/lib/:/opt/mesosphere/lib/:ro," +
        "/etc/pki/ca-trust/extracted/java/cacerts/:" +
        "/usr/lib/jvm/jre1.8.0_112/lib/security/cacerts:ro," +
        "/etc/resolv.conf:/etc/resolv.conf:ro"),
      SubmitMesosNativeLibConf -> Option("/opt/mesosphere/lib/libmesos.so"),
      SubmitExecutorHomeConf -> Option("/opt/spark/dist"),
      SubmitDefaultParalelismConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.
        sparkParallelism.notBlank,
      SubmitKryoSerializationConf -> workflow.settings.sparkSettings.sparkConf.sparkKryoSerialization
        .flatMap(enable => if (enable) Option("org.apache.spark.serializer.KryoSerializer") else None)
    ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> value)) }.flatten.toMap ++ getUserSparkConfig
  }

  private[core] def getUserSparkConfig: Map[String, String] =
    workflow.settings.sparkSettings.sparkConf.userSparkConf.flatMap { sparkProperty =>
      if (sparkProperty.sparkConfKey.isEmpty || sparkProperty.sparkConfValue.isEmpty)
        None
      else Option((sparkProperty.sparkConfKey.toString, sparkProperty.sparkConfValue.toString))
    }.toMap

  private[core] def getSparkSubmitArgs: Map[String, String] = {
    Map(
      SubmitDeployMode -> workflow.settings.sparkSettings.submitArguments.deployMode,
      SubmitSupervise -> workflow.settings.sparkSettings.submitArguments.supervise.map(_.toString),
      SubmitJars -> workflow.settings.sparkSettings.submitArguments.jars.notBlank,
      SubmitPropertiesFile -> workflow.settings.sparkSettings.submitArguments.propertiesFile.notBlank,
      SubmitPackages -> workflow.settings.sparkSettings.submitArguments.packages.notBlank,
      SubmitExcludePackages -> workflow.settings.sparkSettings.submitArguments.excludePackages.notBlank,
      SubmitRepositories -> workflow.settings.sparkSettings.submitArguments.repositories.notBlank,
      SubmitProxyUser -> workflow.settings.sparkSettings.submitArguments.proxyUser.notBlank,
      SubmitDriverJavaOptions -> workflow.settings.sparkSettings.submitArguments.driverJavaOptions.notBlank,
      SubmitDriverLibraryPath -> workflow.settings.sparkSettings.submitArguments.driverLibraryPath.notBlank,
      SubmitDriverClassPath -> workflow.settings.sparkSettings.submitArguments.driverClassPath.notBlank
    ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> value)) }.flatten.toMap ++ userSubmitArgsFromWorkflow
  }

  private[core] def extractSparkHome: Option[String] =
    Properties.envOrNone("SPARK_HOME").notBlank.orElse(Option("/opt/spark/dist"))

  private[core] def addSparkUserConf(sparkConfs: Map[String, String]): Map[String, String] =
    if (!sparkConfs.contains(SubmitSparkUserConf) &&
      workflow.settings.sparkSettings.sparkConf.sparkUser.notBlank.isDefined) {
      sparkConfs ++ Map(SubmitSparkUserConf -> workflow.settings.sparkSettings.sparkConf.sparkUser.notBlank.get)
    } else sparkConfs

  private[core] def addExecutorLogConf(sparkConfs: Map[String, String]): Map[String, String] =
    if (!sparkConfs.contains(SubmitExecutorLogLevelConf) && sys.env.get("SPARK_LOG_LEVEL").notBlank.isDefined) {
      sparkConfs ++ Map(SubmitExecutorLogLevelConf -> sys.env.get("SPARK_LOG_LEVEL").notBlank.get)
    } else sparkConfs

  private[core] def addCalicoNetworkConf(sparkConfs: Map[String, String]): Map[String, String] =
    Properties.envOrNone(CalicoNetworkEnv).notBlank.fold(sparkConfs) { calicoNetwork =>
      sparkConfs ++ Map(
        SubmitDriverCalicoNetworkConf -> calicoNetwork,
        SubmitExecutorCalicoNetworkConf -> calicoNetwork
      )
    }

  private[core] def addNginxPrefixConf(sparkConfs: Map[String, String]): Map[String, String] = {
    for {
      _ <- Properties.envOrNone("MARATHON_APP_LABEL_HAPROXY_1_VHOST").notBlank
      appName <- Properties.envOrNone(DcosServiceName).notBlank
    } yield sparkConfs + (SubmitUiProxyPrefix -> s"/workflows-$appName/${workflow.name}-v${workflow.version}")
  } getOrElse sparkConfs


  private[core] def addMesosSecurityConf(sparkConfs: Map[String, String]): Map[String, String] =
    getMesosConstraintConf ++ getMesosSecurityConfs ++ sparkConfs

  private[core] def addPluginsConfs(sparkConfs: Map[String, String]): Map[String, String] =
    sparkConfs ++ getConfigurationsFromObjects(workflow.pipelineGraph.nodes, GraphStep.SparkSubmitConfMethod)

  //TODO remove variables that is not necessary include it in core-site and hdfs-site
  private[core] def addKerberosConfs(sparkConfs: Map[String, String]): Map[String, String] =
    (workflow.settings.sparkSettings.sparkKerberos,
      HdfsService.getPrincipalName(hdfsConfig).notBlank,
      HdfsService.getKeyTabPath(hdfsConfig).notBlank) match {
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

  //scalastyle:off
  private[core] def getSecurityConfigurations: Map[String, String] = {
    val useDynamicAuthentication = Try {
      Properties.envOrElse("USE_DYNAMIC_AUTHENTICATION", "false").toBoolean
    }.getOrElse(false)
    val vaultHost = Properties.envOrNone("VAULT_HOSTS").notBlank
    val vaultPort = Properties.envOrNone("VAULT_PORT").notBlank
    val vaultToken = Properties.envOrNone("VAULT_TOKEN").notBlank
    val securityProperties = (vaultHost, vaultPort) match {
      case (Some(host), Some(port)) =>
        Map(
          "spark.mesos.driverEnv.VAULT_HOSTS" -> host,
          "spark.mesos.driverEnv.VAULT_HOST" -> host,
          "spark.mesos.driverEnv.VAULT_PORT" -> port,
          "spark.mesos.driverEnv.VAULT_PROTOCOL" -> "https"
        ) ++ {
          if (vaultToken.isDefined && !useDynamicAuthentication)
            Map("spark.mesos.driverEnv.VAULT_TEMP_TOKEN" -> getTemporalToken)
          else Map.empty[String, String]
        }
      case _ =>
        Map.empty[String, String]
    }

    securityProperties
  }

  //scalastyle:on

  private[core] def getMesosConstraintConf: Map[String, String] = {
    val envConstraints = Seq(
      Properties.envOrNone(HostnameConstraint).notBlank,
      Properties.envOrNone(AttributeConstraint).notBlank
    ).flatten

    (workflow.settings.global.mesosConstraint.notBlank, envConstraints) match {
      case (Some(workflowConstraint), _) => Map(SubmitMesosConstraintConf -> workflowConstraint)
      case (None, constraints) if constraints.nonEmpty => Map(SubmitMesosConstraintConf -> constraints.mkString(":"))
      case _ => Map.empty[String, String]
    }
  }

  private[core] def getMesosSecurityConfs: Map[String, String] = {
    val securityOptions = getSecurityConfigurations

    if (workflow.settings.sparkSettings.sparkMesosSecurity && securityOptions.nonEmpty) {
      Properties.envOrNone(MesosRoleEnv).notBlank match {
        case Some(role) =>
          Map("spark.mesos.role" -> role) ++ securityOptions
        case _ =>
          log.warn("Mesos security is enabled but the properties are wrong")
          Map.empty[String, String]
      }
    } else {
      log.warn("Mesos security is enabled but the properties are wrong")
      Map.empty[String, String]
    }
  }

  private[core] def addTlsConfs(sparkConfs: Map[String, String]): Map[String, String] = {
    val securityOptions = getSecurityConfigurations
    val tlsOptions = {
      if (workflow.settings.sparkSettings.sparkDataStoreTls && securityOptions.nonEmpty) {

        import scala.util.Properties.envOrNone

        {
          for {
            certPath <- envOrNone("SPARK_SECURITY_DATASTORE_VAULT_CERT_PATH").notBlank
            certPassPath <- envOrNone("SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH").notBlank
            keyPassPath <- envOrNone("SPARK_SECURITY_DATASTORE_VAULT_KEY_PASS_PATH").notBlank
            trustStorePath <- envOrNone("SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PATH").notBlank
            trustStorePassPath <- envOrNone("SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PASS_PATH").notBlank
          } yield {
            Map(
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_ENABLE" -> "true",
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_CERT_PATH" -> certPath,
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH" -> certPassPath,
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_KEY_PASS_PATH" -> keyPassPath,
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PATH" -> trustStorePath,
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PASS_PATH" -> trustStorePassPath,
              "spark.executorEnv.SPARK_DATASTORE_SSL_ENABLE" -> "true",
              "spark.executorEnv.SPARK_SECURITY_DATASTORE_ENABLE" -> "true",
              "spark.executorEnv.SPARK_SECURITY_DATASTORE_VAULT_CERT_PATH" -> certPath,
              "spark.executorEnv.SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH" -> certPassPath,
              "spark.executorEnv.SPARK_SECURITY_DATASTORE_VAULT_KEY_PASS_PATH" -> keyPassPath,
              "spark.executorEnv.SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PATH" -> trustStorePath,
              "spark.executorEnv.SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PASS_PATH" -> trustStorePassPath
            ) ++ securityOptions
          }
        } getOrElse {
          log.warn("TLS is enabled but the properties are wrong")
          Map.empty[String, String]
        }

      } else {
        log.warn("TLS is enabled but the properties are wrong")
        Map.empty[String, String]
      }
    }

    sparkConfs ++ tlsOptions
  }

  private[core] def addAppNameConf(sparkConfs: Map[String, String]): Map[String, String] =
    if (!sparkConfs.contains(SubmitAppNameConf)) {
      sparkConfs ++ Map(SubmitAppNameConf -> s"${workflow.name}")
    } else sparkConfs

  private[core] def driverLocation(driverPath: String): String = {
    val begin = 0
    val end = 4

    Try {
      driverPath.substring(begin, end) match {
        case "hdfs" => "hdfs"
        case _ => DefaultDriverLocation
      }
    }.getOrElse(DefaultDriverLocation)
  }

  private[core] def userSubmitArgsFromWorkflow: Map[String, String] =
    workflow.settings.sparkSettings.submitArguments.userArguments.flatMap { argument =>
      if (argument.submitArgument.toString.nonEmpty) {
        if (!SubmitArguments.contains(argument.submitArgument.toString))
          log.warn(s"Spark submit argument added unrecognized by Sparta.\t" +
            s"Argument: ${argument.submitArgument}\tValue: ${argument.submitValue}")
        Some(argument.submitArgument.toString.trim -> argument.submitValue.toString.trim)
      } else None
    }.toMap

  private[core] def submitArgsToConf(submitArgs: Map[String, String]): Map[String, String] =
    submitArgs.flatMap { case (argument, value) =>
      SubmitArgumentsToConfProperties.find { case (submitArgument, confProp) => submitArgument == argument }
        .map { case (_, confProp) => confProp -> value }
    }

  private[core] def submitArgsFiltered(submitArgs: Map[String, String]): Map[String, String] =
    submitArgs.filter { case (argument, _) => !SubmitArgumentsToConfProperties.contains(argument) }

  private[core] def addPropValueToConf(pluginsFiles: String,
                                       sparkConfKey: String,
                                       sparkConfs: Map[String, String]): Map[String, String] =
    if (sparkConfs.contains(sparkConfKey))
      sparkConfs.map { case (confKey, value) =>
        if (confKey == sparkConfKey) confKey -> s"$value,$pluginsFiles"
        else confKey -> value
      }
    else sparkConfs ++ Map(sparkConfKey -> pluginsFiles)

  private[core] def addKerberosArguments(submitArgs: Map[String, String]): Map[String, String] =
    (
      workflow.settings.sparkSettings.sparkKerberos,
      HdfsService.getPrincipalName(hdfsConfig).notBlank,
      HdfsService.getKeyTabPath(hdfsConfig).notBlank
    ) match {
      case (true, Some(principalName), Some(keyTabPath)) =>
        log.info(s"Launching Spark Submit with Kerberos security, adding principal and keyTab arguments")
        submitArgs ++ Map(SubmitPrincipal -> principalName, SubmitKeyTab -> keyTabPath)
      case _ =>
        submitArgs
    }

  private[core] def addSupervisedArgument(submitArgs: Map[String, String]): Map[String, String] =
    submitArgs.flatMap { case (argumentKey, value) =>
      if (argumentKey == SubmitSupervise)
        if (value == "true") Some(SubmitSupervise -> "") else None
      else Some(argumentKey -> value)
    }

  private[sparta] def addPluginsFilesToConf(
                                             sparkConfs: Map[String, String],
                                             pluginsFiles: Seq[String]
                                           ): Map[String, String] =
    mixingSparkJarsConfigurations(getJarsSparkConfigurations(pluginsFiles ++ JarsHelper.getJdbcDriverPaths), sparkConfs)
}

object SparkSubmitService {

  def getJarsSparkConfigurations(jarFiles: Seq[String]): Map[String, String] =
    if (jarFiles.exists(_.trim.nonEmpty)) {
      val jarFilesFiltered = jarFiles.filter(file =>
        !file.startsWith("hdfs") && !file.startsWith("http") && file.nonEmpty)
      val classpathConfs = if (jarFilesFiltered.nonEmpty) {
        val files = jarFilesFiltered.mkString(":")
        Map(
          SubmitDriverClassPathConf -> files,
          SubmitExecutorClassPathConf -> files
        )
      } else Map.empty[String, String]

      classpathConfs ++ Map(SubmitJarsConf -> jarFiles.filter(_.nonEmpty).mkString(","))
    } else Map.empty[String, String]

  def mixingSparkJarsConfigurations(
                                     jarConfs: Map[String, String],
                                     sparkConfs: Map[String, String]
                                   ): Map[String, String] = {
    jarConfs ++ sparkConfs.flatMap { case (confKey, value) =>
      if(value.nonEmpty) {
        if (jarConfs.contains(confKey)) {
          val separator = if (confKey.contains("ClassPath")) ":" else ","
          Option(confKey -> s"$value$separator${jarConfs(confKey)}")
        } else Option(confKey -> value)
      } else None
    }
  }

}

