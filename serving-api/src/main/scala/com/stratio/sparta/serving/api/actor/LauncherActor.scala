/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.util.{Calendar, UUID}

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Props, _}
import akka.pattern.ask
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.{SpartaQualityRule, WorkflowError}
import com.stratio.sparta.serving.api.actor.QualityRuleReceiverActor.RetrieveQualityRules
import com.stratio.sparta.serving.api.actor.remote.DispatcherActor.EnqueueJob
import com.stratio.sparta.serving.core.actor.ClusterLauncherActor
import com.stratio.sparta.serving.core.actor.LauncherActor._
import com.stratio.sparta.serving.core.actor.ParametersListenerActor.{ValidateExecutionContextToWorkflow, ValidateExecutionContextToWorkflowId}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant.SpartaDriverClass
import com.stratio.sparta.serving.core.constants.{AppConstant, SparkConstant}
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.helpers.{JarsHelper, LinkHelper}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.enumerators.ExecutionTypeEnum._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services._
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, AkkaClusterUtils}
import org.joda.time.DateTime
import org.json4s.native.Serialization.write

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class LauncherActor(parametersStateActor: ActorRef, localLauncherActor: ActorRef, debugLauncherActor: ActorRef) extends Actor
  with ActionUserAuthorize
  with SpartaSerializer {

  implicit val actorSystem = context.system

  private val ResourceWorkflow = "Workflows"
  private val executionService = PostgresDaoFactory.executionPgService
  private val workflowService = PostgresDaoFactory.workflowPgService
  private val debugPgService = PostgresDaoFactory.debugWorkflowPgService

  val qualityRuleReceiverActor = context.actorOf(Props(new QualityRuleReceiverActor()),
    s"$QualityRuleReceiverActorName-${Calendar.getInstance().getTimeInMillis}-${UUID.randomUUID.toString}")

  val marathonLauncherActor = context.actorOf(
    Props(new MarathonLauncherActor),
    s"$MarathonLauncherActorName-${Calendar.getInstance().getTimeInMillis}-${UUID.randomUUID.toString}"
  )

  val clusterLauncherActor = context.actorOf(
    Props(new ClusterLauncherActor()),
    s"$ClusterLauncherActorName-${Calendar.getInstance().getTimeInMillis}-${UUID.randomUUID.toString}"
  )

  val debugDispatcherActor = AkkaClusterUtils.proxyInstanceForName(DebugDispatcherActorName, MasterRole)

  lazy val qualityRulesEnabled = Try(SpartaConfig.getDetailConfig().get.getString("lineage.enable").toBoolean)
    .getOrElse(false)

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case _ => Restart
  }

  override def postStop(): Unit = {
    log.warn(s"Stopped LauncherActor at time ${System.currentTimeMillis()}")
  }

  def receiveApiActions(action: Any): Any = action match {
    case Launch(workflowIdExecutionContext, user) => launch(workflowIdExecutionContext, user)
    case LaunchExecution(executionId, user) => launchExecution(executionId, user)
    case Debug(debugWorkflow, user) => debug(debugWorkflow, user)
    case _ => log.info("Unrecognized message in Launcher Actor")
  }

  def launchExecution(executionId: String, user: Option[LoggedUser]): Future[Any] = {
    val sendResponseTo = sender
    for {
      execution <- executionService.findExecutionById(executionId)
    } yield {
      val actions = Map(ResourceWorkflow -> com.stratio.sparta.security.Status)
      val workflowRaw = execution.getWorkflowToExecute
      authorizeActionsByResourceId(user, actions, workflowRaw.authorizationId, Option(sendResponseTo)) {
        for {
          newExecution <- createExecution(
            workflowRaw,
            addParameterListsToExecutionContext(
              workflowRaw.settings.global.parametersLists,
              execution.genericDataExecution.executionContext
            ),
            Option(RunExecutionSettings(
              execution.executedFromScheduler,
              execution.genericDataExecution.name,
              execution.genericDataExecution.description,
              execution.executedFromExecution
            )),
            user
          )
        } yield launchExecution(newExecution)
      }
    }
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
                  addParameterListsToExecutionContext(
                    validationContextResult.workflow.settings.global.parametersLists,
                    workflowIdExecutionContext.executionContext
                  ),
                  workflowIdExecutionContext.executionSettings,
                  user
                )
              } yield launchExecution(newExecution)
            } else {
              val information = s"The workflow ${validationContextResult.workflow.name} is invalid" +
                s" with the execution context. Messages: \n\t${validationContextResult.workflowValidation.messages.mkString(".\n\t")}"
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
          log.info(s"Launching workflow: ${workflowWithContext.name} in local mode")
          localLauncherActor
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
        executionService.updateStatus(ExecutionStatusUpdate(
          executionId,
          ExecutionStatus(state = Failed, statusInfo = Option(information))
        ), error)
        log.debug(s"Updated correctly the execution status $executionId to $Failed in LaucnherActor")
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
          debug.workflowDebug.getOrElse(throw new Exception(s"Workflow debug not found with id ${workflowId}")),
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
                  s" with the execution context. Messages: \n\t${validationContextResult.workflowValidation.messages.mkString(".\n\t")}"
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
        genericDataExecution = GenericDataExecution(workflow, local, executionContext, userId = user.map(_.id))
      )
      val workflowLauncherActor = debugLauncherActor

      debugDispatcherActor ! EnqueueJob(write(dummyExecution))
      (workflow, debugDispatcherActor)
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

  private def addParameterListsToExecutionContext(
                                                   workflowParameterList: Seq[String],
                                                   executionContext: ExecutionContext
                                                 ): ExecutionContext = {
    //In a future refactor this must be added -> executionContext.copy(paramsLists = workflowParameterList)
    if (executionContext.paramsLists.isEmpty)
      executionContext.copy(paramsLists = Seq("-"))
    else executionContext
  }

  private def createExecution(
                               workflow: Workflow,
                               executionContext: ExecutionContext,
                               runExecutionSettings: Option[RunExecutionSettings],
                               user: Option[LoggedUser]
                             ): Future[WorkflowExecution] = {
    workflow.settings.global.executionMode match {
      case WorkflowExecutionMode.marathon =>
        createClusterExecution(workflow, executionContext, runExecutionSettings, user, marathon)
      case WorkflowExecutionMode.dispatcher =>
        createClusterExecution(workflow, executionContext, runExecutionSettings, user, dispatcher)
      case WorkflowExecutionMode.local =>
        for {
          anyLocalWorkflowRunning <- executionService.anyLocalWorkflowRunning
          localExecution <- {
            if (!anyLocalWorkflowRunning)
              createLocalExecution(workflow, executionContext, runExecutionSettings, user)
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
                                    executionContext: ExecutionContext,
                                    runExecutionSettings: Option[RunExecutionSettings],
                                    user: Option[LoggedUser]
                                  ): Future[WorkflowExecution] = {
    log.info(s"Creating local execution for workflow ${workflow.name}")

    val launchDate = new DateTime()
    val sparkUri = LinkHelper.getClusterLocalLink
    val typeExecution = if(isSystemWorkflow(workflow)) SystemExecution else UserExecution

    val newExecution = WorkflowExecution(
      genericDataExecution = GenericDataExecution(
        workflow = workflow,
        executionMode = WorkflowExecutionMode.local,
        executionContext = executionContext,
        startDate = Option(launchDate),
        launchDate = Option(launchDate),
        userId = user.map(_.id),
        name = runExecutionSettings.flatMap(_.name),
        description = runExecutionSettings.flatMap(_.description)
      ),
      localExecution = Option(LocalExecution(sparkURI = sparkUri)),
      executedFromScheduler = runExecutionSettings.flatMap(_.executedFromScheduler),
      executedFromExecution = runExecutionSettings.flatMap(_.executedFromExecution),
      executionType = Some(typeExecution)
    )

    for {
      qualityRules <-
        if (workflow.settings.global.enableQualityRules.getOrElse(false)) retrieveQualityRules(workflow, user.map(_.id))
        else Future.successful(Seq.empty[SpartaQualityRule])
      workflowExecution <- Future { newExecution.copy(qualityRules = qualityRules) }
      result <- executionService.createExecution(workflowExecution)
    } yield { result }

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
                                      executionContext: ExecutionContext,
                                      runExecutionSettings: Option[RunExecutionSettings],
                                      user: Option[LoggedUser],
                                      executionMode: WorkflowExecutionMode
                                    ): Future[WorkflowExecution] = {
    log.info(s"Creating marathon execution for workflow ${workflow.name}")

    val launcherExecutionSettings = getLauncherExecutionSettings(workflow)
    val typeExecution = if(isSystemWorkflow(workflow)) SystemExecution else UserExecution

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
        executionMode = executionMode,
        executionContext = executionContext,
        userId = user.map(_.id),
        name = runExecutionSettings.flatMap(_.name),
        description = runExecutionSettings.flatMap(_.description)
      ),
      executedFromScheduler = runExecutionSettings.flatMap(_.executedFromScheduler),
      executedFromExecution = runExecutionSettings.flatMap(_.executedFromExecution),
      executionType = Some(typeExecution)
    )

    for {
      launcherExecutionSettings <- Future {newExecution}
      qualityRules <- if (workflow.settings.global.enableQualityRules.getOrElse(false)) retrieveQualityRules(workflow, user.map(_.id))
      else Future(Seq.empty[SpartaQualityRule])
      workflowExecution <- Future {
        launcherExecutionSettings.copy(qualityRules = qualityRules)
      }
      result <- executionService.createExecution(workflowExecution)
    } yield {
      result
    }
  }

  protected[actor] def createLocalWorkflowExecution( workflow: Workflow,
                                                     executionContext: ExecutionContext,
                                                     runExecutionSettings: Option[RunExecutionSettings],
                                                     user: Option[LoggedUser],
                                                     launchDate: DateTime,
                                                     sparkUri: Option[String]): WorkflowExecution =
    WorkflowExecution(
      genericDataExecution = GenericDataExecution(
        workflow = workflow,
        executionMode = WorkflowExecutionMode.local,
        executionContext = executionContext,
        startDate = Option(launchDate),
        launchDate = Option(launchDate),
        userId = user.map(_.id),
        name = runExecutionSettings.flatMap(_.name),
        description = runExecutionSettings.flatMap(_.description)
      ),
      localExecution = Option(LocalExecution(sparkURI = sparkUri)))


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

  def retrieveQualityRules(workflow: Workflow, loggedUser: Option[String]): Future[Seq[SpartaQualityRule]] = {
    if(qualityRulesEnabled)
      (qualityRuleReceiverActor ? RetrieveQualityRules(workflow, loggedUser)).mapTo[Seq[SpartaQualityRule]]
    else Future.successful(Seq.empty[SpartaQualityRule])
  }

  private def isSystemWorkflow(workflow: Workflow): Boolean = {
    //Check if the id of the workflow is the Planned query one
    workflow.id.fold(false){ wID => wID.equals(AppConstant.DefaultPlannedQRWorkflowId)}
  }
}
