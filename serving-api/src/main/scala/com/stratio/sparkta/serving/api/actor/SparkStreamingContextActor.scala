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

import akka.actor.SupervisorStrategy.Escalate
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparkta.serving.api.exception.ServingApiException
import com.stratio.sparkta.serving.core.models.{SparktaSerializer, AggregationPoliciesModel, PolicyStatusModel}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum
import com.stratio.sparkta.serving.core.{AppConstant, CuratorFactoryHolder}
import com.typesafe.config.Config
import org.json4s.native.Serialization._

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class SparkStreamingContextActor(streamingContextService: StreamingContextService,
                                 policyStatusActor: ActorRef,
                                 clusterConfig: Option[Config])
  extends InstrumentedActor
  with SparktaSerializer {

  val SparkStreamingContextActorPrefix: String = "sparkStreamingContextActor"
  
  implicit val timeout: Timeout = Timeout(10.seconds)

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: ServingApiException => Escalate
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

  override def receive: PartialFunction[Any, Unit] = {
    case Create(policy) => create(policy)
  }

  /**
   * Tries to create a spark streaming context with a given configuration.
   * @param policy that contains the configuration to run.
   */
  private def create(policy: AggregationPoliciesModel): Unit = {
    policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Launched))
    val streamingContextActor = getStreamingContextActor(policy)

    // TODO (anistal) change and use PolicyActor.
    savePolicyInZk(policy)

    streamingContextActor ? Start
  }


  // XXX Private Methods.
  private def savePolicyInZk(policy: AggregationPoliciesModel): Unit = {
    val curatorFramework = CuratorFactoryHolder.getInstance()

    Try({
      read[AggregationPoliciesModel](new Predef.String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/${policy.id}")))
    }) match {
      case Success(_) => log.info(s"Policy ${policy.id} already in zookeeper. Updating it...")
        curatorFramework.setData.forPath(s"${AppConstant.PoliciesBasePath}/${policy.id}", write(policy).getBytes)
      case Failure(e) => curatorFramework.create().creatingParentsIfNeeded().forPath(
        s"${AppConstant.PoliciesBasePath}/${policy.id}", write(policy).getBytes)
    }
  }

  private def getStreamingContextActor(policy: AggregationPoliciesModel): ActorRef = {
    if (clusterConfig.isDefined) {
      context.actorOf(
        Props(new ClusterSparkStreamingContextActor(policy, clusterConfig.get, policyStatusActor)),
        s"$SparkStreamingContextActorPrefix-${policy.name}" )
    } else {
      context.actorOf(
        Props(new LocalSparkStreamingContextActor(policy, streamingContextService, policyStatusActor)),
        s"$SparkStreamingContextActorPrefix-${policy.name}")
    }
  }
}

object SparkStreamingContextActor {

  case class Create(policy: AggregationPoliciesModel)

  case object Start

}