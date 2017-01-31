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

package com.stratio.sparta.serving.core.actor

import akka.actor.{Actor, _}
import com.stratio.sparta.serving.core.actor.StatusActor._
import com.stratio.sparta.serving.core.models.policy.{PoliciesStatusModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.{ClusterListenerUtils, PolicyStatusUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.NodeCache

import scala.util.Try

class StatusActor(val curatorFramework: CuratorFramework) extends Actor
  with PolicyStatusUtils with ClusterListenerUtils {

  override val statusActor = self

  //scalastyle:off cyclomatic.complexity
  override def receive: Receive = {
    case Create(policyStatus) => sender ! ResponseStatus(createStatus(policyStatus))
    case Update(policyStatus) => sender ! ResponseStatus(updateStatus(policyStatus))
    case ClearLastError(id) => sender ! clearLastError(id)
    case FindAll => sender ! ResponseStatuses(findAllStatuses())
    case FindById(id) => sender ! ResponseStatus(findStatusById(id))
    case DeleteAll => sender ! ResponseDelete(deleteAll())
    case AddListener(name, callback) => addListener(name, callback)
    case AddClusterListeners => addClusterListeners(findAllStatuses())
    case Delete(id) => sender ! ResponseDelete(delete(id))
    case _ => log.info("Unrecognized message in Policy Status Actor")
  }
  //scalastyle:on cyclomatic.complexity
}

object StatusActor {

  case class Update(policyStatus: PolicyStatusModel)

  case class Create(policyStatus: PolicyStatusModel)

  case class AddListener(name: String, callback: (PolicyStatusModel, NodeCache) => Unit)

  case class Delete(id: String)

  case object DeleteAll

  case object FindAll

  case class FindById(id: String)

  case class ResponseStatuses(policyStatus: Try[PoliciesStatusModel])

  case class ResponseStatus(policyStatus: Try[PolicyStatusModel])

  case class ResponseDelete(value: Try[_])

  case class ClearLastError(id: String)

  case object AddClusterListeners

}