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
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.actor.FragmentActor.{Response, ResponseFragment}
import com.stratio.sparta.serving.core.actor.StatusActor.{FindAll, FindById, ResponseStatus, Update}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.policy.{PolicyStatusModel, ResponsePolicy}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class PolicyContextHttpServiceTest extends WordSpec
  with PolicyContextHttpService
  with HttpServiceBaseTest {

  val sparkStreamingTestProbe = TestProbe()
  val fragmentActorTestProbe = TestProbe()
  val statusActorTestProbe = TestProbe()

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.LauncherActor -> sparkStreamingTestProbe.ref,
    AkkaConstant.FragmentActor -> fragmentActorTestProbe.ref,
    AkkaConstant.statusActor -> statusActorTestProbe.ref
  )

  override val supervisor: ActorRef = testProbe.ref

  "PolicyContextHttpService.findAll" should {
    "find all policy contexts" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindAll =>
              sender ! Success(Seq(getPolicyStatusModel()))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.PolicyContextPath}") ~> routes ~> check {
        statusActorTestProbe.expectMsg(FindAll)
        responseAs[Seq[PolicyStatusModel]] should equal(Seq(getPolicyStatusModel()))
      }
    }
    "return a 500 if there was any error" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindAll =>
              sender ! Failure(new MockException)
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.PolicyContextPath}") ~> routes ~> check {
        statusActorTestProbe.expectMsg(FindAll)
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyContextHttpService.find" should {
    "find policy contexts by id" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindById(id) =>
              sender ! ResponseStatus(Success(getPolicyStatusModel()))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.PolicyContextPath}/id") ~> routes ~> check {
        statusActorTestProbe.expectMsg(FindById("id"))
        responseAs[PolicyStatusModel] should equal(getPolicyStatusModel())
      }
    }
    "return a 500 if there was any error" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindById(id) =>
              sender ! ResponseStatus(Failure(new MockException))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.PolicyContextPath}/id") ~> routes ~> check {
        statusActorTestProbe.expectMsg(FindById("id"))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyContextHttpService.update" should {
    "update a policy context when the id of the contexts exists" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Update(policyStatus) =>
              sender ! ResponseStatus(Try(policyStatus))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Put(s"/${HttpConstant.PolicyContextPath}", getPolicyStatusModel()) ~> routes ~> check {
        statusActorTestProbe.expectMsgType[Update]
        status should be(StatusCodes.Created)
      }
    }
    "return a 500 if there was any error" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Update(policyStatus) =>
              sender ! ResponseStatus(Try(throw new Exception))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Put(s"/${HttpConstant.PolicyContextPath}", getPolicyStatusModel()) ~> routes ~> check {
        statusActorTestProbe.expectMsgType[Update]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }


  "PolicyContextHttpService.create" should {
    "creates a policy context when the id of the contexts exists" in {
      val fragmentAutopilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = {
          msg match {
            case FragmentActor.PolicyWithFragments(fragment) =>
              sender ! ResponsePolicy(Try(getPolicyModel()))
            case FragmentActor.Create(fragment) => sender ! None
          }
          TestActor.KeepRunning
        }
      })

      startAutopilot(None, fragmentActorTestProbe, fragmentAutopilot)
      startAutopilot(Success(getPolicyModel()))

      Post(s"/${HttpConstant.PolicyContextPath}", getPolicyModel()) ~> routes ~> check {
        testProbe.expectMsgType[LauncherActor.Create]
        status should be(StatusCodes.OK)
      }

      fragmentAutopilot.foreach(_.noAutoPilot)
    }
    "return a 500 if there was any error" in {
      startAutopilot(Failure(new MockException))
      Post(s"/${HttpConstant.PolicyContextPath}", getPolicyModel()) ~> routes ~> check {
        testProbe.expectMsgType[LauncherActor.Create]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
