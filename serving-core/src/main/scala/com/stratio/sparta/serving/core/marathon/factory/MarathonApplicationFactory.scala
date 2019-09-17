/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.marathon.factory

import java.util.UUID

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant.spartaTenant
import com.stratio.sparta.serving.core.constants.MarathonConstant.GenericWorkflowIdentity
import com.stratio.sparta.serving.core.marathon._
import com.stratio.sparta.serving.core.marathon.builder.{DefaultBuilderImplicits, StratioBuilderImplicits, WorkflowBuilderImplicits}
import com.stratio.sparta.serving.core.models.RocketModes.RocketMode
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution

import scala.util.Properties

object MarathonApplicationFactory {

  val DefaultCpus = 1
  val DefaultMem = 1024
  val DefaultInstances = 1
  val DefaultImage = "qa.stratio.com/stratio/ubuntu-base:16.04"
  val DefaultNetwork = "HOST"

  val AkkaClusterPort = 10000
  val AkkaClusterServicePort = 0

  val SparkUIPort = 4041
  val SparkUIServicePort = 0
  val SparkUIHealthCheckPortIndex = 4
  val SparkUIHealthCheckGracePeriodSeconds = 300
  val SparkUIHealthCheckIntervalSeconds = 180
  val SparkUIHealthCheckTimeoutSeconds = 30
  val SparkUIHealthCheckMaxConsecutiveFailures = 3

  def createDefaultApplication(id: String = UUID.randomUUID().toString,
                               dockerImage: String = DefaultImage,
                               cpus: Int = DefaultCpus,
                               mem: Int = DefaultMem,
                               instances: Option[Int] = Option(DefaultInstances),
                               network: String = "HOST"): MarathonApplication = {
    import DefaultBuilderImplicits._
    newMarathonApplication()
      .withId(id)
      .withCpus(cpus)
      .withMem(mem)
      .withInstances(instances)
      .withContainer(MarathonContainer(docker = Docker(
        image = dockerImage,
        network = network
      )))
  }

  def createWorkerApplication(
                               workerBootstrapMode: RocketMode,
                               id: String = UUID.randomUUID().toString,
                               dockerImage: String = DefaultImage,
                               cpus: Int = DefaultCpus,
                               mem: Int = DefaultMem,
                               instances: Option[Int] = Option(DefaultInstances)): MarathonApplication = {

    import WorkflowBuilderImplicits._

    val workerMarathonApplication: MarathonApplication = createStratioApplication(id, dockerImage, cpus, mem, instances)
      .withSpartaDockerImage
      .addVaultEnv
      .addGosecEnv
      .addGosecInstaceAsServerInstanceEnv
      .addXDEnv
      .addHadoopEnv
      .addLoglevelEnv
      .addSecurityEnv
      .addCalicoEnv
      .addExtraEnv
      .addSpartaExtraEnv
      .addPostgresEnv
      .addZookeeperEnv
      .addSpartaConfigEnv
      .addIntelligenceEnv
      .addActorEnv
      .addAppIdentityEnv(AppConstant.spartaTenant)
      .addMinimunPostgresResources()
      .addSpartaWorkerMode(workerBootstrapMode)

    val akkaSeedDockerPortMapping = DockerPortMapping(
      hostPort = AkkaClusterPort,
      containerPort = AkkaClusterPort,
      servicePort = Option(AkkaClusterServicePort),
      protocol = "tcp",
      name = Option("akkaseed")
    )

    val sparkUISeedDockerPortMapping = DockerPortMapping(
      hostPort = SparkUIPort,
      containerPort = SparkUIPort,
      servicePort = Option(SparkUIServicePort),
      protocol = "tcp",
      name = Option("sparkui")
    )

    val marathonHealthCheck = Option(Seq(MarathonHealthCheck(
      protocol = "HTTP",
      portIndex = Option(SparkUIHealthCheckPortIndex),
      gracePeriodSeconds = SparkUIHealthCheckGracePeriodSeconds,
      intervalSeconds = SparkUIHealthCheckIntervalSeconds,
      timeoutSeconds = SparkUIHealthCheckTimeoutSeconds,
      maxConsecutiveFailures = SparkUIHealthCheckMaxConsecutiveFailures,
      ignoreHttp1xx = Option(false)
    )))

    workerMarathonApplication.copy(
      container = workerMarathonApplication.container.copy(
        docker = workerMarathonApplication.container.docker.copy(
          portMappings = Option(
            workerMarathonApplication.container.docker.portMappings.getOrElse(
              Seq.empty[DockerPortMapping]) ++ Seq(akkaSeedDockerPortMapping, sparkUISeedDockerPortMapping)
          ),
          forcePullImage = Option(true)
        )
      ),
      healthChecks = marathonHealthCheck
    )
  }

  def createStratioApplication(id: String = UUID.randomUUID().toString,
                               dockerImage: String = DefaultImage,
                               cpus: Int = DefaultCpus,
                               mem: Int = DefaultMem,
                               instances: Option[Int] = Option(DefaultInstances)): MarathonApplication = {
    import StratioBuilderImplicits._
    createDefaultApplication(id, dockerImage, cpus, mem, instances)
      .withStratioDefaultLabels
      .withAdminRouterLabels
      .withStratioVolumes
      .withIpAddress
      .withDynamicAuthenticationEnv
      .withCompanyBillingLabels
      .withPortDefinitions
      .withPortMappings
      .withNetworkType
      .addMarathonJarEnv
      .addVaultTokenEnv
      .addDynamicAuthEnv
      .addSpartaFileEncodingEnv
      .addSparkHomeEnv
      .addDatastoreCaNameEnv
      .addSpartaSecretFolderEnv
      .addSpartaZookeeperPathEnv
  }

  def createWorkflowApplication(implicit workflowExecution: WorkflowExecution): MarathonApplication = {
    import WorkflowBuilderImplicits._
    createStratioApplication()
      .withId
      .withSpartaDockerImage
      .withCpusFromWorkflowExecution
      .withMemFromWorkflowExecution
      .withHealthChecksFromWorkflow
      .withConstraints
      .withInheritedVariablesLabels
      .withForcePullImage
      .withPrivileged
      .addWorkflowPropertiesEnv
      .addWorkflowExecutionEnv
      .addVaultEnv
      .addGosecEnv
      .addXDEnv
      .addHadoopEnv
      .addLoglevelEnv
      .addSecurityEnv
      .addCalicoEnv
      .addExtraEnv
      .addSpartaExtraEnv
      .addPostgresEnv
      .addZookeeperEnv
      .addSpartaConfigEnv
      .addIntelligenceEnv
      .addMarathonAppEnv
      .addMesosRoleExecutionEnv
      .addActorEnv
      .addSpartaMarathonEnv
      .addMesosNativeJavaLibraryEnv
      .addLdLibraryEnv
      .addUserDefinedEnv
      .addAppIdentityEnv(Properties.envOrElse(GenericWorkflowIdentity, spartaTenant))
  }
}