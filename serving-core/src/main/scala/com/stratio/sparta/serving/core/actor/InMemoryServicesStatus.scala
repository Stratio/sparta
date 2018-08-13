/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.event.slf4j.SLF4JLogging
import akka.persistence._
import akka.serialization.SerializationExtension
import com.stratio.sparta.core.models.{ResultStep, WorkflowError}
import com.stratio.sparta.serving.core.actor.DebugStepDataPublisherActor.{DebugStepDataChange, DebugStepDataRemove}
import com.stratio.sparta.serving.core.actor.DebugStepErrorPublisherActor.{DebugStepErrorChange, DebugStepErrorRemove}
import com.stratio.sparta.serving.core.actor.DebugWorkflowPublisherActor.{DebugWorkflowChange, DebugWorkflowRemove}
import com.stratio.sparta.serving.core.actor.EnvironmentPublisherActor.{EnvironmentChange, EnvironmentRemove}
import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.{ExecutionChange, ExecutionRemove}
import com.stratio.sparta.serving.core.actor.GroupPublisherActor.{GroupChange, GroupRemove}
import com.stratio.sparta.serving.core.actor.ParameterListPublisherActor.{ParameterListChange, ParameterListRemove}
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.{StatusChange, StatusRemove}
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor._
import com.stratio.sparta.serving.core.models.env.Environment
import com.stratio.sparta.serving.core.models.parameters.ParameterList
import com.stratio.sparta.serving.core.models.workflow._


trait InMemoryServicesStatus extends PersistentActor with SLF4JLogging {

  var workflows = scala.collection.mutable.Map[String, Workflow]()
  var statuses = scala.collection.mutable.Map[String, WorkflowStatus]()
  var executions = scala.collection.mutable.Map[String, WorkflowExecution]()
  var groups = scala.collection.mutable.Map[String, Group]()
  var debugWorkflows = scala.collection.mutable.Map[String, DebugWorkflow]()
  var debugStepData = scala.collection.mutable.Map[String, ResultStep]()
  var debugStepError = scala.collection.mutable.Map[String, WorkflowError]()
  var parameterLists = scala.collection.mutable.Map[String, ParameterList]()
  var environment : Option[Environment] = None
  var environmentState = scala.collection.mutable.Map[String, String]()
  val snapShotInterval = 1000
  val serialization = SerializationExtension(context.system)
  val serializer = serialization.findSerializerFor(SnapshotState(
    workflows,
    statuses,
    executions,
    groups,
    debugWorkflows,
    debugStepData,
    debugStepError,
    parameterLists,
    environmentState,
    environment
  ))

  def addWorkflow(workflow: Workflow): Unit =
    workflow.id.foreach(id => workflows += (id -> workflow))

  def addDebugWorkflow(debugWorkflow: DebugWorkflow): Unit =
    debugWorkflow.workflowOriginal.id.foreach(id => debugWorkflows += (id -> debugWorkflow))

  def addDebugStepData(path: String, data: ResultStep): Unit =
    debugStepData += (path -> data)

  def addDebugStepError(path: String, error: WorkflowError): Unit =
    debugStepError += (path -> error)

  def addStatus(status: WorkflowStatus): Unit =
    statuses += (status.id -> status)

  def addExecution(execution: WorkflowExecution): Unit =
    executions += (execution.id -> execution)

  def addParameterList(parameterList: ParameterList): Unit =
    parameterLists += (parameterList.name -> parameterList)

  def addEnvironment(newEnvironment: Environment): Unit = {
    environmentState.clear()
    environment = Option(newEnvironment)
    newEnvironment.variables.foreach(variable => environmentState += (variable.name -> variable.value))
  }

  def removeStatus(id: String): Unit =
    statuses -= id

  def removeExecution(id: String): Unit =
    executions -= id

  def removeParameterList(parameterList: ParameterList): Unit =
    parameterLists -= parameterList.name

  def removeEnvironment(oldEnvironment: Environment): Unit = {
    environment = None
    environmentState.clear()
  }

  def addGroup(group: Group): Unit =
    group.id.foreach(id => groups += (id -> group))

  def removeGroup(group: Group): Unit =
    group.id.foreach(id => groups -= id)

  def removeWorkflows(workflow: Workflow): Unit =
    workflow.id.foreach(id => workflows -= id)

  def removeDebugWorkflow(debugWorkflow: DebugWorkflow): Unit =
    debugWorkflow.workflowOriginal.id.foreach(id => debugWorkflows -= id)

  def removeDebugStepData(path: String): Unit =
    debugStepData -= path

  def removeDebugStepError(path: String): Unit =
    debugStepError -= path


  val receiveRecover: Receive = eventsReceive.orElse(snapshotRecover).orElse(recoverComplete)

  //scalastyle:off
  def eventsRecover: Receive = {
    case StatusChange(_, status) => addStatus(status)
    case StatusRemove(_, status) => removeStatus(status.id)
    case WorkflowChange(_, workflow) => addWorkflow(workflow)
    case GroupChange(_, group) => addGroup(group)
    case ExecutionChange(_, execution) => addExecution(execution)
    case ParameterListChange(_, parameterList) => addParameterList(parameterList)
    case EnvironmentChange(_, newEnvironment) => addEnvironment(newEnvironment)
    case EnvironmentRemove(_, oldEnvironment) => removeEnvironment(oldEnvironment)
    case ExecutionRemove(_, execution) => removeExecution(execution.id)
    case WorkflowRemove(_, workflow) => removeWorkflows(workflow)
    case GroupRemove(_, group) => removeGroup(group)
    case ParameterListRemove(_, parameterList) => removeParameterList(parameterList)
    case DebugWorkflowChange(_, debugWorkflow) => addDebugWorkflow(debugWorkflow)
    case DebugStepDataChange(path, debugData) => addDebugStepData(path, debugData)
    case DebugStepErrorChange(path, debugError) => addDebugStepError(path, debugError)
    case DebugWorkflowRemove(_, debugWorkflow) => removeDebugWorkflow(debugWorkflow)
    case DebugStepDataRemove(path, _) => removeDebugStepData(path)
    case DebugStepErrorRemove(path, _) => removeDebugStepError(path)
  }

