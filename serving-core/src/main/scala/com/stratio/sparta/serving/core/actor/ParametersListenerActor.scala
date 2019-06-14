/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import java.io.StringReader

import akka.actor.{Actor, ActorRef}
import akka.event.slf4j.SLF4JLogging
import com.github.mustachejava.DefaultMustacheFactory
import com.stratio.sparta.core.models.WorkflowValidationMessage
import com.stratio.sparta.serving.core.actor.ParametersListenerActor._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowExecutionContext, WorkflowIdExecutionContext, _}
import com.stratio.sparta.serving.core.services.dao.WorkflowPostgresDao
import com.twitter.mustache.ScalaObjectHandler
import org.json4s.jackson.Serialization._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class ParametersListenerActor extends Actor with SpartaSerializer with SLF4JLogging {

  implicit val ec = context.system.dispatchers.lookup("sparta-actors-dispatcher")

  case class ParametersToApplyContext(parametersToApply: Map[String, String], parametersWithoutValue: Seq[String])

  val moustacheFactory = new DefaultMustacheFactory
  val globalParametersService = PostgresDaoFactory.globalParametersService
  val parameterListService = PostgresDaoFactory.parameterListPostgresDao
  val workflowService = PostgresDaoFactory.workflowPgService

  override def preStart(): Unit = {
    moustacheFactory.setObjectHandler(new ScalaObjectHandler)
  }

  override def postStop(): Unit = {
    log.warn(s"Stopped ParametersListenerActor at time ${System.currentTimeMillis()}")
  }

  def applyContextToWorkflowAndResponse(
                                         executionContext: ExecutionContext,
                                         sendResponseTo: ActorRef,
                                         workflowFuture: Future[Workflow]
                                       ): Unit = {
    Try {
      for {
        workflow <- workflowFuture
        contextResult <- applyContextToWorkflow(WorkflowExecutionContext(workflow, executionContext))
      } yield sendResponseTo ! Try(contextResult._1)
    } match {
      case Success(result) =>
        result match {
          case action: Future[_] =>
            action.onFailure { case e =>
              sendResponseTo ! Failure(e)
            }
        }
      case Failure(e) => sendResponseTo ! Failure(e)
    }
  }

  def applyContextToWorkflow(workflowExecutionContext: WorkflowExecutionContext): Future[(Workflow, Seq[String])] = {
    import workflowExecutionContext._

    val workflowWithoutEnv = write(workflow)
    val writer = new java.io.StringWriter()
    val mustache = moustacheFactory.compile(new StringReader(workflowWithoutEnv), "MoustacheEnv")
    val inputWorkflowWithParameters = WorkflowPostgresDao.addParametersUsed(workflow)

    for {
      parametersToApplyContext <- getParametersToApplyContext(inputWorkflowWithParameters, executionContext)
    } yield {
      import parametersToApplyContext._

      val parametersWithBraces = parametersWithoutValue.map(value => "{{{" ++ value ++ "}}}")
      val unusedParameters = (parametersWithoutValue zip parametersWithBraces).toMap
      val parametersToApplyWithUnused = parametersToApply ++ unusedParameters

      mustache.execute(writer, parametersToApplyWithUnused)
      val parsedStr = writer.toString
      writer.flush()

      val workflowParsed = read[Workflow](parsedStr)
      val parametersFiltered = if (inputWorkflowWithParameters.settings.global.parametersUsed.nonEmpty)
        Option(parametersToApplyWithUnused.filterKeys(key =>
          inputWorkflowWithParameters.settings.global.parametersUsed.contains(key))
        )
      else None
      val workflowWithParameters = workflowParsed.copy(
        settings = workflowParsed.settings.copy(
          global = workflowParsed.settings.global.copy(
            parametersUsed = inputWorkflowWithParameters.settings.global.parametersUsed
          )
        ),
        parametersUsedInExecution = parametersFiltered
      )

      (workflowWithParameters, parametersWithoutValue)
    }
  }

  def getParametersToApplyContext(
                                   workflow: Workflow,
                                   executionContext: ExecutionContext
                                 ): Future[ParametersToApplyContext] = {
    for {
      workflowParamLists <- getVariablesFromParamLists(workflow.settings.global.parametersLists)
      parentsParamLists <- getParentsFromParamLists(workflow.settings.global.parametersLists)
      paramListVariables <- getVariablesFromParamLists(executionContext.paramsLists)
      globalParametersVariables <- getVariablesFromGlobalParameters
    } yield {
      val parametersFromExecutionContext = executionContext.toParametersMap
      val parentsParametersAndGlobal = parentsParamLists :+ "Global"
      val executionParametersAddPrefixes: Map[String, String] = for {
        (paramName, paramValue) <- parametersFromExecutionContext.filterKeys(!_.contains("."))
        parentParamList <- parentsParametersAndGlobal
      } yield (s"$parentParamList.$paramName", paramValue)

        val parametersToApply = globalParametersVariables ++ workflowParamLists ++
          paramListVariables ++ parametersFromExecutionContext ++ executionParametersAddPrefixes
        val parametersWithoutValue = workflow.settings.global.parametersUsed.filterNot(parameter =>
          parametersToApply.contains(parameter))

      ParametersToApplyContext(parametersToApply, parametersWithoutValue)
    }
  }

  def validateContextToWorkflowAndResponse(
                                            executionContext: ExecutionContext,
                                            ignoreCustomParams: Boolean,
                                            sendResponseTo: ActorRef,
                                            workflowFuture: Future[Workflow]
                                          ): Unit = {
    Try {
      for {
        workflow <- workflowFuture
        validateContextResult <- validateContextToWorkflow(
          WorkflowExecutionContext(workflow, executionContext),
          ignoreCustomParams
        )
      } yield sendResponseTo ! Try(validateContextResult)
    } match {
      case Success(result) =>
        result match {
          case action: Future[_] =>
            action.onFailure { case e =>
              sendResponseTo ! Failure(e)
            }
        }
      case Failure(e) => sendResponseTo ! Failure(e)
    }
  }

  def validateContextToWorkflow(
                                 workflowExecutionContext: WorkflowExecutionContext,
                                 ignoreCustomParams: Boolean
                               ): Future[ValidationContextResult] = {
    for {
      contextResult <- applyContextToWorkflow(workflowExecutionContext)
    } yield {
      val invalidParameters = if (ignoreCustomParams)
        contextResult._2.filter(param => !param.contains("."))
      else contextResult._2
      val validationResult = if (invalidParameters.nonEmpty)
        WorkflowValidation(
          valid = false,
          messages = Seq(
            WorkflowValidationMessage(
              s"Parameters without value: ${invalidParameters.mkString(",")}")
          )
        )
      else WorkflowValidation(valid = true, messages = Seq.empty)

      ValidationContextResult(contextResult._1, validationResult)
    }
  }

  def doRunWithExecutionContextViewAndResponse(
                                                workflowFuture: Future[Workflow],
                                                sendResponseTo: ActorRef
                                              ): Unit = {
    Try {
      for {
        runWithExecutionContextView <- doRunWithExecutionContextView(workflowFuture)
      } yield sendResponseTo ! Try(runWithExecutionContextView)
    } match {
      case Success(result) =>
        result match {
          case action: Future[_] =>
            action.onFailure { case e =>
              sendResponseTo ! Failure(e)
            }
        }
      case Failure(e) => sendResponseTo ! Failure(e)
    }
  }

  def doRunWithExecutionContextView(workflowFuture: Future[Workflow]): Future[RunWithExecutionContextView] = {
    for {
      workflow <- workflowFuture
      globalParameters <- globalParametersService.find()
      groupsAndContexts <- Future.sequence {
        workflow.settings.global.parametersLists.map { settingsParameterList =>
          parameterListService.findByParentWithContexts(settingsParameterList)
        }
      }
      executionContext: ExecutionContext = ExecutionContext(
        extraParams = Seq.empty,
        paramsLists = groupsAndContexts.map(_.parameterList.name)
      )
      workflowWithParameters = WorkflowPostgresDao.addParametersUsed(workflow)
      parametersToApply <- getParametersToApplyContext(workflowWithParameters, executionContext)
    } yield {
      val extraParams = workflowWithParameters.settings.global.parametersUsed.filter(parameter =>
        !parametersToApply.parametersToApply.contains(parameter)
      )
      RunWithExecutionContextView(
        groupsAndContexts = groupsAndContexts,
        globalParameters = Option(globalParameters),
        extraParams = extraParams
      )
    }
  }


  def getVariablesFromParamLists(paramsLists: Seq[String]): Future[Map[String, String]] = {
    Future.sequence {
      paramsLists.map(paramList => parameterListService.findByName(paramList))
    }.map { paramsLists =>
      paramsLists.flatMap(paramList => paramList.mapOfParameters ++ paramList.mapOfParametersWithPrefix).toMap
    }
  }

  def getParentsFromParamLists(paramsLists: Seq[String]): Future[Seq[String]] = {
    Future.sequence {
      paramsLists.map(paramList => parameterListService.findByName(paramList))
    }.map { paramsLists =>
      paramsLists.map(paramList => paramList.parent.getOrElse(paramList.name))
    }
  }

  def getVariablesFromGlobalParameters: Future[Map[String, String]] =
    for {
      globalParameters <- globalParametersService.find()
    } yield {
      globalParameters.toVariablesMap ++ globalParameters.toVariablesMapWithPrefix
    }

  override def receive: Receive = {
    case ApplyExecutionContextToWorkflow(workflowExecutionContext) =>
      applyContextToWorkflowAndResponse(
        workflowExecutionContext.executionContext,
        sender(),
        Future(workflowExecutionContext.workflow)
      )
    case ApplyExecutionContextToWorkflowId(workflowIdExecutionContext) =>
      applyContextToWorkflowAndResponse(
        workflowIdExecutionContext.executionContext,
        sender(),
        workflowService.findWorkflowById(workflowIdExecutionContext.workflowId)
      )
    case ValidateExecutionContextToWorkflow(workflowExecutionContext, ignoreCustomParams) =>
      validateContextToWorkflowAndResponse(
        workflowExecutionContext.executionContext,
        ignoreCustomParams,
        sender(),
        Future(workflowExecutionContext.workflow)
      )
    case ValidateExecutionContextToWorkflowId(workflowIdExecutionContext, ignoreCustomParams) =>
      validateContextToWorkflowAndResponse(
        workflowIdExecutionContext.executionContext,
        ignoreCustomParams,
        sender(),
        workflowService.findWorkflowById(workflowIdExecutionContext.workflowId)
      )
    case GetRunWithExecutionContextView(workflow) =>
      doRunWithExecutionContextViewAndResponse(
        Future(workflow),
        sender()
      )
    case GetRunWithExecutionContextViewById(workflowId) =>
      doRunWithExecutionContextViewAndResponse(
        workflowService.findWorkflowById(workflowId),
        sender()
      )
  }

}

object ParametersListenerActor {

  case class ApplyExecutionContextToWorkflow(workflowExecutionContext: WorkflowExecutionContext)

  case class ValidateExecutionContextToWorkflow(
                                                 workflowExecutionContext: WorkflowExecutionContext,
                                                 ignoreCustomParams: Boolean
                                               )

  case class ValidateExecutionContextToWorkflowId(
                                                   workflowIdExecutionContext: WorkflowIdExecutionContext,
                                                   ignoreCustomParams: Boolean
                                                 )

  case class ApplyExecutionContextToWorkflowId(workflowIdExecutionContext: WorkflowIdExecutionContext)

  case class GetRunWithExecutionContextView(workflow: Workflow)

  case class GetRunWithExecutionContextViewById(workflowId: String)

}