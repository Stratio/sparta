/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import java.util.{Calendar, UUID}

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorRef, ActorSystem, AllForOneStrategy, Cancellable, Props, SupervisorStrategy}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.core.properties.ValidatingPropertyMap.option2NotBlankOption
import com.stratio.sparta.serving.core.actor.EnvironmentCleanerActor.TriggerCleaning
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor.ExecutionStatusChange
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.marathon.service.{MarathonService, MarathonUpAndDownComponent}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.submit.SubmissionResponse
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.utils.{SchedulerUtils, SpartaClusterUtils}
import com.typesafe.config.Config
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.joda.time.DateTime
import org.json4s.jackson.Serialization.read

import scala.concurrent._
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Failure, Properties, Success, Try}

class SchedulerMonitorActor extends Actor with SchedulerUtils with SpartaClusterUtils with SpartaSerializer {

  import SchedulerMonitorActor._

  implicit val ec = context.system.dispatchers.lookup("sparta-actors-dispatcher")

  implicit val actorSystem: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  lazy val marathonConfig: Config = SpartaConfig.getMarathonConfig().get
  lazy val marathonUpAndDownComponent = MarathonUpAndDownComponent(marathonConfig)

  val marathonService = new MarathonService(marathonUpAndDownComponent)

  val StartKey = "-start"
  val StopKey = "-stop"

  lazy val executionService = PostgresDaoFactory.executionPgService
  lazy val inconsistentStatusCheckerActor: ActorRef = context.actorOf(
    Props(new InconsistentStatusCheckerActor()),
    s"$InconsistentStatusCheckerActorName-${Calendar.getInstance().getTimeInMillis}-${UUID.randomUUID.toString}"
  )
  lazy val cleanerActor: ActorRef = context.actorOf(Props(new EnvironmentCleanerActor()),
    s"$EnvironmentCleanerActorName-${Calendar.getInstance().getTimeInMillis}-${UUID.randomUUID.toString}")

  type SchedulerAction = WorkflowExecution => Unit

  val executionsStoppedInDbButRunningInDcosState = scala.collection.mutable.HashMap.empty[String, (String, Int)]
  val onStatusChangeActions = scala.collection.mutable.Buffer[SchedulerAction]()
  val invalidStartupStateActions = scala.collection.mutable.Buffer[SchedulerAction]()
  val scheduledActions = scala.collection.mutable.Buffer[(String, Cancellable)]()
  val propertyCheckInterval = "marathon.checkInterval"
  val defaultTimeInterval = "3m"
  val marathonApiUri = Properties.envOrNone("MARATHON_TIKI_TAKKA_MARATHON_URI").notBlank
  lazy val checkTaskMarathon: Cancellable = {
    scheduleMsg(propertyCheckInterval,
      defaultTimeInterval,
      propertyCheckInterval,
      defaultTimeInterval,
      self,
      TriggerCheck)
  }
  lazy val checkCleanerMarathon: Cancellable = {
    scheduleMsg(propertyCheckInterval,
      defaultTimeInterval,
      propertyCheckInterval,
      defaultTimeInterval,
      cleanerActor,
      TriggerCleaning)
  }

  lazy val maxAttemptsCount: Int = Try {
    SpartaConfig.getDetailConfig().get.getInt(SchedulerStopMaxCount)
  }.toOption.getOrElse(DefaultSchedulerStopMaxCount)

  override def supervisorStrategy: SupervisorStrategy = AllForOneStrategy() {
    case _ => Restart
  }

