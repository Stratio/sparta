/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import akka.actor.{Actor, ActorRef}
import akka.event.slf4j.SLF4JLogging
import akka.pattern.ask

import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
import com.stratio.sparta.serving.core.actor.ParametersListenerActor._
import com.stratio.sparta.serving.core.models.dto.DtoImplicits._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.models.workflow.migration.{WorkflowAndromeda, WorkflowCassiopeia}
import com.stratio.sparta.serving.core.services.WorkflowValidatorService
import com.stratio.sparta.serving.core.services.migration.CassiopeiaMigrationService
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, PostgresDaoFactory}

class WorkflowActor(
                     launcherActor: ActorRef,
                     parametersStateActor: ActorRef
                   )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  import DtoModelImplicits._
  import WorkflowActor._
  import com.stratio.sparta.serving.core.models.workflow.migration.MigrationModelImplicits._

  val ResourceWorkflow = "Workflows"

  private val workflowPgService = PostgresDaoFactory.workflowPgService
  private val groupPgService = PostgresDaoFactory.groupPgService
  private val workflowValidatorService = new WorkflowValidatorService()
  private val migrationService = new CassiopeiaMigrationService()


  //scalastyle:off
  def receiveApiActions(action: Any): Any = action match {
    case Run(id, user) => run(id, user)
    case RunWithVariables(executionVariables, user) => runWithVariables(executionVariables, user)
    case RunWithWorkflowIdExecutionContext(workflowIdExecutionContext, user) => runWithExecutionContext(workflowIdExecutionContext, user)
    case CreateWorkflow(workflow, user) => create(workflow, user)
    case Update(workflow, user) => update(workflow, user)
    case Find(id, user) => find(id, user)
    case FindByIdList(workflowIds, user) => findByIdList(workflowIds, user)
    case Query(query, user) => doQuery(query, user)
    case FindAll(user) => findAll(user)
    case FindAllDto(user) => findAllDto(user)
    case FindAllByGroup(group, user) => findAllByGroup(group, user)
    case DeleteWorkflow(id, user) => delete(id, user)
    case DeleteWorkflowWithAllVersions(workflowDelete, user) => deleteWithAllVersion(workflowDelete, user)
    case DeleteList(workflowIds, user) => deleteList(workflowIds, user)
    case ValidateWorkflow(workflow, user) => validate(workflow, user)
    case ValidateWorkflowWithoutExContext(workflow, user) => validateWithoutExecutionContext(workflow, user)
    case ValidateWorkflowIdWithExContext(workflowIdExecutionContext, user) => validateWithExecutionContext(workflowIdExecutionContext, user)
    case CreateWorkflowVersion(workflowVersion, user) => createVersion(workflowVersion, user)
    case RenameWorkflow(workflowRename, user) => rename(workflowRename, user)
    case MoveWorkflow(workflowMove, user) => moveTo(workflowMove, user)
    case MigrateFromCassiopeia(workflowCassiopeia, user) => migrateWorkflowFromCassiopeia(workflowCassiopeia, user)
    case MigrateFromAndromeda(workflowAndromeda, user) => migrateWorkflowFromAndromeda(workflowAndromeda, user)
    case RunWithParametersView(workflow, user) => runWithParametersView(workflow, user)
    case RunWithParametersViewId(workflowId, user) => runWithParametersViewById(workflowId, user)
    case _ => log.info("Unrecognized message in Workflow Actor")
  }

  //scalastyle:on

  //TODO this method should be removed when the front migrate to latest version and call run with ex.context
  def run(id: String, user: Option[LoggedUser]): Unit =
    runWithExecutionContext(WorkflowIdExecutionContext(id, ExecutionContext()), user)

  //TODO this method should be removed when the front migrate to latest version and call run with ex.context
  def runWithVariables(executionVariables: WorkflowExecutionVariables, user: Option[LoggedUser]): Unit =
    runWithExecutionContext(
      WorkflowIdExecutionContext(
        executionVariables.workflowId,
        ExecutionContext(extraParams = executionVariables.variables)
      ),
      user
    )

  def runWithExecutionContext(
                               workflowIdExecutionContext: WorkflowIdExecutionContext,
                               user: Option[LoggedUser]
                             ): Unit =
    launcherActor.forward(Launch(workflowIdExecutionContext, user))

  //TODO this method should be removed when the front migrate to latest version and call validate with ex. context
  def validate(workflowRaw: Workflow, user: Option[LoggedUser]): Unit = {
    val authorizationId = workflowRaw.authorizationId
    val action = Map(ResourceWorkflow -> Edit)

    authorizeActionsByResourceId(user, action, authorizationId, Option(sender)) {
      for {
        response <- (parametersStateActor ? ValidateExecutionContextToWorkflow(
          workflowExecutionContext = WorkflowExecutionContext(workflowRaw, ExecutionContext()),
          ignoreCustomParams = true
        )).mapTo[Try[ValidationContextResult]]
      } yield manageValidationResult(response)
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

  def runWithParametersView(workflow: Workflow, user: Option[LoggedUser]): Unit = {
    val senderResponseTo = Option(sender)
    val authorizationId = workflow.authorizationId
    val action = Map(ResourceWorkflow -> View)

    authorizeActionsByResourceId(user, action, authorizationId, senderResponseTo) {
      for {
        response <- (parametersStateActor ? GetRunWithExecutionContextView(workflow))
          .mapTo[Try[RunWithExecutionContextView]]
      } yield response
    }
  }

  def runWithParametersViewById(workflowId: String, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      workflowRaw <- workflowPgService.findWorkflowById(workflowId)
    } yield {
      val action = Map(ResourceWorkflow -> View)
      val authorizationId = workflowRaw.authorizationId

      authorizeActionsByResourceId(user, action, authorizationId, senderResponseTo) {
        for {
          response <- (parametersStateActor ? GetRunWithExecutionContextViewById(workflowId))
            .mapTo[Try[RunWithExecutionContextView]]
        } yield response
      }
    }
  }

  def findAll(user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceWorkflow -> View)) {
      workflowPgService.findAllWorkflows()
    }

  def findAllDto(user: Option[LoggedUser]): Unit = {
    val sendTo = sender
    authorizeActionResultResources(user, Map(ResourceWorkflow -> View), Some(sendTo)) {
      workflowPgService.findAllWorkflows().map(workflows => workflows.map { workflow =>
        val workflowDto: WorkflowDto = workflow
        workflowDto
      })
    }
  }

  def find(id: String, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceWorkflow -> View)) {
      workflowPgService.findWorkflowById(id)
    }

  def findAllByGroup(groupID: String, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceWorkflow -> View)) {
      workflowPgService.findByGroupID(groupID)
    }

  def findByIdList(workflowIds: Seq[String], user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceWorkflow -> View)) {
      workflowPgService.findByIdList(workflowIds)
    }

  def doQuery(query: WorkflowQuery, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceWorkflow -> View)) {
      workflowPgService.doQuery(query)
    }

  def create(workflow: Workflow, user: Option[LoggedUser]): Unit =
    authorizeActionsByResourceId(user, Map(ResourceWorkflow -> Create), workflow.authorizationId) {
      workflowPgService.createWorkflow(workflow)
    }

  def update(workflow: Workflow, user: Option[LoggedUser]): Unit =
    authorizeActionsByResourceId(user, Map(ResourceWorkflow -> Edit), workflow.authorizationId) {
      workflowPgService.updateWorkflow(workflow)
    }

  def delete(id: String, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      workflow <- workflowPgService.findWorkflowById(id)
    } yield {
      val authorizationId = Option(workflow.authorizationId).getOrElse("N/A")
      authorizeActionsByResourceId(
        user, Map(ResourceWorkflow -> Delete, ResourceWorkflow -> Status), authorizationId, senderResponseTo) {
        workflowPgService.deleteWorkflowById(id)
      }
    }
  }

  def deleteWithAllVersion(workflowDelete: WorkflowDelete, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      group <- groupPgService.findGroupById(workflowDelete.groupId)
    } yield {
      val authorizationId = s"${group.name}/${workflowDelete.name}"
      authorizeActionsByResourceId(
        user,
        Map(ResourceWorkflow -> Delete, ResourceWorkflow -> Status), authorizationId, senderResponseTo) {
        workflowPgService.deleteWithAllVersions(workflowDelete)
      }
    }
  }

  def deleteList(workflowIds: Seq[String], user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      workflows <- workflowPgService.findByIdList(workflowIds)
    } yield {
      val resourcesId = workflows.map(_.authorizationId)
      authorizeActionsByResourcesIds(
        user,
        Map(ResourceWorkflow -> Delete, ResourceWorkflow -> Status),
        resourcesId, senderResponseTo
      ) {
        workflowPgService.deleteWorkflowList(workflowIds)
      }
    }
  }

  def createVersion(workflowVersion: WorkflowVersion, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      workflow <- workflowPgService.findWorkflowById(workflowVersion.id)
    } yield {
      val authorizationId = Option(workflow.authorizationId).getOrElse("N/A")
      authorizeActionsByResourceId(user, Map(ResourceWorkflow -> Create), authorizationId, senderResponseTo) {
        workflowPgService.createVersion(workflowVersion)
      }
    }
  }

  def rename(workflowRename: WorkflowRename, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      group <- groupPgService.findGroupById(workflowRename.groupId)
    } yield {
      val authorizationId = s"${group.name}/${workflowRename.newName}"
      authorizeActionsByResourceId(user, Map(ResourceWorkflow -> Edit), authorizationId, senderResponseTo) {
        workflowPgService.rename(workflowRename)
      }
    }
  }

  def moveTo(workflowMove: WorkflowMove, user: Option[LoggedUser]): Unit = {
    val senderResponseTo = Option(sender)
    for {
      group <- groupPgService.findGroupById(workflowMove.groupTargetId)
    } yield {
      val authorizationId = s"${group.name}/${workflowMove.workflowName}"
      authorizeActionsByResourceId(user, Map(ResourceWorkflow -> Edit), authorizationId, senderResponseTo) {
        workflowPgService.moveTo(workflowMove).map(list => {
          val workflowsDto: Seq[WorkflowDto] = list
          workflowsDto
        })
      }
    }
  }

  def migrateWorkflowFromCassiopeia(workflowCassiopeia: WorkflowCassiopeia, user: Option[LoggedUser]): Unit = {
    authorizeActions[ResponseWorkflowAndromeda](user, Map(ResourceWorkflow -> View)) {
      Try {
        val workflow : WorkflowAndromeda = workflowCassiopeia
        workflow
      }
    }
  }

  def migrateWorkflowFromAndromeda(workflowAndromeda: WorkflowAndromeda, user: Option[LoggedUser]): Unit = {
    authorizeActions[ResponseWorkflow](user, Map(ResourceWorkflow -> View)) {
      Try {
        val workflow : Workflow = workflowAndromeda
        workflow
      }
    }
  }

  private def manageValidationResult(validationContextResult: Try[ValidationContextResult]): Try[WorkflowValidation] = {
    Try {
      validationContextResult match {
        case Success(result) =>
          if (result.workflowValidation.valid)
            workflowValidatorService.validateAll(result.workflow)
          else result.workflowValidation
        case Failure(e) =>
          throw e
      }
    }
  }
}

