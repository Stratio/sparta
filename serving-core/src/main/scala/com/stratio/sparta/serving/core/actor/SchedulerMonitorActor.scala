/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import scala.concurrent._
import scala.io.Source
import scala.util.{Failure, Properties, Success, Try}

import akka.actor.{Actor, ActorRef, Cancellable, Props}

import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.core.properties.ValidatingPropertyMap.option2NotBlankOption
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor.ExecutionStatusChange
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.submit.SubmissionResponse
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.dao.WorkflowExecutionPostgresDao
import com.stratio.sparta.serving.core.utils.{PostgresDaoFactory, SchedulerUtils}
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.joda.time.DateTime
import org.json4s.jackson.Serialization.read
import scala.concurrent._
import scala.io.Source
import scala.util.{Failure, Properties, Success, Try}
import com.stratio.sparta.serving.core.utils.{PostgresDaoFactory, SchedulerUtils}
import com.stratio.sparta.serving.core.services.dao.WorkflowExecutionPostgresDao
import com.stratio.sparta.serving.core.utils.{SchedulerUtils, SpartaClusterUtils}
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.joda.time.DateTime
import org.json4s.jackson.Serialization.read
import scala.concurrent._
import scala.io.Source
import scala.util.{Failure, Properties, Success, Try}

import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}

class SchedulerMonitorActor extends Actor with SchedulerUtils with SpartaClusterUtils with SpartaSerializer {

  import scala.concurrent.ExecutionContext.Implicits.global

  import SchedulerMonitorActor._

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  lazy val executionService = PostgresDaoFactory.executionPgService

  lazy val inconsistentStatusCheckerActor: ActorRef =
    context.system.actorOf(Props(new InconsistentStatusCheckerActor()), AkkaConstant.InconsistentStatusCheckerActorName)
  implicit lazy val currentInstanceName: Option[String] = instanceName

  type SchedulerAction = WorkflowExecution => Unit

  private val onStatusChangeActions = scala.collection.mutable.Buffer[SchedulerAction]()
  private val invalidStartupStateActions = scala.collection.mutable.Buffer[SchedulerAction]()
  private val scheduledActions = scala.collection.mutable.Buffer[(String, Cancellable)]()
  private val propertyCheckInterval = "marathon.checkInterval"
  private val defaultTimeInterval = "30s"
  private val marathonApiUri = Properties.envOrNone("MARATHON_TIKI_TAKKA_MARATHON_URI").notBlank
  private lazy val checkTaskMarathon: Cancellable = {
    scheduleMsg(propertyCheckInterval,
      defaultTimeInterval,
      propertyCheckInterval,
      defaultTimeInterval,
      self,
      TriggerCheck)
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
    case msg@InconsistentStatuses(_, _) =>
      cleanOrphanedWorkflows(msg.runningButActuallyStopped, msg.stoppedButActuallyRunning)
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

  val manageStartupStateAction: WorkflowExecution => Unit = (workflowExecution: WorkflowExecution) => {
    if (workflowExecution.lastStatus.state == Uploaded ||
      workflowExecution.lastStatus.state == Launched ||
      workflowExecution.lastStatus.state == NotStarted ||
      workflowExecution.lastStatus.state == Starting &&
        !scheduledActions.exists(task => task._1 == workflowExecution.getExecutionId)) {
      val task = scheduleOneTask(AwaitWorkflowChangeStatus, DefaultAwaitWorkflowChangeStatus)(
        checkStatus(workflowExecution.getExecutionId))
      scheduledActions += (workflowExecution.getExecutionId -> task)
    }
  }

  //scalastyle:off
  def localStop(workflowExecution: WorkflowExecution): Unit = {
    val updatedExecution = if (workflowExecution.lastStatus.state == Stopping) {
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
          val newStatus = Stopped
          val newInformation = Option(information)
          log.info(information)
          executionService.updateStatus(ExecutionStatusUpdate(
            workflowExecution.getExecutionId,
            ExecutionStatus(
              state = newStatus,
              statusInfo = newInformation
            )))
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
    } else if (workflowExecution.lastStatus.state == Stopped || workflowExecution.lastStatus.state == Failed) {
      scheduledActions.filter(_._1 == workflowExecution.getExecutionId).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      val newStatus = if (workflowExecution.lastStatus.state == Failed) NotDefined else Finished
      val newStatusInfo = if (newStatus == Finished)
        Option("The workflow was successfully finished with local scheduler")
      else None
      executionService.updateStatus(ExecutionStatusUpdate(
        workflowExecution.getExecutionId,
        ExecutionStatus(state = newStatus, statusInfo = newStatusInfo)
      ))
    } else workflowExecution

    if (updatedExecution.genericDataExecution.endDate.isEmpty) {
      Try(executionService.setEndDate(updatedExecution, new DateTime()))
        .getOrElse(log.warn(s"Impossible to update endDate in execution id: ${workflowExecution.getExecutionId}"))
    }
  }

  //scalastyle:on

  def marathonStop(workflowExecution: WorkflowExecution): Unit = {
    if (workflowExecution.lastStatus.state == Stopped || workflowExecution.lastStatus.state == Failed) {
      log.info("Stop message received")
      scheduledActions.filter(_._1 == workflowExecution.getExecutionId).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      log.info(s"Finishing workflow with Marathon API")
      val executionUpdated = Try {
        new MarathonService(context).kill(workflowExecution.marathonExecution.get.marathonId)
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
              ExecutionStatus(state = Failed, statusInfo = Some(error))
            ), wError)
          } else workflowExecution
      }
      if (executionUpdated.genericDataExecution.endDate.isEmpty) {
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

  //scalastyle:off
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
          val information = s"Checker: the workflow execution did not start correctly after maximum deployment time"
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
          executionService.updateStatus(ExecutionStatusUpdate(
            id,
            ExecutionStatus(state = NotDefined, statusInfo = Some(information))
          ))
        case _ =>
          val information = s"Checker: the workflow execution  has invalid state ${workflowExecution.lastStatus.state}"
          log.info(information.replace("  ", s" $id "))
          executionService.updateStatus(ExecutionStatusUpdate(
            id,
            ExecutionStatus(state = Failed, statusInfo = Some(information))
          ))
      }

