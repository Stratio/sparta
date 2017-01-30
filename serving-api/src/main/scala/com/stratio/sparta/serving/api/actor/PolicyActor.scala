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
import com.stratio.sparta.serving.core.actor.FragmentActor.ResponseFragment
import com.stratio.sparta.serving.core.actor.StatusActor
import com.stratio.sparta.serving.core.actor.StatusActor.ResponseStatus
import com.stratio.sparta.serving.core.dao.ErrorDAO
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, PolicyUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException

import scala.util.Try

/**
  * Implementation of supported CRUD operations over ZK needed to manage policies.
  */
class PolicyActor(curatorFramework: CuratorFramework, statusActor: ActorRef, fragmentActorRef: ActorRef)
  extends Actor with PolicyUtils with CheckpointUtils {

  import PolicyActor._

  override val fragmentActor: Option[ActorRef] = Some(fragmentActorRef)

  //scalastyle:off
  override def receive: Receive = {
    case Create(policy) => create(policy)
    case Update(policy) => update(policy)
    case Delete(id) => delete(id)
    case Find(id) => find(id)
    case FindByName(name) => findByName(name.toLowerCase)
    case FindAll() => findAll()
    case DeleteAll() => deleteAll()
    case FindByFragmentType(fragmentType) => findByFragmentType(fragmentType)
    case FindByFragment(fragmentType, id) => findByFragmentId(fragmentType, id)
    case FindByFragmentName(fragmentType, name) => findByFragmentName(fragmentType, name)
    case DeleteCheckpoint(policy) => deleteCheckpoint(policy)
    case ResponseFragment(fragment) => loggingResponseFragment(fragment)
    case ResponseStatus(status) => loggingResponsePolicyStatus(status)
    case Error(id) => error(id)
    case _ => log.info("Unrecognized message in Policy Actor")
  }

  //scalastyle:on

  def findAll(): Unit =
    sender ! ResponsePolicies(Try {
      getPolicies(curatorFramework, withFragments = true)
    }.recover {
      case e: NoNodeException => Seq.empty[PolicyModel]
    })

  def deleteAll(): Unit =
    sender ! ResponsePolicies(Try {
      val policiesModels = getPolicies(curatorFramework, withFragments = false)
      policiesModels.foreach(policyModel => {
        deleteCheckpointPath(policyModel)
        deletePolicy(policyModel, curatorFramework)
      })
      policiesModels
    }.recover {
      case e: NoNodeException => throw new ServingCoreException(
        ErrorModel.toString(new ErrorModel(ErrorModel.CodeErrorDeletingPolicy, s"Error deleting policies"))
      )
    })

  def findByFragmentType(fragmentType: String): Unit =
    sender ! ResponsePolicies(
      Try {
        getPolicies(curatorFramework, withFragments = true)
          .filter(apm => apm.fragments.exists(f => f.fragmentType == fragmentType))
      }.recover {
        case e: NoNodeException => Seq.empty[PolicyModel]
      })

  def findByFragmentId(fragmentType: String, id: String): Unit =
    sender ! ResponsePolicies(
      Try {
        getPolicies(curatorFramework, withFragments = true)
          .filter(apm => apm.fragments.exists(f =>
            if (f.id.isDefined)
              f.id.get == id && f.fragmentType == fragmentType
            else false
          ))
      }.recover {
        case e: NoNodeException => Seq.empty[PolicyModel]
      })

  def findByFragmentName(fragmentType: String, name: String): Unit =
    sender ! ResponsePolicies(
      Try {
        getPolicies(curatorFramework, withFragments = true)
          .filter(apm => apm.fragments.exists(f => f.name == name && f.fragmentType == fragmentType))
      }.recover {
        case e: NoNodeException => Seq.empty[PolicyModel]
      })

  def find(id: String): Unit =
    sender ! ResponsePolicy(Try {
      policyWithFragments(byId(id, curatorFramework), withFragmentCreation = false)
    }.recover {
      case e: NoNodeException =>
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy with id $id.")
        ))
    })

  def findByName(name: String): Unit =
    sender ! ResponsePolicy(Try {
      existsByNameId(name, None, curatorFramework).map(policy =>
        policyWithFragments(policy, withFragmentCreation = false))
        .getOrElse(throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsPolicyWithName, s"No policy with name $name"))))
    })

  def create(policy: PolicyModel): Unit =
    sender ! ResponsePolicy(Try {
      val searchPolicy = existsByNameId(policy.name, policy.id, curatorFramework)
      if (searchPolicy.isDefined) {
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeExistsPolicyWithName,
            s"Policy with name ${policy.name} exists. The actual policy name is: ${searchPolicy.get.name}")
        ))
      }
      val policySaved = writePolicy(policyWithId(policy), curatorFramework)
      statusActor ! StatusActor.Create(PolicyStatusModel(
        id = policySaved.id.get,
        status = PolicyStatusEnum.NotStarted,
        name = Option(policy.name),
        description = Option(policy.description)
      ))
      policySaved
    })

  def update(policy: PolicyModel): Unit = {
    val response = ResponsePolicy(Try {
      val searchPolicy = existsByNameId(policy.name, policy.id, curatorFramework)
      if (searchPolicy.isEmpty) {
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeExistsPolicyWithName,
            s"Policy with name ${policy.name} not exists.")
        ))
      } else {
        val policySaved = updatePolicy(policyWithId(policy), curatorFramework)
        statusActor ! StatusActor.Update(PolicyStatusModel(
          id = policySaved.id.get,
          status = PolicyStatusEnum.NotDefined,
          name = Option(policy.name),
          description = Option(policy.description)
        ))
        policySaved
      }
    }.recover {
      case e: NoNodeException =>
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy with name ${policy.name}.")
        ))
    })
    sender ! response
  }

  def delete(id: String): Unit =
    sender ! Response(Try {
      val policyModel = byId(id, curatorFramework)
      deleteCheckpointPath(policyModel)
      deletePolicy(policyModel, curatorFramework)
    }.recover {
      case e: NoNodeException =>
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId,
            s"No policy with id $id.")
        ))
    })

  def deleteCheckpoint(policy: PolicyModel): Unit =
    sender ! Response(Try(deleteCheckpointPath(policy)))

  def error(id: String): Unit = {
    sender ! Try[PolicyErrorModel] {
      ErrorDAO.getInstance.dao.get(id).getOrElse(throw new NoSuchElementException(s"No error data found for $id"))
    }
  }
}

object PolicyActor extends SLF4JLogging {

  case class Create(policy: PolicyModel)

  case class Update(policy: PolicyModel)

  case class Delete(name: String)

  case class DeleteAll()

  case class FindAll()

  case class Find(id: String)

  case class FindByName(name: String)

  case class FindByFragmentType(fragmentType: String)

  case class FindByFragment(fragmentType: String, id: String)

  case class FindByFragmentName(fragmentType: String, name: String)

  case class DeleteCheckpoint(policy: PolicyModel)

  case class Response(status: Try[_])

  case class ResponsePolicies(policies: Try[Seq[PolicyModel]])

  case class ResponsePolicy(policy: Try[PolicyModel])

  case class Error(id: String)

}