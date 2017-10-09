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
import com.stratio.sparta.serving.core.actor.ExecutionActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class ExecutionHttpServiceTest extends WordSpec
  with ExecutionHttpService
  with HttpServiceBaseTest {

  val executionActorTestProbe = TestProbe()
  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.ExecutionActorName -> executionActorTestProbe.ref
  )

  override val supervisor: ActorRef = executionActorTestProbe.ref

  "ExecutionHttpService.findAll" should {
    "find all workflow executions" in {
      val executionActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindAll(user) =>
              sender ! Left(Success(Seq(getWorkflowExecutionModel)))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, executionActorTestProbe, executionActorAutoPilot)
      Get(s"/${HttpConstant.ExecutionsPath}") ~> routes(rootUser) ~> check {
        executionActorTestProbe.expectMsg(FindAll(rootUser))
        responseAs[Seq[WorkflowExecution]] should equal(Seq(getWorkflowExecutionModel))
      }
    }
    "return a 500 if there was any error" in {
      val executionActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindAll(user) =>
              sender ! Left(Failure(new MockException))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, executionActorTestProbe, executionActorAutoPilot)
      Get(s"/${HttpConstant.ExecutionsPath}") ~> routes(rootUser) ~> check {
        executionActorTestProbe.expectMsg(FindAll(rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "ExecutionHttpService.find" should {
    "find workflow execution by id" in {
      val executionActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindById(id, user) =>
              sender ! Left(Success(getWorkflowExecutionModel))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, executionActorTestProbe, executionActorAutoPilot)
      Get(s"/${HttpConstant.ExecutionsPath}/id") ~> routes(rootUser) ~> check {
        executionActorTestProbe.expectMsg(FindById("id", rootUser))
        responseAs[WorkflowExecution] should equal(getWorkflowExecutionModel)
      }
    }
    "return a 500 if there was any error" in {
      val executionActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindById(id, user) =>
              sender ! Left(Failure(new MockException))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, executionActorTestProbe, executionActorAutoPilot)
      Get(s"/${HttpConstant.ExecutionsPath}/id") ~> routes(rootUser) ~> check {
        executionActorTestProbe.expectMsg(FindById("id", rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "ExecutionHttpService.update" should {
    "update a workflow execution  when the id exists" in {
      val executionActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Update(workflowExecution, user) =>
              sender ! Left(Try(workflowExecution))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, executionActorTestProbe, executionActorAutoPilot)
      Put(s"/${HttpConstant.ExecutionsPath}", getWorkflowExecutionModel) ~> routes(rootUser) ~> check {
        executionActorTestProbe.expectMsgType[Update]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      val executionActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Update(workflowExecution, user) =>
              sender ! Left(Try(throw new Exception))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, executionActorTestProbe, executionActorAutoPilot)
      Put(s"/${HttpConstant.ExecutionsPath}", getWorkflowExecutionModel) ~> routes(rootUser) ~> check {
        executionActorTestProbe.expectMsgType[Update]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "ExecutionHttpService.create" should {
    "create a workflow execution" in {
      val executionActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case CreateExecution(workflowExecution, user) =>
              sender ! Left(Try(workflowExecution))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, executionActorTestProbe, executionActorAutoPilot)
      Post(s"/${HttpConstant.ExecutionsPath}", getWorkflowExecutionModel) ~> routes(rootUser) ~> check {
        executionActorTestProbe.expectMsgType[CreateExecution]
        responseAs[WorkflowExecution] should equal(getWorkflowExecutionModel)
      }
    }
    "return a 500 if there was any error" in {
      val executionActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case CreateExecution(workflowExecution, user) =>
              sender ! Left(Try(throw new Exception))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, executionActorTestProbe, executionActorAutoPilot)
      Post(s"/${HttpConstant.ExecutionsPath}", getWorkflowExecutionModel) ~> routes(rootUser) ~> check {
        executionActorTestProbe.expectMsgType[CreateExecution]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
