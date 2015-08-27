/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.serving.api.actor

import java.util.UUID

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.stratio.sparkta.serving.api.actor.PolicyActor._
import com.stratio.sparkta.serving.api.exception.ServingApiException
import com.stratio.sparkta.serving.core.AppConstant
import com.stratio.sparkta.serving.core.models._
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.Serialization._

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

/**
 * Implementation of supported CRUD operations over ZK needed to manage policies.
 */
class PolicyActor(curatorFramework: CuratorFramework) extends Actor
with SLF4JLogging {

  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()

  override def receive: Receive = {
    case Create(policy) => create(policy)
    case Update(policy) => update(policy)
    case Delete(id) => delete(id)
    case Find(id) => find(id)
    case FindAll() => findAll()
    case FindByFragment(fragmentType, id) => findByFragment(fragmentType, id)
  }

  def findAll(): Unit =
    sender ! ResponsePolicies(Try({
      val children = curatorFramework.getChildren.forPath(s"${AppConstant.PoliciesBasePath}")
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        read[AggregationPoliciesModel](new String(curatorFramework.getData.forPath(
          s"${AppConstant.PoliciesBasePath}/$element")))).toSeq
    }).recover {
      case e: NoNodeException => Seq()
    })

  def findByFragment(fragmentType: String, id: String): Unit =
    sender ! ResponsePolicies(Try({
      val children = curatorFramework.getChildren.forPath(s"${AppConstant.PoliciesBasePath}")
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        read[AggregationPoliciesModel](new String(curatorFramework.getData.forPath(
          s"${AppConstant.PoliciesBasePath}/$element")))).filter(apm =>
        (apm.fragments.filter(f => f.id.get == id)).size > 0).toSeq
    }).recover {
      case e: NoNodeException => Seq()
    })

  def find(id: String): Unit =
    sender ! new ResponsePolicy(Try({
      read[AggregationPoliciesModel](new Predef.String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/$id")))
    }).recover {
      case e: NoNodeException => throw new ServingApiException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsPolicytWithId, s"No policy  with id ${id}.")
      ))
    })

  def create(policy: AggregationPoliciesModel): Unit =
    sender ! Response(Try({
      if(existsByName(policy.name)) {
        throw new ServingApiException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeExistsPolicytWithName,
            s"Policy with name ${policy.name} exists.")
        ))
      }
      val fragmentS = policy.copy(id = Some(s"${UUID.randomUUID.toString}"))
      curatorFramework.create().creatingParentsIfNeeded().forPath(
        s"${AppConstant.PoliciesBasePath}/${fragmentS.id.get}", write(fragmentS).getBytes)
    }))

  def update(policy: AggregationPoliciesModel): Unit = {
    sender ! Response(Try({
      if (existsByName(policy.name, policy.id)) {
        throw new ServingApiException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeExistsPolicytWithName,
            s"Policy with name ${policy.name} exists.")
        ))
      }
      curatorFramework.setData.forPath(s"${AppConstant.PoliciesBasePath}/${policy.id.get}", write(policy).getBytes)
    }).recover {
      case e: NoNodeException => throw new ServingApiException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsPolicytWithId, s"No policy  with id ${policy.id.get}.")
      ))
    })
  }

  def delete(id: String): Unit =
    sender ! Response(Try({
      curatorFramework.delete().forPath(s"${AppConstant.PoliciesBasePath}/$id")
    }).recover {
      case e: NoNodeException => throw new ServingApiException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId,
          s"No policy with id $id.")
      ))
    })

  private def existsByName(name: String, id: Option[String] = None): Boolean = {
    Try({
      val children = curatorFramework.getChildren.forPath(s"${AppConstant.PoliciesBasePath}")
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        read[AggregationPoliciesModel](new String(curatorFramework.getData.forPath(
          s"${AppConstant.PoliciesBasePath}/$element"))))
        .filter(policy => if(id.isDefined) policy.name == name && policy.id.get != id.get
         else policy.name == name).toSeq.size > 0
    }) match {
      case Success(result) => result
      case Failure(exception) => {
        log.error(exception.getLocalizedMessage, exception)
        false
      }
    }
  }
}

object PolicyActor {

  case class Create(policy: AggregationPoliciesModel)

  case class Update(policy: AggregationPoliciesModel)

  case class Delete(name: String)

  case class FindAll()

  case class Find(name: String)

  case class FindByFragment(fragmentType: String, id: String)

  case class Response(status: Try[_])

  case class ResponsePolicies(policies: Try[Seq[AggregationPoliciesModel]])

  case class ResponsePolicy(policy: Try[AggregationPoliciesModel])
}