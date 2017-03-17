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
import com.stratio.sparta.serving.core.actor.FragmentActor.ResponseFragment
import com.stratio.sparta.serving.core.actor.StatusActor.ResponseStatus
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.policy.fragment.FragmentElementModel
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel, ResponsePolicy}
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, CheckpointUtils, PolicyUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException

import scala.util.{Failure, Success, Try}

/**
  * Implementation of supported CRUD operations over ZK needed to manage policies.
  */
class PolicyActor(val curatorFramework: CuratorFramework, statusActor: ActorRef,
                  val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with PolicyUtils with CheckpointUtils with ActionUserAuthorize{

  import PolicyActor._

  val ResourcePol = "policy"
  val ResourceCP = "checkpoint"

  //scalastyle:off
  override def receive: Receive = {
    case CreatePolicy(policy, user) => create(policy, user)
    case Update(policy, user) => update(policy, user)
    case DeletePolicy(id, user) => delete(id, user)
    case Find(id, user) => find(id, user)
    case FindByName(name, user) => findByName(name.toLowerCase, user)
    case FindAll(user) => findAll(user)
    case DeleteAll(user) => deleteAll(user)
    case FindByFragmentType(fragmentType, user) => findByFragmentType(fragmentType, user)
    case FindByFragment(fragmentType, id, user) => findByFragmentId(fragmentType, id, user)
    case FindByFragmentName(fragmentType, name, user) => findByFragmentName(fragmentType, name, user)
    case DeleteCheckpoint(policy, user) => deleteCheckpoint(policy, user)
    case ResponseFragment(fragment) => loggingResponseFragment(fragment)
    case ResponseStatus(status) => loggingResponsePolicyStatus(status)
    case _ => log.info("Unrecognized message in Policy Actor")
  }

  //scalastyle:on

  def findAll(user: Option[LoggedUser]): Unit = {
    def callback() = ResponsePolicies(Try {
      findAllPolicies(withFragments = true)
    }.recover {
      case _: NoNodeException => Seq.empty[PolicyModel]
    })

    securityActionAuthorizer[ResponsePolicies](secManagerOpt, user, Map(ResourcePol -> View), callback)
  }

  def deleteAll(user: Option[LoggedUser]): Unit = {
    def callback() = ResponsePolicies(Try(deleteAllPolicies()).recover {
      case _: NoNodeException => throw new ServingCoreException(
        ErrorModel.toString(new ErrorModel(ErrorModel.CodeErrorDeletingPolicy, s"Error deleting policies")))
    })

    securityActionAuthorizer[ResponsePolicies](secManagerOpt, user, Map(ResourcePol -> Delete), callback)
  }

  def findByFragmentType(fragmentType: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponsePolicies(
      Try(findPoliciesByFragmentType(fragmentType)).recover {
        case _: NoNodeException => Seq.empty[PolicyModel]
      })

    securityActionAuthorizer[ResponsePolicies](secManagerOpt, user, Map(ResourcePol -> View), callback)
  }

  def findByFragmentId(fragmentType: String, id: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponsePolicies(
      Try(findPoliciesByFragmentId(fragmentType, id)).recover {
        case _: NoNodeException => Seq.empty[PolicyModel]
      })

    securityActionAuthorizer[ResponsePolicies](secManagerOpt, user, Map(ResourcePol -> View), callback)
  }

  def findByFragmentName(fragmentType: String, name: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponsePolicies(
      Try(findPoliciesByFragmentName(fragmentType, name)).recover {
        case _: NoNodeException => Seq.empty[PolicyModel]
      })

    securityActionAuthorizer[ResponsePolicies](secManagerOpt, user, Map(ResourcePol -> View), callback)
  }

  def find(id: String, user: Option[LoggedUser]): Unit = {
    def callback() =  ResponsePolicy(Try(findPolicy(id)).recover {
      case _: NoNodeException =>
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy with id $id.")
        ))
    })

    securityActionAuthorizer[ResponsePolicy](secManagerOpt, user, Map(ResourcePol -> View), callback)
  }

  def findByName(name: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponsePolicy(Try(findPolicyByName(name)))

    securityActionAuthorizer[ResponsePolicy](secManagerOpt, user, Map(ResourcePol -> View), callback)
  }


  def create(policy: PolicyModel, user: Option[LoggedUser]): Unit = {
    def callback() = ResponsePolicy(Try(createPolicy(policy)))
    securityActionAuthorizer[ResponsePolicy](secManagerOpt, user, Map(ResourcePol -> Create), callback)
  }

  def update(policy: PolicyModel, user: Option[LoggedUser]): Unit = {
    def callback() = ResponsePolicy(Try(updatePolicy(policy)).recover {
      case _: NoNodeException =>
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy with name ${policy.name}.")
        ))
    })
    securityActionAuthorizer[ResponsePolicy](secManagerOpt, user, Map(ResourcePol -> Edit), callback)
  }

  def delete(id: String, user: Option[LoggedUser]): Unit = {
    def callback() = Response(Try {
      deletePolicy(id)
    }.recover {
      case _: NoNodeException =>
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId,
            s"No policy with id $id.")
        ))
    })

    securityActionAuthorizer[Response](secManagerOpt, user, Map(ResourcePol -> Delete), callback)
  }

  def deleteCheckpoint(policy: PolicyModel, user: Option[LoggedUser]): Unit = {
    def callback() = Response(Try(deleteCheckpointPath(policy)))

    securityActionAuthorizer[Response](secManagerOpt, user,
      Map(ResourceCP -> Delete, ResourcePol -> View),
      callback
      )
  }


  def loggingResponseFragment(response: Try[FragmentElementModel]): Unit =
    response match {
      case Success(fragment) =>
        log.info(s"Fragment created correctly: \n\tId: ${fragment.id}\n\tName: ${fragment.name}")
      case Failure(e) =>
        log.error(s"Fragment creation failure. Error: ${e.getLocalizedMessage}", e)
    }

  def loggingResponsePolicyStatus(response: Try[PolicyStatusModel]): Unit =
    response match {
      case Success(statusModel) =>
        log.info(s"Policy status model created or updated correctly: " +
          s"\n\tId: ${statusModel.id}\n\tStatus: ${statusModel.status}")
      case Failure(e) =>
        log.error(s"Policy status model creation failure. Error: ${e.getLocalizedMessage}", e)
    }
}

object PolicyActor extends SLF4JLogging {

  case class CreatePolicy(policy: PolicyModel, user: Option[LoggedUser])

  case class Update(policy: PolicyModel, user: Option[LoggedUser])

  case class DeletePolicy(name: String, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class Find(id: String, user: Option[LoggedUser])

  case class FindByName(name: String, user: Option[LoggedUser])

  case class FindByFragmentType(fragmentType: String, user: Option[LoggedUser])

  case class FindByFragment(fragmentType: String, id: String, user: Option[LoggedUser])

  case class FindByFragmentName(fragmentType: String, name: String, user: Option[LoggedUser])

  case class DeleteCheckpoint(policy: PolicyModel, user: Option[LoggedUser])

  case class Response(status: Try[_])

  case class ResponsePolicies(policies: Try[Seq[PolicyModel]])

}