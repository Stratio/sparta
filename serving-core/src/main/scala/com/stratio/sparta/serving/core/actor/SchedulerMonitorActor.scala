/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{ActorRef, Cancellable, Props}
import com.stratio.sparta.sdk.enumerators.PhaseEnum
import com.stratio.sparta.sdk.models.WorkflowError
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap.option2NotBlankOption
import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.{ExecutionChange, ExecutionRemove}
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.{StatusChange, StatusRemove}
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.submit.SubmissionResponse
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.{ExecutionService, WorkflowStatusService}
import com.stratio.sparta.serving.core.utils.SchedulerUtils
import org.apache.curator.framework.CuratorFramework
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.joda.time.DateTime
import org.json4s.jackson.Serialization.read

import scala.concurrent._
import scala.io.Source
import scala.util.{Failure, Properties, Success, Try}

class SchedulerMonitorActor extends InMemoryServicesStatus with SchedulerUtils with SpartaSerializer {

  import SchedulerMonitorActor._

  val curatorFramework: CuratorFramework = getInstanceCurator
  def getInstanceCurator : CuratorFramework =  CuratorFactoryHolder.getInstance()
  val statusService = new WorkflowStatusService(curatorFramework)
  val executionService = new ExecutionService(curatorFramework)
  lazy val inconsistentStatusCheckerActor : ActorRef =
    context.system.actorOf(Props(new InconsistentStatusCheckerActor()), AkkaConstant.InconsistentStatusCheckerActorName)
  implicit lazy val currentInstanceName: Option[String] = instanceName

  override def persistenceId: String = AkkaConstant.InconsistentStatusCheckerActorName

  type SchedulerAction = WorkflowStatus => Unit

  private val onStatusChangeActions = scala.collection.mutable.Buffer[SchedulerAction]()
  private val invalidStateActions = scala.collection.mutable.Buffer[SchedulerAction]()
  private val invalidStartupStateActions = scala.collection.mutable.Buffer[SchedulerAction]()
  private val scheduledActions = scala.collection.mutable.Buffer[(String, Cancellable)]()
  private val propertyCheckInterval = "marathon.checkInterval"
  private val defaultTimeInterval = "30s"
  private val marathonApiUri = Properties.envOrNone("MARATHON_TIKI_TAKKA_MARATHON_URI").notBlank
  private lazy val checkTaskMarathon: Cancellable = { scheduleMsg(propertyCheckInterval,
    defaultTimeInterval,
    propertyCheckInterval,
    defaultTimeInterval,
    self,
    TriggerCheck)
  }

