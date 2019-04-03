/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, ActorRef, Cancellable}
import akka.cluster.Cluster
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.AggregationTimeHelper
import com.stratio.sparta.serving.api.actor.ScheduledWorkflowTaskExecutorActor._
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.enumerators.ScheduledActionType._
import com.stratio.sparta.serving.core.models.enumerators.ScheduledTaskType._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.enumerators.{ScheduledTaskState, ScheduledTaskType}
import com.stratio.sparta.serving.core.models.orchestrator.ScheduledWorkflowTask
import com.stratio.sparta.serving.core.models.workflow.WorkflowIdExecutionContext
import com.stratio.sparta.serving.core.utils.SpartaClusterUtils

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

//scalastyle:off
class ScheduledWorkflowTaskExecutorActor(launcherActor: ActorRef) extends Actor with SLF4JLogging with SpartaClusterUtils {

  implicit val executionContext: ExecutionContext = context.dispatcher
  val executionPgService = PostgresDaoFactory.executionPgService
  val scheduledWorkflowTaskPgService = PostgresDaoFactory.scheduledWorkflowTaskPgService

  val checkActionsDelay = Try(SpartaConfig.getSpartaConfig().get.getInt("workflow.scheduler.delay")).getOrElse(10)

  val cluster = Cluster(context.system)

  val checkActionsToExecute: Cancellable = context.system.scheduler.schedule(checkActionsDelay seconds, checkActionsDelay seconds, self, CheckActionsToExecute)

  val scheduledActions = scala.collection.mutable.Map.empty[String, WorkflowAction]

  override def preStart(): Unit = {
    self ! CheckActionsToExecute
  }

  override def postStop(): Unit = {
    checkActionsToExecute.cancel()
    cancelAndClearAllActions()
    super.postStop()
  }

  def cancelAndClearAllActions(): Unit = {
    scheduledActions.foreach { case (_, workflowAction) => cancelAndRemoveTask(workflowAction.scheduledWorkflowTask) }
    scheduledActions.clear()
  }

  override def receive: Receive = {
    case CheckActionsToExecute =>
      if (isThisNodeClusterLeader(cluster)) {
        val activeWorkflowTasksInDb = getActiveActionsToExecuteInDb

        stopActions(activeWorkflowTasksInDb).onComplete {
          case Success(newActionsExecuted) =>
            if(newActionsExecuted.nonEmpty)
              log.info(s"Stopped scheduled actions in workflow scheduler: ${newActionsExecuted.mkString(",")}")
          case Failure(ex) =>
            log.error(ex.getLocalizedMessage, ex)
        }
        executeActions(activeWorkflowTasksInDb).onComplete {
          case Success(newActionsExecuted) =>
            if(newActionsExecuted.nonEmpty)
              log.info(s"Scheduled new actions in workflow scheduler: ${newActionsExecuted.mkString(",")}")
          case Failure(ex) =>
            log.error(ex.getLocalizedMessage, ex)
        }
      } else cancelAndClearAllActions()
    case RunWorkflowAction(actionId, taskType, workflowIdExecutionContext, userId) =>
      if (isThisNodeClusterLeader(cluster)) {
        scheduledWorkflowTaskPgService.setStateScheduledWorkflowTask(actionId, ScheduledTaskState.EXECUTED)
        launcherActor ! Launch(workflowIdExecutionContext, userId)
      } else cancelAndClearAllActions()
    case StopExecutionAction(actionId, taskType, executionId) =>
      if (isThisNodeClusterLeader(cluster)) {
        scheduledWorkflowTaskPgService.setStateScheduledWorkflowTask(actionId, ScheduledTaskState.EXECUTED)
        executionPgService.stopExecution(executionId)
      } else cancelAndClearAllActions()
  }

  def executeActions(activeWorkflowTasksInDb: Future[Seq[ScheduledWorkflowTask]]): Future[Seq[String]] = {
    for {
      activeTasksInDb <- activeWorkflowTasksInDb
      workflowIdsRunning <- getWorkflowsRunning
    } yield {
      activeTasksInDb.flatMap { activeTask =>
        val activeAndRunningTask = scheduledActions.exists { case (_, workflowAction) =>
          activeTask.id == workflowAction.scheduledWorkflowTask.id
        }

        if (!activeAndRunningTask) {
          activeTask.taskType match {
            case PERIODICAL =>
              executeTask(activeTask)
            case ONE_TIME =>
              if (activeTask.state != ScheduledTaskState.EXECUTED)
                executeTask(activeTask)
              else None
            case UNIQUE_PERIODICAL =>
              if (!workflowIdsRunning.contains(activeTask.entityId))
                executeTask(activeTask)
              else None
          }
        } else None
      }
    }
  }

  def executeTask(scheduledWorkflowTask: ScheduledWorkflowTask): Option[String] = {
    val period = Try(AggregationTimeHelper.parseValueToMilliSeconds(scheduledWorkflowTask.duration.get)).toOption
    val delay = {
      val initTime = scheduledWorkflowTask.initDate - System.currentTimeMillis()
      if(initTime < 0)
        0
      else initTime
    }

    scheduledWorkflowTask.taskType match {
      case PERIODICAL | UNIQUE_PERIODICAL if period.isDefined && delay >= 0 && period.get >= 0 =>
        executePeriodicalAction(scheduledWorkflowTask, delay, period.get)
      case ONE_TIME if delay >= 0 =>
        executeOneTimeAction(scheduledWorkflowTask, delay)
      case _ =>
        log.warn(s"Impossible to execute scheduled workflow action, check input options: $scheduledWorkflowTask")
        None
    }
  }

