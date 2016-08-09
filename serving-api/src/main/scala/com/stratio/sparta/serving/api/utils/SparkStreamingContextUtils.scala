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

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import akka.actor._
import akka.pattern.ask
import org.apache.curator.framework.CuratorFramework
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.api.actor.SparkStreamingContextActor.Start
import com.stratio.sparta.serving.api.actor.{ClusterLauncherActor, LocalSparkStreamingContextActor}
import com.stratio.sparta.serving.api.helpers.SpartaHelper._
import com.stratio.sparta.serving.core.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.models.AggregationPoliciesModel
import com.stratio.sparta.serving.core.policy.status.PolicyStatusEnum

trait SparkStreamingContextUtils extends PolicyStatusUtils
  with PolicyUtils {

  val SparkStreamingContextActorPrefix: String = "sparkStreamingContextActor"

  def launch(policy: AggregationPoliciesModel,
             policyStatusActor: ActorRef,
             streamingContextService: StreamingContextService,
             context: ActorContext): Future[Try[AggregationPoliciesModel]] =
    for {
      isAvailable <- isContextAvailable(policyStatusActor)
    } yield Try {
      if (isAvailable) {
        val streamingLauncherActor =
          getStreamingContextActor(policy, policyStatusActor, streamingContextService, context)
        updatePolicy(policy, PolicyStatusEnum.Launched, policyStatusActor)
        streamingLauncherActor ! Start(policy)
      }
      policy
    }

  def getStreamingContextActor(policy: AggregationPoliciesModel,
                               policyStatusActor: ActorRef,
                               streamingContextService: StreamingContextService,
                               context: ActorContext): ActorRef = {
    val actorName = cleanActorName(s"$SparkStreamingContextActorPrefix-${policy.name}")
    val policyActor = context.children.find(children => children.path.name == actorName)

    policyActor match {
      case Some(actor) =>
        actor
      case None =>
        SpartaConfig.getClusterConfig match {
          case Some(clusterConfig) =>
            log.info(s"launched -> $actorName")
            getClusterLauncher(policy, policyStatusActor, context, actorName)
          case None =>
            getLocalLauncher(policy, policyStatusActor, streamingContextService, context, actorName)
        }
    }
  }

  def getLocalLauncher(policy: AggregationPoliciesModel,
                       policyStatusActor: ActorRef,
                       streamingContextService: StreamingContextService,
                       context: ActorContext,
                       actorName: String): ActorRef = {
    context.actorOf(
      Props(new LocalSparkStreamingContextActor(streamingContextService, policyStatusActor)), actorName)
  }

  def getClusterLauncher(policy: AggregationPoliciesModel,
                         policyStatusActor: ActorRef,
                         context: ActorContext,
                         actorName: String): ActorRef = {
    context.actorOf(Props(new ClusterLauncherActor(policyStatusActor)), actorName)
  }
}
