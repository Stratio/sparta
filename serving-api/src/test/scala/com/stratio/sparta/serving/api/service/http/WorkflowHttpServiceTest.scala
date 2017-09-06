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
import com.stratio.sparta.serving.api.actor.WorkflowActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.TemplateActor.ResponseTemplate
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
import com.stratio.sparta.serving.core.actor.{TemplateActor, LauncherActor, StatusActor}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}
import com.stratio.sparta.serving.core.models.workflow._
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class WorkflowHttpServiceTest extends WordSpec
with WorkflowHttpService
with HttpServiceBaseTest {
  override val supervisor: ActorRef = testProbe.ref
  val sparkStreamingTestProbe = TestProbe()
  val fragmentActorTestProbe = TestProbe()

  val statusActorTestProbe = TestProbe()
  val rootUser = Some(LoggedUser("1234","root", "dummyMail","0",Seq.empty[String],Seq.empty[String]))
  val dummyUser = Some(LoggedUserConstant.AnonymousUser)

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.LauncherActorName -> sparkStreamingTestProbe.ref,
    AkkaConstant.TemplateActorName -> fragmentActorTestProbe.ref,
    AkkaConstant.StatusActorName -> statusActorTestProbe.ref
  )

  "PolicyHttpService.find" should {
    "return a 500 if there was any error" in {
      startAutopilot(Left(ResponseWorkflow(Failure(new MockException()))))
      Get(s"/${HttpConstant.WorkflowsPath}/find/id") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyHttpService.findByName" should {
    "return a 500 if there was any error" in {
      startAutopilot(Left(ResponseWorkflow(Failure(new MockException()))))
      Get(s"/${HttpConstant.WorkflowsPath}/findByName/name") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindByName]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  //TODO add test when the refactor ends
  "PolicyHttpService.findByFragment" should {
    /*
    "find a policy from its fragments when the policy has status" in {
      val fragmentActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FragmentActor.FindByTypeAndId(fragmentType, id, userId) =>
              sender ! Left(ResponseFragment(Success(getFragmentModel())))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, fragmentActorTestProbe, fragmentActorAutoPilot)

      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case StatusActor.FindById(id, user) =>
              sender ! Left(StatusActor.ResponseStatus(Success(getPolicyStatusModel())))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)

      val workflow = getPolicyModel()

      startAutopilot(Left(ResponsePolicies(Success(Seq(workflow)))))
      Get(s"/${HttpConstant.PolicyPath}/fragment/input/name") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindByFragment]
        responseAs[Seq[WorkflowModel]] should equal(Seq(workflow))
      }
    }
    */
  }

  //TODO add test when the refactor ends
  "PolicyHttpService.findAll" should {
    /*
    "find all policies" in {
      startAutopilot(Left(ResponsePolicies(Success(Seq(getPolicyModel())))))
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case StatusActor.FindById(id, user) =>
              sender ! Left(StatusActor.ResponseStatus(Success(getPolicyStatusModel())))
              TestActor.NoAutoPilot
          }
      })

      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.PolicyPath}/all") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindAll]
        responseAs[Seq[WorkflowModel]] should equal(Seq(getPolicyModel()))
      }
    }
    */
    "return a 500 if there was any error" in {
      startAutopilot(Left(ResponseWorkflow(Failure(new MockException()))))
      Get(s"/${HttpConstant.WorkflowsPath}/all") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindAll]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyHttpService.update" should {
    "return an OK because the policy was updated" in {
      startAutopilot(Left(ResponseWorkflow(Success(getWorkflowModel()))))
      Put(s"/${HttpConstant.WorkflowsPath}", getWorkflowModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Update]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Response(Failure(new MockException()))))
      Put(s"/${HttpConstant.WorkflowsPath}", getWorkflowModel()) ~> routes(dummyUser) ~> check {
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
            case StatusActor.DeleteStatus(id, user) =>
              sender ! Left(StatusActor.ResponseDelete(Success(true)))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(Left(Response(Success(getFragmentModel()))))
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Delete(s"/${HttpConstant.WorkflowsPath}/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteWorkflow]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Response(Failure(new MockException()))))
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case StatusActor.DeleteStatus(id, user) =>
              sender ! Left(StatusActor.ResponseDelete(Success(true)))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(Left(Response(Failure(new MockException()))))
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Delete(s"/${HttpConstant.WorkflowsPath}/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteWorkflow]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyHttpService.run" should {
    "return an OK and the name of the policy run" in {
      val policyAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Launch(policy, user) =>
              sender ! Left(Success(getWorkflowModel()))
              TestActor.NoAutoPilot
            case Delete => TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, sparkStreamingTestProbe, policyAutoPilot)
      startAutopilot(Left(ResponseWorkflow(Success(getWorkflowModel()))))
      Get(s"/${HttpConstant.WorkflowsPath}/run/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      val policyAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Launch(policy, user) =>
              sender ! Left(Success(getWorkflowModel()))
              TestActor.NoAutoPilot
            case Delete => TestActor.NoAutoPilot
          }
      })
      startAutopilot(Left(Response(Failure(new MockException()))))
      startAutopilot(None, sparkStreamingTestProbe, policyAutoPilot)
      Get(s"/${HttpConstant.WorkflowsPath}/run/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PolicyHttpService.download" should {
    "return an OK and the attachment filename" in {
      startAutopilot(Left(ResponseWorkflow(Success(getWorkflowModel()))))
      Get(s"/${HttpConstant.WorkflowsPath}/download/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.OK)
        header("Content-Disposition").isDefined should be(true)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Response(Failure(new MockException()))))
      Get(s"/${HttpConstant.WorkflowsPath}/download/id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