object WorkflowActor extends SLF4JLogging {

  case class Run(id: String, user: Option[LoggedUser])

  case class RunWithVariables(executionVariables: WorkflowExecutionVariables, user: Option[LoggedUser])

  case class RunWithWorkflowIdExecutionContext(
                                                workflowIdExecutionContext: WorkflowIdExecutionContext,
                                                user: Option[LoggedUser]
                                              )

  case class ValidateWorkflow(workflow: Workflow, user: Option[LoggedUser])

  case class ValidateWorkflowWithoutExContext(workflow: Workflow, user: Option[LoggedUser])

  case class ValidateWorkflowIdWithExContext(
                                              workflowExecutionContext: WorkflowIdExecutionContext,
                                              user: Option[LoggedUser]
                                            )

  case class CreateWorkflow(workflow: Workflow, user: Option[LoggedUser])

  case class Update(workflow: Workflow, user: Option[LoggedUser])

  case class DeleteWorkflow(id: String, user: Option[LoggedUser])

  case class DeleteWorkflowWithAllVersions(query: WorkflowDelete, user: Option[LoggedUser])

  case class DeleteList(workflowIds: Seq[String], user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class FindAllDto(user: Option[LoggedUser])

  case class Find(id: String, user: Option[LoggedUser])

  case class FindAllByGroup(groupID: String, user: Option[LoggedUser])

  case class FindByIdList(workflowIds: Seq[String], user: Option[LoggedUser])

  case class Query(query: WorkflowQuery, user: Option[LoggedUser])

  case class CreateWorkflowVersion(query: WorkflowVersion, user: Option[LoggedUser])

  case class RenameWorkflow(query: WorkflowRename, user: Option[LoggedUser])

  case class MoveWorkflow(query: WorkflowMove, user: Option[LoggedUser])

  case class MigrateFromCassiopeia(workflowCassiopeia: WorkflowCassiopeia, user: Option[LoggedUser])

  case class MigrateFromAndromeda(workflowAndromeda: WorkflowAndromeda, user: Option[LoggedUser])

  case class RunWithParametersView(workflow: Workflow, user: Option[LoggedUser])

  case class RunWithParametersViewId(workflowId: String, user: Option[LoggedUser])

  type ResponseRun = Try[String]

  type ResponseWorkflows = Try[Seq[Workflow]]

  type ResponseWorkflowsDto = Try[Seq[WorkflowDto]]

  type ResponseWorkflow = Try[Workflow]

  type ResponseWorkflowValidation = Try[WorkflowValidation]

  type ResponseRunWithExecutionContextView = Try[RunWithExecutionContextView]

  type ResponseWorkflowAndromeda = Try[WorkflowAndromeda]
}