  def snapshotRecover: Receive = {
    case SnapshotOffer(_, snapshotJson: Array[Byte]) =>
      val snapshot = serializer.fromBinary(snapshotJson).asInstanceOf[SnapshotState]

      workflows = snapshot.workflows
      statuses = snapshot.statuses
      executions = snapshot.executions
      groups = snapshot.groups
      debugWorkflows = snapshot.debugWorkflows
      debugStepData = snapshot.debugStepData
      debugStepError = snapshot.debugStepError
      parameterLists = snapshot.parameterLists
      environmentState = snapshot.environmentState
      environment = snapshot.environment
  }

  def recoverComplete: Receive = {
    case RecoveryCompleted =>
      log.info(s"Recovery complete for Actor id: $persistenceId")
  }

  def snapshotSaveNotificationReceive: Receive = {
    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"Snapshot failed to save: Metadata -> $metadata. Reason -> ${reason.toString}")

    case SaveSnapshotSuccess(metadata) =>
      log.info(s"Snapshot saved successfully: Metadata -> $metadata")
  }

  def checkSaveSnapshot(): Unit = {
    if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) {
      val bytes = serializer.toBinary(SnapshotState(
        workflows,
        statuses,
        executions,
        groups,
        debugWorkflows,
        debugStepData,
        debugStepError,
        parameterLists,
        environmentState,
        environment
      ))
      saveSnapshot(bytes)
    }
  }

  def eventsReceive: Receive = {
    case request@StatusChange(_, workflowStatus) =>
      persist(request) { _ =>
        addStatus(workflowStatus)
        checkSaveSnapshot()
      }
    case request@StatusRemove(_, workflowStatus) =>
      persist(request) { _ =>
        removeStatus(workflowStatus.id)
        checkSaveSnapshot()
      }
    case request@WorkflowChange(_, workflow) =>
      persist(request) { _ =>
        addWorkflow(workflow)
        checkSaveSnapshot()
      }
    case request@GroupChange(_, group) =>
      persist(request) { _ =>
        addGroup(group)
        checkSaveSnapshot()
      }
    case request@ParameterListChange(_, parameterList) =>
      persist(request) { _ =>
        addParameterList(parameterList)
        checkSaveSnapshot()
      }
    case request@EnvironmentChange(_, newEnvironment) =>
      persist(request) { _ =>
        addEnvironment(newEnvironment)
        checkSaveSnapshot()
      }
    case request@ExecutionChange(_, execution) =>
      persist(request) { _ =>
        addExecution(execution)
        checkSaveSnapshot()
      }
    case request@ExecutionRemove(_, execution) =>
      persist(request) { _ =>
        removeExecution(execution.id)
        checkSaveSnapshot()
      }
    case request@WorkflowRemove(_, workflow) =>
      persist(request) { _ =>
        removeWorkflows(workflow)
        checkSaveSnapshot()
      }
    case request@GroupRemove(_, group) =>
      persist(request) { _ =>
        removeGroup(group)
        checkSaveSnapshot()
      }
    case request@ParameterListRemove(_, parameterList) =>
      persist(request) { _ =>
        removeParameterList(parameterList)
        checkSaveSnapshot()
      }
    case request@EnvironmentRemove(_, oldEnvironment) =>
      persist(request) { _ =>
        removeEnvironment(oldEnvironment)
        checkSaveSnapshot()
      }
    case request@DebugWorkflowChange(_, debugWorkflow) =>
      persist(request) { _ =>
        addDebugWorkflow(debugWorkflow)
        checkSaveSnapshot()
      }
    case request@DebugStepDataChange(path, stepData) =>
      persist(request) { _ =>
        addDebugStepData(path, stepData)
        checkSaveSnapshot()
      }
    case request@DebugStepErrorChange(path, stepError) =>
      persist(request) { _ =>
        addDebugStepError(path, stepError)
        checkSaveSnapshot()
      }
    case request@DebugWorkflowRemove(_, debugWorkflow) =>
      persist(request) { _ =>
        removeDebugWorkflow(debugWorkflow)
        checkSaveSnapshot()
      }
    case request@DebugStepErrorRemove(path, _) =>
      persist(request) { _ =>
        removeDebugStepError(path)
        checkSaveSnapshot()
      }
    case request@DebugStepDataRemove(path, _) =>
      persist(request) { _ =>
        removeDebugStepData(path)
        checkSaveSnapshot()
      }
  }
}

case class SnapshotState(
                          workflows: scala.collection.mutable.Map[String, Workflow],
                          statuses: scala.collection.mutable.Map[String, WorkflowStatus],
                          executions: scala.collection.mutable.Map[String, WorkflowExecution],
                          groups: scala.collection.mutable.Map[String, Group],
                          debugWorkflows: scala.collection.mutable.Map[String, DebugWorkflow],
                          debugStepData: scala.collection.mutable.Map[String, ResultStep],
                          debugStepError: scala.collection.mutable.Map[String, WorkflowError],
                          parameterLists: scala.collection.mutable.Map[String, ParameterList],
                          environmentState: scala.collection.mutable.Map[String, String],
                          environment: Option[Environment]
                        )
