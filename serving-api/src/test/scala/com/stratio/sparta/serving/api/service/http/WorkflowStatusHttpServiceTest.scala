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
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.StatusActor.{FindAll, FindById, Update}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class WorkflowStatusHttpServiceTest extends WordSpec
  with WorkflowStatusHttpService
  with HttpServiceBaseTest {

  val statusActorTestProbe = TestProbe()
  val rootUser = Some(LoggedUser("1234","root", "dummyMail","0",Seq.empty[String],Seq.empty[String]))

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.StatusActorName -> statusActorTestProbe.ref
  )

  override val supervisor: ActorRef = testProbe.ref

  "WorkflowStatusHttpService.findAll" should {
    "find all policy contexts" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindAll(user) =>
              sender ! Left(Success(Seq(getWorkflowStatusModel())))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.WorkflowStatusesPath}") ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsg(FindAll(rootUser))
        responseAs[Seq[WorkflowStatus]] should equal(Seq(getWorkflowStatusModel()))
      }
    }
    "return a 500 if there was any error" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindAll(user) =>
              sender ! Left(Failure(new MockException))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.WorkflowStatusesPath}") ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsg(FindAll(rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowStatusHttpService.find" should {
    "find policy contexts by id" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindById(id, user) =>
              sender ! Left(Success(getWorkflowStatusModel()))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.WorkflowStatusesPath}/id") ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsg(FindById("id", rootUser))
        responseAs[WorkflowStatus] should equal(getWorkflowStatusModel())
      }
    }
    "return a 500 if there was any error" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindById(id, user) =>
              sender ! Left(Failure(new MockException))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.WorkflowStatusesPath}/id") ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsg(FindById("id", rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowStatusHttpService.update" should {
    "update a policy context when the id of the contexts exists" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Update(policyStatus, user) =>
              sender ! Left(Try(policyStatus))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Put(s"/${HttpConstant.WorkflowStatusesPath}", getWorkflowStatusModel()) ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsgType[Update]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Update(policyStatus, user) =>
              sender ! Left(Try(throw new Exception))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Put(s"/${HttpConstant.WorkflowStatusesPath}", getWorkflowStatusModel()) ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsgType[Update]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}