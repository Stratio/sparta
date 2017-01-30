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

import akka.actor._
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.api.actor.LauncherActor.Start
import com.stratio.sparta.serving.api.actor.{ClusterLauncherActor, LocalLauncherActor}
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.models.policy.PolicyModel
import com.stratio.sparta.serving.core.utils.PolicyStatusUtils

trait LauncherActorUtils extends PolicyStatusUtils {

  val SparkStreamingContextActorPrefix: String = "contextLauncherActor"

  def launch(policy: PolicyModel,
             statusActor: ActorRef,
             streamingContextService: StreamingContextService,
             context: ActorContext): PolicyModel = {
    if (isAvailableToRun(policy)) {
      log.info("Streaming Context Available, launching policy ... ")
      val launcherActor = getLauncherActor(policy, statusActor, streamingContextService, context)
      launcherActor ! Start(policy)
    }
    policy
  }

  def getLauncherActor(policy: PolicyModel,
                       statusActor: ActorRef,
                       streamingContextService: StreamingContextService,
                       context: ActorContext): ActorRef = {
    val actorName = cleanActorName(s"$SparkStreamingContextActorPrefix-${policy.name}")
    val policyActor = context.children.find(children => children.path.name == actorName)

    policyActor match {
      case Some(actor) =>
        actor
      case None =>
        log.info(s"Launched -> $actorName")
        if (isLocal(policy)) {
          log.info(s"Launching policy: ${policy.name} with actor: $actorName in local mode")
          getLocalLauncher(policy, statusActor, streamingContextService, context, actorName)
        } else {
          log.info(s"Launching policy: ${policy.name} with actor: $actorName in cluster mode")
          getClusterLauncher(policy, statusActor, context, actorName)
        }
    }
  }

  private def getLocalLauncher(policy: PolicyModel,
                               statusActor: ActorRef,
                               streamingContextService: StreamingContextService,
                               context: ActorContext,
                               actorName: String): ActorRef =
    context.actorOf(Props(new LocalLauncherActor(streamingContextService, statusActor)), actorName)

  def getClusterLauncher(policy: PolicyModel,
                         statusActor: ActorRef,
                         context: ActorContext,
                         actorName: String): ActorRef =
    context.actorOf(Props(new ClusterLauncherActor(statusActor)), actorName)
}
