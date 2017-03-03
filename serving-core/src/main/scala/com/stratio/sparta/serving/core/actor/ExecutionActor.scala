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

import akka.actor.Actor
import com.stratio.sparta.serving.core.actor.ExecutionActor._
import com.stratio.sparta.serving.core.models.submit.SubmitRequest
import com.stratio.sparta.serving.core.utils.ExecutionUtils
import org.apache.curator.framework.CuratorFramework

class ExecutionActor(val curatorFramework: CuratorFramework) extends Actor with ExecutionUtils {

  override def receive: Receive = {
    case Create(request) => sender ! createRequest(request)
    case Update(request) => sender ! updateRequest(request)
    case FindAll => sender ! findAllRequests()
    case FindById(id) => sender ! findRequestById(id)
    case DeleteAll => sender ! deleteAllRequests()
    case Delete(id) => sender ! deleteRequest(id)
    case _ => log.info("Unrecognized message in Policy Request Actor")
  }

}

object ExecutionActor {

  case class Update(request: SubmitRequest)

  case class Create(request: SubmitRequest)

  case class Delete(id: String)

  case object DeleteAll

  case object FindAll

  case class FindById(id: String)
}
