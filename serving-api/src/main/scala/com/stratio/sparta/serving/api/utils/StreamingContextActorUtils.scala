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
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.api.actor.SparkStreamingContextActor.Start
import com.stratio.sparta.serving.api.actor.{ClusterLauncherActor, LocalSparkStreamingContextActor}
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.PolicyModel

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util.Try

trait StreamingContextActorUtils extends PolicyStatusUtils
  with SLF4JLogging {

  val SparkStreamingContextActorPrefix: String = "sparkStreamingContextActor"

  def launch(policy: PolicyModel,
             policyStatusActor: ActorRef,
             streamingContextService: StreamingContextService,
             context: ActorContext): Future[Try[PolicyModel]] =
    for {
      isAvailable <- isContextAvailable(policy, policyStatusActor)
    } yield Try {
      if (isAvailable) {
        log.info("Streaming Context Available, launching policy ... ")
        val streamingLauncherActor =
          getStreamingContextActor(policy, policyStatusActor, streamingContextService, context)
        streamingLauncherActor ! Start(policy)
      }
      policy
    }

  def getStreamingContextActor(policy: PolicyModel,
                               policyStatusActor: ActorRef,
                               streamingContextService: StreamingContextService,
                               context: ActorContext): ActorRef = {
    val actorName = cleanActorName(s"$SparkStreamingContextActorPrefix-${policy.name}")
    val policyActor = context.children.find(children => children.path.name == actorName)

    policyActor match {
      case Some(actor) =>
        actor
      case None =>
        log.info(s"Launched -> $actorName")
        if (isLocalMode(policy)) {
          log.info(s"Launching policy: ${policy.name} with actor: $actorName in local mode")
          getLocalLauncher(policy, policyStatusActor, streamingContextService, context, actorName)
        } else {
          log.info(s"Launching policy: ${policy.name} with actor: $actorName in cluster mode")
          getClusterLauncher(policy, policyStatusActor, context, actorName)
        }
    }
  }

  def getLocalLauncher(policy: PolicyModel,
                       policyStatusActor: ActorRef,
                       streamingContextService: StreamingContextService,
                       context: ActorContext,
                       actorName: String): ActorRef = {
    context.actorOf(
      Props(new LocalSparkStreamingContextActor(streamingContextService, policyStatusActor)), actorName)
  }

  def getClusterLauncher(policy: PolicyModel,
                         policyStatusActor: ActorRef,
                         context: ActorContext,
                         actorName: String): ActorRef = {
    context.actorOf(Props(new ClusterLauncherActor(policyStatusActor)), actorName)
  }
}
