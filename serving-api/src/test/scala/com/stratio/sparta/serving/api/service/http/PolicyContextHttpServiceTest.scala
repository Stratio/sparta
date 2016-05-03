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
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.{TestActor, TestProbe}
import com.stratio.sparkta.serving.api.service.http.PolicyContextHttpService
import com.stratio.sparta.sdk.exception.MockException
import com.stratio.sparta.serving.api.actor.SparkStreamingContextActor
import com.stratio.sparta.serving.api.actor.SparkStreamingContextActor.Create
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.actor.FragmentActor.ResponseFragment
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.{PoliciesStatusModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor.{FindAll, Update}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class PolicyContextHttpServiceTest extends WordSpec
with PolicyContextHttpService
with HttpServiceBaseTest {

  val sparkStreamingTestProbe = TestProbe()
  val fragmentActorTestProbe = TestProbe()
  val policyStatusActorTestProbe = TestProbe()

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.SparkStreamingContextActor -> sparkStreamingTestProbe.ref,
    AkkaConstant.FragmentActor -> fragmentActorTestProbe.ref,
    AkkaConstant.PolicyStatusActor -> policyStatusActorTestProbe.ref
  )

  override val supervisor: ActorRef = testProbe.ref

  "PolicyContextHttpService.findAll" should {
    "find all policy contexts" in {
      val policyStatusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindAll =>
              sender ! PolicyStatusActor.Response(Success(PoliciesStatusModel(Seq(getPolicyStatusModel()))))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, policyStatusActorTestProbe, policyStatusActorAutoPilot)
      Get(s"/${HttpConstant.PolicyContextPath}") ~> routes ~> check {
        policyStatusActorTestProbe.expectMsg(FindAll)
        responseAs[PoliciesStatusModel] should equal(PoliciesStatusModel(Seq(getPolicyStatusModel())))
      }
    }
    "return a 500 if there was any error" in {
      val policyStatusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindAll =>
              sender ! PolicyStatusActor.Response(Failure(new MockException))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, policyStatusActorTestProbe, policyStatusActorAutoPilot)
      Get(s"/${HttpConstant.PolicyContextPath}") ~> routes ~> check {
        policyStatusActorTestProbe.expectMsg(FindAll)
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyContextHttpService.update" should {
    "update a policy context when the id of the contexts exists" in {
      val policyStatusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Update(policyStatus) =>
              sender ! Option(policyStatus)
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, policyStatusActorTestProbe, policyStatusActorAutoPilot)
      Put(s"/${HttpConstant.PolicyContextPath}", getPolicyStatusModel()) ~> routes ~> check {
        policyStatusActorTestProbe.expectMsgType[Update]
        status should be(StatusCodes.Created)
      }
    }
    "return a 500 if there was any error" in {
      val policyStatusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Update(policyStatus) =>
              sender ! None
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, policyStatusActorTestProbe, policyStatusActorAutoPilot)
      Put(s"/${HttpConstant.PolicyContextPath}", getPolicyStatusModel()) ~> routes ~> check {
        policyStatusActorTestProbe.expectMsgType[Update]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }


  "PolicyContextHttpService.create" should {
    "creates a policy context when the id of the contexts exists" in {
      val fragmentAutopilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = {
          msg match {
            case FragmentActor.FindByTypeAndId(fragmentType, id) =>
              sender ! ResponseFragment(Success(getFragmentModel()))
            case FragmentActor.Create(fragment) => sender ! None
          }
          TestActor.KeepRunning
        }
      })

      startAutopilot(None, fragmentActorTestProbe, fragmentAutopilot)
      startAutopilot(Success(getPolicyModel()))

      Post(s"/${HttpConstant.PolicyContextPath}", getPolicyModel()) ~> routes ~> check {
        testProbe.expectMsgType[SparkStreamingContextActor.Create]
        status should be(StatusCodes.OK)
      }

      fragmentAutopilot.foreach(_.noAutoPilot)
    }
    "return a 500 if there was any error" in {
      startAutopilot(Failure(new MockException))
      Post(s"/${HttpConstant.PolicyContextPath}", getPolicyModel()) ~> routes ~> check {
        testProbe.expectMsgType[SparkStreamingContextActor.Create]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
