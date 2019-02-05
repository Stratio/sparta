/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import java.util.{Calendar, UUID}

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorRef, AllForOneStrategy, Cancellable, Props, SupervisorStrategy}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.core.properties.ValidatingPropertyMap.option2NotBlankOption
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor.ExecutionStatusChange
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.submit.SubmissionResponse
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.utils.{SchedulerUtils, SpartaClusterUtils}
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.joda.time.DateTime
import org.json4s.jackson.Serialization.read

import scala.concurrent._
import scala.io.Source
import scala.util.{Failure, Properties, Success, Try}

class SchedulerMonitorActor extends Actor with SchedulerUtils with SpartaClusterUtils with SpartaSerializer {

  import SchedulerMonitorActor._

  implicit val ec = context.system.dispatchers.lookup("sparta-actors-dispatcher")

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator
  val marathonService = new MarathonService(context)

  lazy val executionService = PostgresDaoFactory.executionPgService
  lazy val inconsistentStatusCheckerActor: ActorRef = context.actorOf(
    Props(new InconsistentStatusCheckerActor()),
    s"$InconsistentStatusCheckerActorName-${Calendar.getInstance().getTimeInMillis}-${UUID.randomUUID.toString}"
  )
  implicit lazy val currentInstanceName: Option[String] = instanceName

  type SchedulerAction = WorkflowExecution => Unit

  val executionsStoppedInDbButRunningInDcosState = scala.collection.mutable.HashMap.empty[String, (String, Int)]
  val onStatusChangeActions = scala.collection.mutable.Buffer[SchedulerAction]()
  val invalidStartupStateActions = scala.collection.mutable.Buffer[SchedulerAction]()
  val scheduledActions = scala.collection.mutable.Buffer[(String, Cancellable)]()
  val propertyCheckInterval = "marathon.checkInterval"
  val defaultTimeInterval = "1m"
  val marathonApiUri = Properties.envOrNone("MARATHON_TIKI_TAKKA_MARATHON_URI").notBlank
  lazy val checkTaskMarathon: Cancellable = {
    scheduleMsg(propertyCheckInterval,
      defaultTimeInterval,
      propertyCheckInterval,
      defaultTimeInterval,
      self,
      TriggerCheck)
  }

  lazy val maxAttemptsCount: Int = Try {
    SpartaConfig.getDetailConfig().get.getInt(SchedulerStopMaxCount)
  }.toOption.getOrElse(DefaultSchedulerStopMaxCount)

  override def supervisorStrategy: SupervisorStrategy = AllForOneStrategy() {
    case _ => Restart
  }

