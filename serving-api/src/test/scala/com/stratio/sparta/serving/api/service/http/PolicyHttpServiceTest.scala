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
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.{TestActor, TestProbe}
import com.stratio.sparta.sdk.exception.MockException
import com.stratio.sparta.serving.api.actor.LauncherActor
import com.stratio.sparta.serving.api.actor.PolicyActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.FragmentActor.ResponseFragment
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
import com.stratio.sparta.serving.core.actor.{FragmentActor, LauncherActor, StatusActor}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.dto.LoggedUserConstant
import com.stratio.sparta.serving.core.models.policy._
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class PolicyHttpServiceTest extends WordSpec
with PolicyHttpService
with HttpServiceBaseTest {
  override val supervisor: ActorRef = testProbe.ref
  val sparkStreamingTestProbe = TestProbe()
  val fragmentActorTestProbe = TestProbe()

  val statusActorTestProbe = TestProbe()

  val dummyUser = Some(LoggedUserConstant.AnonymousUser)

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.LauncherActorName -> sparkStreamingTestProbe.ref,
    AkkaConstant.FragmentActorName -> fragmentActorTestProbe.ref,
    AkkaConstant.StatusActorName -> statusActorTestProbe.ref
  )

  "PolicyHttpService.find" should {
    "return a 500 if there was any error" in {
      startAutopilot(ResponsePolicy(Failure(new MockException())))
      Get(s"/${HttpConstant.PolicyPath}/find/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyHttpService.findByName" should {
    "return a 500 if there was any error" in {
      startAutopilot(ResponsePolicy(Failure(new MockException())))
      Get(s"/${HttpConstant.PolicyPath}/findByName/name") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindByName]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyHttpService.findByFragment" should {
    "find a policy from its fragments when the policy has status" in {
      val fragmentActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FragmentActor.FindByTypeAndId(fragmentType, id) =>
              sender ! ResponseFragment(Success(getFragmentModel()))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, fragmentActorTestProbe, fragmentActorAutoPilot)

      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case StatusActor.FindById(id) =>
              sender ! StatusActor.ResponseStatus(Success(getPolicyStatusModel()))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)

      startAutopilot(ResponsePolicies(Success(Seq(getPolicyModel()))))
      Get(s"/${HttpConstant.PolicyPath}/fragment/input/name") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindByFragment]
        responseAs[Seq[PolicyModel]] should equal(Seq(getPolicyModel()))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(ResponsePolicy(Failure(new MockException())))
      Get(s"/${HttpConstant.PolicyPath}/fragment/input/name") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindByFragment]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyHttpService.findAll" should {
    "find all policies" in {
      startAutopilot(ResponsePolicies(Success(Seq(getPolicyModel()))))
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case StatusActor.FindById(id) =>
              sender ! StatusActor.ResponseStatus(Success(getPolicyStatusModel()))
              TestActor.NoAutoPilot
          }
      })

      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.PolicyPath}/all") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindAll]
        responseAs[Seq[PolicyModel]] should equal(Seq(getPolicyModel()))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(ResponsePolicy(Failure(new MockException())))
      Get(s"/${HttpConstant.PolicyPath}/all") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindAll]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyHttpService.update" should {
    "return an OK because the policy was updated" in {
      startAutopilot(ResponsePolicy(Success(getPolicyModel())))
      Put(s"/${HttpConstant.PolicyPath}", getPolicyModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Update]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Response(Failure(new MockException())))
      Put(s"/${HttpConstant.PolicyPath}", getPolicyModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Update]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyHttpService.remove" should {
    "return an OK because the policy was deleted" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case StatusActor.Delete(id) =>
              sender ! StatusActor.ResponseDelete(Success(true))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(Response(Success(getFragmentModel())))
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Delete(s"/${HttpConstant.PolicyPath}/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Delete]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Response(Failure(new MockException())))
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case StatusActor.Delete(id) =>
              sender ! StatusActor.ResponseDelete(Success(true))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(Response(Failure(new MockException())))
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Delete(s"/${HttpConstant.PolicyPath}/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Delete]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyHttpService.run" should {
    "return an OK and the name of the policy run" in {
      val policyAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Launch(policy) =>
              sender ! Success(getPolicyModel())
              TestActor.NoAutoPilot
            case Delete => TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, sparkStreamingTestProbe, policyAutoPilot)
      startAutopilot(ResponsePolicy(Success(getPolicyModel())))
      Get(s"/${HttpConstant.PolicyPath}/run/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      val policyAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Launch(policy) =>
              sender ! Success(getPolicyModel())
              TestActor.NoAutoPilot
            case Delete => TestActor.NoAutoPilot
          }
      })
      startAutopilot(Response(Failure(new MockException())))
      startAutopilot(None, sparkStreamingTestProbe, policyAutoPilot)
      Get(s"/${HttpConstant.PolicyPath}/run/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyHttpService.download" should {
    "return an OK and the attachment filename" in {
      startAutopilot(ResponsePolicy(Success(getPolicyModel())))
      Get(s"/${HttpConstant.PolicyPath}/download/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.OK)
        header("Content-Disposition").isDefined should be(true)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Response(Failure(new MockException())))
      Get(s"/${HttpConstant.PolicyPath}/download/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
