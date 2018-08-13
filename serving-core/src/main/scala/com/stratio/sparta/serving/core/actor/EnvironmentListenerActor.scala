/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import java.io.StringReader

import com.github.mustachejava.DefaultMustacheFactory
import com.stratio.sparta.serving.core.actor.EnvironmentListenerActor._
import com.stratio.sparta.serving.core.actor.EnvironmentPublisherActor.{EnvironmentChange, EnvironmentRemove}
import com.stratio.sparta.serving.core.actor.ParameterListPublisherActor.{ParameterListChange, ParameterListRemove}
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor.{WorkflowChange, WorkflowRemove}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowExecutionContext, WorkflowIdExecutionContext}
import com.stratio.sparta.serving.core.services.WorkflowService
import com.twitter.mustache.ScalaObjectHandler
import org.json4s.jackson.Serialization._

import scala.util.Try

class EnvironmentListenerActor extends InMemoryServicesStatus with SpartaSerializer {

  override def persistenceId: String = AkkaConstant.EnvironmentStatusListenerActorName

  val moustacheFactory = new DefaultMustacheFactory

  override def preStart(): Unit = {
    moustacheFactory.setObjectHandler(new ScalaObjectHandler)

    context.system.eventStream.subscribe(self, classOf[WorkflowChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowRemove])
    context.system.eventStream.subscribe(self, classOf[EnvironmentChange])
    context.system.eventStream.subscribe(self, classOf[EnvironmentRemove])
    context.system.eventStream.subscribe(self, classOf[ParameterListChange])
    context.system.eventStream.subscribe(self, classOf[ParameterListRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[WorkflowChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRemove])
    context.system.eventStream.unsubscribe(self, classOf[EnvironmentChange])
    context.system.eventStream.unsubscribe(self, classOf[EnvironmentRemove])
    context.system.eventStream.unsubscribe(self, classOf[ParameterListChange])
    context.system.eventStream.unsubscribe(self, classOf[ParameterListRemove])
  }

  def manageGetEnvironment(): Unit = {
    sender ! environmentState.toMap
  }

  def applyContextToWorkflowId(workflowIdExecutionContext: WorkflowIdExecutionContext): Unit = {
    import workflowIdExecutionContext._
    workflows.get(workflowId) match {
      case Some(workflow) =>
        applyContextToWorkflow(WorkflowExecutionContext(workflow, executionContext))
      case None =>
        sender ! Try(throw new ServerException(s"Workflow not found with id $workflowId"))
    }
  }

  def applyContextToWorkflow(workflowExecutionContext: WorkflowExecutionContext): Unit = {
    import workflowExecutionContext._
    import workflowExecutionContext.executionContext._

    val result = Try {
      val workflowWithoutEnv = write(workflow)
      val writer = new java.io.StringWriter()
      val mustache = moustacheFactory.compile(new StringReader(workflowWithoutEnv), "MoustacheEnv")
      val workflowParamLists = getVariablesFromParamLists(workflow.settings.global.parametersLists)
      val paramListVariables = getVariablesFromParamLists(paramsLists)
      val variablesToApply = getVariablesFromEnvironment(withEnvironment) ++ workflowParamLists ++
        paramListVariables ++ executionContext.toVariablesMap
      mustache.execute(writer, variablesToApply)
      val parsedStr = writer.toString
      writer.flush()
      val workflowParsed = read[Workflow](parsedStr)
      val inputWorkflowWithParameters = WorkflowService.addParametersUsed(workflow)
      val variablesFiltered = if(inputWorkflowWithParameters.settings.global.parametersUsed.nonEmpty)
        Option(variablesToApply.filterKeys(key =>
          inputWorkflowWithParameters.settings.global.parametersUsed.contains(key))
        )
      else None

      workflowParsed.copy(
        settings = workflowParsed.settings.copy(
          global = workflowParsed.settings.global.copy(
            parametersUsed = inputWorkflowWithParameters.settings.global.parametersUsed
          )
        ),
        parametersUsedInExecution = variablesFiltered
      )
    }

    sender ! result
  }

  def getVariablesFromParamLists(paramsLists: Seq[String]): Map[String, String] = {
    parameterLists
      .filterKeys(list => paramsLists.contains(list))
      .values
      .flatMap(paramList => paramList.mapOfParameters ++ paramList.mapOfParametersWithPrefix)
      .toMap
  }

  def getVariablesFromEnvironment(withEnvironment: Boolean): Map[String, String] = {
    if(withEnvironment) {
      val envWithPrefix = environmentState.map { case (key, value) => s"env.$key" -> value }.toMap
      environmentState.toMap ++ envWithPrefix
    } else Map.empty[String, String]
  }

  val receiveCommand: Receive = apiReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  def apiReceive: Receive = {
    case GetEnvironment =>
      manageGetEnvironment()
    case ApplyExecutionContextToWorkflow(workflowExecutionContext) =>
      applyContextToWorkflow(workflowExecutionContext)
    case ApplyExecutionContextToWorkflowId(workflowIdExecutionContext) =>
      applyContextToWorkflowId(workflowIdExecutionContext)
  }

}

object EnvironmentListenerActor {

  case object GetEnvironment

  case class ApplyExecutionContextToWorkflow(workflowExecutionContext: WorkflowExecutionContext)

  case class ApplyExecutionContextToWorkflowId(workflowIdExecutionContext: WorkflowIdExecutionContext)

}