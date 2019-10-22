/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.marathon.builder

import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant.{SystemHadoopUserName, spartaTenant, version}
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant.SubmitMesosConstraintConf
import com.stratio.sparta.serving.core.constants.{AppConstant, SparkConstant}
import com.stratio.sparta.serving.core.helpers.InfoHelper
import com.stratio.sparta.serving.core.marathon._
import com.stratio.sparta.serving.core.marathon.service.MarathonService.{calculateMaxTimeout, getHealthChecks}
import com.stratio.sparta.serving.core.models.RocketModes.RocketMode
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowExecution}
import com.stratio.sparta.serving.core.services.SparkSubmitService
import com.typesafe.config.Config

import scala.util.{Properties, Try}

//scalastyle:off
object WorkflowBuilderImplicits {

  lazy val marathonConfig: Config = SpartaConfig.getMarathonConfig().get

  implicit class Builder(marathonApplication: MarathonApplication) extends SpartaSerializer {

    def withId(implicit workflowExecution: WorkflowExecution): MarathonApplication =
      marathonApplication.copy(
        workflowExecution.marathonExecution.map(_.marathonId)
          .getOrElse(throw new RuntimeException("The marathonId doesn't exist in this workflowExecution"))
      )

    def withSpartaDockerImage: MarathonApplication = {
      val appInfo = InfoHelper.getAppInfo
      val versionParsed = if (appInfo.pomVersion != "${project.version}") appInfo.pomVersion else version
      val defaultDockerImage = s"qa.stratio.com/stratio/sparta:$versionParsed"

      marathonApplication.copy(
        container = marathonApplication.container.copy(
          docker = marathonApplication.container.docker.copy(
            image = Try(marathonConfig.getString("docker.image")).toOption.getOrElse(defaultDockerImage)
          )
        )
      )
    }

    def withForcePullImage(implicit workflowExecution: WorkflowExecution): MarathonApplication =
      marathonApplication.copy(
        container = marathonApplication.container.copy(
          docker = marathonApplication.container.docker.copy(
            forcePullImage = Option(workflowExecution.getWorkflowToExecute.settings.global.marathonDeploymentSettings.fold(DefaultForcePullImage) {
              marathonSettings => marathonSettings.forcePullImage.getOrElse(DefaultForcePullImage)
            })
          )
        )
      )

    def withPrivileged(implicit workflowExecution: WorkflowExecution): MarathonApplication =
      marathonApplication.copy(
        container = marathonApplication.container.copy(
          docker = marathonApplication.container.docker.copy(
            privileged = Option(workflowExecution.getWorkflowToExecute.settings.global.marathonDeploymentSettings.fold(DefaultPrivileged) {
              marathonSettings => marathonSettings.privileged.getOrElse(DefaultPrivileged)
            })
          )
        )
      )

    def withCpusFromWorkflowExecution(implicit workflowExecution: WorkflowExecution): MarathonApplication = {
      val sparkCpus = for {
        execution <- workflowExecution.sparkSubmitExecution
        sparkDriverCores <- execution.sparkConfigurations.get("spark.driver.cores")
      } yield {
        sparkDriverCores.toDouble
      }

      marathonApplication.copy(
        cpus = sparkCpus.getOrElse(marathonApplication.cpus)
      )
    }

    def withMemFromWorkflowExecution(implicit workflowExecution: WorkflowExecution): MarathonApplication = {
      val sparkMem = for {
        execution <- workflowExecution.sparkSubmitExecution
        sparkDriverMemory <- execution.sparkConfigurations.get("spark.driver.memory")
      } yield {
        transformMemoryToInt(sparkDriverMemory)
      }

      marathonApplication.copy(
        mem = sparkMem.getOrElse(marathonApplication.mem) + getSOMemory
      )
    }

    def withHealthChecksFromWorkflow(implicit workflowExecution: WorkflowExecution): MarathonApplication = {
      val healthChecks: Option[Seq[MarathonHealthCheck]] = getHealthChecks(workflowExecution.getWorkflowToExecute)
      val healthCheckEnvVariables: Map[String, String] =
        Map(SpartaMarathonTotalTimeBeforeKill -> calculateMaxTimeout(healthChecks).toString)

      marathonApplication.copy(
        healthChecks = Option(marathonApplication.healthChecks
          .getOrElse(Seq.empty[MarathonHealthCheck]) ++ healthChecks.getOrElse(Seq.empty[MarathonHealthCheck])),
        env = Option(marathonApplication.env.getOrElse(Map.empty[String, String]) ++ healthCheckEnvVariables)
      )
    }

    def withConstraints(implicit workflowExecution: WorkflowExecution): MarathonApplication =
      marathonApplication.copy(
        constraints = getConstraint(workflowExecution.getWorkflowToExecute)
      )

    def withInheritedVariablesLabels: MarathonApplication =
      marathonApplication.copy(
        labels = marathonApplication.labels ++ getWorkflowInheritedVariables
      )

    def addWorkflowPropertiesEnv(implicit workflowExecution: WorkflowExecution): MarathonApplication = {
      import com.stratio.sparta.core.properties.ValidatingPropertyMap._
      val submitExecution = workflowExecution.sparkSubmitExecution.get
      val marathonAppHeapSize = Try(getSOMemory / 2).getOrElse(512)


      addEnv(
        Map(
          AppMainEnv -> Option(AppMainClass),
          AppTypeEnv -> Option(MarathonApp),
          PluginFiles -> Option(submitExecution.pluginFiles.mkString(",")),
          ExecutionIdEnv -> submitExecution.driverArguments.get(SparkSubmitService.ExecutionIdKey),
          AppHeapSizeEnv -> Option(s"-Xmx${marathonAppHeapSize}m"),
          GenericWorkflowIdentity -> Properties.envOrNone(GenericWorkflowIdentity),
          UserNameEnv -> workflowExecution.genericDataExecution.userId,
          MarathonAppConstraints -> submitExecution.sparkConfigurations.get(SubmitMesosConstraintConf),
          SpartaPluginInstance -> Option(Properties.envOrElse(SpartaWorkflowsPluginInstance, Properties.envOrElse(SpartaPluginInstance, AppConstant.spartaServerMarathonAppId))),
          SpartaDyplonPluginInstance -> Option(Properties.envOrElse(SpartaWorkflowsDyplonPluginInstance, Properties.envOrElse(SpartaDyplonPluginInstance, AppConstant.spartaServerMarathonAppId)))
        ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> value)) }.flatten.toMap
      )
    }

    def addWorkflowExecutionEnv(implicit workflowExecution: WorkflowExecution): MarathonApplication = addEnv {
      workflowExecution.sparkSubmitExecution.get.sparkConfigurations.flatMap { case (key, value) =>
        if (key.startsWith("spark.mesos.driverEnv.")) {
          Option((key.split("spark.mesos.driverEnv.").tail.head, value))
        } else None
      }
    }

    def addVaultEnv: MarathonApplication = addEnv {
      sys.env.filterKeys(key => key.startsWith("VAULT") && key != VaultTokenEnv)
    }

    def addGosecEnv: MarathonApplication = addEnv {
      val invalid = Seq(
        "SPARTA_PLUGIN_LOCAL_HOSTNAME",
        "SPARTA_PLUGIN_LDAP_CREDENTIALS",
        "SPARTA_PLUGIN_LDAP_PRINCIPAL",
        SpartaPluginInstance,
        SpartaDyplonPluginInstance
      )
      sys.env.filterKeys(key =>
        (
          key == GosecAuthEnableEnv ||
            key == "DYPLON_SYSTEM_TENANT" ||
            key == SpartaDyplonTenantName ||
            key == "SPARTA_TENANT_NAME" ||
            key == "CROSSDATA_SECURITY_MANAGER_ENABLED" ||
            key == "SPARTA_SECURITY_MANAGER_HTTP_ENABLED" ||
            key.startsWith("SPARTA_PLUGIN") ||
            key.startsWith("GOSEC_CROSSDATA") ||
            key.startsWith("CROSSDATA_DYPLON") ||
            key.startsWith("CROSSDATA_PLUGIN") ||
            key == "EOS_TENANT"
          ) &&
          !invalid.contains(key)
      )
    }

    def addXDEnv: MarathonApplication = addEnv {
      sys.env.filterKeys { key =>
        key.startsWith("CROSSDATA_CORE_CATALOG") ||
          key.startsWith("CROSSDATA_STORAGE") ||
          key.startsWith("CROSSDATA_GOVERNANCE") ||
          key.startsWith("CROSSDATA_SQL") ||
          key.startsWith("CROSSDATA_SECURITY")
      }
    }

    def addGosecInstaceAsServerInstanceEnv: MarathonApplication = addEnv {
      Map(
        SpartaPluginInstance -> Properties.envOrElse(SpartaPluginInstance, AppConstant.spartaServerMarathonAppId),
        SpartaDyplonPluginInstance -> Properties.envOrElse(SpartaDyplonPluginInstance, AppConstant.spartaServerMarathonAppId)
      )
    }

    def addHadoopEnv: MarathonApplication = addEnv {
      sys.env.filterKeys { key =>
        key.startsWith("HADOOP") || key.startsWith("CORE_SITE") || key.startsWith("HDFS_SITE") ||
          key.startsWith("HDFS")
      }
    }

    def addLoglevelEnv: MarathonApplication = addEnv {
      sys.env.filterKeys { key =>
        key.contains("LOG_LEVEL") || key.startsWith("LOG")
      }
    }

    def addSecurityEnv: MarathonApplication = addEnv {
      sys.env.filterKeys { key =>
        (key.startsWith("SECURITY") && key.contains("ENABLE")) || key.contains("SPARTA_SECURITY_VAULT")
      }
    }

    def addCalicoEnv: MarathonApplication = addEnv {
      sys.env.filterKeys(key => key.startsWith("CALICO"))
    }

    def addExtraEnv: MarathonApplication = addEnv {
      sys.env.filterKeys(key => key.contains("EXTRA_PROPERTIES"))
    }

    def addSpartaExtraEnv: MarathonApplication = addEnv {
      sys.env.filterKeys(key => key.startsWith("SPARTA_EXTRA")).map { case (key, value) =>
        key.replaceAll("SPARTA_EXTRA_", "") -> value
      }
    }

    def addPostgresEnv: MarathonApplication = addEnv {
      sys.env.filterKeys { key =>
        key.startsWith("SPARTA_POSTGRES")
      }
    }

    def addZookeeperEnv: MarathonApplication = addEnv {
      sys.env.filterKeys { key =>
        key.startsWith("SPARTA_ZOOKEEPER")
      }
    }

    def addIntelligenceEnv: MarathonApplication = addEnv {
      sys.env.filterKeys { key =>
        key.contains("INTELLIGENCE")
      }
    }

    //TODO can remove?
    def addMarathonAppEnv: MarathonApplication = addEnv {
      sys.env.filterKeys(key => key.startsWith("MARATHON_APP"))
    }

    def addSpartaConfigEnv: MarathonApplication = addEnv {
      sys.env.filterKeys { key =>
        key.startsWith("SPARTA_CONFIG") || key.startsWith("SPARTA_TIMEOUT_API_CALLS")
      }
    }

    def addActorEnv: MarathonApplication = addEnv {
      sys.env.filterKeys { key =>
        key.startsWith("SPARTA_ACTORS")
      }
    }

    def addSpartaMarathonEnv: MarathonApplication = addEnv {
      sys.env.filterKeys { key =>
        key.startsWith("SPARTA_MARATHON")
      }
    }

    def addMarathonEnv: MarathonApplication = addEnv {
      sys.env.filterKeys { key =>
        key.startsWith("MARATHON")
      }
    }

    def addMesosRoleExecutionEnv(implicit workflowExecution: WorkflowExecution): MarathonApplication =
      addEnv {
        workflowExecution.sparkSubmitExecution.get.sparkConfigurations.flatMap { case (key, value) =>
          if (key == SparkConstant.SubmitMesosRoleConf) {
            Option(MesosRoleEnv, value)
          } else None
        }
      }

    def addUserDefinedEnv(implicit workflowExecution: WorkflowExecution): MarathonApplication = {
      addEnv {
        import com.stratio.sparta.core.properties.ValidatingPropertyMap._
        workflowExecution.getWorkflowToExecute.settings.global.
          marathonDeploymentSettings.fold(Map.empty[String, String]) { marathonSettings =>
          marathonSettings.logLevel.notBlank.fold(Map.empty[String, String]) {
            logLevel =>
              val level = logLevel.toString.toUpperCase
              Seq(sparkLogLevel -> level, spartaLogLevel -> level, spartaRedirectorLogLevel -> level).toMap
          } ++
            marathonSettings.userEnvVariables.flatMap { kvPair => Option(kvPair.key.toString -> kvPair.value.toString) }.toMap
        }
      }
    }

    def addAppIdentityEnv(identity: String): MarathonApplication = addEnv {
      val spartaServerServiceIdWithPath = Properties.envOrElse(SpartaServerServiceIdWithPath, spartaTenant)
      Map(
        ServerTenantName -> spartaTenant,
        AppConstant.SpartaServiceName -> AppConstant.instanceNameWithDefault,
        SpartaServerServiceIdWithPath -> spartaServerServiceIdWithPath,
        TenantIdentity -> identity,
        DcosServiceName -> identity,
        SpartaPostgresUser -> identity,
        SystemHadoopUserName -> identity
      )
    }

    def addMesosNativeJavaLibraryEnv: MarathonApplication = {
      val mesosNativeJavaLibraryMaybe = Properties.envOrNone(MesosNativeJavaLibraryEnv).orElse(Option(HostMesosNativeLib))
      mesosNativeJavaLibraryMaybe.foldLeft(marathonApplication)((ma, mesosNativeJavaLibrary) => {
        ma.copy(
          env = Option(marathonApplication.env.getOrElse(Map.empty[String, String]) ++ Map(MesosNativeJavaLibraryEnv -> mesosNativeJavaLibrary))
        )
      })
    }

    def addLdLibraryEnv: MarathonApplication = {
      val ldNativeLibraryMaybe = Properties.envOrNone(MesosNativeJavaLibraryEnv) match {
        case Some(_) => None
        case None => Option(HostMesosLib)
      }
      ldNativeLibraryMaybe.foldLeft(marathonApplication)((ma, ldNativeLibrary) => {
        ma.copy(
          env = Option(marathonApplication.env.getOrElse(Map.empty[String, String]) ++ Map(LdLibraryEnv -> ldNativeLibrary))
        )
      })
    }

    def addMinimunPostgresResources(): MarathonApplication = addEnv {
      Map(
        "SPARTA_POSTGRES_POOL_MAX_CONNECTIONS" -> "2",
        "SPARTA_POSTGRES_POOL_MIN_CONNECTIONS" -> "2",
        "SPARTA_POSTGRES_POOL_THREADS" -> "2",
        "SPARTA_POSTGRES_EXECUTION_CONTEXT_PARALLELISM" -> "4",
        "SPARTA_POSTGRES_KEEP_ALIVE" -> "false"
      )
    }

    def addSpartaWorkerMode(bootstrapMode: RocketMode): MarathonApplication = addEnv {
      Map(
        "SPARTA_BOOTSTRAP_MODE" -> bootstrapMode.toString,
        "SPARTA_AKKA_CLUSTER_ROLES" -> bootstrapMode.toString
      )
    }

    protected[builder] def addEnv(newEnvs: Map[String, String]): MarathonApplication =
      marathonApplication.copy(
        env = Option(marathonApplication.env.getOrElse(Map.empty[String, String]) ++ newEnvs)
      )
  }

  protected[builder] def transformMemoryToInt(memory: String): Int = Try(memory match {
    case mem if mem.contains("G") => mem.replace("G", "").toInt * 1024
    case mem if mem.contains("g") => mem.replace("g", "").toInt * 1024
    case mem if mem.contains("m") => mem.replace("m", "").toInt
    case mem if mem.contains("M") => mem.replace("M", "").toInt
    case _ => memory.toInt
  }).getOrElse(DefaultMemory)


  protected[builder] def getSOMemory: Int =
    Properties.envOrNone(SpartaOSMemoryEnv) match {
      case Some(x) =>
        Try(x.toInt).filter(y => y >= MinSOMemSize).getOrElse(DefaultSOMemSize)
      case None => DefaultSOMemSize
    }


  protected[builder] def getConstraint(workflowModel: Workflow): Option[Seq[Seq[String]]] = {
    import com.stratio.sparta.core.properties.ValidatingPropertyMap._
    val envConstraints = Seq(
      Properties.envOrNone(HostnameConstraint).notBlank,
      Properties.envOrNone(OperatorConstraint).notBlank,
      Properties.envOrNone(AttributeConstraint).notBlank
    ).flatten

    (workflowModel.settings.global.mesosConstraint.notBlank, envConstraints) match {
      case (Some(workflowConstraint), _) =>
        val constraints = workflowConstraint.split(":")
        Option(
          Seq(
            Seq(
              Option(constraints.head),
              Option(workflowModel.settings.global.mesosConstraintOperator.notBlankWithDefault("CLUSTER")), {
                if (constraints.size == 2 || constraints.size == 3) Option(constraints.last) else None
              }
            ).flatten
          ))
      case (None, constraints) if constraints.size == 3 =>
        Option(Seq(constraints))
      case _ =>
        None
    }
  }

  protected[builder] def getWorkflowInheritedVariables: Map[String, String] = {
    import com.stratio.sparta.core.properties.ValidatingPropertyMap._
    val prefix = Properties.envOrElse(workflowLabelsPrefix, "INHERITED_TO_WORKFLOW")
    val labelFromPrefix = sys.env.filterKeys { key =>
      key.startsWith(prefix)
    }
    val fixedLabels = Properties.envOrNone(fixedWorkflowLabels).notBlank match {
      case Some(labels) =>
        labels.split(",").flatMap { label =>
          val splittedLabel = label.split("=")

          splittedLabel.headOption.map(key => key -> splittedLabel.lastOption.getOrElse(""))
        }.toMap
      case None => Map.empty[String, String]
    }

    fixedLabels ++ labelFromPrefix
  }
}