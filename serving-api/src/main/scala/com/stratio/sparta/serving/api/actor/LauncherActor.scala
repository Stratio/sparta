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

import akka.actor.{Props, _}
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.security.{Execute, SpartaSecurityManager}
import com.stratio.sparta.serving.core.actor.ClusterLauncherActor
import com.stratio.sparta.serving.core.actor.LauncherActor.{Launch, Start}
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.services.{WorkflowService, WorkflowStatusService}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework

import scala.util.Try

class LauncherActor(
                     val streamingContextService: StreamingContextService,
                     val curatorFramework: CuratorFramework
                   )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  //TODO change dyplon to new names: policy -> workflow
  private val ResourcePol = "policy"
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val workflowService = new WorkflowService(curatorFramework)

  override def receive: Receive = {
    case Launch(id, user) => launch(id, user)
    case _ => log.info("Unrecognized message in Launcher Actor")
  }

  def launch(id: String, user: Option[LoggedUser]): Unit = {
    securityActionAuthorizer(user, Map(ResourcePol -> Execute)) {
      Try {
        val workflow = workflowService.findById(id)

        if (statusService.isAvailableToRun(workflow)) {
          log.info("Context available, launching workflow ... ")
          val actorName = AkkaConstant.cleanActorName(s"LauncherActor-${workflow.name}")
          val childLauncherActor = context.children.find(children => children.path.name == actorName)
          val workflowActor = childLauncherActor match {
            case Some(actor) =>
              actor
            case None =>
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
          workflowActor ! Start(workflow)
        }
      }
    }
  }
}