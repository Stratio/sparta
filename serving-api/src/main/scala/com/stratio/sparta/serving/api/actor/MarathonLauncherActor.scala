/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.util.UUID

import akka.actor.{Actor, ActorRef, PoisonPill}
import com.stratio.sparta.sdk.models.WorkflowError
import com.stratio.sparta.sdk.enumerators.PhaseEnum
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.SparkConstant
import com.stratio.sparta.serving.core.constants.SparkConstant._
import com.stratio.sparta.serving.core.helpers.{JarsHelper, WorkflowHelper}
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services._
import com.stratio.sparta.serving.core.utils._
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

class MarathonLauncherActor(val curatorFramework: CuratorFramework, statusListenerActor: ActorRef) extends Actor
  with SchedulerUtils {

  private val executionService = new ExecutionService(curatorFramework)
  private val statusService = new WorkflowStatusService(curatorFramework)

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow, userId: Option[String]) => initializeSubmitRequest(workflow, userId: Option[String])
    case _ => log.info("Unrecognized message in Marathon Launcher Actor")
  }

  //scalastyle:off
  def initializeSubmitRequest(workflow: Workflow, userId: Option[String]): Unit = {
    Try {
      val sparkSubmitService = new SparkSubmitService(workflow)
      log.info(s"Initializing options to submit the Workflow App associated to workflow: ${workflow.name}")
      val detailConfig = SpartaConfig.getDetailConfig.getOrElse {
        val message = "Impossible to extract detail configuration"
        log.error(message)
        throw new RuntimeException(message)
      }
      val zookeeperConfig = SpartaConfig.getZookeeperConfig.getOrElse {
        val message = "Impossible to extract Zookeeper Configuration"
        log.error(message)
        throw new RuntimeException(message)
      }
      val driverFile = sparkSubmitService.extractDriverSubmit(detailConfig)
      val pluginJars = JarsHelper.clusterUserPluginJars(workflow)
      val localPluginJars = JarsHelper.getLocalPathFromJars(pluginJars)
      val sparkHome = sparkSubmitService.validateSparkHome
      val driverArgs = sparkSubmitService.extractDriverArgs(zookeeperConfig, pluginJars, detailConfig)
      val (sparkSubmitArgs, sparkConfs) = sparkSubmitService.extractSubmitArgsAndSparkConf(localPluginJars)
      val executionSubmit = WorkflowExecution(
        id = workflow.id.get,
        sparkSubmitExecution = Option(SparkSubmitExecution(
          driverClass = SpartaDriverClass,
          driverFile = driverFile,
          pluginFiles = pluginJars,
          master = SparkConstant.SparkMesosMaster,
          submitArguments = sparkSubmitArgs,
          sparkConfigurations = sparkConfs,
          driverArguments = driverArgs,
          sparkHome = sparkHome
        )),
        sparkDispatcherExecution = None,
        marathonExecution = Option(MarathonExecution(marathonId = WorkflowHelper.getMarathonId(workflow))),
        genericDataExecution = Option(GenericDataExecution(
          workflow = workflow,
          executionMode = marathon,
          executionId = UUID.randomUUID.toString,
          userId = userId
        ))
      )

      executionService.create(executionSubmit).getOrElse(
        throw new Exception("Unable to create an execution submit in Zookeeper"))
      new MarathonService(context, workflow, executionSubmit)
    } match {
      case Failure(exception) =>
        val information = s"Error initializing Workflow App"
        log.error(information, exception)
        val error = WorkflowError(
          information,
          PhaseEnum.Launch,
          exception.toString,
          Try(exception.getCause.getMessage).toOption.getOrElse(exception.getMessage)
        )
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Failed,
          statusInfo = Option(information)
        ))
        executionService.setLastError(workflow.id.get, error)
        self ! PoisonPill
      case Success(marathonApp) =>
        val information = "Workflow App configuration initialized successfully"
        log.info(information)
        statusService.update(WorkflowStatus(
          workflow.id.get,
          status = NotStarted))
        Try(marathonApp.launch()) match {
          case Success(_) =>
            statusService.update(WorkflowStatus(
              id = workflow.id.get,
              status = Uploaded,
              statusInfo = Option(information),
              lastUpdateDateWorkflow = workflow.lastUpdateDate
            ))
            val sparkUri = NginxUtils.buildSparkUI(
              s"${workflow.group.name}/${workflow.name}/${workflow.name}-v${workflow.version}")
            executionService.setLaunchDate(workflow.id.get, new DateTime())
            executionService.setSparkUri(workflow.id.get, sparkUri)
          case Failure(exception) =>
            val information = s"An error was encountered while launching the Workflow App in the Marathon API"
            val error = WorkflowError(
              information,
              PhaseEnum.Launch,
              exception.toString,
              Try(exception.getCause.getMessage).toOption.getOrElse(exception.getMessage)
            )
            statusService.update(WorkflowStatus(
              id = workflow.id.get,
              status = Failed,
              statusInfo = Option(information)
            ))
            executionService.setLastError(workflow.id.get, error)
        }
    }
  }
}