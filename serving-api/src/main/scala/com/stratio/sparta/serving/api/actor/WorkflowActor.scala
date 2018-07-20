/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, ActorRef}
import akka.event.slf4j.SLF4JLogging

import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
import com.stratio.sparta.serving.core.actor.WorkflowInMemoryApi._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.dto.DtoImplicits._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.{GroupService, CassiopeiaMigrationService, WorkflowService, WorkflowValidatorService}
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, CheckpointUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException
import scala.util.Try

import com.stratio.sparta.serving.core.models.workflow.migration.{WorkflowAndromeda, WorkflowCassiopeia}

class WorkflowActor(
                     val curatorFramework: CuratorFramework,
                     inMemoryWorkflowApi: ActorRef,
                     launcherActor: ActorRef,
                     envStateActor: ActorRef
                   )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with CheckpointUtils with ActionUserAuthorize {

  import WorkflowActor._
  import WorkflowDtoImplicit._

  val ResourceWorkflow = "Workflows"
  val ResourceEnvironment = "Environment"

  private val workflowService = new WorkflowService(curatorFramework)
  private val groupService = new GroupService(curatorFramework)
  private val wServiceWithEnv = new WorkflowService(curatorFramework, Option(context.system), Option(envStateActor))
  private val workflowValidatorService = new WorkflowValidatorService(Option(curatorFramework))
  private val migrationService = new CassiopeiaMigrationService(curatorFramework)

  //scalastyle:off
  override def receive: Receive = {
    case Stop(id, user) => stop(id, user)
    case Reset(id, user) => reset(id, user)
    case Run(id, user) => run(id, user)
    case CreateWorkflow(workflow, user) => create(workflow, user)
    case CreateWorkflows(workflows, user) => createList(workflows, user)
    case Update(workflow, user) => update(workflow, user)
    case UpdateList(workflows, user) => updateList(workflows, user)
    case Find(id, user) => find(id, user)
    case FindWithEnv(id, user) => findWithEnv(id, user)
    case FindByIdList(workflowIds, user) => findByIdList(workflowIds, user)
    case Query(query, user) => doQuery(query, user)
    case FindAll(user) => findAll(user)
    case FindAllWithEnv(user) => findAllWithEnv(user)
    case FindAllMonitoring(user) => findAllMonitoring(user)
    case FindAllByGroup(group, user) => findAllByGroup(group, user)
    case DeleteWorkflow(id, user) => delete(id, user)
    case DeleteWorkflowWithAllVersions(workflowDelete, user) => deleteWithAllVersion(workflowDelete, user)
    case DeleteList(workflowIds, user) => deleteList(workflowIds, user)
    case DeleteAll(user) => deleteAll(user)
    case DeleteCheckpoint(id, user) => deleteCheckpoint(id, user)
    case ResetAllStatuses(user) => resetAllStatuses(user)
    case ValidateWorkflow(workflow, user) => validate(workflow, user)
    case CreateWorkflowVersion(workflowVersion, user) => createVersion(workflowVersion, user)
    case RenameWorkflow(workflowRename, user) => rename(workflowRename, user)
    case MoveWorkflow(workflowMove, user) => moveTo(workflowMove, user)
    case MigrateFromCassiopeia(workflowCassiopeia, user) => migrateWorkflowFromCassiopeia(workflowCassiopeia, user)
    case _ => log.info("Unrecognized message in Workflow Actor")
  }

  //scalastyle:on

  def run(id: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceWorkflow -> Status)
    val authorizationId = workflowService.findById(id).authorizationId
    authorizeActionsByResourceId[Response](user, actions, authorizationId) {
      Try {
        launcherActor.forward(Launch(id.toString, user))
      }
    }
  }

  def stop(id: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceWorkflow -> Status)
    val authorizationId = workflowService.findById(id).authorizationId
    authorizeActionsByResourceId[ResponseAny](user, actions, authorizationId) {
      workflowService.stop(id)
    }
  }

  def reset(id: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceWorkflow -> Status)
    val authorizationId = workflowService.findById(id).authorizationId
    authorizeActionsByResourceId[ResponseAny](user, actions, authorizationId) {
      workflowService.reset(id)
    }
  }

  def validate(workflow: Workflow, user: Option[LoggedUser]): Unit =
    authorizeActionsByResourceId[ResponseWorkflowValidation](
      user,
      Map(ResourceWorkflow -> Edit),
      workflow.authorizationId
    ) {
      Try {
        workflowValidatorService.validateAll(wServiceWithEnv.applyEnv(workflow))
      }
    }

  def findAll(user: Option[LoggedUser]): Unit =
    filterResultsWithAuthorization(user, Map(ResourceWorkflow -> View), Option(inMemoryWorkflowApi)) {
      FindAllMemoryWorkflowRaw
    }

  def findAllMonitoring(user: Option[LoggedUser]): Unit =
    filterResultsWithAuthorization(user, Map(ResourceWorkflow -> View), Option(inMemoryWorkflowApi)) {
      FindAllMemoryWorkflowDto
    }

  def findAllWithEnv(user: Option[LoggedUser]): Unit =
    authorizeActionsAndFilterResults(
      user,
      Map(ResourceEnvironment -> View),
      Map(ResourceWorkflow -> View),
      Option(inMemoryWorkflowApi)
    ) {
      FindAllMemoryWorkflowWithEnv
    }

  def find(id: String, user: Option[LoggedUser]): Unit =
    authorizeResultByResourceId(user, Map(ResourceWorkflow -> View), Option(inMemoryWorkflowApi)) {
      FindMemoryWorkflowRaw(id)
    }

  def findAllByGroup(groupID: String, user: Option[LoggedUser]): Unit =
    filterResultsWithAuthorization(user, Map(ResourceWorkflow -> View), Option(inMemoryWorkflowApi)) {
      FindByGroupMemoryWorkflowDto(groupID)
    }

  def findWithEnv(id: String, user: Option[LoggedUser]): Unit =
    authorizeActionsAndFilterResults(
      user,
      Map(ResourceEnvironment -> View),
      Map(ResourceWorkflow -> View),
      Option(inMemoryWorkflowApi)
    ) {
      FindMemoryWorkflowWithEnv(id)
    }

  def findByIdList(workflowIds: Seq[String], user: Option[LoggedUser]): Unit =
    filterResultsWithAuthorization(user, Map(ResourceWorkflow -> View), Option(inMemoryWorkflowApi)) {
      FindByIdsMemoryWorkflowRaw(workflowIds)
    }

  def doQuery(query: WorkflowQuery, user: Option[LoggedUser]): Unit =
    filterResultsWithAuthorization(user, Map(ResourceWorkflow -> View), Option(inMemoryWorkflowApi)) {
      FindByQueryMemoryWorkflowRaw(query)
    }

  def create(workflow: Workflow, user: Option[LoggedUser]): Unit =
    authorizeActionsByResourceId[ResponseWorkflow](user, Map(ResourceWorkflow -> Create), workflow.authorizationId) {
      Try {
        val withEnv = wServiceWithEnv.applyEnv(workflow)
        wServiceWithEnv.create(workflow, Option(withEnv))
      }
    }

  def createList(workflows: Seq[Workflow], user: Option[LoggedUser]): Unit = {
    val resourcesId = workflows.map(_.authorizationId)
    authorizeActionsByResourcesIds[ResponseWorkflows](user, Map(ResourceWorkflow -> Create), resourcesId) {
      Try {
        val withEnv = workflows.map(workflow => wServiceWithEnv.applyEnv(workflow))
        wServiceWithEnv.createList(workflows, withEnv)
      }
    }
  }

  def update(workflow: Workflow, user: Option[LoggedUser]): Unit =
    authorizeActionsByResourceId(user, Map(ResourceWorkflow -> Edit), workflow.authorizationId) {
      Try {
        val withEnv = wServiceWithEnv.applyEnv(workflow)
        wServiceWithEnv.update(workflow, Option(withEnv))
      }.recover {
        case _: NoNodeException =>
          throw new ServerException(s"No workflow with name ${workflow.name}.")
      }
    }

  def updateList(workflows: Seq[Workflow], user: Option[LoggedUser]): Unit = {
    val resourcesId = workflows.map(_.authorizationId)
    authorizeActionsByResourcesIds(user, Map(ResourceWorkflow -> Edit), resourcesId) {
      Try {
        val withEnv = workflows.map(workflow => wServiceWithEnv.applyEnv(workflow))
        wServiceWithEnv.updateList(workflows, withEnv)
      }
    }
  }

  def delete(id: String, user: Option[LoggedUser]): Unit = {
    val authorizationId = workflowService.findById(id).authorizationId
    authorizeActionsByResourceId[Response](
      user,
      Map(ResourceWorkflow -> Delete, ResourceWorkflow -> Status),
      authorizationId
    ) {
      workflowService.delete(id)
    }
  }

  def deleteWithAllVersion(workflowDelete: WorkflowDelete, user: Option[LoggedUser]): Unit = {
    val groupName = groupService.findByID(workflowDelete.groupId)
    val authorizationId = s"$groupName/${workflowDelete.name}"
    authorizeActionsByResourceId[Response](
      user,
      Map(ResourceWorkflow -> Delete, ResourceWorkflow -> Status),
      authorizationId
    ) {
      workflowService.deleteWithAllVersions(workflowDelete)
    }
  }

  def deleteList(workflowIds: Seq[String], user: Option[LoggedUser]): Unit = {
    val resourcesId = workflowIds.map(id => workflowService.findById(id).authorizationId)
    authorizeActionsByResourcesIds[Response](
      user,
      Map(ResourceWorkflow -> Delete, ResourceWorkflow -> Status),
      resourcesId
    ) {
      workflowService.deleteList(workflowIds)
    }
  }

  def deleteAll(user: Option[LoggedUser]): Unit = {
    val resourcesId = workflowService.findAll.map(_.authorizationId)
    val actions = Map(ResourceWorkflow -> Delete, ResourceWorkflow -> Status)
    authorizeActionsByResourcesIds[Response](user, actions, resourcesId) {
      workflowService.deleteAll()
    }
  }

  def resetAllStatuses(user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceWorkflow -> Status)
    val resourcesId = workflowService.findAll.map(_.authorizationId)
    authorizeActionsByResourcesIds[Response](user, actions, resourcesId) {
      workflowService.resetAllStatuses()
    }
  }

  def deleteCheckpoint(id: String, user: Option[LoggedUser]): Unit = {
    val authorizationId = workflowService.findById(id).authorizationId
    authorizeActionsByResourceId[Response](user, Map(ResourceWorkflow -> Status), authorizationId) {
      Try(deleteCheckpointPath(workflowService.findById(id)))
    }
  }

  def createVersion(workflowVersion: WorkflowVersion, user: Option[LoggedUser]): Unit = {
    val workflow = workflowService.findById(workflowVersion.id)
    val group = workflowVersion.group.getOrElse(workflow.group).name
    val authorizationId = s"$group/${workflow.name}"
    authorizeActionsByResourceId[ResponseWorkflow](user, Map(ResourceWorkflow -> Create), authorizationId) {
      Try(workflowService.createVersion(workflowVersion))
    }
  }

  def rename(workflowRename: WorkflowRename, user: Option[LoggedUser]): Unit = {
    val groupName = groupService.findByID(workflowRename.groupId).map(_.name).getOrElse("N/A")
    val authorizationId = s"$groupName/${workflowRename.newName}"
    authorizeActionsByResourceId[Response](user, Map(ResourceWorkflow -> Edit), authorizationId) {
      workflowService.rename(workflowRename)
    }
  }

  def moveTo(workflowMove: WorkflowMove, user: Option[LoggedUser]): Unit = {
    val groupName = groupService.findByID(workflowMove.groupTargetId).map(_.name).getOrElse("N/A")
    val authorizationId = s"$groupName/${workflowMove.workflowName}"
    authorizeActionsByResourceId[ResponseWorkflowsDto](user, Map(ResourceWorkflow -> Edit), authorizationId) {
      Try {
        val workflowsDto: Seq[WorkflowDto] = workflowService.moveTo(workflowMove)
        workflowsDto
      } recover {
        case _: NoNodeException => Seq.empty[WorkflowDto]
      }
    }
  }

  def migrateWorkflowFromCassiopeia(workflowCassiopeia: WorkflowCassiopeia, user: Option[LoggedUser]): Unit = {
    authorizeActions[ResponseWorkflowAndromeda](user, Map(ResourceWorkflow -> Edit)) {
      Try {
        migrationService.migrateWorkflowFromCassiopeia(workflowCassiopeia)
      }
    }
  }
}