  override def preStart(): Unit = {

    //Trigger check only if run in Marathon
    if (marathonApiUri.isDefined){
      checkTaskMarathon
      checkCleanerMarathon
    }

    mediator ! Subscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)
    onStatusChangeActions += manageStopAction
    onStatusChangeActions += manageCacheAction
    invalidStartupStateActions += manageStartupStateAction
    invalidStartupStateActions += manageStopStateAction
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)
    scheduledActions.foreach { case (_, task) => if (!task.isCancelled) task.cancel() }

    if (marathonApiUri.isDefined){
      checkTaskMarathon.cancel()
      checkCleanerMarathon.cancel()
    }

    log.warn(s"Stopped SchedulerMonitorActor at time ${System.currentTimeMillis()}")
  }

  override def receive: Receive = {
    case ExecutionStatusChange(_, executionStatusChange) =>
      import executionStatusChange._
      if (originalExecution.lastStatus.state != newExecution.lastStatus.state) {
        executeActions(newExecution, onStatusChangeActions)
      }
      executeActions(newExecution, invalidStartupStateActions)
    case TriggerCheck =>
      checkForOrphanedWorkflows()
    case InconsistentStatuses(startedInDatabaseButStoppedInDcos, stoppedInDatabaseRunningInDcos) =>
      val workflowsToCleanInDatabase = getStatedInDbButStoppedInDcosAndUpdateState(startedInDatabaseButStoppedInDcos)
      cleanOrphanedWorkflows(workflowsToCleanInDatabase, stoppedInDatabaseRunningInDcos)
  }

  def executeActions(
                      workflowExecution: WorkflowExecution,
                      actions: scala.collection.mutable.Buffer[SchedulerAction]
                    ): Unit = {
    if (isThisNodeClusterLeader(cluster)) {
      log.debug("Executing schedulerMonitor actions")
      actions.foreach { callback =>
        Future {
          try {
            blocking(callback(workflowExecution))
          } catch {
            case e: Exception =>
              log.error(s"Error executing action for workflow execution ${workflowExecution.getExecutionId}." +
                s" With exception: ${e.getLocalizedMessage}")
          }
        }(context.dispatcher)
      }
    }
  }

  val manageStopAction: WorkflowExecution => Unit = (execution: WorkflowExecution) => {
      if (
        stopStates.contains(execution.lastStatus.state) &&
        execution.genericDataExecution.executionMode == marathon &&
          execution.marathonExecution.isDefined && marathonApiUri.isDefined
      ) {
        marathonStop(execution)
      } else if (
        stopStates.contains(execution.lastStatus.state) &&
        execution.genericDataExecution.executionMode == dispatcher &&
        execution.sparkDispatcherExecution.isDefined
      ) {
        dispatcherStop(execution)
      } else if(
        stopAndStoppingStates.contains(execution.lastStatus.state) &&
          execution.genericDataExecution.executionMode == local
      ) {
        localStop(execution)
      }
  }

  val manageCacheAction: WorkflowExecution => Unit = (execution: WorkflowExecution) => {
    executionService.updateCacheExecutionStatus(execution)
  }

  val awaitWorkflowStatusTime: WorkflowExecution => Int = (workflowExecution: WorkflowExecution) => {
    import com.stratio.sparta.serving.core.marathon.service.MarathonService._
    calculateMaxTimeout(getHealthChecks(workflowExecution.getWorkflowToExecute))
  }

  val manageStartupStateAction: WorkflowExecution => Unit = (workflowExecution: WorkflowExecution) => {
    if (workflowExecution.lastStatus.state == NotStarted &&
        !scheduledActions.exists(task => task._1 == workflowExecution.getExecutionId + StartKey)) {
      log.info(s"Scheduling checkRunningStatus for execution id [${workflowExecution.getExecutionId}]")
      val task = scheduleOneTask(AwaitWorkflowChangeStatus, awaitWorkflowStatusTime(workflowExecution))(
        checkRunningStatus(workflowExecution.getExecutionId))
      scheduledActions += (workflowExecution.getExecutionId + StartKey -> task)
    }
  }

  val manageStopStateAction: WorkflowExecution => Unit = (workflowExecution: WorkflowExecution) => {
    if (
      (
        workflowExecution.lastStatus.state == Stopping ||
          workflowExecution.lastStatus.state == StoppingByUser ||
          workflowExecution.lastStatus.state == Failed
        ) && !scheduledActions.exists(task => task._1 == workflowExecution.getExecutionId + StopKey)) {
      val delay = ClusterLauncherActor.getShutDownTimeoutEngine(workflowExecution) + SparkConstant.DefaultShutdownTaskTimeoutMs
      log.info(s"Scheduling checkStopStatus for execution id [${workflowExecution.getExecutionId}] with delay $delay")
      val task = context.system.scheduler.scheduleOnce(delay milli) {
        checkStopStatus(workflowExecution.getExecutionId)
      }
      scheduledActions += (workflowExecution.getExecutionId + StopKey -> task)
    }
  }

  //scalastyle:off
  def localStop(workflowExecution: WorkflowExecution): Unit = {
    if (workflowExecution.lastStatus.state == Stopping || workflowExecution.lastStatus.state == StoppedByUser || workflowExecution.lastStatus.state == StoppingByUser ||  workflowExecution.lastStatus.state == Failed) {
      log.info("Stop message received")
      scheduledActions.filter(_._1 == workflowExecution.getExecutionId).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      Try {
        stopStreamingContext()
      } match {
        case Success(_) =>
          val information = "The workflow was successfully stopped"
          val newInformation = Option(information)
          log.info(information)
          if (workflowExecution.lastStatus.state != Failed) {
            val newStatus = workflowExecution.lastStatus.state match {
              case StoppingByUser => StoppedByUser
              case Stopping => Stopped
              case _ => workflowExecution.lastStatus.state
            }
            executionService.updateStatus(ExecutionStatusUpdate(
              workflowExecution.getExecutionId,
              ExecutionStatus(
                state = newStatus,
                statusInfo = newInformation
              )))
          }
        case Failure(e) =>
          val error = "An error was encountered while stopping contexts"
          log.error(error, e)
          if (workflowExecution.lastStatus.state != Failed) {
            val wError = WorkflowError(
              error,
              PhaseEnum.Stop,
              e.toString,
              ExceptionHelper.toPrintableException(e)
            )
            executionService.updateStatus(ExecutionStatusUpdate(
              workflowExecution.getExecutionId,
              ExecutionStatus(state = Failed, statusInfo = Option(error)))
              , wError)
          }
      }
    }

    val finishedExecution = if (workflowExecution.lastStatus.state == Stopped || workflowExecution.lastStatus.state == StoppedByUser || workflowExecution.lastStatus.state == Failed) {
      scheduledActions.filter(_._1.contains(workflowExecution.getExecutionId)).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      val newStatus = workflowExecution.lastStatus.state match {
        case Failed => Failed
        case StoppedByUser => StoppedByUser
        case _ => Finished
      }
      val newStatusInfo = if (newStatus == Finished || newStatus == StoppedByUser)
        Option("The workflow was successfully finished with local scheduler")
      else None
      executionService.updateStatus(ExecutionStatusUpdate(
        workflowExecution.getExecutionId,
        ExecutionStatus(state = newStatus, statusInfo = newStatusInfo)
      ))
    } else workflowExecution

    if (finishedExecution.genericDataExecution.endDate.isEmpty) {
      Try(executionService.setEndDate(finishedExecution, new DateTime()))
        .getOrElse(log.warn(s"Impossible to update endDate in execution id: ${workflowExecution.getExecutionId}"))
    }
  }

  def marathonStop(workflowExecution: WorkflowExecution, force: Boolean = false): Unit = {
    if (workflowExecution.lastStatus.state == Stopped || workflowExecution.lastStatus.state == StoppedByUser || workflowExecution.lastStatus.state == Failed || force) {
      log.info("Stop message received")
      scheduledActions.filter(_._1.contains(workflowExecution.getExecutionId)).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      log.info(s"Finishing workflow with Marathon API")
      val executionUpdated = Try {
        marathonService.kill(workflowExecution.marathonExecution.get.marathonId)
      } match {
        case Success(_) =>
          val information = s"Workflow correctly finished in Marathon API"
          val newStatus = if (workflowExecution.lastStatus.state == Failed || workflowExecution.lastStatus.state == StoppedByUser) NotDefined else Finished
          val newInformation = if (newStatus == Finished || newStatus == StoppedByUser) Option(information)
          else None
          log.info(information)
          executionService.updateStatus(ExecutionStatusUpdate(
            workflowExecution.getExecutionId,
            ExecutionStatus(state = newStatus, statusInfo = newInformation)
          ))
        case Failure(e) =>
          log.error("An error was encountered while sending a stop message to Marathon API", e)
          workflowExecution
      }
      if (executionUpdated.genericDataExecution.endDate.isEmpty) {
        log.debug(s"Updating endDate in execution id: ${workflowExecution.getExecutionId}")
        Try(executionService.setEndDate(executionUpdated, new DateTime()))
          .getOrElse(log.warn(s"Impossible to update endDate in execution id: ${workflowExecution.getExecutionId}"))
      }
    }
  }

  @deprecated
  def dispatcherStop(workflowExecution: WorkflowExecution): Unit = {
    if (workflowExecution.lastStatus.state == Stopping || workflowExecution.lastStatus.state == Failed) {
      log.info("Stop message received")
      scheduledActions.filter(_._1.contains(workflowExecution.getExecutionId)).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      log.info(s"Finishing workflow with Dispatcher API")
      val executionUpdated = Try {
        val urlWithAppId = s"${workflowExecution.sparkDispatcherExecution.get.killUrl}/" +
          s"${workflowExecution.sparkExecution.get.applicationId}"
        log.info(s"Killing application (${workflowExecution.sparkExecution.get.applicationId}) " +
          s"with Spark Dispatcher Submissions API in url: $urlWithAppId")
        val post = new HttpPost(urlWithAppId)
        val postResponse = HttpClientBuilder.create().build().execute(post)

        read[SubmissionResponse](Source.fromInputStream(postResponse.getEntity.getContent).mkString)
      } match {
        case Success(submissionResponse) if submissionResponse.success =>
          val information = s"Workflow killed correctly with Spark API"
          val newStatus = if (workflowExecution.lastStatus.state == Failed) NotDefined else Killed
          log.info(information)
          executionService.updateStatus(ExecutionStatusUpdate(
            workflowExecution.getExecutionId,
            ExecutionStatus(state = newStatus, statusInfo = Some(information))
          ))
        case Success(submissionResponse) =>
          log.debug(s"Failed response: $submissionResponse")
          val information = s"Error while stopping task"
          log.info(information)
          val updateStateResult = executionService.updateStatus(ExecutionStatusUpdate(
            workflowExecution.getExecutionId,
            ExecutionStatus(state = Failed, statusInfo = Some(information))
          ))
          val wError = submissionResponse.message.map(error => WorkflowError(information, PhaseEnum.Stop, error, error))
          wError.map(error => executionService.setLastError(updateStateResult, error)).getOrElse(updateStateResult)
        case Failure(e) =>
          val error = "Impossible to parse submission killing response"
          log.error(error, e)
          val wError = WorkflowError(
            error,
            PhaseEnum.Stop,
            e.toString,
            ExceptionHelper.toPrintableException(e)
          )
          executionService.updateStatus(ExecutionStatusUpdate(
            workflowExecution.getExecutionId,
            ExecutionStatus(state = Failed, statusInfo = Some(error))
          ), wError)
      }
      if (executionUpdated.genericDataExecution.endDate.isEmpty) {
        Try(executionService.setEndDate(executionUpdated, new DateTime()))
          .getOrElse(log.warn(s"Impossible to update endDate in execution id: ${workflowExecution.getExecutionId}"))
      }
    }
  }

  def checkStopStatus(id: String): Unit = {
    for {
      workflowExecution <- executionService.findExecutionById(id)
    } yield {
      val wrongStopStates = Seq(Stopping, StoppingByUser)
      val validStopStates = Seq(Stopped, Failed, Killed, Finished, StoppedByUser)

      workflowExecution.lastStatus.state match {
        case status if wrongStopStates.contains(status) =>
          val information = s"Checker: the workflow execution  did not stop correctly after maximum deployment time"
          log.warn(information.replace("  ", s" $id "))
          executionService.updateStatus(ExecutionStatusUpdate(
            id,
            ExecutionStatus(state = Failed, statusInfo = Some(information))
          ))
        case status if validStopStates.contains(status) =>
          val information = s"Checker: the workflow execution  stopped correctly"
          log.info(information.replace("  ", s" $id "))
          if (status != Failed) {
            executionService.updateStatus(ExecutionStatusUpdate(
              id,
              ExecutionStatus(state = NotDefined, statusInfo = Some(information))
            ))
          }
        case _ =>
          val information = s"Checker: the workflow execution  has invalid state ${workflowExecution.lastStatus.state}"
          log.info(information.replace("  ", s" $id "))
          if (workflowExecution.lastStatus.state != Failed) {
            executionService.updateStatus(ExecutionStatusUpdate(
              id,
              ExecutionStatus(state = Failed, statusInfo = Some(information))
            ))
          }
      }

      scheduledActions.filter(_._1.contains(id)).foreach { task =>
        scheduledActions -= task
      }
    }

  }

  def checkRunningStatus(id: String): Unit = {
    for {
      workflowExecution <- executionService.findExecutionById(id)
    } yield {
      val wrongStartStates = Seq(Launched, Starting, Uploaded, NotStarted, Created)
      val validStartStates = Seq(Started)

      workflowExecution.lastStatus.state match {
        case status if wrongStartStates.contains(status) =>
          val information = s"Checker: the workflow execution  did not start correctly after maximum deployment time"
          log.warn(information.replace("  ", s" $id "))
          executionService.updateStatus(ExecutionStatusUpdate(
            id,
            ExecutionStatus(state = Failed, statusInfo = Some(information))
          ))
        case status if validStartStates.contains(status) =>
          val information = s"Checker: the workflow execution  started correctly"
          log.info(information.replace("  ", s" $id "))
          executionService.updateStatus(ExecutionStatusUpdate(
            id,
            ExecutionStatus(state = NotDefined, statusInfo = Some(information))
          ))
        case _ =>
          val information = s"Checker: the workflow execution  has correct finish state ${workflowExecution.lastStatus.state}"
          log.info(information.replace("  ", s" $id "))
      }

      scheduledActions.filter(_._1.contains(id)).foreach { task =>
        scheduledActions -= task
      }
    }

  }

  //scalastyle:on

  def checkForOrphanedWorkflows(): Unit = {
    for {
      executions <- executionService.findExecutionsByStatus(SchedulerMonitorActor.runningStates)
    } yield {
      val startedExecutionsInDatabase: Map[String, String] = fromExecutionsToMapMarathonIdExecutionId(executions)

      inconsistentStatusCheckerActor ! CheckConsistency(startedExecutionsInDatabase)
    }
  }

  def getStatedInDbButStoppedInDcosAndUpdateState(
                                                   startedInDatabaseButStoppedInDcos: Map[String, String]
                                                 ): Map[String, String] = {
    startedInDatabaseButStoppedInDcos.foreach { case (marathonId, executionId) =>
      executionsStoppedInDbButRunningInDcosState.get(marathonId) match {
        case Some((_, attempts)) =>
          log.debug(s"Increasing counter with inconsistent state over Database and Marathon with id: $marathonId and attempts: $attempts")
          executionsStoppedInDbButRunningInDcosState.update(marathonId, (executionId, attempts + 1))
        case None =>
          log.debug(s"Creating counter with inconsistent state over Database and Marathon with id: $marathonId")
          executionsStoppedInDbButRunningInDcosState += (marathonId -> ((executionId, 1)))
      }
    }
    executionsStoppedInDbButRunningInDcosState.flatMap{ case (marathonId, (executionId, attempts)) =>
      if(attempts >= maxAttemptsCount){
        log.debug(s"The counter for inconsistent state over Database and Marathon with id: $marathonId" +
          s" is greater or equal to max attempts: $attempts." +
          s" It will be removed from the state and will be sent to clean function")
        executionsStoppedInDbButRunningInDcosState.remove(marathonId)
        Option(marathonId -> executionId)
      } else None
    }.toMap
  }

  def cleanOrphanedWorkflows(
                              startedInDatabaseButStoppedInDcos: Map[String, String],
                              stoppedInDatabaseButRunningInDcos: Seq[String]
                            ): Unit = {
    // Kill all the workflows that are stored as RUNNING but that are actually STOPPED in Marathon.
    // Update the status with discrepancy as the StatusInfo and Stopped as Status
    val runningButActuallyStoppedFiltered = startedInDatabaseButStoppedInDcos.filter{ case (_, idExecution) =>
      !scheduledActions.exists(task => task._1 == idExecution + StartKey)
    }
    val listNewStatuses = for {
      (nameWorkflowExecution, idExecution) <- runningButActuallyStoppedFiltered
    } yield {

      val information = "Checker: there was a discrepancy between the monitored and the current status in Marathon" +
        " of the workflow  . Therefore, the workflow was terminated."
      log.info(information.replace("  ", s" with id $idExecution and name $nameWorkflowExecution "))
      ExecutionStatusUpdate(
        idExecution,
        ExecutionStatus(state = Stopped, statusInfo = Some(information))
      )
    }

    listNewStatuses.foreach { newStatus =>
      Try(executionService.updateStatus(newStatus)) match {
        case Success(execution) =>
          log.debug(s"Stopped correctly the execution ${execution.getExecutionId} with workflow ${execution.genericDataExecution.workflow.name}")
        case Failure(exception) =>
          log.warn(s"Error stopping execution ${newStatus.id} with exception ${ExceptionHelper.toPrintableException(exception)}")
      }
    }


    // Kill all the workflow executions that are stored as STOPPED but that are actually RUNNING in Marathon.
    // Reuse the last Status because it might have failed just the previous DELETE Rest call for that appID
    // and overwriting the status could cause the loss of useful info
    val seqStoppedAsExecutionId = fromDCOSName2ExecutionId(stoppedInDatabaseButRunningInDcos)

    if (seqStoppedAsExecutionId.nonEmpty) {
      log.info(s" Stopped but running: ${seqStoppedAsExecutionId.map(id => s"Application stopped: $id").mkString(",")}")
    }

    for {
      executions <- executionService.findExecutionsByIds(seqStoppedAsExecutionId)
    } yield {
      executions.foreach { execution =>
        execution.id.foreach { idExecution =>
          marathonStop(execution, force = true)
          log.info(s"The application with id $idExecution was marked ${execution.lastStatus.state} " +
            s"but it was running, therefore its termination was triggered")
        }
      }
    }
  }
}


