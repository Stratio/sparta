/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, ActorRef}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowQuery, WorkflowValidation, WorkflowVersion}
import com.stratio.sparta.serving.core.services.{WorkflowService, WorkflowValidatorService}
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, CheckpointUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException

import scala.util.Try

class WorkflowActor(
                     val curatorFramework: CuratorFramework,
                     launcherActor: ActorRef,
                     envStateActor: ActorRef
                   )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with CheckpointUtils with ActionUserAuthorize {

  import WorkflowActor._

  val ResourceWorkflow = "workflow"
  val ResourceCP = "checkpoint"
  val ResourceStatus = "status"

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
    case FindAllByGroup(group, user) => findAllByGroup(group, user)
    case DeleteWorkflow(id, user) => delete(id, user)
    case DeleteList(workflowIds, user) => deleteList(workflowIds, user)
    case DeleteAll(user) => deleteAll(user)
    case DeleteCheckpoint(id, user) => deleteCheckpoint(id, user)
    case ResetAllStatuses(user) => resetAllStatuses(user)
    case ValidateWorkflow(workflow, user) => validate(workflow, user)
    case CreateWorkflowVersion(workflowVersion, user) => createVersion(workflowVersion, user)
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
    securityActionAuthorizer[ResponseWorkflowValidation](user, Map(ResourceWorkflow -> View)) {
      Try(workflowValidatorService.validate(workflow))
    }

  def findAll(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflows](user, Map(ResourceWorkflow -> View)) {
      Try {
        workflowService.findAll
      } recover {
        case _: NoNodeException => Seq.empty[Workflow]
      }
    }

  def findAllWithEnv(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflows](user, Map(ResourceWorkflow -> View)) {
      Try {
        wServiceWithEnv.findAll
      } recover {
        case _: NoNodeException => Seq.empty[Workflow]
      }
    }

  def find(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflow](user, Map(ResourceWorkflow -> View)) {
      Try(workflowService.findById(id)).recover {
        case _: NoNodeException =>
          throw new ServerException(s"No workflow with id $id.")
      }
    }

  def findAllByGroup(group: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflows](user, Map(ResourceWorkflow -> View)) {
      Try(workflowService.findByGroup(group)).recover {
        case _: NoNodeException => Seq.empty[Workflow]
      }
    }

  def findWithEnv(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflow](user, Map(ResourceWorkflow -> View)) {
      Try(wServiceWithEnv.findById(id)).recover {
        case _: NoNodeException =>
          throw new ServerException(s"No workflow with id $id.")
      }
    }

  def findByIdList(workflowIds: Seq[String], user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflows](user, Map(ResourceWorkflow -> View)) {
      Try(workflowService.findByIdList(workflowIds))
    }

  def doQuery(query: WorkflowQuery, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflow](user, Map(ResourceWorkflow -> View)) {
      Try(workflowService.find(query))
    }

  def create(workflow: Workflow, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflow](user, Map(ResourceWorkflow -> Create, ResourceStatus -> Create)) {
      Try(workflowService.create(workflow))
    }

  def createList(workflows: Seq[Workflow], user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseWorkflows](user, Map(ResourceWorkflow -> Create, ResourceStatus -> Create)) {
      Try(workflowService.createList(workflows))
    }

  def update(workflow: Workflow, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceWorkflow -> Edit)) {
      Try(workflowService.update(workflow)).recover {
        case _: NoNodeException =>
          throw new ServerException(s"No workflow with name ${workflow.name}.")
      }
    }

  def updateList(workflows: Seq[Workflow], user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceWorkflow -> Edit)) {
      Try(workflowService.updateList(workflows))
    }

  def delete(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Response](user, Map(ResourceWorkflow -> Delete, ResourceCP -> Delete)) {
      workflowService.delete(id)
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

  case class DeleteAll(user: Option[LoggedUser])

  case class DeleteList(workflowIds: Seq[String], user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class FindAllWithEnv(user: Option[LoggedUser])

  case class Find(id: String, user: Option[LoggedUser])

  case class FindAllByGroup(group: String, user: Option[LoggedUser])

  case class FindWithEnv(id: String, user: Option[LoggedUser])

  case class FindByIdList(workflowIds: Seq[String], user: Option[LoggedUser])

  case class Query(query: WorkflowQuery, user: Option[LoggedUser])

  case class CreateWorkflowVersion(query: WorkflowVersion, user: Option[LoggedUser])

  case class DeleteCheckpoint(id: String, user: Option[LoggedUser])

  case class ResetAllStatuses(user: Option[LoggedUser])

  type Response = Try[Unit]

  type ResponseAny = Try[Any]

  type ResponseWorkflows = Try[Seq[Workflow]]

  type ResponseWorkflow = Try[Workflow]

  type ResponseWorkflowValidation = Try[WorkflowValidation]

}