  def executePeriodicalAction(scheduledWorkflowTask: ScheduledWorkflowTask, delay: Long, period: Long): Option[String] = {
    scheduledWorkflowTask.actionType match {
      case RUN if scheduledWorkflowTask.executionContext.isDefined =>
        val workflowIdExecutionContext = WorkflowIdExecutionContext(
          workflowId = scheduledWorkflowTask.entityId,
          executionContext = scheduledWorkflowTask.executionContext.get
        )
        val action = RunWorkflowAction(scheduledWorkflowTask.id, scheduledWorkflowTask.taskType, workflowIdExecutionContext, scheduledWorkflowTask.loggedUser)
        val cancellableTask = context.system.scheduler.schedule(delay millis, period millis, self, action)
        scheduledActions += (scheduledWorkflowTask.id -> WorkflowAction(scheduledWorkflowTask, cancellableTask))
        Option(scheduledWorkflowTask.id)
      case STOP =>
        val action = StopExecutionAction(scheduledWorkflowTask.id, scheduledWorkflowTask.taskType, scheduledWorkflowTask.id)
        val cancellableTask = context.system.scheduler.schedule(delay millis, period millis, self, action)
        scheduledActions += (scheduledWorkflowTask.id -> WorkflowAction(scheduledWorkflowTask, cancellableTask))
        Option(scheduledWorkflowTask.id)
      case _ =>
        log.warn(s"Impossible to execute periodical workflow action, check input options: $scheduledWorkflowTask")
        None
    }
  }

  def executeOneTimeAction(scheduledWorkflowTask: ScheduledWorkflowTask, delay: Long): Option[String] = {
    scheduledWorkflowTask.actionType match {
      case RUN if scheduledWorkflowTask.executionContext.isDefined =>
        val workflowIdExecutionContext = WorkflowIdExecutionContext(
          workflowId = scheduledWorkflowTask.entityId,
          executionContext = scheduledWorkflowTask.executionContext.get
        )
        val action = RunWorkflowAction(scheduledWorkflowTask.id, scheduledWorkflowTask.taskType, workflowIdExecutionContext, scheduledWorkflowTask.loggedUser)
        val cancellableTask = context.system.scheduler.scheduleOnce(delay millis, self, action)
        scheduledActions += (scheduledWorkflowTask.id -> WorkflowAction(scheduledWorkflowTask, cancellableTask))
        Option(scheduledWorkflowTask.id)
      case STOP =>
        val action = StopExecutionAction(scheduledWorkflowTask.id, scheduledWorkflowTask.taskType, scheduledWorkflowTask.id)
        val cancellableTask = context.system.scheduler.scheduleOnce(delay millis, self, action)
        scheduledActions += (scheduledWorkflowTask.id -> WorkflowAction(scheduledWorkflowTask, cancellableTask))
        Option(scheduledWorkflowTask.id)
      case _ =>
        log.warn(s"Impossible to execute one time workflow action, check input options: $scheduledWorkflowTask")
        None
    }
  }

  def stopActions(activeWorkflowTasksInDb: Future[Seq[ScheduledWorkflowTask]]): Future[Seq[String]] = {
    for {
      activeTasksInDb <- activeWorkflowTasksInDb
    } yield {
      val notActiveButRunningTasks = scheduledActions.filter { case (_, workflowAction) =>
        !activeTasksInDb.exists(activeWorkflowTaskInDb =>
          activeWorkflowTaskInDb.id == workflowAction.scheduledWorkflowTask.id)
      }.toMap
      notActiveButRunningTasks.map { case (_, workflowAction) => cancelAndRemoveTask(workflowAction.scheduledWorkflowTask) }.toSeq
    }
  }

  def cancelAndRemoveTask(scheduledWorkflowTask: ScheduledWorkflowTask): String = {
    scheduledActions.get(scheduledWorkflowTask.id).foreach { workflowAction =>
      if (!workflowAction.task.isCancelled)
        workflowAction.task.cancel()
      scheduledActions -= scheduledWorkflowTask.id
    }
    scheduledWorkflowTask.id
  }

  def getWorkflowsRunning: Future[Seq[String]] = {
    val runningStates = Seq(Launched, Starting, Started, Uploaded)
    executionPgService.findExecutionsByStatus(runningStates).map { executions =>
      executions.map(_.getWorkflowToExecute.id.get)
    }
  }

  def getActiveActionsToExecuteInDb: Future[Seq[ScheduledWorkflowTask]] = {
    scheduledWorkflowTaskPgService.filterScheduledWorkflowTaskByActive(active = true)
  }

}

object ScheduledWorkflowTaskExecutorActor {

  case object CheckActionsToExecute

  case class WorkflowAction(scheduledWorkflowTask: ScheduledWorkflowTask, task: Cancellable)

  case class RunWorkflowAction(actionId: String, taskType: ScheduledTaskType, workflowIdExecutionContext: WorkflowIdExecutionContext, loggedUser: Option[LoggedUser])

  case class StopExecutionAction(actionId: String, taskType: ScheduledTaskType, executionId: String)

}

