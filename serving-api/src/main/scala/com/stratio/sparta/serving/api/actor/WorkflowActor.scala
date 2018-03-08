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
import com.stratio.sparta.serving.core.services.{WorkflowService, WorkflowValidatorService}
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, CheckpointUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException

import scala.util.Try

class WorkflowActor(
                     val curatorFramework: CuratorFramework,
                     inMemoryWorkflowApi: ActorRef,
                     launcherActor: ActorRef,
                     envStateActor: ActorRef
                   )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with CheckpointUtils with ActionUserAuthorize {

  import WorkflowActor._
  import WorkflowDtoImplicit._

  val ResourceWorkflow = "workflow"
  val ResourceCP = "checkpoint"
  val ResourceStatus = "status"
  val ResourceEnvironment = "environment"

  private val workflowService = new WorkflowService(curatorFramework)
  private val wServiceWithEnv = new WorkflowService(curatorFramework, Option(context.system), Option(envStateActor))
  private val workflowValidatorService = new WorkflowValidatorService(Option(curatorFramework))

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
    case _ => log.info("Unrecognized message in Workflow Actor")
  }

  //scalastyle:on

  def run(id: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceStatus -> Edit)
    securityActionAuthorizer[Response](user, actions) {
      Try {
        launcherActor.forward(Launch(id.toString, user))
      }
    }
  }

  def stop(id: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceStatus -> Edit)
    securityActionAuthorizer[ResponseAny](user, actions) {
      workflowService.stop(id)
    }
  }

  def reset(id: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceStatus -> Edit)
    securityActionAuthorizer[ResponseAny](user, actions) {
      workflowService.reset(id)
    }
  }

  def validate(workflow: Workflow, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflowValidation](user, Map(ResourceWorkflow -> View, ResourceStatus -> View)) {
      Try {
        val withEnv = wServiceWithEnv.applyEnv(workflow)
        val basicValidation = workflowValidatorService.validate(workflow)
        val advancedValidation = workflowValidatorService.validateJsoneySettings(withEnv)
        basicValidation.combineWithAnd(basicValidation, advancedValidation)
      }
    }

  def findAll(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceWorkflow -> View, ResourceStatus -> View),
      Option(inMemoryWorkflowApi)
    ) {
      FindAllMemoryWorkflowRaw
    }

  def findAllMonitoring(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceWorkflow -> View, ResourceStatus -> View),
      Option(inMemoryWorkflowApi)
    ) {
      FindAllMemoryWorkflowDto
    }

  def findAllWithEnv(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceWorkflow -> View, ResourceStatus -> View, ResourceEnvironment -> View),
      Option(inMemoryWorkflowApi)
    ) {
      FindAllMemoryWorkflowWithEnv
    }

  def find(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceWorkflow -> View, ResourceStatus -> View),
      Option(inMemoryWorkflowApi)
    ) {
      FindMemoryWorkflowRaw(id)
    }

  def findAllByGroup(groupID: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceWorkflow -> View, ResourceStatus -> View),
      Option(inMemoryWorkflowApi)
    ) {
      FindByGroupMemoryWorkflowDto(groupID)
    }

  def findWithEnv(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceWorkflow -> View, ResourceStatus -> View, ResourceEnvironment -> View),
      Option(inMemoryWorkflowApi)
    ) {
      FindMemoryWorkflowWithEnv(id)
    }

  def findByIdList(workflowIds: Seq[String], user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceWorkflow -> View, ResourceStatus -> View),
      Option(inMemoryWorkflowApi)
    ) {
      FindByIdsMemoryWorkflowRaw(workflowIds)
    }

  def doQuery(query: WorkflowQuery, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceWorkflow -> View, ResourceStatus -> View),
      Option(inMemoryWorkflowApi)
    ) {
      FindByQueryMemoryWorkflowRaw(query)
    }

  def create(workflow: Workflow, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflow](user, Map(ResourceWorkflow -> Create, ResourceStatus -> Create)) {
      Try {
        val withEnv = wServiceWithEnv.applyEnv(workflow)
        wServiceWithEnv.create(workflow, Option(withEnv))
      }
    }

  def createList(workflows: Seq[Workflow], user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflows](user, Map(ResourceWorkflow -> Create, ResourceStatus -> Create)) {
      Try {
        val withEnv = workflows.map(workflow => wServiceWithEnv.applyEnv(workflow))
        wServiceWithEnv.createList(workflows, withEnv)
      }
    }

  def update(workflow: Workflow, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceWorkflow -> Edit)) {
      Try {
        val withEnv = wServiceWithEnv.applyEnv(workflow)
        wServiceWithEnv.update(workflow, Option(withEnv))
      }.recover {
        case _: NoNodeException =>
          throw new ServerException(s"No workflow with name ${workflow.name}.")
      }
    }

  def updateList(workflows: Seq[Workflow], user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceWorkflow -> Edit)) {
      Try {
        val withEnv = workflows.map(workflow => wServiceWithEnv.applyEnv(workflow))
        wServiceWithEnv.updateList(workflows, withEnv)
      }
    }

  def delete(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Response](user, Map(ResourceWorkflow -> Delete, ResourceCP -> Delete)) {
      workflowService.delete(id)
    }

  def deleteWithAllVersion(workflowDelete: WorkflowDelete, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Response](user, Map(ResourceWorkflow -> Delete, ResourceCP -> Delete)) {
      workflowService.deleteWithAllVersions(workflowDelete)
    }

  def deleteList(workflowIds: Seq[String], user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Response](user,
      Map(ResourceWorkflow -> Delete, ResourceStatus -> Delete, ResourceCP -> Delete)) {
      workflowService.deleteList(workflowIds)
    }

  def deleteAll(user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceWorkflow -> Delete, ResourceStatus -> Delete, ResourceCP -> Delete)
    securityActionAuthorizer[Response](user, actions) {
      workflowService.deleteAll()
    }
  }

  def resetAllStatuses(user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceStatus -> Edit)
    securityActionAuthorizer[Response](user, actions) {
      workflowService.resetAllStatuses()
    }
  }

  def deleteCheckpoint(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Response](user, Map(ResourceCP -> Delete, ResourceWorkflow -> View)) {
      Try(deleteCheckpointPath(workflowService.findById(id)))
    }

  def createVersion(workflowVersion: WorkflowVersion, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflow](user, Map(ResourceWorkflow -> Create, ResourceStatus -> Create)) {
      Try(workflowService.createVersion(workflowVersion))
    }

  def rename(workflowRename: WorkflowRename, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Response](user, Map(ResourceWorkflow -> Edit)) {
      workflowService.rename(workflowRename)
    }

  def moveTo(workflowMove: WorkflowMove, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflowsDto](user, Map(ResourceWorkflow -> Edit)) {
      Try {
        val workflowsDto: Seq[WorkflowDto] = workflowService.moveTo(workflowMove)
        workflowsDto
      } recover {
        case _: NoNodeException => Seq.empty[WorkflowDto]
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

  type Response = Try[Unit]

  type ResponseAny = Try[Any]

  type ResponseWorkflows = Try[Seq[Workflow]]

  type ResponseWorkflowsDto = Try[Seq[WorkflowDto]]

  type ResponseWorkflow = Try[Workflow]

  type ResponseWorkflowValidation = Try[WorkflowValidation]

}