object WorkflowActor extends SLF4JLogging {

  case class Run(id: String, user: Option[LoggedUser])

  case class Stop(id: String, user: Option[LoggedUser])

  case class Reset(id: String, user: Option[LoggedUser])

  case class ValidateWorkflow(workflow: Workflow, user: Option[LoggedUser])

  case class CreateWorkflow(workflow: Workflow, user: Option[LoggedUser])

  case class CreateWorkflows(workflows: Seq[Workflow], user: Option[LoggedUser])

  case class Update(workflow: Workflow, user: Option[LoggedUser])

  case class UpdateList(workflows: Seq[Workflow], user: Option[LoggedUser])

  case class DeleteWorkflow(id: String, user: Option[LoggedUser])

  case class DeleteWorkflowWithAllVersions(query: WorkflowDelete, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class DeleteList(workflowIds: Seq[String], user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class FindAllWithEnv(user: Option[LoggedUser])

  case class FindAllMonitoring(user: Option[LoggedUser])

  case class Find(id: String, user: Option[LoggedUser])

  case class FindAllByGroup(groupID: String, user: Option[LoggedUser])

  case class FindWithEnv(id: String, user: Option[LoggedUser])

  case class FindByIdList(workflowIds: Seq[String], user: Option[LoggedUser])

  case class Query(query: WorkflowQuery, user: Option[LoggedUser])

  case class CreateWorkflowVersion(query: WorkflowVersion, user: Option[LoggedUser])

  case class RenameWorkflow(query: WorkflowRename, user: Option[LoggedUser])

  case class DeleteCheckpoint(id: String, user: Option[LoggedUser])

  case class ResetAllStatuses(user: Option[LoggedUser])

  case class MoveWorkflow(query: WorkflowMove, user: Option[LoggedUser])

  case class MigrateFromCassiopeia(workflowCassiopeia:WorkflowCassiopeia, user: Option[LoggedUser])

  type Response = Try[Unit]

  type ResponseAny = Try[Any]

  type ResponseWorkflows = Try[Seq[Workflow]]

  type ResponseWorkflowsDto = Try[Seq[WorkflowDto]]

  type ResponseWorkflow = Try[Workflow]

  type ResponseWorkflowValidation = Try[WorkflowValidation]

  type ResponseWorkflowAndromeda = Try[WorkflowAndromeda]
}

