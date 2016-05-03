/**
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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

import akka.actor.ActorRef
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import com.stratio.sparta.serving.core.models.PolicyStatusModel
import com.stratio.sparta.serving.core.policy.status.PolicyStatusEnum
import com.stratio.sparta.serving.core.{MockConfigFactory, SpartaConfig}

class SparkStreamingContextUtilsTest extends BaseUtilsTest
  with SparkStreamingContextUtils
  with BeforeAndAfter {

  val policyModel = getPolicyModel()
  val spyActor = spy(this)
  val policyStatusModel = PolicyStatusModel(id = "id1", status = PolicyStatusEnum.Started)

  before {
    reset(spyActor)
  }

  "SparkStreamingContextActor.launch" should {

    "return failed policyModel when policy fails to start" in {
      doReturn(Future(true))
        .when(spyActor)
        .isContextAvailable(policyStatusActorRef)
      doNothing()
        .when(spyActor)
        .updatePolicy(policyModel, PolicyStatusEnum.Launched, policyStatusActorRef)
      doReturn(None)
        .when(spyActor)
        .getStreamingContextActor(policyModel, policyStatusActorRef, streamingContextService, policyStatusActor.context)

      for {
        response <- spyActor.launch(policyModel, policyStatusActorRef, streamingContextService, policyStatusActor
          .context)
      } yield response should be(policyModel)

      verify(spyActor, times(0)).startPolicy(sparkStreamingContextActorRef)
      verify(spyActor, times(1)).updatePolicy(policyModel, PolicyStatusEnum.Failed, policyStatusActorRef)
    }

    "return policyModel when policy starts successfully" in {

      doReturn(Future(true))
        .when(spyActor)
        .isContextAvailable(policyStatusActorRef)
      doNothing()
        .when(spyActor)
        .updatePolicy(policyModel, PolicyStatusEnum.Launched, policyStatusActorRef)
      doReturn(Option(sparkStreamingContextActorRef))
        .when(spyActor)
        .getStreamingContextActor(policyModel, policyStatusActorRef, streamingContextService, policyStatusActor.context)


      for {
        response <- spyActor.launch(policyModel, policyStatusActorRef, streamingContextService, policyStatusActor
          .context)
      } yield response should be(policyModel)

      verify(spyActor, times(1)).startPolicy(Matchers.any(classOf[ActorRef]))
      verify(spyActor, times(0)).updatePolicy(policyModel, PolicyStatusEnum.Failed, policyStatusActorRef)
    }
  }

  "SparkStreamingContextActor.createNewPolicy" should {
    "return exception when policy already exists" in {
      doReturn(true)
        .when(spyActor)
        .existsByName(name = "testpolicy", id = None, curatorFramework = curatorFramework)

      intercept[RuntimeException] {
        spyActor.createNewPolicy(policy = policyModel,
          policyStatusActor = policyStatusActorRef,
          curatorFramework = curatorFramework,
          streamingContextService = streamingContextService,
          context = policyStatusActor.context)
      }
    }

    "return valid aggregationPolicyModel when policy does not yet exist" in {
      doReturn(false)
        .when(spyActor)
        .existsByName(name = "testpolicy", id = None, curatorFramework = curatorFramework)

      doReturn((policyModel))
        .when(spyActor)
        .launchNewPolicy(policy = policyModel,
          policyStatusActor = policyStatusActorRef,
          curatorFramework = curatorFramework,
          streamingContextService = streamingContextService,
          context = policyStatusActor.context)

      spyActor.createNewPolicy(policy = policyModel,
        policyStatusActor = policyStatusActorRef,
        curatorFramework = curatorFramework,
        streamingContextService = streamingContextService,
        context = policyStatusActor.context) should be(policyModel)

      verify(spyActor, times(1)).launchNewPolicy(policy = policyModel,
        policyStatusActor = policyStatusActorRef,
        curatorFramework = curatorFramework,
        streamingContextService = streamingContextService,
        context = policyStatusActor.context)
    }
  }

  "SparkStreamingContextActor.launchNewPolicy" should {
    "return aggregationPolicyModel" in {
      doReturn(policyModel)
        .when(spyActor)
        .policyWithId(policyModel)
      doReturn(Future(Option(policyStatusModel)))
        .when(spyActor)
        .createPolicy(policyStatusActorRef, policyModel)
      doNothing()
        .when(spyActor)
        .savePolicyInZk(policyModel, curatorFramework)
      doReturn(Future(Success(policyModel)))
        .when(spyActor)
        .launch(policy = policyModel,
          policyStatusActor = policyStatusActorRef,
          streamingContextService = streamingContextService,
          context = policyStatusActor.context)

      spyActor.launchNewPolicy(policy = policyModel,
        policyStatusActor = policyStatusActorRef,
        curatorFramework = curatorFramework,
        streamingContextService = streamingContextService,
        context = policyStatusActor.context) should be(policyModel)

      verify(spyActor, times(1)).savePolicyInZk(policy = policyModel, curatorFramework = curatorFramework)
      verify(spyActor, times(1)).launch(policy = policyModel,
        policyStatusActor = policyStatusActorRef,
        streamingContextService = streamingContextService,
        context = policyStatusActor.context)
    }
  }

  "SparkStreamingContextActor.getStreamingContextActor" should {
    "return ClusterLauncherActor" in {

      SpartaConfig.initMainConfig(Option(mesosConfig), new MockConfigFactory(mesosConfig))

      spyActor.getStreamingContextActor(getPolicyModel(name = "clusterPolicy"),
        policyStatusActorRef,
        streamingContextService,
        policyStatusActor.context)

      verify(spyActor, times(1)).getClusterLauncher(getPolicyModel(name = "clusterPolicy"), policyStatusActorRef,
        policyStatusActor.context, "sparkStreamingContextActor-clusterPolicy")
    }

    "return LocalSparkStreamingContextActor" in {
      SpartaConfig.initMainConfig(Option(localConfig), new MockConfigFactory(localConfig))
      doNothing()
        .when(spyActor)
        .killPolicy(policyStatusActorRef, "sparkStreamingContextActor-localPolicy")

      spyActor.getStreamingContextActor(getPolicyModel(name = "localPolicy"),
        policyStatusActorRef,
        streamingContextService,
        policyStatusActor.context)

      verify(spyActor, times(1)).getLocalLauncher(getPolicyModel(name = "localPolicy"), policyStatusActorRef,
        streamingContextService, policyStatusActor.context, "sparkStreamingContextActor-localPolicy")
    }
  }
}
