/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services


import java.nio.file.{Files, Paths}
import javax.xml.bind.DatatypeConverter

import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.GraphStep
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant.{SubmitMasterConf, _}
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.helpers.WorkflowHelper._
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.services.SparkSubmitService._
import com.stratio.sparta.serving.core.utils.ArgumentsUtils
import com.typesafe.config.Config
import org.apache.spark.security.VaultHelper._

import scala.collection.JavaConversions._
import scala.util.{Failure, Properties, Success, Try}

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
      "workflowId" -> workflow.id.get,
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

  def getSparkLocalWorkflowConfig: Map[String, String] = getUserSparkConfig

  /** Private Methods **/

  private[core] def getSparkClusterConfig: Map[String, String] = {
    Map(
      SubmitCoarseConf -> workflow.settings.sparkSettings.sparkConf.coarse.map(_.toString),
      SubmitGracefullyStopConf -> workflow.settings.streamingSettings.stopGracefully.map(_.toString),
      SubmitGracefullyStopTimeoutConf -> workflow.settings.streamingSettings.stopGracefulTimeout.notBlank,
      SubmitLogStagesProgressConf -> workflow.settings.sparkSettings.sparkConf.logStagesProgress.map(_.toString),
      SubmitHdfsCacheConf -> workflow.settings.sparkSettings.sparkConf.hdfsTokenCache.map(_.toString),
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
      SubmitBackPressureMaxRateConf -> workflow.settings.streamingSettings.backpressureMaxRate.notBlank,
      SubmitExecutorExtraJavaOptionsConf -> workflow.settings.sparkSettings.sparkConf.executorExtraJavaOptions.notBlank,
      SubmitMemoryFractionConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.
        sparkMemoryFraction.notBlank,
      SubmitExecutorDockerImageConf -> workflow.settings.sparkSettings.sparkConf.executorDockerImage.notBlank
        .orElse(Option("qa.stratio.com/stratio/spark-stratio-driver:2.2.0-1.0.0")),
      SubmitExecutorDockerVolumeConf -> Option("/opt/mesosphere/packages/:/opt/mesosphere/packages/:ro," +
        "/opt/mesosphere/lib/:/opt/mesosphere/lib/:ro," +
        "/etc/pki/ca-trust/extracted/java/cacerts/:" +
        "/usr/lib/jvm/jre1.8.0_112/lib/security/cacerts:ro," +
        "/etc/resolv.conf:/etc/resolv.conf:ro"),
      SubmitMesosNativeLibConf -> Option("/opt/mesosphere/lib/libmesos.so"),
      SubmitExecutorHomeConf -> Option("/opt/spark/dist"),
      SubmitDefaultParalelismConf -> workflow.settings.sparkSettings.sparkConf.sparkResourcesConf.
        sparkParallelism.notBlank,
      SubmitBlockIntervalConf -> workflow.settings.streamingSettings.blockInterval.notBlank,
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
      SubmitDeployMode -> workflow.settings.sparkSettings.submitArguments.deployMode.map(_.toString),
      SubmitDriverJavaOptions -> workflow.settings.sparkSettings.submitArguments.driverJavaOptions.notBlank
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

  private[core] def addCalicoNetworkConf(sparkConfs: Map[String, String]): Map[String, String] = {
    val calicoNetworkFromEnv = for {
      calicoEnable <- Properties.envOrNone(CalicoEnableEnv)
      if Try(calicoEnable.toBoolean).getOrElse(false)
      calicoNetwork <- Properties.envOrNone(CalicoNetworkEnv)
    } yield calicoNetwork
    
    calicoNetworkFromEnv match {
      case Some(someNetwork) => sparkConfs ++ Map(
        SubmitDriverCalicoNetworkConf -> someNetwork,
        SubmitExecutorCalicoNetworkConf -> someNetwork
      )
      case _ => sparkConfs
    }
  }

  private[core] def addNginxPrefixConf(sparkConfs: Map[String, String]): Map[String, String] = {
    for {
      _ <- Properties.envOrNone("MARATHON_APP_LABEL_HAPROXY_1_VHOST").notBlank
      appName <- Properties.envOrNone(DcosServiceName).notBlank
    } yield sparkConfs + (SubmitUiProxyPrefix -> s"/workflows-$appName${workflow.group.name}/${workflow.name}/${workflow.name}-v${workflow.version}")
  } getOrElse sparkConfs


  private[core] def addMesosSecurityConf(sparkConfs: Map[String, String]): Map[String, String] =
    getMesosConstraintConf ++ getMesosSecurityConfs ++ sparkConfs

  private[core] def addPluginsConfs(sparkConfs: Map[String, String]): Map[String, String] =
    sparkConfs ++
      getConfigurationsFromObjects(workflow, GraphStep.SparkSubmitConfMethod) ++
      getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)

  //TODO remove variables that is not necessary include it in core-site and hdfs-site
  private[core] def addKerberosConfs(sparkConfs: Map[String, String]): Map[String, String] =
    (workflow.settings.sparkSettings.sparkKerberos, HdfsService.getPrincipalName(hdfsConfig).notBlank) match {
      case (true, Some(principalName)) =>
        log.info(s"Launching Spark Submit with Kerberos security, adding principal configurations")
        sparkConfs ++ Map(
          "spark.hadoop.yarn.resourcemanager.principal" -> principalName,
          "spark.yarn.principal" -> principalName
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
    val securityProperties: Map[String, String] = (vaultHost, vaultPort) match {
      case (Some(host), Some(port)) =>
        Map(
          "spark.mesos.driverEnv.VAULT_HOSTS" -> host,
          "spark.mesos.driverEnv.VAULT_HOST" -> host,
          "spark.mesos.driverEnv.VAULT_PORT" -> port,
          "spark.mesos.driverEnv.VAULT_PROTOCOL" -> "https"
        ) ++ {
          if (vaultToken.isDefined && !useDynamicAuthentication)
            getTemporalToken match {
              case Success(token) =>
                Map("spark.mesos.driverEnv.VAULT_TEMP_TOKEN" -> token)
              case Failure(x) =>
                log.error("The temporal token could not be retrieved")
                Map.empty[String, String]
            }
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
      (Properties.envOrNone(MesosRoleEnv).notBlank, Properties.envOrNone(TenantEnv).notBlank) match {
        case (Some(role), Some(tenantName)) =>
          Map(
            "spark.mesos.role" -> role,
            "spark.mesos.driverEnv.SPARK_SECURITY_MESOS_ENABLE" -> "true",
            "spark.mesos.driverEnv.SPARK_SECURITY_MESOS_VAULT_PATH" -> s"v1/userland/passwords/$tenantName/mesos"
          ) ++ securityOptions
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
            driverSecretFolder <- envOrNone("SPARK_DRIVER_SECRET_FOLDER").notBlank
          } yield {
            Map(
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_ENABLE" -> "true",
              "spark.mesos.driverEnv.SPARK_DRIVER_SECRET_FOLDER" -> driverSecretFolder,
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_CERT_PATH" -> certPath,
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH" -> certPassPath,
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_KEY_PASS_PATH" -> keyPassPath,
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PATH" -> trustStorePath,
              "spark.mesos.driverEnv.SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PASS_PATH" -> trustStorePassPath,
              "spark.executorEnv.SPARK_DATASTORE_SSL_ENABLE" -> "true",
              "spark.executorEnv.SPARK_SECURITY_DATASTORE_ENABLE" -> "true",
              "spark.executorEnv.SPARK_DRIVER_SECRET_FOLDER" -> driverSecretFolder,
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
      val appName = s"${workflow.group.name.replaceFirst("/", "").replaceAll("/", "-")}-${workflow.name}-v${workflow.version}"
      sparkConfs ++ Map(SubmitAppNameConf -> appName)
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
    mixingSparkJarsConfigurations(getJarsSparkConfigurations(pluginsFiles), sparkConfs)
}

object SparkSubmitService {

  lazy val spartaTenant = Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME",
    Properties.envOrElse("TENANT_NAME", "sparta"))
  lazy val spartaLocalAppName = s"$spartaTenant-spark-standalone"
  lazy val extraSparkJarsPath = Try(SpartaConfig.crossdataConfig.get.getString("session.sparkjars-path"))
    .getOrElse("/opt/sds/sparta/repo")
  lazy val mapExtraSparkJars: Seq[String] = Seq(
    s"$extraSparkJarsPath/org/elasticsearch/elasticsearch-spark-20_2.11/6.1.1/elasticsearch-spark-20_2.11-6.1.1.jar",
    s"$extraSparkJarsPath/com/databricks/spark-avro_2.11/4.0.0/spark-avro_2.11-4.0.0.jar"
  )

  def getJarsSparkConfigurations(jarFiles: Seq[String],extraJars : Boolean  = false): Map[String, String] =
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

      if (extraJars && !Try(SpartaConfig.sparkConfig.get.getString("master")).getOrElse("local").contains("local")) {
        classpathConfs ++ Map(SubmitJarsConf -> (jarFiles ++: mapExtraSparkJars).filter(_.nonEmpty).mkString(","))
      } else {
        classpathConfs ++ Map(SubmitJarsConf -> jarFiles.filter(_.nonEmpty).mkString(","))
      }
    } else Map.empty[String, String]

  def mixingSparkJarsConfigurations(
                                     jarConfs: Map[String, String],
                                     sparkConfs: Map[String, String]
                                   ): Map[String, String] = {
    jarConfs ++ sparkConfs.flatMap { case (confKey, value) =>
      if (value.nonEmpty) {
        if (jarConfs.contains(confKey)) {
          val separator = if (confKey.contains("ClassPath")) ":" else ","
          Option(confKey -> s"$value$separator${jarConfs(confKey)}")
        } else Option(confKey -> value)
      } else None
    }
  }

  def getSparkStandAloneConfig: Map[String, String] = {
    val defaultConf = Map(
      SubmitNameConf -> spartaLocalAppName
    )
    val referenceConf = SpartaConfig.sparkConfig.fold(defaultConf) { sparkConfig =>
      sparkConfig.entrySet().iterator().toSeq.map { values =>
        s"spark.${values.getKey}" -> values.getValue.render().replace("\"", "")
      }.toMap
    }
    val envConf = sys.env.filterKeys(key => key.startsWith("SPARK_EXTRA_CONFIG"))
      .map{ case (key, value) =>
        key.replaceAll("SPARK_EXTRA_CONFIG_", "").replaceAll("_", ".") -> value
      }
    referenceConf ++ defaultConf ++ envConf
  }

}

