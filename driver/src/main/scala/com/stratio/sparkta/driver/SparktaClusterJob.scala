/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.driver

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import com.stratio.sparkta.driver.SparktaJob._
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.driver.util.PolicyUtils
import com.stratio.sparkta.serving.core.actor.FragmentActor
import com.stratio.sparkta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparkta.serving.core.dao.ErrorDAO
import com.stratio.sparkta.serving.core.helpers.PolicyHelper
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel, SparktaSerializer}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparkta.serving.core.policy.status.{PolicyStatusActor, PolicyStatusEnum}
import com.stratio.sparkta.serving.core.{CuratorFactoryHolder, SparktaConfig}
import com.typesafe.config.ConfigFactory
import org.apache.curator.framework.CuratorFramework

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object SparktaClusterJob extends SparktaSerializer {

  implicit val timeout: Timeout = Timeout(3.seconds)
  final val PolicyIdIndex = 0
  final val ZookeperConfigurationIndex = 1
  final val DetailConfigurationIndex = 2

  def main(args: Array[String]): Unit = {
    assert(args.length == 3, s"Invalid number of params: ${args.length}, args: $args")
    Try {
      val policyId = args(PolicyIdIndex)
      val detailConfiguration = new String(BaseEncoding.base64().decode(args(DetailConfigurationIndex)))
      val zookeperConfiguration = new String(BaseEncoding.base64().decode(args(ZookeperConfigurationIndex)))

      initSparktaConfig(detailConfiguration, zookeperConfiguration)

      val curatorFramework = CuratorFactoryHolder.getInstance()
      val policyZk = getPolicyFromZookeeper(policyId, curatorFramework)
      implicit val system = ActorSystem(policyId)
      val fragmentActor = system.actorOf(Props(new FragmentActor(curatorFramework)), AkkaConstant.FragmentActor)
      val policy = PolicyHelper.parseFragments(
        PolicyHelper.fillFragments(policyZk, fragmentActor, timeout))
      val policyStatusActor = system.actorOf(Props(new PolicyStatusActor(curatorFramework)),
        AkkaConstant.PolicyStatusActor)

      Try {
        policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Starting))
        Try(ErrorDAO().dao.delete(policy.id.get))

        val streamingContextService = new StreamingContextService(Some(policyStatusActor))
        val ssc =
          streamingContextService.clusterStreamingContext(policy, Map("spark.app.name" -> s"${policy.name}")).get

        ssc.start
        policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Started))
        log.info(s"Starting Streaming Context for policy $policyId")
        ssc.awaitTermination()
      } match {
        case Success(_) => {
          policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Stopped))
          log.info(s"Stopped Streaming Context for policy $policyId")
        }
        case Failure(exception) => {
          log.error(exception.getLocalizedMessage, exception)
          policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Failed))
        }
      }
    } match {
      case Success(_) => log.info("Streaming context is running")
      case Failure(exception) => log.error(exception.getLocalizedMessage, exception)
    }
  }

  def initSparktaConfig(detailConfig: String, zKConfig: String): Unit = {
    val configStr = s"${detailConfig.stripPrefix("{").stripSuffix("}")}\n${zKConfig.stripPrefix("{").stripSuffix("}")}"
    log.info(s"Parsed config: sparkta { $configStr }")
    SparktaConfig.initMainConfig(Option(ConfigFactory.parseString(s"sparkta{$configStr}")))
    SparktaConfig.initDAOs
  }

  def getPolicyFromZookeeper(policyId: String, curatorFramework: CuratorFramework): AggregationPoliciesModel = {
    Try {
      PolicyUtils.parseJson(new Predef.String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/${policyId}")))
    } match {
      case Success(policy) => policy
      case Failure(e) => log.error(s"Cannot load policy $policyId", e); throw e
    }
  }
}