  override def preStart(): Unit = {

    //Trigger check only if run in Marathon
    if (marathonApiUri.isDefined) checkTaskMarathon

    mediator ! Subscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)
    onStatusChangeActions += manageStopAction
    onStatusChangeActions += manageCacheAction
    invalidStartupStateActions += manageStartupStateAction
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)
    scheduledActions.foreach { case (_, task) => if (!task.isCancelled) task.cancel() }

    if (marathonApiUri.isDefined) checkTaskMarathon.cancel()

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
    if (stopStates.contains(execution.lastStatus.state)) {
      if (execution.genericDataExecution.executionMode == marathon && execution.marathonExecution.isDefined)
        marathonStop(execution)
      else if (execution.genericDataExecution.executionMode == dispatcher &&
        execution.sparkDispatcherExecution.isDefined)
        dispatcherStop(execution)
      else if (execution.genericDataExecution.executionMode == local)
        localStop(execution)
    }
  }

  val manageCacheAction: WorkflowExecution => Unit = (execution: WorkflowExecution) => {
    executionService.updateCacheExecutionStatus(execution)
  }

  val awaitWorkflowStatusTime: WorkflowExecution => Int = (workflowExecution: WorkflowExecution) => {
    import MarathonService._
    calculateMaxTimeout(getHealthChecks(workflowExecution.getWorkflowToExecute))
  }

  val manageStartupStateAction: WorkflowExecution => Unit = (workflowExecution: WorkflowExecution) => {
    if (workflowExecution.lastStatus.state == Uploaded ||
      workflowExecution.lastStatus.state == Launched ||
      workflowExecution.lastStatus.state == NotStarted ||
      workflowExecution.lastStatus.state == Starting &&
        !scheduledActions.exists(task => task._1 == workflowExecution.getExecutionId)) {

      val task = scheduleOneTask(AwaitWorkflowChangeStatus, awaitWorkflowStatusTime(workflowExecution))(
        checkStatus(workflowExecution.getExecutionId))
      scheduledActions += (workflowExecution.getExecutionId -> task)
    }
  }

  //scalastyle:off
  def localStop(workflowExecution: WorkflowExecution): Unit = {
    val updatedExecution = if (workflowExecution.lastStatus.state == Stopping || workflowExecution.lastStatus.state == Failed) {
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
            val newStatus = Stopped
            executionService.updateStatus(ExecutionStatusUpdate(
              workflowExecution.getExecutionId,
              ExecutionStatus(
                state = newStatus,
                statusInfo = newInformation
              )))
          } else workflowExecution
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
          } else workflowExecution
      }
    } else workflowExecution

    val finishedExecution = if (updatedExecution.lastStatus.state == Stopped || updatedExecution.lastStatus.state == Failed) {
      scheduledActions.filter(_._1 == updatedExecution.getExecutionId).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      val newStatus = if (updatedExecution.lastStatus.state == Failed) NotDefined else Finished
      val newStatusInfo = if (newStatus == Finished)
        Option("The workflow was successfully finished with local scheduler")
      else None
      executionService.updateStatus(ExecutionStatusUpdate(
        updatedExecution.getExecutionId,
        ExecutionStatus(state = newStatus, statusInfo = newStatusInfo)
      ))
    } else updatedExecution

    if (finishedExecution.genericDataExecution.endDate.isEmpty) {
      Try(executionService.setEndDate(finishedExecution, new DateTime()))
        .getOrElse(log.warn(s"Impossible to update endDate in execution id: ${workflowExecution.getExecutionId}"))
    }
  }

  def marathonStop(workflowExecution: WorkflowExecution, force: Boolean = false): Unit = {
    if (workflowExecution.lastStatus.state == Stopped || workflowExecution.lastStatus.state == Failed || force) {
      log.info("Stop message received")
      scheduledActions.filter(_._1 == workflowExecution.getExecutionId).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      log.info(s"Finishing workflow with Marathon API")
      val executionUpdated = Try {
        marathonService.kill(workflowExecution.marathonExecution.get.marathonId)
      } match {
        case Success(_) =>
          val information = s"Workflow correctly finished in Marathon API"
          val newStatus = if (workflowExecution.lastStatus.state == Failed) NotDefined else Finished
          val newInformation = if (newStatus == Finished) Option(information)
          else None
          log.info(information)
          executionService.updateStatus(ExecutionStatusUpdate(
            workflowExecution.getExecutionId,
            ExecutionStatus(state = newStatus, statusInfo = newInformation)
          ))
        case Failure(e) =>
          val error = "An error was encountered while sending a stop message to Marathon API"
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
              ExecutionStatus(state = NotDefined, statusInfo = Some(error))
            ), wError)
          } else workflowExecution
      }
      if (executionUpdated.genericDataExecution.endDate.isEmpty) {
        log.debug(s"Updating endDate in execution id: ${workflowExecution.getExecutionId}")
        Try(executionService.setEndDate(executionUpdated, new DateTime()))
          .getOrElse(log.warn(s"Impossible to update endDate in execution id: ${workflowExecution.getExecutionId}"))
      }
    }
  }

  def dispatcherStop(workflowExecution: WorkflowExecution): Unit = {
    if (workflowExecution.lastStatus.state == Stopping || workflowExecution.lastStatus.state == Failed) {
      log.info("Stop message received")
      scheduledActions.filter(_._1 == workflowExecution.getExecutionId).foreach { task =>
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

  def checkStatus(id: String): Unit = {
    for {
      workflowExecution <- executionService.findExecutionById(id)
    } yield {
      val wrongStartStates = Seq(Launched, Starting, Uploaded, NotStarted, Created)
      val validStartStates = Seq(Started)
      val wrongStopStates = Seq(Stopping)
      val validStopStates = Seq(Stopped, Failed, Killed, Finished)

      workflowExecution.lastStatus.state match {
        case status if wrongStartStates.contains(status) =>
          val information = s"Checker: the workflow execution  did not start correctly after maximum deployment time"
          log.warn(information.replace("  ", s" $id "))
          executionService.updateStatus(ExecutionStatusUpdate(
            id,
            ExecutionStatus(state = Failed, statusInfo = Some(information))
          ))
        case status if wrongStopStates.contains(status) =>
          val information = s"Checker: the workflow execution  did not stop correctly after maximum deployment time"
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

      scheduledActions.filter(_._1 == id).foreach { task =>
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
      !scheduledActions.exists(task => task._1 == idExecution)
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

  val stopStates = Seq(Stopped, Failed, Stopping)
  val finishedStates = Seq(Created, Finished, Failed)
  val notRunningStates: Seq[WorkflowStatusEnum.Value] = stopStates ++ finishedStates
  val runningStates: Seq[WorkflowStatusEnum.Value] = Seq(Launched, Starting, Started, Uploaded, NotStarted)

  def fromDCOSName2ExecutionId(stoppedButActuallyRunning: Seq[String]): Seq[String] =
    stoppedButActuallyRunning.flatMap { nameWorkflowDCOS =>
      Try {
        val splittedNameDCOS = nameWorkflowDCOS.substring(nameWorkflowDCOS.indexOf("/home")).split("/")
        splittedNameDCOS.last
      }.toOption
    }

  def fromExecutionsToMapMarathonIdExecutionId(
                                                executions: Seq[WorkflowExecution]
                                              )(implicit instanceName: Option[String]): Map[String, String] = {
    executions.filter { execution =>
      execution.genericDataExecution.executionMode == marathon &&
        !notRunningStates.contains(execution.lastStatus.state)
    }.flatMap { execution =>
      execution.marathonExecution match {
        case Some(marathonExecution) => Option(s"/${marathonExecution.marathonId}", execution.getExecutionId)
        case None => None
      }
    }.toMap
  }
}