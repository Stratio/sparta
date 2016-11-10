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

import com.stratio.sparta.serving.core.config.{MockConfigFactory, SpartaConfig}
import com.stratio.sparta.serving.core.models.{PoliciesStatusModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor.Response
import com.stratio.sparta.serving.core.policy.status.PolicyStatusEnum
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

@RunWith(classOf[JUnitRunner])
class PolicyStatusUtilsTest extends BaseUtilsTest with PolicyStatusUtils {

  "SparkStreamingContextActor.isAnyPolicyStarted" should {

    "return true if any policy is Started" in {
      val spyActor = spy(this)
      doReturn(
        Future(Response(Success(PoliciesStatusModel(Seq(PolicyStatusModel(id = "id1", status = PolicyStatusEnum
          .Started)))))))
        .when(spyActor)
        .findAllPolicies(policyStatusActorRef)

      for {
        response <- spyActor.isAnyPolicyStarted(policyStatusActorRef)
      } yield response should be(Future(true))
    }


    "return true if any policy is Starting" in {
      val spyActor = spy(this)
      doReturn(
        Future(Response(Success(PoliciesStatusModel(Seq(PolicyStatusModel(id = "id1", status = PolicyStatusEnum
          .Starting)))))))
        .when(spyActor)
        .findAllPolicies(policyStatusActorRef)

      for {
        response <- spyActor.isAnyPolicyStarted(policyStatusActorRef)
      } yield response should be(Future(true))
    }

    "return false if there is no policy Starting/Started mode" in {
      val spyActor = spy(this)
      doReturn(
        Future(Response(Success(PoliciesStatusModel(Seq(PolicyStatusModel(id = "id1", status = PolicyStatusEnum.Failed),
          PolicyStatusModel(id = "id1", status = PolicyStatusEnum.Launched),
          PolicyStatusModel(id = "id1", status = PolicyStatusEnum.NotStarted),
          PolicyStatusModel(id = "id1", status = PolicyStatusEnum.Stopped),
          PolicyStatusModel(id = "id1", status = PolicyStatusEnum.Stopping)))))))
        .when(spyActor)
        .findAllPolicies(policyStatusActorRef)


      for {
        response <- spyActor.isAnyPolicyStarted(policyStatusActorRef)
      } yield response should be(Future(false))
    }
  }

  "SparkStreamingContextActor.isContextAvailable" should {
    "return true when execution mode is yarn" in {
      val spyActor = spy(this)
      doReturn(
        Future(Response(Success(PoliciesStatusModel(Seq(PolicyStatusModel(id = "id1", status = PolicyStatusEnum
          .Starting)))))))
        .when(spyActor)
        .findAllPolicies(policyStatusActorRef)


      SpartaConfig.initMainConfig(Option(yarnConfig), new MockConfigFactory(yarnConfig))

      for {
        response <- spyActor.isContextAvailable(policyStatusActorRef)
      } yield response should be(true)
    }

    "return true when execution mode is mesos" in {
      val spyActor = spy(this)
      doReturn(
        Future(Response(Success(PoliciesStatusModel(Seq(PolicyStatusModel(id = "id1", status = PolicyStatusEnum
          .Started)))))))
        .when(spyActor)
        .findAllPolicies(policyStatusActorRef)


      SpartaConfig.initMainConfig(Option(mesosConfig), new MockConfigFactory(mesosConfig))

      for {
        response <- spyActor.isContextAvailable(policyStatusActorRef)
      } yield response should be(true)
    }


    "return true when execution mode is local and there is no running policy" in {
      val spyActor = spy(this)
      doReturn(
        Future(Response(Success(PoliciesStatusModel(Seq(PolicyStatusModel(id = "id1", status = PolicyStatusEnum
          .Stopped)))))))
        .when(spyActor)
        .findAllPolicies(policyStatusActorRef)


      SpartaConfig.initMainConfig(Option(localConfig), new MockConfigFactory(localConfig))

      for {
        response <- spyActor.isContextAvailable(policyStatusActorRef)
      } yield response should be(true)
    }

    "return true when execution mode is standalone and there is no running policy" in {
      val spyActor = spy(this)
      doReturn(
        Future(Response(Success(PoliciesStatusModel(Seq(PolicyStatusModel(id = "id1", status = PolicyStatusEnum
          .Failed)))))))
        .when(spyActor)
        .findAllPolicies(policyStatusActorRef)


      SpartaConfig.initMainConfig(Option(standaloneConfig), new MockConfigFactory(standaloneConfig))

      for {
        response <- spyActor.isContextAvailable(policyStatusActorRef)
      } yield response should be(true)
    }

    "return false when execution mode is local and there is already one running policy" in {
      val spyActor = spy(this)
      doReturn(Future(true))
        .when(spyActor)
        .isAnyPolicyStarted(policyStatusActorRef)

      SpartaConfig.initMainConfig(Option(localConfig), new MockConfigFactory(localConfig))

      for {
        response <- spyActor.isContextAvailable(policyStatusActorRef)
      } yield response should be(false)
    }
  }
}