  override def preStart(): Unit = {

    //Trigger check only if run in Marathon
    if (marathonApiUri.isDefined) checkTaskMarathon

    context.system.eventStream.subscribe(self, classOf[StatusChange])
    context.system.eventStream.subscribe(self, classOf[StatusRemove])
    context.system.eventStream.subscribe(self, classOf[WorkflowRemove])
    context.system.eventStream.subscribe(self, classOf[WorkflowRawChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowRawRemove])
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
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRemove])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRawChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRawRemove])
    context.system.eventStream.unsubscribe(self, classOf[ExecutionChange])
    context.system.eventStream.unsubscribe(self, classOf[ExecutionRemove])

    scheduledActions.foreach { case (_, task) => if (!task.isCancelled) task.cancel() }

    if (marathonApiUri.isDefined) checkTaskMarathon.cancel()
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
    case request@WorkflowRemove(path, workflow) =>
      persist(request) { wRemove =>
        wRemove.workflow.id.foreach { id =>
          statuses.get(id).foreach { wStatus =>
            if(!finishedStates.contains(wStatus.status))
              executeActions(wStatus.copy(status = Failed), onStatusChangeActions)
          }
          removeWorkflowsWithEnv(wRemove.workflow)
        }
        checkSaveSnapshot()
      }
    case TriggerCheck =>
      checkForOrphanedWorkflows()
    case msg@InconsistentStatuses(_,_) =>
      cleanOrphanedWorkflows(msg.runningButActuallyStopped,msg.stoppedButActuallyRunning)
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
      executions.get(workflowStatus.id)
      match {
        case Some(execution) =>
          for {
            gEx <- execution.genericDataExecution
          } yield {
            if (gEx.executionMode == marathon && execution.marathonExecution.isDefined)
              marathonStop(workflowStatus, execution)
            else if (gEx.executionMode == dispatcher && execution.sparkDispatcherExecution.isDefined)
              dispatcherStop(workflowStatus, execution)
            else if (gEx.executionMode == local)
              localStop(workflowStatus)
          }
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
        stopStreamingContext()
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
            val wError = WorkflowError(
              error,
              PhaseEnum.Stop,
              e.toString,
              Try(e.getCause.getMessage).toOption.getOrElse(e.getMessage)
            )
            statusService.update(WorkflowStatus(
              id = workflowStatus.id,
              status = Failed,
              statusInfo = Some(error)
            ))
            executionService.setLastError(workflowStatus.id, wError)
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
    if (workflowStatus.status == Stopping || workflowStatus.status == Failed) {
      Try(executionService.setEndDate(workflowStatus.id, new DateTime()))
        .getOrElse(log.warn(s"Impossible to update endDate in execution id: ${workflowStatus.id}"))
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
        new MarathonService(context).kill(execution.marathonExecution.get.marathonId)
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
            val wError = WorkflowError(
              error,
              PhaseEnum.Stop,
              e.toString,
              Try(e.getCause.getMessage).toOption.getOrElse(e.getMessage)
            )
            statusService.update(WorkflowStatus(
              id = workflowStatus.id,
              status = Failed,
              statusInfo = Some(error)
            ))
            executionService.setLastError(workflowStatus.id, wError)
          }
      }
      Try(executionService.setEndDate(execution.id, new DateTime()))
        .getOrElse(log.warn(s"Impossible to update endDate in execution id: ${execution.id}"))
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
          val wError = submissionResponse.message.map(error => WorkflowError(information, PhaseEnum.Stop, error, error))
          statusService.update(WorkflowStatus(
            id = workflowStatus.id,
            status = Failed,
            statusInfo = Some(information)
          ))
          wError.foreach(error => executionService.setLastError(workflowStatus.id, error))
        case Failure(e) =>
          val error = "Impossible to parse submission killing response"
          log.error(error, e)
          val wError = WorkflowError(
            error,
            PhaseEnum.Stop,
            e.toString,
            Try(e.getCause.getMessage).toOption.getOrElse(e.getMessage)
          )
          statusService.update(WorkflowStatus(
            id = workflowStatus.id,
            status = Failed,
            statusInfo = Some(error)
          ))
          executionService.setLastError(workflowStatus.id, wError)
      }
      Try(executionService.setEndDate(execution.id, new DateTime()))
        .getOrElse(log.warn(s"Impossible to update endDate in execution id: ${execution.id}"))
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


  def checkForOrphanedWorkflows(): Unit = {

    val currentRunningWorkflows: Map[String, String] = fromStatusesToMapWorkflowNameAndId(statuses, executions, workflowsRaw)

    inconsistentStatusCheckerActor ! CheckConsistency(currentRunningWorkflows)
  }


  def cleanOrphanedWorkflows(runningButActuallyStopped: Map[String, String], stoppedButActuallyRunning: Seq[String]): Unit = {
    // Kill all the workflows that are stored as RUNNING but that are actually STOPPED in Marathon.
    // Update the status with discrepancy as the StatusInfo and Stopped as Status
    val listNewStatuses =
    for {
      (nameWorkflow, idWorkflow) <- runningButActuallyStopped
    } yield {
      val information = "Checker: there was a discrepancy between the monitored and the current status in Marathon of the workflow. Therefore, the workflow was terminated."
      log.info(information.replace("  ", s" with id $idWorkflow and name $nameWorkflow "))
      WorkflowStatus(id = idWorkflow, status = Stopped, statusInfo = Some(information))
    }

    listNewStatuses.foreach(newStatus => statusService.update(newStatus))


    // Kill all the workflows that are stored as STOPPED but that are actually RUNNING in Marathon.
    // Reuse the last Status because it might have failed just the previous DELETE Rest call for that appID
    // and overwriting the status could cause the loss of useful info

    val mapStoppedAsNameAndGroup = fromDCOSName2NameAndTupleGroupVersion(stoppedButActuallyRunning)

    log.debug(s" Stopped but running: ${ for ( (id,(value, version)) <- mapStoppedAsNameAndGroup) yield "Application stopped: " + id + " with group " + value + " and version " + version}")

    val listIDsToStop: Seq[String] = {
      for {
        (idWorkflow, rawWorkflow) <- workflowsRaw
      } yield {
        if (mapStoppedAsNameAndGroup.contains(rawWorkflow.name) &&
          mapStoppedAsNameAndGroup(rawWorkflow.name)._1 == rawWorkflow.group.name &&
          mapStoppedAsNameAndGroup(rawWorkflow.name)._2 == rawWorkflow.version)
          Some(idWorkflow)
        else None
      }
    }.flatten.toSeq

    listIDsToStop.foreach {
      idWorkflow =>
        for {
          status <- statuses.get(idWorkflow)
          execution <- executions.get(idWorkflow)
        } yield {
          marathonStop(status, execution)
          log.info(s"The application with id $idWorkflow was marked ${status.status} but it was running, therefore its termination was triggered")
        }
    }
  }
}


