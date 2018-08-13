/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor


import akka.actor.{Props, _}
import akka.pattern.ask
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.core.actor.ClusterLauncherActor
import com.stratio.sparta.serving.core.actor.EnvironmentListenerActor.{ApplyExecutionContextToWorkflow, ApplyExecutionContextToWorkflowId}
import com.stratio.sparta.serving.core.actor.LauncherActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.Failed
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.{DebugWorkflowService, ExecutionService, WorkflowService, WorkflowStatusService}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class LauncherActor(curatorFramework: CuratorFramework,
                    statusListenerActor: ActorRef,
                    envStateActor: ActorRef
                   )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val ResourceWorkflow = "Workflows"
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val executionService = new ExecutionService(curatorFramework)
  private val workflowService = new WorkflowService(curatorFramework)
  private val debugService = new DebugWorkflowService(curatorFramework)
  private val marathonLauncherActor = context.actorOf(Props(
    new MarathonLauncherActor(curatorFramework, statusListenerActor)), MarathonLauncherActorName)
  private val clusterLauncherActor = context.actorOf(Props(
    new ClusterLauncherActor(curatorFramework, statusListenerActor)), ClusterLauncherActorName)

  def receiveApiActions(action : Any): Unit = action match {
    case Launch(workflowIdExecutionContext, user) => launch(workflowIdExecutionContext, user)
    case Debug(debugWorkflow, user) => debug(debugWorkflow, user)
    case _ => log.info("Unrecognized message in Launcher Actor")
  }

  def launch(workflowIdExecutionContext: WorkflowIdExecutionContext, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceWorkflow -> com.stratio.sparta.security.Status)
    val workflowRaw = workflowService.findById(workflowIdExecutionContext.workflowId)
    val authorizationId = workflowRaw.authorizationId

    authorizeActionsByResourceId(user, actions, authorizationId) {
      for {
        response <- (envStateActor ? ApplyExecutionContextToWorkflowId(workflowIdExecutionContext))
          .mapTo[Try[Workflow]]
      } yield {
        Try {
          response match {
            case Success(workflowWithContext) =>
              launchWorkflow(workflowWithContext, workflowRaw, workflowIdExecutionContext.executionContext, user)
            case Failure(e) =>
              log.warn(s"Error applying execution context, executing without it. ${e.getLocalizedMessage}")
              launchWorkflow(workflowRaw, workflowRaw, workflowIdExecutionContext.executionContext, user)
          }
        }
      }
    }
  }

  def launchWorkflow(
                      workflowWithContext: Workflow,
                      workflowRaw: Workflow,
                      executionContext: ExecutionContext,
                      user: Option[LoggedUser]
                    ): Boolean = {
    Try {
      workflowService.validateWorkflow(workflowWithContext)

      val workflowLauncherActor = workflowWithContext.settings.global.executionMode match {
        case WorkflowExecutionMode.marathon =>
          log.info(s"Launching workflow: ${workflowWithContext.name} in marathon mode")
          marathonLauncherActor
        case WorkflowExecutionMode.dispatcher =>
          log.info(s"Launching workflow: ${workflowWithContext.name} in cluster mode")
          clusterLauncherActor
        case WorkflowExecutionMode.local if !workflowService.anyLocalWorkflowRunning =>
          val actorName = AkkaConstant.cleanActorName(s"LauncherActor-${workflowWithContext.name}")
          val childLauncherActor = context.children.find(children => children.path.name == actorName)
          log.info(s"Launching workflow: ${workflowWithContext.name} in local mode")
          childLauncherActor.getOrElse(
            context.actorOf(Props(new LocalLauncherActor(curatorFramework)), actorName))
        case _ =>
          throw new Exception(
            s"Invalid execution mode in workflow ${workflowWithContext.name}: " +
              s"${workflowWithContext.settings.global.executionMode}")
      }

      workflowLauncherActor ! Start(workflowWithContext, workflowRaw, executionContext, user.map(_.id))
      workflowLauncherActor
    } match {
      case Success(launcherActor) =>
        log.debug(s"Workflow ${workflowWithContext.name} launched to: ${launcherActor.toString()}")
        true
      case Failure(exception) =>
        val information = s"Error launching workflow with the selected execution mode"
        log.error(information)
        val error = WorkflowError(
          information,
          PhaseEnum.Launch,
          exception.toString,
          ExceptionHelper.toPrintableException(exception)
        )
        executionService.setLastError(workflowWithContext.id.get, error)
        statusService.update(WorkflowStatus(
          id = workflowWithContext.id.get,
          status = Failed,
          statusInfo = Option(information)
        ))
        throw exception
    }
  }

  def debug(workflowIdExecutionContext: WorkflowIdExecutionContext, user: Option[LoggedUser]): Unit = {

    import workflowIdExecutionContext._

    val actions = Map(ResourceWorkflow -> com.stratio.sparta.security.Status)
    val debug = debugService.findByID(workflowId)
      .getOrElse(throw new ServerException(s"No workflow debug execution with id $workflowId"))
    val authorizationId = debug.authorizationId
    val workflowToDebug = debug.workflowDebug.getOrElse(debug.workflowOriginal)

    authorizeActionsByResourceId(user, actions, authorizationId) {
      for {
        response <- (envStateActor ? ApplyExecutionContextToWorkflow(
          WorkflowExecutionContext(workflowToDebug, executionContext))).mapTo[Try[Workflow]]
      } yield {
        Try {
          response match {
            case Success(workflowWithContext) =>
              debugWorkflow(workflowWithContext, debug.workflowOriginal, workflowId, executionContext, user)
            case Failure(e) =>
              log.warn(s"Error applying execution context, executing without it. ${e.getLocalizedMessage}")
              debugWorkflow(workflowToDebug, debug.workflowOriginal, workflowId, executionContext,user)
          }
        }
      }
    }
  }

  def debugWorkflow(
                     workflowWithContext: Workflow,
                     workflowOriginal: Workflow,
                     debugId: String,
                     executionContext: ExecutionContext,
                     user: Option[LoggedUser]
                   ): DateTime = {
    Try {
      val workflow = workflowWithContext.copy(id = workflowWithContext.id.orElse(workflowOriginal.id))

      workflowService.validateDebugWorkflow(workflow)

      val actorName = AkkaConstant.cleanActorName(s"DebugActor-${workflow.name}")
      val childLauncherActor = context.children.find(children => children.path.name == actorName)
      log.info(s"Debugging workflow: ${workflow.name}")
      val workflowLauncherActor = childLauncherActor.getOrElse(
        context.actorOf(Props(new DebugLauncherActor(curatorFramework)), actorName))

      debugService.removeDebugStepData(debugId)
      workflowLauncherActor ! StartDebug(workflow)
      (workflow, workflowLauncherActor)
    } match {
      case Success((workflow, launcherActor)) =>
        val startDate = new DateTime()
        log.debug(s"Workflow ${workflow.name} debugged into: ${launcherActor.toString()} at time: $startDate")
        startDate
      case Failure(exception) =>
        val information = s"Error debugging workflow with the selected execution mode"
        log.error(information)
        debugService.setSuccessful(debugId, state = false)
        debugService.setError(
          debugId,
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