/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor


import akka.actor.{Props, _}
import com.stratio.sparta.sdk.enumerators.PhaseEnum
import com.stratio.sparta.sdk.models.WorkflowError
import com.stratio.sparta.security.{Edit, Status, SpartaSecurityManager}
import com.stratio.sparta.serving.core.actor.ClusterLauncherActor
import com.stratio.sparta.serving.core.actor.LauncherActor.{Debug, Launch, Start}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.Failed
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import com.stratio.sparta.serving.core.services.{DebugWorkflowService, ExecutionService, WorkflowService, WorkflowStatusService}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

class LauncherActor(curatorFramework: CuratorFramework,
                    statusListenerActor: ActorRef,
                    envStateActor: ActorRef
                   )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  private val ResourceStatus = "Workflow Detail"
  private val ResourceWorkflow = "Workflows"
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val executionService = new ExecutionService(curatorFramework)
  private val workflowService = new WorkflowService(curatorFramework, Option(context.system), Option(envStateActor))
  private val debugWorkflowService =
    new DebugWorkflowService(curatorFramework, Option(context.system), Option(envStateActor))
  private val marathonLauncherActor = context.actorOf(Props(
    new MarathonLauncherActor(curatorFramework, statusListenerActor)), MarathonLauncherActorName)
  private val clusterLauncherActor = context.actorOf(Props(
    new ClusterLauncherActor(curatorFramework, statusListenerActor)), ClusterLauncherActorName)

  override def receive: Receive = {
    case Launch(id, user) => launch(id, user)
    case Debug(id, user) => debug(id, user)
    case _ => log.info("Unrecognized message in Launcher Actor")
  }

  def launch(id: String, user: Option[LoggedUser]): Unit = {
    securityActionAuthorizer(user, Map(ResourceStatus -> Edit)) {
      Try {
        val workflow = workflowService.findById(id)

        workflowService.validateWorkflow(workflow)

        val workflowLauncherActor = workflow.settings.global.executionMode match {
          case WorkflowExecutionMode.marathon =>
            log.info(s"Launching workflow: ${workflow.name} in marathon mode")
            marathonLauncherActor
          case WorkflowExecutionMode.dispatcher =>
            log.info(s"Launching workflow: ${workflow.name} in cluster mode")
            clusterLauncherActor
          case WorkflowExecutionMode.local if !workflowService .anyLocalWorkflowRunning =>
            val actorName = AkkaConstant.cleanActorName(s"LauncherActor-${workflow.name}")
            val childLauncherActor = context.children.find(children => children.path.name == actorName)
            log.info(s"Launching workflow: ${workflow.name} in local mode")
            childLauncherActor.getOrElse(
              context.actorOf(Props(new LocalLauncherActor(curatorFramework)), actorName))
          case _ =>
            throw new Exception(
              s"Invalid execution mode in workflow ${workflow.name}: ${workflow.settings.global.executionMode}")
        }

        workflowLauncherActor ! Start(workflow, user.map(_.id))
        (workflow, workflowLauncherActor)
      } match {
        case Success((workflow, launcherActor)) =>
          log.debug(s"Workflow ${workflow.name} launched to: ${launcherActor.toString()}")
        case Failure(exception) =>
          val information = s"Error launching workflow with the selected execution mode"
          log.error(information)
          val error = WorkflowError(information, PhaseEnum.Launch, exception.toString, exception.getCause.getMessage)
          statusService.update(WorkflowStatus(
            id = id,
            status = Failed,
            statusInfo = Option(information)
          ))
          executionService.setLastError(id, error)
      }
    }
  }

  def debug(id: String, user: Option[LoggedUser]): Unit = {
    securityActionAuthorizer(user, Map(ResourceWorkflow -> Status)) {
      Try {
        val debugExecution = debugWorkflowService.findByID(id)
          .getOrElse(throw new ServerException(s"No workflow debug execution with id $id"))
        val workflow = debugExecution.workflowDebug
          .getOrElse(throw new ServerException(s"The workflow debug is not created yet"))

        workflowService.validateDebugWorkflow(workflow)

        val actorName = AkkaConstant.cleanActorName(s"DebugActor-${workflow.name}")
        val childLauncherActor = context.children.find(children => children.path.name == actorName)
        log.info(s"Debugging workflow: ${workflow.name}")
        val workflowLauncherActor = childLauncherActor.getOrElse(
          context.actorOf(Props(new DebugLauncherActor(curatorFramework)), actorName))

        debugWorkflowService.removeDebugStepData(id)
        workflowLauncherActor ! Start(workflow, user.map(_.id))
        (workflow, workflowLauncherActor)
      } match {
        case Success((workflow, launcherActor)) =>
          val startDate = new DateTime()
          log.debug(s"Workflow ${workflow.name} debugged into: ${launcherActor.toString()} at time: $startDate")
          Try(startDate)
        case Failure(exception) =>
          val information = s"Error debugging workflow with the selected execution mode"
          log.error(information)
          debugWorkflowService.setSuccessful(id, state = false)
          debugWorkflowService.setError(
            id,
            Option(WorkflowError(
              information,
              PhaseEnum.Launch,
              exception.toString,
              Try(exception.getCause.getMessage).toOption.getOrElse(exception.getMessage)
            )))
          throw exception
      }
    }
  }

}