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

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.stratio.sparkta.serving.core.AppConstant
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, StreamingContextStatusEnum}
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException.NoNodeException
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.Serialization._

import scala.collection.JavaConversions
import scala.util.Try

/**
 * List of all possible akka messages used to manage policies.
 */
case class PolicySupervisorActor_create(policy: AggregationPoliciesModel)
case class PolicySupervisorActor_update(policy: AggregationPoliciesModel)
case class PolicySupervisorActor_delete(name: String)
case class PolicySupervisorActor_findAll()
case class PolicySupervisorActor_find(name: String)
case class PolicySupervisorActor_findByFragment(fragmentType: String, name: String)

case class PolicySupervisorActor_response(status: Try[Unit])
case class PolicySupervisorActor_response_policies(policies: Try[Seq[AggregationPoliciesModel]])
case class PolicySupervisorActor_response_policy(policy: Try[AggregationPoliciesModel])


/**
 * Implementation of supported CRUD operations over ZK needed to manage policies.
 * @author anistal
 */
class PolicyActor(curatorFramework: CuratorFramework) extends Actor
with SLF4JLogging {

  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()

  override def receive: Receive = {
    case PolicySupervisorActor_create(policy) => create(policy)
    case PolicySupervisorActor_update(policy) => update(policy)
    case PolicySupervisorActor_delete(name) => delete(name)
    case PolicySupervisorActor_find(name) => find(name)
    case PolicySupervisorActor_findAll() => findAll()
    case PolicySupervisorActor_findByFragment(fragmentType, name) => findByFragment(fragmentType, name)
  }

  def findAll(): Unit =
    sender ! PolicySupervisorActor_response_policies(Try({
      val children = curatorFramework.getChildren.forPath(s"${AppConstant.PoliciesBasePath}")
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        read[AggregationPoliciesModel](new String(curatorFramework.getData.forPath(
          s"${AppConstant.PoliciesBasePath}/$element")))).toSeq
    }).recover {
      case e: NoNodeException => Seq()
    })

  def findByFragment(fragmentType: String, name: String): Unit =
    sender ! PolicySupervisorActor_response_policies(Try({
      val children = curatorFramework.getChildren.forPath(s"${AppConstant.PoliciesBasePath}")
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        read[AggregationPoliciesModel](new String(curatorFramework.getData.forPath(
          s"${AppConstant.PoliciesBasePath}/$element")))).filter(apm =>
        (apm.fragments.filter(f => f.name == name)).size > 0).toSeq
    }).recover {
      case e: NoNodeException => Seq()
    })

  def find(name: String): Unit =
    sender ! new PolicySupervisorActor_response_policy(Try({
      read[AggregationPoliciesModel](new Predef.String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/$name")))
    }))

  def create(policy: AggregationPoliciesModel): Unit =
    sender ! PolicySupervisorActor_response(Try({
      curatorFramework.create().creatingParentsIfNeeded().forPath(
        s"${AppConstant.PoliciesBasePath}/${policy.name}", write(policy).getBytes)
    }))

  def update(policy: AggregationPoliciesModel): Unit =
    sender ! PolicySupervisorActor_response(Try({
      curatorFramework.setData.forPath(s"${AppConstant.PoliciesBasePath}/${policy.name}", write(policy).getBytes)
    }))

  def delete(name: String): Unit =
    sender ! PolicySupervisorActor_response(Try({
      curatorFramework.delete().forPath(s"${AppConstant.PoliciesBasePath}/$name")
    }))
}