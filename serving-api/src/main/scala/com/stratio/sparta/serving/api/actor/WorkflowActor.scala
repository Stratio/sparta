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
import com.stratio.sparta.serving.core.actor.StatusActor.ResponseStatus
import com.stratio.sparta.serving.core.actor.TemplateActor.ResponseTemplate
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.{ResponseWorkflow, TemplateElement, Workflow, WorkflowStatus}
import com.stratio.sparta.serving.core.services.WorkflowService
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, CheckpointUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException

import scala.util.{Failure, Success, Try}

class WorkflowActor(val curatorFramework: CuratorFramework, statusActor: ActorRef,
                    val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with CheckpointUtils with ActionUserAuthorize {

  import WorkflowActor._

  //TODO change dyplon to new names: workflow -> workflow
  val ResourcePol = "policy"
  val ResourceCP = "checkpoint"
  val ResourceContext = "context"
  private val workflowService = new WorkflowService(curatorFramework)

  //scalastyle:off
  override def receive: Receive = {
    case CreateWorkflow(workflow, user) => create(workflow, user)
    case Update(workflow, user) => update(workflow, user)
    case DeleteWorkflow(id, user) => delete(id, user)
    case Find(id, user) => find(id, user)
    case FindByName(name, user) => findByName(name.toLowerCase, user)
    case FindAll(user) => findAll(user)
    case DeleteAll(user) => deleteAll(user)
    case FindByTemplateType(fragmentType, user) => findByTemplateType(fragmentType, user)
    case FindByTemplateName(fragmentType, name, user) => findByTemplateName(fragmentType, name, user)
    case DeleteCheckpoint(workflow, user) => deleteCheckpoint(workflow, user)
    case ResponseTemplate(fragment) => loggingResponseTemplate(fragment)
    case ResponseStatus(status) => loggingResponseWorkflowStatus(status)
    case _ => log.info("Unrecognized message in Workflow Actor")
  }

  //scalastyle:on

  def findAll(user: Option[LoggedUser]): Unit = {
    def callback() = ResponseWorkflows(Try {
      workflowService.findAll
    }.recover {
      case _: NoNodeException => Seq.empty[Workflow]
    })

    securityActionAuthorizer[ResponseWorkflows](secManagerOpt, user, Map(ResourcePol -> View), callback)
  }

  def findByTemplateType(fragmentType: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseWorkflows(
      Try(workflowService.findByTemplateType(fragmentType)).recover {
        case _: NoNodeException => Seq.empty[Workflow]
      })

    securityActionAuthorizer[ResponseWorkflows](secManagerOpt, user, Map(ResourcePol -> View), callback)
  }

  def findByTemplateName(fragmentType: String, name: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseWorkflows(
      Try(workflowService.findByTemplateName(fragmentType, name)).recover {
        case _: NoNodeException => Seq.empty[Workflow]
      })

    securityActionAuthorizer[ResponseWorkflows](secManagerOpt, user, Map(ResourcePol -> View), callback)
  }

  def find(id: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseWorkflow(Try(workflowService.findById(id)).recover {
      case _: NoNodeException =>
        throw new ServerException(s"No workflow with id $id.")
    })

    securityActionAuthorizer[ResponseWorkflow](secManagerOpt, user, Map(ResourcePol -> View), callback)
  }

  def findByName(name: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseWorkflow(Try(workflowService.findByName(name)))

    securityActionAuthorizer[ResponseWorkflow](secManagerOpt, user, Map(ResourcePol -> View), callback)
  }

  def create(workflow: Workflow, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseWorkflow(Try(workflowService.create(workflow)))

    securityActionAuthorizer[ResponseWorkflow](secManagerOpt,
      user,
      Map(ResourcePol -> Create, ResourceContext -> Create),
      callback)
  }

  def update(workflow: Workflow, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseWorkflow(Try(workflowService.update(workflow)).recover {
      case _: NoNodeException =>
        throw new ServerException(s"No workflow with name ${workflow.name}.")
    })

    securityActionAuthorizer(secManagerOpt, user, Map(ResourcePol -> Edit), callback)
  }

  def delete(id: String, user: Option[LoggedUser]): Unit = {
    def callback() = Response(workflowService.delete(id))

    securityActionAuthorizer[Response](secManagerOpt,
      user,
      Map(ResourcePol -> Delete, ResourceCP -> Delete),
      callback)
  }

  def deleteAll(user: Option[LoggedUser]): Unit = {
    def callback() = Response(workflowService.deleteAll())

    securityActionAuthorizer[Response](secManagerOpt,
      user,
      Map(ResourcePol -> Delete, ResourceContext -> Delete, ResourceCP -> Delete),
      callback)
  }

  def deleteCheckpoint(workflow: Workflow, user: Option[LoggedUser]): Unit = {
    def callback() = Response(Try(deleteCheckpointPath(workflow)))

    securityActionAuthorizer[Response](secManagerOpt, user,
      Map(ResourceCP -> Delete, ResourcePol -> View),
      callback
    )
  }

  def loggingResponseTemplate(response: Try[TemplateElement]): Unit =
    response match {
      case Success(fragment) =>
        log.info(s"Template created correctly: \n\tId: ${fragment.id}\n\tName: ${fragment.name}")
      case Failure(e) =>
        log.error(s"Template creation failure. Error: ${e.getLocalizedMessage}", e)
    }

  def loggingResponseWorkflowStatus(response: Try[WorkflowStatus]): Unit =
    response match {
      case Success(statusModel) =>
        log.info(s"Workflow status model created or updated correctly: " +
          s"\n\tId: ${statusModel.id}\n\tStatus: ${statusModel.status}")
      case Failure(e) =>
        log.error(s"Workflow status model creation failure. Error: ${e.getLocalizedMessage}", e)
    }
}

object WorkflowActor extends SLF4JLogging {

  case class CreateWorkflow(workflow: Workflow, user: Option[LoggedUser])

  case class Update(workflow: Workflow, user: Option[LoggedUser])

  case class DeleteWorkflow(name: String, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class Find(id: String, user: Option[LoggedUser])

  case class FindByName(name: String, user: Option[LoggedUser])

  case class FindByTemplateType(templateType: String, user: Option[LoggedUser])

  case class FindByTemplateName(templateType: String, name: String, user: Option[LoggedUser])

  case class DeleteCheckpoint(workflow: Workflow, user: Option[LoggedUser])

  case class Response(status: Try[_])

  case class ResponseWorkflows(workflow: Try[Seq[Workflow]])

}