object SchedulerMonitorActor{

  trait Notification

  object TriggerCheck extends Notification

  object RetrieveStatuses extends Notification

  object RetrieveWorkflowsEnv extends Notification

  case class CheckConsistency(runningInZookeeper: Map[String, String]) extends Notification

  case class InconsistentStatuses(runningButActuallyStopped: Map[String, String], stoppedButActuallyRunning: Seq[String]) extends Notification

  val stopStates = Seq(Stopped, Failed, Stopping)
  val finishedStates = Seq(Created, Finished, Failed)
  val notRunningStates: Seq[WorkflowStatusEnum.Value] = stopStates ++ finishedStates


  def fromDCOSName2NameAndTupleGroupVersion(stoppedButActuallyRunning : Seq[String]) : Map[String, (String,Long)] =
    stoppedButActuallyRunning.map { nameWorkflowDCOS =>
      val splittedNameDCOS = nameWorkflowDCOS.substring(nameWorkflowDCOS.indexOf("/home")).split("/")
      val nameAndVersion = splittedNameDCOS.last
      val actualName = nameAndVersion.replaceAll("(-v[0-9]*)", "")
      // We want to cut out the -v and the name from the string to retrieve the version
      val regexVersion = "(?<=-v)[0-9]*".r
      val version = regexVersion.findFirstIn(nameAndVersion).fold(0L){_.toLong}
      // The last two items of the array will contain [nameWorkflow, nameWorkflow-v] so they don't belong to the group name
      val group = s"${splittedNameDCOS.slice(0, splittedNameDCOS.size - 2).mkString("/")}"
      (actualName, (group, version))
    }.toMap

  def fromStatusesToMapWorkflowNameAndId(statuses: scala.collection.mutable.Map[String, WorkflowStatus],
                                         executions: scala.collection.mutable.Map[String, WorkflowExecution],
                                         workflows: scala.collection.mutable.Map[String, Workflow])(implicit instanceName: Option[String]): Map[String, String] = {

   val filteredExecutions = executions.filter{ executions =>
     executions._2.genericDataExecution.fold (false) { ge => ge.executionMode == marathon}
   }.keys.toSeq

    statuses.filter { status =>
      !notRunningStates.contains(status._2.status) && filteredExecutions.contains(status._1)
    }.keys.toSeq.flatMap { id =>
      workflows.get(id) match {
        case Some(workflow) =>
          val name = s"/sparta/${instanceName.get}/workflows${workflow.group.name}/${workflow.name}/${workflow.name}-v${workflow.version}"
          Some(name, id)
        case None => None
      }
    }.toMap
  }
}