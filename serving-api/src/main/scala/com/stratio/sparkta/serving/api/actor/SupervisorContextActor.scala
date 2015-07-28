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

import akka.actor._
import com.stratio.sparkta.serving.api.actor.SupervisorContextActor._

class SupervisorContextActor extends InstrumentedActor {

  private var contextActors: Map[String, ContextActorStatus] = Map()

  override def receive: PartialFunction[Any, Unit] = {
    case SupAddContextStatus(name: String, contextStatus: ContextActorStatus) =>
      synchronized(doAddContextStatus(name, contextStatus))
    case SupGetContextStatus(name: String) => doGetContextStatus(name)
    case SupDeleteContextStatus(name: String) => synchronized(doDeleteContextStatus(name))
    case SupGetAllContextStatus => doGetAllContextStatus
  }

  private def doAddContextStatus(name: String, contextStatus: ContextActorStatus): Unit = {
    contextActors += (name -> contextStatus)
    doGetAllContextStatus
  }

  private def doDeleteContextStatus(name: String): Unit = {
    contextActors -= name
    doGetAllContextStatus
  }

  private def doGetContextStatus(name: String): Unit = {
    sender ! new SupResponse_ContextStatus(contextActors.get(name))
  }

  private def doGetAllContextStatus: Unit = {
    sender ! new SupResponse_AllContextStatus(contextActors)
  }

  override def postStop(): Unit = {
    super.postStop()
  }
}

object SupervisorContextActor {

  case class SupAddContextStatus(name: String, contextStatus: ContextActorStatus)

  case class SupGetContextStatus(name: String)

  case class SupDeleteContextStatus(name: String)

  case object SupGetAllContextStatus

  case class SupResponse_AllContextStatus(contextActors: Map[String, ContextActorStatus])

  case class SupResponse_ContextStatus(contextStatus: Option[ContextActorStatus])

}


