/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import akka.actor._
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.workflow.step.DebugResults
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.actor.DebugWorkflowInMemoryApi._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.DebugWorkflow
import com.stratio.sparta.serving.core.services.DebugWorkflowService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework

import scala.util.Try

class DebugWorkflowActor(val curatorFramework: CuratorFramework,
                         inMemoryDebugWorkflowApi: ActorRef)
                        (implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  import DebugWorkflowActor._

  val ResourceWorkflow = "workflow"
  val ResourceStatus = "status"
  private val debugWorkflowService = new DebugWorkflowService(curatorFramework)

  override def receive: Receive = {
    case CreateDebugWorkflow(workflow, user) => createDebugWorkflow(workflow, user)
    case Find(id, user) => find(id, user)
    case FindAll(user) => findAll(user)
    case GetResults(id, user) => getResults(id, user)
    case Run(id, user) => run(id, user)
  }

  //@TODO[fl] controllare permessi dyplon

  def createDebugWorkflow(debugWorkflow: DebugWorkflow, user: Option[LoggedUser]) : Unit = {
    securityActionAuthorizer[ResponseDebugWorkflow](user, Map(ResourceWorkflow -> Create, ResourceStatus -> Create)) {
      debugWorkflowService.createDebugWorkflow(debugWorkflow)
    }
  }

  def find(id: String, user: Option[LoggedUser]) : Unit =
    securityActionAuthorizer(
    user,
    Map(ResourceWorkflow -> View, ResourceStatus -> View),
    Option(inMemoryDebugWorkflowApi)
  ) {
      FindMemoryDebugWorkflow(id)
  }

  def findAll(user: Option[LoggedUser]) : Unit =  securityActionAuthorizer(
    user,
    Map(ResourceWorkflow -> View, ResourceStatus -> View),
    Option(inMemoryDebugWorkflowApi)
  ) {
    FindAllMemoryDebugWorkflows
  }

  def getResults(id: String, user: Option[LoggedUser]) : Unit =
    securityActionAuthorizer(user,
    Map(ResourceWorkflow -> View, ResourceStatus -> View),
      Option(inMemoryDebugWorkflowApi)) {
      FindMemoryDebugResultsWorkflow(id)
    }

  //@TODO[fl] add the actual running
  def run(id: String, user: Option[LoggedUser]) : Unit = {
    val actions = Map(ResourceStatus -> Edit)
    securityActionAuthorizer(user, actions) {
      Try {
        id
      }
    }
  }

}

object DebugWorkflowActor extends SLF4JLogging {

  case class CreateDebugWorkflow(debugWorkflow: DebugWorkflow, user: Option[LoggedUser])

  case class Find(id: String, user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class GetResults(id: String, user: Option[LoggedUser])

  case class Run(id: String, user: Option[LoggedUser])


  type ResponseDebugWorkflow = Try[DebugWorkflow]

  type ResponseDebugWorkflows = Try[Seq[DebugWorkflow]]

  type ResponseResult = Try[DebugResults]

  type ResponseRun = Try[String]

  type ResponseAny = Try[Any]

}
