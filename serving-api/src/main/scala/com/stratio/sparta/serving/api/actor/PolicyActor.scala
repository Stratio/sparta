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

import java.util.UUID
import scala.collection.JavaConversions
import scala.util.Try

import akka.actor.{Actor, ActorRef}
import akka.event.slf4j.SLF4JLogging
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException
import org.json4s.jackson.Serialization.write

import com.stratio.sparta.serving.api.constants.ActorsConstant
import com.stratio.sparta.serving.api.utils.PolicyUtils
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.serving.core.policy.status.{PolicyStatusActor, PolicyStatusEnum}

/**
 * Implementation of supported CRUD operations over ZK needed to manage policies.
 */
class PolicyActor(curatorFramework: CuratorFramework, policyStatusActor: ActorRef)
  extends Actor
    with SpartaSerializer
    with PolicyUtils {

  import PolicyActor._

  override def receive: Receive = {
    case Create(policy) => create(policy)
    case Update(policy) => update(policy)
    case Delete(id) => delete(id)
    case Find(id) => find(id)
    case FindByName(name) => findByName(name.toLowerCase)
    case FindAll() => findAll()
    case FindByFragment(fragmentType, id) => findByFragment(fragmentType, id)
  }

  def findAll(): Unit =
    sender ! ResponsePolicies(Try({
      val children = curatorFramework.getChildren.forPath(s"${AppConstant.PoliciesBasePath}")
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        byId(element, curatorFramework)).toSeq
    }).recover {
      case e: NoNodeException => Seq()
    })

  def findByFragment(fragmentType: String, id: String): Unit =
    sender ! ResponsePolicies(Try({
      val children = curatorFramework.getChildren.forPath(s"${AppConstant.PoliciesBasePath}")
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        byId(element, curatorFramework)).filter(apm =>
        apm.fragments.exists(f => f.id.get == id)).toSeq
    }).recover {
      case e: NoNodeException => Seq()
    })

  def find(id: String): Unit =
    sender ! new ResponsePolicy(Try({
      byId(id, curatorFramework)
    }).recover {
      case e: NoNodeException => throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy with id $id.")
      ))
    })

  def findByName(name: String): Unit =
    sender ! ResponsePolicy(Try({
      val children = curatorFramework.getChildren.forPath(s"${AppConstant.PoliciesBasePath}")
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        byId(element, curatorFramework)).filter(policy => policy.name == name).head
    }).recover {
      case e: NoNodeException => throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsPolicyWithName, s"No policy with name $name.")
      ))
      case e: NoSuchElementException => throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsPolicyWithName, s"No policy with name $name.")
      ))
    })

  def associateStatus(model: AggregationPoliciesModel): Unit = {
    policyStatusActor ! PolicyStatusActor.Create(PolicyStatusModel(model.id.get, PolicyStatusEnum.NotStarted))
  }

  def create(policy: AggregationPoliciesModel): Unit =
    sender ! ResponsePolicy(Try({
      val searchPolicy = existsByNameId(policy.name, None, curatorFramework)
      if (searchPolicy.isDefined) {
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeExistsPolicyWithName,
            s"Policy with name ${policy.name} exists. The actual policty id is: ${searchPolicy.get.id}")
        ))
      }
      val policyS = policy.copy(id = Some(s"${UUID.randomUUID.toString}"),
        name = policy.name.toLowerCase,
        version = Some(ActorsConstant.UnitVersion))
      curatorFramework.create().creatingParentsIfNeeded().forPath(
        s"${AppConstant.PoliciesBasePath}/${policyS.id.get}", write(policyS).getBytes)

      associateStatus(policyS)

      policyS
    }))

  def update(policy: AggregationPoliciesModel): Unit = {
    sender ! Response(Try({
      val searchPolicy = existsByNameId(policy.name, policy.id, curatorFramework)
      if (searchPolicy.isEmpty) {
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeExistsPolicyWithName,
            s"Policy with name ${policy.name} not exists.")
        ))
      } else {
        val policyS = policy.copy(
          name = policy.name.toLowerCase,
          version = setVersion(searchPolicy.get, policy))
        deleteCheckpointPath(policy)
        curatorFramework.setData.forPath(s"${AppConstant.PoliciesBasePath}/${policyS.id.get}", write(policyS).getBytes)
      }
    }).recover {
      case e: NoNodeException => throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy with id ${policy.id.get}.")
      ))
    })
  }

  def delete(id: String): Unit =
    sender ! Response(Try({
      val policyModel = byId(id, curatorFramework)
      deleteCheckpointPath(policyModel)
      curatorFramework.delete().forPath(s"${AppConstant.PoliciesBasePath}/$id")
    }).recover {
      case e: NoNodeException => throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId,
          s"No policy with id $id.")
      ))
    })
}

object PolicyActor extends SLF4JLogging {

  case class Create(policy: AggregationPoliciesModel)

  case class Update(policy: AggregationPoliciesModel)

  case class Delete(name: String)

  case class FindAll()

  case class Find(id: String)

  case class FindByName(name: String)

  case class FindByFragment(fragmentType: String, id: String)

  case class Response(status: Try[_])

  case class ResponsePolicies(policies: Try[Seq[AggregationPoliciesModel]])

  case class ResponsePolicy(policy: Try[AggregationPoliciesModel])

}