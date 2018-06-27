/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.{TestActor, TestProbe}
import com.stratio.sparta.core.exception.MockException
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
