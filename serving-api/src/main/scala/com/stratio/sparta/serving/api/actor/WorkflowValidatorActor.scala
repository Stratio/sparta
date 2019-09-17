/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import akka.actor.Props
import akka.pattern.ask
import com.stratio.sparta.core.models.WorkflowValidationMessage
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.actor.ParametersListenerActor
import com.stratio.sparta.serving.core.actor.ParametersListenerActor._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.authorization.{GosecUser, LoggedUser}
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.WorkflowValidatorService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class WorkflowValidatorActor extends ActionUserAuthorize {

  val ResourceWorkflow = "Workflows"

  private val workflowPgService = PostgresDaoFactory.workflowPgService

  import WorkflowValidatorActor._

  lazy val workflowValidatorService = new WorkflowValidatorService()
  lazy val parametersStateActor = context.system.actorOf(Props[ParametersListenerActor])

  def receiveApiActions(action: Any): Any = action match {
    case ValidateWorkflowStepsJob(workflow, user, _) => validateSteps(workflow, user)
    case ValidateWorkflowWithoutExContextJob(workflow, user, _) => validateWithoutExecutionContext(workflow, user)
    case ValidateWorkflowIdWithExContextJob(workflowIdExecutionContext, user, _) => validateWithExecutionContext(workflowIdExecutionContext, user)
    case _ => log.info("Unrecognized message in Workflow Validator Actor")
  }

  def validateSteps(workflowRaw: Workflow, user: Option[LoggedUser]): Unit = {
    val authorizationId = workflowRaw.authorizationId
    val action = Map(ResourceWorkflow -> Edit)

    authorizeActionsByResourceId(user, action, authorizationId, Option(sender)) {
      for {
        validationContextResult <- (parametersStateActor ? ValidateExecutionContextToWorkflow(
          workflowExecutionContext = WorkflowExecutionContext(workflowRaw, ExecutionContext()),
          ignoreCustomParams = true
        )).mapTo[Try[ValidationContextResult]]
      } yield {
        Try {
          validationContextResult match {
            case Success(result) =>
              if (result.workflowValidation.valid)
                workflowValidatorService.validateSinglePlugins(result.workflow)
              else result.workflowValidation
            case Failure(e) =>
              throw e
          }
        }
      }
    }
  }

  def validateWithoutExecutionContext(workflowRaw: Workflow, user: Option[LoggedUser]): Unit = {
    val authorizationId = workflowRaw.authorizationId
    val action = Map(ResourceWorkflow -> Edit)

    authorizeActionsByResourceId(user, action, authorizationId, Option(sender)) {
      workflowValidatorService.validateAll(workflowRaw)
    }
  }

  def validateWithExecutionContext(
                                    workflowIdExecutionContext: WorkflowIdExecutionContext,
                                    user: Option[LoggedUser]
                                  ): Future[Unit] = {

    val senderResponseTo = Option(sender)
    for {
      workflowRaw <- workflowPgService.findWorkflowById(workflowIdExecutionContext.workflowId)
    } yield {
      val action = Map(ResourceWorkflow -> Edit)
      val authorizationId = workflowRaw.authorizationId
      authorizeActionsByResourceId(user, action, authorizationId, senderResponseTo) {
        for {
          response <- (parametersStateActor ? ValidateExecutionContextToWorkflowId(
            workflowIdExecutionContext = workflowIdExecutionContext, ignoreCustomParams = false
          )).mapTo[Try[ValidationContextResult]]
        } yield manageValidationResult(response)
      }
    }
  }

  private def manageValidationResult(validationContextResult: Try[ValidationContextResult]): Try[WorkflowValidationResponse] = {
    Try {
      validationContextResult match {
        case Success(result) =>
          val validation = {
            if (result.workflowValidation.valid)
              workflowValidatorService.validateAll(result.workflow)
            else result.workflowValidation
          }
          WorkflowValidationResponse(validation.valid, validation.messages)
        case Failure(e) =>
          throw ServerException.create(e.getLocalizedMessage, e)
      }
    }
  }
}

object WorkflowValidatorActor {

  case class ValidateWorkflowSteps(workflow: Workflow, user: Option[LoggedUser])

  case class ValidateWorkflowStepsJob(workflow: Workflow, user: Option[GosecUser], validateWorkflowSteps: Boolean)

  case class ValidateWorkflowWithoutExContext(workflow: Workflow, user: Option[LoggedUser])

  case class ValidateWorkflowWithoutExContextJob(workflow: Workflow, user: Option[GosecUser], validateWorkflowWithoutExContext: Boolean)

  case class ValidateWorkflowIdWithExContext(
                                              workflowExecutionContext: WorkflowIdExecutionContext,
                                              user: Option[LoggedUser]
                                            )

  case class ValidateWorkflowIdWithExContextJob(
                                                 workflowExecutionContext: WorkflowIdExecutionContext,
                                                 user: Option[GosecUser],
                                                 validateWorkflowIdWithExContext: Boolean
                                               )

  case class WorkflowValidationResponse(valid: Boolean, messages: Seq[WorkflowValidationMessage])

  def props: Props = Props[WorkflowValidatorActor]

}

