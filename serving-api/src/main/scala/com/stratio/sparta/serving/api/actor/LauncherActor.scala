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
import akka.actor.{Props, _}
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.security.{Edit, SpartaSecurityManager}
import com.stratio.sparta.serving.core.actor.ClusterLauncherActor
import com.stratio.sparta.serving.core.actor.LauncherActor.{Launch, Start}
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.services.{WorkflowService, WorkflowStatusService}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework

import scala.util.Try

class LauncherActor(val streamingContextService: StreamingContextService,
                    val curatorFramework: CuratorFramework,
                    val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  private val ResourceType = "context"
  private val workflowService = new WorkflowService(curatorFramework)
  private val statusService = new WorkflowStatusService(curatorFramework)

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy() {
      case _: ServingCoreException => Escalate
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

  override def receive: Receive = {

    case Launch(workflow, user) =>
      def callback() = create(workflow)

      securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Edit), callback)
    case _ => log.info("Unrecognized message in Launcher Actor")
  }

  def create(workflow: Workflow): Try[Workflow] =
    Try {
      if (workflow.id.isEmpty) workflowService.create(workflow)
      launch(workflow, context)
    }

  def launch(workflow: Workflow, context: ActorContext): Workflow = {
    if (statusService.isAvailableToRun(workflow)) {
      log.info("Streaming Context available, launching workflow ... ")
      val actorName = AkkaConstant.cleanActorName(s"LauncherActor-${workflow.name}")
      val workflowActor = context.children.find(children => children.path.name == actorName)

      val launcherActor = workflowActor match {
        case Some(actor) =>
          actor
        case None =>
          log.info(s"Launched -> $actorName")
          if (workflow.settings.global.executionMode == AppConstant.ConfigLocal) {
            log.info(s"Launching workflow: ${workflow.name} with actor: $actorName in local mode")
            context.actorOf(Props(
              new LocalLauncherActor(streamingContextService, streamingContextService.curatorFramework)), actorName)
          } else {
            if (workflow.settings.global.executionMode == AppConstant.ConfigMarathon) {
              log.info(s"Launching workflow: ${workflow.name} with actor: $actorName in marathon mode")
              context.actorOf(Props(new MarathonLauncherActor(streamingContextService.curatorFramework)), actorName)
            }
            else {
              log.info(s"Launching workflow: ${workflow.name} with actor: $actorName in cluster mode")
              context.actorOf(Props(new ClusterLauncherActor(streamingContextService.curatorFramework)), actorName)
            }
          }
      }
      launcherActor ! Start(workflow)
    }
    workflow
  }
}