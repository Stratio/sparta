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
import com.stratio.sparta.serving.core.actor.LauncherActor._
import com.stratio.sparta.serving.core.actor.ParametersListenerActor.{ApplyExecutionContextToWorkflow, ApplyExecutionContextToWorkflowId}
import com.stratio.sparta.serving.core.actor.ParametersListenerActor.{ValidateExecutionContextToWorkflow, ValidateExecutionContextToWorkflowId}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant.SpartaDriverClass
import com.stratio.sparta.serving.core.constants.{AkkaConstant, SparkConstant}
import com.stratio.sparta.serving.core.helpers.{JarsHelper, LinkHelper}
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services._
import com.stratio.sparta.serving.core.services.dao.{DebugWorkflowPostgresDao, WorkflowExecutionPostgresDao, WorkflowPostgresDao}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class LauncherActor(
                    statusListenerActor: ActorRef,
                    parametersStateActor: ActorRef
                   )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val ResourceWorkflow = "Workflows"
  private val executionService = new WorkflowExecutionPostgresDao
  private val workflowService = new WorkflowPostgresDao
  private val debugPgService = new DebugWorkflowPostgresDao()

  private val marathonLauncherActor = context.actorOf(Props(new MarathonLauncherActor), MarathonLauncherActorName)
  private val clusterLauncherActor = context.actorOf(Props(
    new ClusterLauncherActor(statusListenerActor)), ClusterLauncherActorName)

  def receiveApiActions(action: Any): Any = action match {
    case Launch(workflowIdExecutionContext, user) => launch(workflowIdExecutionContext, user)
    case Debug(debugWorkflow, user) => debug(debugWorkflow, user)
    case _ => log.info("Unrecognized message in Launcher Actor")
  }

  def launch(workflowIdExecutionContext: WorkflowIdExecutionContext, user: Option[LoggedUser]): Future[Any] = {
    val sendResponseTo = sender
    for {
      workflowRaw <- workflowService.findWorkflowById(workflowIdExecutionContext.workflowId)
      response <- (parametersStateActor ? ValidateExecutionContextToWorkflowId(
        workflowIdExecutionContext = workflowIdExecutionContext,
        ignoreCustomParams = false
      )).mapTo[Try[ValidationContextResult]]
    } yield {
      val actions = Map(ResourceWorkflow -> com.stratio.sparta.security.Status)
      authorizeActionsByResourceId(user, actions, workflowRaw.authorizationId, Option(sendResponseTo)) {
        response match {
          case Success(validationContextResult) =>
            if (validationContextResult.workflowValidation.valid) {
            for {
              newExecution <- createExecution(
                validationContextResult.workflow,
                workflowRaw,
                workflowIdExecutionContext.executionContext,
                workflowIdExecutionContext.executionSettings,
                user
              )
            } yield launchExecution(newExecution)
            } else {
              val information = s"The workflow ${validationContextResult.workflow.name} is invalid" +
                s" with the execution context. ${validationContextResult.workflowValidation.messages.mkString(",")}"
              log.error(information)
              throw new Exception(information)
            }
          case Failure(e) =>
            log.error(s"Error applying execution context to workflow id ${workflowIdExecutionContext.workflowId}", e)
            throw e
        }
      }

    }
  }

  def launchExecution(workflowExecution: WorkflowExecution): String = {
    Try {
      val workflowWithContext = workflowExecution.getWorkflowToExecute
      val workflowLauncherActor = workflowWithContext.settings.global.executionMode match {
        case WorkflowExecutionMode.marathon =>
          log.info(s"Launching workflow: ${workflowWithContext.name} in marathon mode")
          marathonLauncherActor
        case WorkflowExecutionMode.dispatcher =>
          log.info(s"Launching workflow: ${workflowWithContext.name} in cluster mode")
          clusterLauncherActor
        case WorkflowExecutionMode.local =>
          val actorName = AkkaConstant.cleanActorName(s"LauncherActor-${workflowWithContext.name}")
          val childLauncherActor = context.children.find(children => children.path.name == actorName)
          log.info(s"Launching workflow: ${workflowWithContext.name} in local mode")
          childLauncherActor.getOrElse(
            context.actorOf(Props(new LocalLauncherActor()), actorName))
        case _ =>
          throw new Exception(
            s"Invalid execution mode in workflow ${workflowWithContext.name}: " +
              s"${workflowWithContext.settings.global.executionMode}")
      }

      workflowLauncherActor ! Start(workflowExecution)
      workflowExecution.getExecutionId
    } match {
      case Success(executionId) =>
        log.debug(s"Workflow execution $executionId created and launched")
        executionId
      case Failure(exception) =>
        val information = s"Error launching workflow with the selected execution mode"
        log.error(information)
        val error = WorkflowError(
          information,
          PhaseEnum.Launch,
          exception.toString,
          ExceptionHelper.toPrintableException(exception)
        )
        val executionId = workflowExecution.getExecutionId
        for{
          _ <- executionService.setLastError(executionId, error)
          _ <- executionService.updateStatus(ExecutionStatusUpdate(
            executionId,
            ExecutionStatus(state = Failed, statusInfo = Option(information))
          ))
        } yield {
          log.debug(s"Updated correctly the execution status $executionId to $Failed in LaucnherActor")
        }
        throw exception
    }
  }

  def debug(workflowIdExecutionContext: WorkflowIdExecutionContext, user: Option[LoggedUser]): Unit = {

    import workflowIdExecutionContext._

    val actions = Map(ResourceWorkflow -> com.stratio.sparta.security.Status)
    val sendResponseTo = sender

    for {
      debug <- debugPgService.findDebugWorkflowById(workflowId)
      response <- (parametersStateActor ? ValidateExecutionContextToWorkflow(
        workflowExecutionContext = WorkflowExecutionContext(
          debug.workflowDebug.getOrElse(debug.workflowOriginal),
          executionContext
        ),
        ignoreCustomParams = false
      )).mapTo[Try[ValidationContextResult]]
    } yield {
      val authorizationId = debug.authorizationId
      authorizeActionsByResourceId(user, actions, authorizationId, Option(sendResponseTo)) {
        Try {
          response match {
            case Success(validationContextResult) =>
              if (validationContextResult.workflowValidation.valid)
                debugWorkflow(
                  validationContextResult.workflow,
                  debug.workflowOriginal,
                  workflowId,
                  executionContext,
                  user
                )
              else {
                val information = s"The workflow ${validationContextResult.workflow.name} is invalid" +
                  s" with the execution context. ${validationContextResult.workflowValidation.messages.mkString(",")}"
                log.error(information)
                throw new Exception(information)
              }
            case Failure(e) =>
              log.error(s"Error applying execution context in debug", e)
              throw e
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

      val dummyExecution = WorkflowExecution(
        genericDataExecution = GenericDataExecution(workflow, workflow, local, executionContext)
      )
      val actorName = AkkaConstant.cleanActorName(s"DebugActor-${workflow.name}")
      val childLauncherActor = context.children.find(children => children.path.name == actorName)
      log.info(s"Debugging workflow: ${workflow.name}")
      val workflowLauncherActor = childLauncherActor.getOrElse {
        context.actorOf(Props(new DebugLauncherActor()), actorName)
      }

      debugPgService.removeDebugStepData(debugId)
      workflowLauncherActor ! StartDebug(dummyExecution)
      (workflow, workflowLauncherActor)
    } match {
      case Success((workflow, launcherActor)) =>
        val startDate = new DateTime()
        log.debug(s"Workflow ${workflow.name} debugged into: ${launcherActor.toString()} at time: $startDate")
        startDate
      case Failure(exception) =>
        val information = s"Error debugging workflow with the selected execution mode"
        log.error(information)
        debugPgService.setSuccessful(debugId, state = false)
        debugPgService.setError(
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

  private def createExecution(
                               workflow: Workflow,
                               workflowRaw: Workflow,
                               executionContext: ExecutionContext,
                               runExecutionSettings: Option[RunExecutionSettings],
                               user: Option[LoggedUser]
                             ): Future[WorkflowExecution] = {
    workflow.settings.global.executionMode match {
      case WorkflowExecutionMode.marathon =>
        createClusterExecution(workflow, workflowRaw, executionContext, runExecutionSettings, user, marathon)
      case WorkflowExecutionMode.dispatcher =>
        createClusterExecution(workflow, workflowRaw, executionContext, runExecutionSettings, user, dispatcher)
      case WorkflowExecutionMode.local =>
        for {
          anyLocalWorkflowRunning <- executionService.anyLocalWorkflowRunning
          localExecution <- {
            if (!anyLocalWorkflowRunning)
              createLocalExecution(workflow, workflowRaw, executionContext, runExecutionSettings, user)
            else throw new Exception(s"There are executions running and only one it's supported in local mode")
          }
        } yield localExecution
      case _ =>
        throw new Exception(
          s"Invalid execution mode in workflow ${workflow.name}: " +
            s"${workflow.settings.global.executionMode}")
    }
  }

  private def createLocalExecution(
                                    workflow: Workflow,
                                    workflowRaw: Workflow,
                                    executionContext: ExecutionContext,
                                    runExecutionSettings: Option[RunExecutionSettings],
                                    user: Option[LoggedUser]
                                  ): Future[WorkflowExecution] = {
    log.info(s"Creating local execution for workflow ${workflow.name}")

    val launchDate = new DateTime()
    val sparkUri = LinkHelper.getClusterLocalLink
    val newExecution = WorkflowExecution(
      genericDataExecution = GenericDataExecution(
        workflow = workflow,
        workflowRaw = workflowRaw,
        executionMode = WorkflowExecutionMode.local,
        executionContext = executionContext,
        startDate = Option(launchDate),
        launchDate = Option(launchDate),
        userId = user.map(_.id),
        name = runExecutionSettings.flatMap(_.name),
        description = runExecutionSettings.flatMap(_.description)
      ),
      localExecution = Option(LocalExecution(sparkURI = sparkUri))
    )

    executionService.createExecution(newExecution)
  }

  case class LauncherExecutionSettings(
                                        driverFile: String,
                                        pluginJars: Seq[String],
                                        sparkHome: String,
                                        driverArgs: Map[String, String],
                                        sparkSubmitArgs: Map[String, String],
                                        sparkConfigurations: Map[String, String]
                                      )

  private def createClusterExecution(
                                      workflow: Workflow,
                                      workflowRaw: Workflow,
                                      executionContext: ExecutionContext,
                                      runExecutionSettings: Option[RunExecutionSettings],
                                      user: Option[LoggedUser],
                                      executionMode: WorkflowExecutionMode
                                    ): Future[WorkflowExecution] = {
    log.info(s"Creating marathon execution for workflow ${workflow.name}")

    val launcherExecutionSettings = getLauncherExecutionSettings(workflow)

    import launcherExecutionSettings._

    val newExecution = WorkflowExecution(
      sparkSubmitExecution = Option(SparkSubmitExecution(
        driverClass = SpartaDriverClass,
        driverFile = driverFile,
        pluginFiles = pluginJars,
        master = SparkConstant.SparkMesosMaster,
        submitArguments = sparkSubmitArgs,
        sparkConfigurations = sparkConfigurations,
        driverArguments = driverArgs,
        sparkHome = sparkHome
      )),
      marathonExecution = None,
      sparkDispatcherExecution = None,
      genericDataExecution = GenericDataExecution(
        workflow = workflow,
        workflowRaw = workflowRaw,
        executionMode = executionMode,
        executionContext = executionContext,
        userId = user.map(_.id),
        name = runExecutionSettings.flatMap(_.name),
        description = runExecutionSettings.flatMap(_.description)
      )
    )

    executionService.createExecution(newExecution)
  }

  private def getLauncherExecutionSettings(workflowWithContext: Workflow): LauncherExecutionSettings = {
    val sparkSubmitService = new SparkSubmitService(workflowWithContext)
    val sparkHome = sparkSubmitService.validateSparkHome
    val driverFile = sparkSubmitService.extractDriverSubmit
    val pluginJars = JarsHelper.clusterUserPluginJars(workflowWithContext)
    val localPluginJars = JarsHelper.getLocalPathFromJars(pluginJars)
    val driverArgs = sparkSubmitService.extractDriverArgs(pluginJars)
    val (sparkSubmitArgs, sparkConfigurations) = sparkSubmitService.extractSubmitArgsAndSparkConf(localPluginJars)

    log.debug("Spark submit arguments and spark configurations ready to add inside the execution")

    LauncherExecutionSettings(driverFile, pluginJars, sparkHome, driverArgs, sparkSubmitArgs, sparkConfigurations)
  }

}
