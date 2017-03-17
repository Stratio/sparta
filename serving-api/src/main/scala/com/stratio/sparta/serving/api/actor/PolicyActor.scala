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
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel, ResponsePolicy}
import com.stratio.sparta.serving.core.models.policy.fragment.FragmentElementModel
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, PolicyUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException

import scala.util.{Failure, Success, Try}

/**
  * Implementation of supported CRUD operations over ZK needed to manage policies.
  */
class PolicyActor(val curatorFramework: CuratorFramework, statusActor: ActorRef)
  extends Actor with PolicyUtils with CheckpointUtils {

  import PolicyActor._

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
    case _ => log.info("Unrecognized message in Policy Actor")
  }

  //scalastyle:on

  def findAll(): Unit =
    sender ! ResponsePolicies(Try {
      findAllPolicies(withFragments = true)
    }.recover {
      case e: NoNodeException => Seq.empty[PolicyModel]
    })

  def deleteAll(): Unit =
    sender ! ResponsePolicies(Try(deleteAllPolicies()).recover {
      case e: NoNodeException => throw new ServingCoreException(
        ErrorModel.toString(new ErrorModel(ErrorModel.CodeErrorDeletingPolicy, s"Error deleting policies")))
    })

  def findByFragmentType(fragmentType: String): Unit =
    sender ! ResponsePolicies(
      Try(findPoliciesByFragmentType(fragmentType)).recover {
        case e: NoNodeException => Seq.empty[PolicyModel]
      })

  def findByFragmentId(fragmentType: String, id: String): Unit =
    sender ! ResponsePolicies(
      Try(findPoliciesByFragmentId(fragmentType, id)).recover {
        case e: NoNodeException => Seq.empty[PolicyModel]
      })

  def findByFragmentName(fragmentType: String, name: String): Unit =
    sender ! ResponsePolicies(
      Try(findPoliciesByFragmentName(fragmentType, name)).recover {
        case e: NoNodeException => Seq.empty[PolicyModel]
      })

  def find(id: String): Unit =
    sender ! ResponsePolicy(Try(findPolicy(id)).recover {
      case e: NoNodeException =>
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy with id $id.")
        ))
    })

  def findByName(name: String): Unit =
    sender ! ResponsePolicy(Try(findPolicyByName(name)))

  def create(policy: PolicyModel): Unit =
    sender ! ResponsePolicy(Try(createPolicy(policy)))

  def update(policy: PolicyModel): Unit = {
    sender ! ResponsePolicy(Try(updatePolicy(policy)).recover {
      case e: NoNodeException =>
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy with name ${policy.name}.")
        ))
    })
  }

  def delete(id: String): Unit =
    sender ! Response(Try {

    }.recover {
      case e: NoNodeException =>
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId,
            s"No policy with id $id.")
        ))
    })

  def deleteCheckpoint(policy: PolicyModel): Unit =
    sender ! Response(Try(deleteCheckpointPath(policy)))


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

}