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

package com.stratio.sparta.serving.api.utils

import akka.actor.{Props, _}
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.api.actor.{LocalLauncherActor, MarathonLauncherActor}
import com.stratio.sparta.serving.core.actor.ClusterLauncherActor
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.policy.PolicyModel
import com.stratio.sparta.serving.core.utils.PolicyStatusUtils

trait LauncherActorUtils extends PolicyStatusUtils {

  val contextLauncherActorPrefix = "contextLauncherActor"

  val streamingContextService: StreamingContextService

  def launch(policy: PolicyModel, context: ActorContext): PolicyModel = {
    if (isAvailableToRun(policy)) {
      log.info("Streaming Context Available, launching policy ... ")
      val actorName = cleanActorName(s"$contextLauncherActorPrefix-${policy.name}")
      val policyActor = context.children.find(children => children.path.name == actorName)

      val launcherActor = policyActor match {
        case Some(actor) =>
          actor
        case None =>
          log.info(s"Launched -> $actorName")
          if (isExecutionType(policy, AppConstant.ConfigLocal)) {
            log.info(s"Launching policy: ${policy.name} with actor: $actorName in local mode")
            context.actorOf(Props(
              new LocalLauncherActor(streamingContextService, streamingContextService.curatorFramework)), actorName)

          } else {
            if(isExecutionType(policy, AppConstant.ConfigMarathon)) {
              log.info(s"Launching policy: ${policy.name} with actor: $actorName in marathon mode")
              context.actorOf(Props(new MarathonLauncherActor(streamingContextService.curatorFramework)), actorName)
            }
            else {
              log.info(s"Launching policy: ${policy.name} with actor: $actorName in cluster mode")
              context.actorOf(Props(new ClusterLauncherActor(streamingContextService.curatorFramework)), actorName)
            }
          }
      }
      launcherActor ! Start(policy)
    }
    policy
  }
}
