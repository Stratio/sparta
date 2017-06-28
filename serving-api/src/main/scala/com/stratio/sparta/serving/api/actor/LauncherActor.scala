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

import akka.actor.SupervisorStrategy.Escalate
import akka.actor._
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.security.{Edit, SpartaSecurityManager}
import com.stratio.sparta.serving.api.utils.LauncherActorUtils
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.workflow.WorkflowModel
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, PolicyUtils}
import org.apache.curator.framework.CuratorFramework

import scala.util.Try

class LauncherActor(val streamingContextService: StreamingContextService, val curatorFramework: CuratorFramework,
                    val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with LauncherActorUtils with PolicyUtils with ActionUserAuthorize{

  val ResourceType = "context"

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy() {
      case _: ServingCoreException => Escalate
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

  override def receive: Receive = {

    case Launch(policy, user) =>
      def callback() = create(policy)
      securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Edit), callback)
    case _ => log.info("Unrecognized message in Launcher Actor")
  }

  def create(policy: WorkflowModel): Try[WorkflowModel] =
    Try {
      if (policy.id.isEmpty) createPolicy(policy)
      launch(policy, context)
    }
}