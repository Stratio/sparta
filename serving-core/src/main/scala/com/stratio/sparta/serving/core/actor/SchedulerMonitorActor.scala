/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.Cancellable
import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.{ExecutionChange, ExecutionRemove}
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.{StatusChange, StatusRemove}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.submit.SubmissionResponse
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.WorkflowStatusService
import com.stratio.sparta.serving.core.utils.SchedulerUtils
import org.apache.curator.framework.CuratorFramework
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.json4s.jackson.Serialization.read

import scala.concurrent._
import scala.io.Source
import scala.util.{Failure, Success, Try}

class SchedulerMonitorActor extends InMemoryServicesStatus with SchedulerUtils with SpartaSerializer {

  val curatorFramework: CuratorFramework = CuratorFactoryHolder.getInstance()
  val statusService = new WorkflowStatusService(curatorFramework)
  val stopStates = Seq(Stopped, Failed, Stopping)

  override def persistenceId: String = AkkaConstant.StatusChangeActorName

  type SchedulerAction = WorkflowStatus => Unit

  private val onStatusChangeActions = scala.collection.mutable.Buffer[SchedulerAction]()
  private val invalidStateActions = scala.collection.mutable.Buffer[SchedulerAction]()
  private val invalidStartupStateActions = scala.collection.mutable.Buffer[SchedulerAction]()
  private val scheduledActions = scala.collection.mutable.Buffer[(String, Cancellable)]()

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[StatusChange])
    context.system.eventStream.subscribe(self, classOf[StatusRemove])
    context.system.eventStream.subscribe(self, classOf[ExecutionChange])
    context.system.eventStream.subscribe(self, classOf[ExecutionRemove])

    onStatusChangeActions += manageStopAction
    onStatusChangeActions += manageRunningAction
    invalidStateActions += manageInvalidStateAction
    invalidStartupStateActions += manageStartupStateAction
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[StatusChange])
    context.system.eventStream.unsubscribe(self, classOf[StatusRemove])
    context.system.eventStream.unsubscribe(self, classOf[ExecutionChange])
    context.system.eventStream.unsubscribe(self, classOf[ExecutionRemove])

    scheduledActions.foreach { case (_, task) => if (!task.isCancelled) task.cancel() }
  }

  val receiveCommand: Receive = managementReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  def managementReceive: Receive = {
    case request@StatusChange(path, workflowStatus) =>
      persist(request) { stChange =>
        val cachedStatus = statuses.get(stChange.workflowStatus.id)
        if (cachedStatus.isDefined && cachedStatus.get.status != stChange.workflowStatus.status) {
          executeActions(stChange.workflowStatus, onStatusChangeActions)
        } else if (cachedStatus.isEmpty) {
          executeActions(stChange.workflowStatus, invalidStartupStateActions)
        }
        executeActions(stChange.workflowStatus, invalidStateActions)
        addStatus(stChange.workflowStatus)
        checkSaveSnapshot()
      }
  }

  def executeActions(
                      workflowStatus: WorkflowStatus,
                      actions: scala.collection.mutable.Buffer[SchedulerAction]
                    ): Unit = {
    actions.foreach { callback =>
      Future {
        try {
          blocking(callback(workflowStatus))
        } catch {
          case e: Exception => log.error(s"Error executing action for workflow status ${workflowStatus.id}." +
            s" With exception: ${e.getLocalizedMessage}")
        }
      }(context.dispatcher)
    }
  }

  val manageStopAction: WorkflowStatus => Unit = (workflowStatus: WorkflowStatus) => {
    if (stopStates.contains(workflowStatus.status)) {
      (workflowStatus.lastExecutionMode, executions.get(workflowStatus.id))
      match {
        case (Some(lastExecutionMode), Some(execution))
          if lastExecutionMode == ConfigMarathon && execution.marathonExecution.isDefined =>
          marathonStop(workflowStatus, execution)
        case (Some(lastExecutionMode), Some(execution))
          if lastExecutionMode == ConfigMesos &&
            execution.sparkDispatcherExecution.isDefined &&
            execution.sparkExecution.isDefined =>
          dispatcherStop(workflowStatus, execution)
        case (Some(lastExecutionMode), _) if lastExecutionMode == ConfigLocal =>
          localStop(workflowStatus)
        case _ =>
          log.debug("Stop message received and any action will be executed")
      }
    }
  }

  val manageRunningAction: WorkflowStatus => Unit = (workflowStatus: WorkflowStatus) => {
    if (workflowStatus.status == Uploaded ||
      workflowStatus.status == Launched &&
        !scheduledActions.exists(task => task._1 == workflowStatus.id)) {
      val task = scheduleOneTask(AwaitWorkflowChangeStatus, DefaultAwaitWorkflowChangeStatus)(
        checkStatus(workflowStatus.id))
      scheduledActions += (workflowStatus.id -> task)
    }
  }

  val manageInvalidStateAction: WorkflowStatus => Unit = (workflowStatus: WorkflowStatus) => {
    if (workflowStatus.status == Starting && !scheduledActions.exists(task => task._1 == workflowStatus.id)) {
      val task = scheduleOneTask(AwaitWorkflowChangeStatus, DefaultAwaitWorkflowChangeStatus)(
        checkStatus(workflowStatus.id))
      scheduledActions += (workflowStatus.id -> task)
    }
  }

  val manageStartupStateAction: WorkflowStatus => Unit = (workflowStatus: WorkflowStatus) => {
    if (workflowStatus.status == Uploaded || workflowStatus.status == Launched || workflowStatus.status == NotStarted &&
      !scheduledActions.exists(task => task._1 == workflowStatus.id)) {
      val task = scheduleOneTask(AwaitWorkflowChangeStatus, DefaultAwaitWorkflowChangeStatus)(
        checkStatus(workflowStatus.id))
      scheduledActions += (workflowStatus.id -> task)
    }
  }

  def localStop(workflowStatus: WorkflowStatus): Unit = {
    if (workflowStatus.status == Stopping) {
      log.info("Stop message received from Zookeeper")
      scheduledActions.filter(_._1 == workflowStatus.id).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      Try {
        stopSparkContext()
      } match {
        case Success(_) =>
          val information = "The workflow was successfully stopped"
          val newStatus = Stopped
          val newInformation = Option(information)
          log.info(information)
          statusService.update(WorkflowStatus(
            id = workflowStatus.id,
            status = newStatus,
            statusInfo = newInformation
          ))
        case Failure(e) =>
          val error = "An error was encountered while stopping contexts"
          log.error(error, e)
          if (workflowStatus.status != Failed) {
            statusService.update(WorkflowStatus(
              id = workflowStatus.id,
              status = Failed,
              statusInfo = Some(error),
              lastError = Option(WorkflowError(error, PhaseEnum.Stop, e.toString))
            ))
          }
      }
    }
    if (workflowStatus.status == Stopped || workflowStatus.status == Failed) {
      scheduledActions.filter(_._1 == workflowStatus.id).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      val newStatus = if (workflowStatus.status == Failed) NotDefined else Finished
      statusService.update(WorkflowStatus(
        id = workflowStatus.id,
        status = newStatus
      ))
    }
  }

  def marathonStop(workflowStatus: WorkflowStatus, execution: WorkflowExecution): Unit = {
    if (workflowStatus.status == Stopped || workflowStatus.status == Failed) {
      log.info("Stop message received from Zookeeper")
      scheduledActions.filter(_._1 == workflowStatus.id).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      log.info(s"Finishing workflow with Marathon API")
      Try {
        new MarathonService(context, curatorFramework).kill(execution.marathonExecution.get.marathonId)
      } match {
        case Success(_) =>
          val information = s"Workflow correctly finished in Marathon API"
          val newStatus = if (workflowStatus.status == Failed) NotDefined else Finished
          val newInformation = if (workflowStatus.status == Failed) None
          else Option(information)
          log.info(information)
          statusService.update(WorkflowStatus(
            id = workflowStatus.id,
            status = newStatus,
            statusInfo = newInformation
          ))
        case Failure(e) =>
          val error = "An error was encountered while sending a stop message to Marathon API"
          log.error(error, e)
          if (workflowStatus.status != Failed) {
            statusService.update(WorkflowStatus(
              id = workflowStatus.id,
              status = Failed,
              statusInfo = Some(error),
              lastError = Option(WorkflowError(error, PhaseEnum.Stop, e.toString))
            ))
          }
      }
    }
  }

  def dispatcherStop(workflowStatus: WorkflowStatus, execution: WorkflowExecution): Unit = {
    if (workflowStatus.status == Stopping || workflowStatus.status == Failed) {
      log.info("Stop message received from Zookeeper")
      scheduledActions.filter(_._1 == workflowStatus.id).foreach { task =>
        if (!task._2.isCancelled) task._2.cancel()
        scheduledActions -= task
      }
      log.info(s"Finishing workflow with Dispatcher API")
      Try {
        val urlWithAppId = s"${execution.sparkDispatcherExecution.get.killUrl}/" +
          s"${execution.sparkExecution.get.applicationId}"
        log.info(s"Killing application (${execution.sparkExecution.get.applicationId}) " +
          s"with Spark Dispatcher Submissions API in url: $urlWithAppId")
        val post = new HttpPost(urlWithAppId)
        val postResponse = HttpClientBuilder.create().build().execute(post)

        read[SubmissionResponse](Source.fromInputStream(postResponse.getEntity.getContent).mkString)
      } match {
        case Success(submissionResponse) if submissionResponse.success =>
          val information = s"Workflow killed correctly with Spark API"
          val newStatus = if (workflowStatus.status == Failed) NotDefined else Killed
          log.info(information)
          statusService.update(WorkflowStatus(
            id = workflowStatus.id,
            status = newStatus,
            statusInfo = Some(information)
          ))
        case Success(submissionResponse) =>
          log.debug(s"Failed response: $submissionResponse")
          val information = s"Error while stopping task"
          log.info(information)
          statusService.update(WorkflowStatus(
            id = workflowStatus.id,
            status = Failed,
            statusInfo = Some(information),
            lastError = submissionResponse.message.map(error => WorkflowError(information, PhaseEnum.Stop, error))
          ))
        case Failure(e) =>
          val error = "Impossible to parse submission killing response"
          log.error(error, e)
          statusService.update(WorkflowStatus(
            id = workflowStatus.id,
            status = Failed,
            statusInfo = Some(error),
            lastError = Option(WorkflowError(error, PhaseEnum.Stop, e.toString))
          ))
      }
    }
  }

  //scalastyle:off
  def checkStatus(id: String): Unit = {
    val workflowStatus = statuses.get(id)

    if (workflowStatus.isDefined) {
      val wrongStartStates = Seq(Launched, Starting, Uploaded, NotStarted, Created)
      val validStartStates = Seq(Started)
      val wrongStopStates = Seq(Stopping)
      val validStopStates = Seq(Stopped, Failed, Killed, Finished)

      workflowStatus.get.status match {
        case status if wrongStartStates.contains(status) =>
          val information = s"Checker: the workflow  did not start correctly after maximum deployment time"
          log.warn(information.replace("  ", s" $id "))
          statusService.update(WorkflowStatus(id = id, status = Failed, statusInfo = Some(information)))
        case status if wrongStopStates.contains(status) =>
          val information = s"Checker: the workflow  did not stop correctly after maximum deployment time"
          log.warn(information.replace("  ", s" $id "))
          statusService.update(WorkflowStatus(id = id, status = Failed, statusInfo = Some(information)))
        case status if validStartStates.contains(status) =>
          val information = s"Checker: the workflow  started correctly"
          log.info(information.replace("  ", s" $id "))
          statusService.update(WorkflowStatus(id = id, status = NotDefined, statusInfo = Some(information)))
        case status if validStopStates.contains(status) =>
          val information = s"Checker: the workflow  stopped correctly"
          log.info(information.replace("  ", s" $id "))
          statusService.update(WorkflowStatus(id = id, status = NotDefined, statusInfo = Some(information)))
        case _ =>
          val information = s"Checker: the workflow  has invalid state ${workflowStatus.get.status}"
          log.info(information.replace("  ", s" $id "))
          statusService.update(WorkflowStatus(id = id, status = Failed, statusInfo = Some(information)))
      }
    } else log.info(s"Unable to find status $id in checker")

    scheduledActions.filter(_._1 == id).foreach { task =>
      scheduledActions -= task
    }
  }

}