      scheduledActions.filter(_._1 == id).foreach { task =>
        scheduledActions -= task
      }
    }

  }

  //scalastyle:on

  def checkForOrphanedWorkflows(): Unit = {
    for {
      executions <- executionService.findAllExecutions()
    } yield {
      val currentRunningWorkflows: Map[String, String] = fromExecutionsToMapMarathonIdExecutionId(executions)

      inconsistentStatusCheckerActor ! CheckConsistency(currentRunningWorkflows)
    }
  }


  def cleanOrphanedWorkflows(
                              runningButActuallyStopped: Map[String, String],
                              stoppedButActuallyRunning: Seq[String]
                            ): Unit = {
    // Kill all the workflows that are stored as RUNNING but that are actually STOPPED in Marathon.
    // Update the status with discrepancy as the StatusInfo and Stopped as Status
    val listNewStatuses = for {
      (nameWorkflowExecution, idExecution) <- runningButActuallyStopped
    } yield {
      val information = "Checker: there was a discrepancy between the monitored and the current status in Marathon" +
        " of the workflow. Therefore, the workflow was terminated."
      log.info(information.replace("  ", s" with id $idExecution and name $nameWorkflowExecution "))
      ExecutionStatusUpdate(
        idExecution,
        ExecutionStatus(state = Stopped, statusInfo = Some(information))
      )
    }

    listNewStatuses.foreach(newStatus => executionService.updateStatus(newStatus))

    // Kill all the workflow executions that are stored as STOPPED but that are actually RUNNING in Marathon.
    // Reuse the last Status because it might have failed just the previous DELETE Rest call for that appID
    // and overwriting the status could cause the loss of useful info
    val seqStoppedAsExecutionId = fromDCOSName2ExecutionId(stoppedButActuallyRunning)

    log.debug(s" Stopped but running: ${
      for {
        id <- seqStoppedAsExecutionId
      } yield s"Application stopped: $id"
    }")

    for {
      executions <- executionService.findAllExecutions()
    } yield {
      executions.foreach { execution =>
        execution.id.foreach { idExecution =>
          if (seqStoppedAsExecutionId.contains(idExecution)) {
            marathonStop(execution)
            log.info(s"The application with id $idExecution was marked ${execution.lastStatus.state} " +
              s"but it was running, therefore its termination was triggered")
          }
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

  case class CheckConsistency(runningInDatabase: Map[String, String]) extends Notification

  case class InconsistentStatuses(
                                   runningButActuallyStopped: Map[String, String],
                                   stoppedButActuallyRunning: Seq[String]
                                 ) extends Notification

  val stopStates = Seq(Stopped, Failed, Stopping)
  val finishedStates = Seq(Created, Finished, Failed)
  val notRunningStates: Seq[WorkflowStatusEnum.Value] = stopStates ++ finishedStates

  def fromDCOSName2ExecutionId(stoppedButActuallyRunning: Seq[String]): Seq[String] =
    stoppedButActuallyRunning.map { nameWorkflowDCOS =>
      val splittedNameDCOS = nameWorkflowDCOS.substring(nameWorkflowDCOS.indexOf("/home")).split("/")

      splittedNameDCOS.last
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