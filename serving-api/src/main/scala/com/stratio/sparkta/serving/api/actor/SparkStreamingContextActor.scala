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

import java.util.UUID

import akka.actor.SupervisorStrategy.Escalate
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparkta.serving.api.exception.ServingApiException
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel, SparktaSerializer}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum
import com.stratio.sparkta.serving.core.{AppConstant, CuratorFactoryHolder, SparktaConfig}
import org.json4s.jackson.Serialization.{read, write}

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class SparkStreamingContextActor(streamingContextService: StreamingContextService,
                                 policyStatusActor: ActorRef) extends InstrumentedActor with SparktaSerializer {

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
    val policyWithIdModel = policyWithId(policy)
    policyStatusActor ? Update(PolicyStatusModel(policyWithIdModel.id.get, PolicyStatusEnum.Launched))
    getStreamingContextActor(policyWithIdModel) match {
      case Some(streamingContextActor) => {
        // TODO (anistal) change and use PolicyActor.
        savePolicyInZk(policyWithIdModel)
        streamingContextActor ? Start
      }
      case None => {
        policyStatusActor ? Update(PolicyStatusModel(policyWithIdModel.id.get, PolicyStatusEnum.Failed))
      }
    }
  }

  private def policyWithId(policy: AggregationPoliciesModel) =
    policy.id match {
      case None => policy.copy(id = Some(UUID.randomUUID.toString))
      case Some(_) => policy
    }


  // XXX Private Methods.
  private def savePolicyInZk(policy: AggregationPoliciesModel): Unit = {
    val curatorFramework = CuratorFactoryHolder.getInstance()

    Try({
      read[AggregationPoliciesModel](new Predef.String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/${policy.id.get}")))
    }) match {
      case Success(_) => log.info(s"Policy ${policy.id.get} already in zookeeper. Updating it...")
        curatorFramework.setData.forPath(s"${AppConstant.PoliciesBasePath}/${policy.id.get}", write(policy).getBytes)
      case Failure(e) => curatorFramework.create().creatingParentsIfNeeded().forPath(
        s"${AppConstant.PoliciesBasePath}/${policy.id.get}", write(policy).getBytes)
    }
  }

  private def getStreamingContextActor(policy: AggregationPoliciesModel): Option[ActorRef] = {
    SparktaConfig.getClusterConfig match {
      case Some(clusterConfig) => {
        val zookeeperConfig = SparktaConfig.getZookeeperConfig
        val hdfsConfig = SparktaConfig.getHdfsConfig
        val detailConfig = SparktaConfig.getDetailConfig

        if (zookeeperConfig.isDefined && hdfsConfig.isDefined) {
          Some(context.actorOf(Props(new ClusterSparkStreamingContextActor(
            policy, streamingContextService, clusterConfig, hdfsConfig.get, zookeeperConfig.get, detailConfig)),
            s"$SparkStreamingContextActorPrefix-${policy.name}"))
        } else None
      }
      case None => Some(context.actorOf(
        Props(new LocalSparkStreamingContextActor(
          policy, streamingContextService, policyStatusActor)),
        s"$SparkStreamingContextActorPrefix-${policy.name}"))
    }
  }
}

object SparkStreamingContextActor {

  case class Create(policy: AggregationPoliciesModel)

  case object Start

}