object SchedulerMonitorActor {

  trait Notification

  object TriggerCheck extends Notification

  object RetrieveStatuses extends Notification

  object RetrieveWorkflowsEnv extends Notification

  case class CheckConsistency(startedWorkflowsInDatabase: Map[String, String]) extends Notification

  case class InconsistentStatuses(
                                   startedInDatabaseButStoppedInDcos: Map[String, String], //(MarathonId, ExecutionId)
                                   stoppedInDatabaseRunningInDcos: Seq[String]
                                 ) extends Notification

  val stopStates = Seq(Stopped, Failed, StoppedByUser)
  val stopAndStoppingStates = Seq(Stopped, Failed, StoppedByUser, Stopping, StoppingByUser)
  val finishedStates = Seq(Created, Finished, Failed)
  val notRunningStates: Seq[WorkflowStatusEnum.Value] = stopStates ++ finishedStates
  val runningStates: Seq[WorkflowStatusEnum.Value] = Seq(Launched, Starting, Started, Uploaded, NotStarted, Stopping, StoppingByUser)

  def fromDCOSName2ExecutionId(stoppedButActuallyRunning: Seq[String]): Seq[String] =
    stoppedButActuallyRunning.flatMap { nameWorkflowDCOS =>
      Try {
        val splittedNameDCOS = nameWorkflowDCOS.substring(nameWorkflowDCOS.indexOf("/home")).split("/")
        splittedNameDCOS.last
      }.toOption
    }

  def fromExecutionsToMapMarathonIdExecutionId(executions: Seq[WorkflowExecution]): Map[String, String] = {
    //Filter executions and obtains only the running states
    executions.filter { execution =>
      execution.genericDataExecution.executionMode == marathon &&
        !notRunningStates.contains(execution.lastStatus.state)
    }.flatMap { execution =>
      execution.marathonExecution match {
        case Some(marathonExecution) => Option(s"${marathonExecution.marathonId}", execution.getExecutionId)
        case None => None
      }
    }.toMap
  }

  def props: Props = Props[SchedulerMonitorActor]
}