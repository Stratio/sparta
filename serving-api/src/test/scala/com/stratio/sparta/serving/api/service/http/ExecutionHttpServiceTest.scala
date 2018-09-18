/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.TestActor
import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.serving.api.actor.ExecutionActor
import com.stratio.sparta.serving.api.actor.ExecutionActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.{DtoModelImplicits, WorkflowExecution, WorkflowExecutionDto}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class ExecutionHttpServiceTest extends WordSpec
  with ExecutionHttpService
  with HttpServiceBaseTest {

  val rootUser = Option(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))

  override implicit val actors: Map[String, ActorRef] = Map()

  override val supervisor: ActorRef = testProbe.ref

  "ExecutionHttpService.findAll" should {
    "find all workflow executions" in {
      startAutopilot(Left(Success(Seq(getWorkflowExecutionModel))))
      Get(s"/${HttpConstant.ExecutionsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindAll(rootUser))
        responseAs[Seq[WorkflowExecution]] should equal(Seq(getWorkflowExecutionModel))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException)))
      Get(s"/${HttpConstant.ExecutionsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindAll(rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "ExecutionHttpService.findAllDto" should {
    "find all workflow executions dto" in {

      import DtoModelImplicits._

      val execution = getWorkflowExecutionModel
      val executionDto: WorkflowExecutionDto = execution
      startAutopilot(Left(Success(Seq(executionDto))))
      Get(s"/${HttpConstant.ExecutionsPath}/findAllDto") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindAllDto(rootUser))
        responseAs[Seq[WorkflowExecutionDto]] should equal(Seq(executionDto))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException)))
      Get(s"/${HttpConstant.ExecutionsPath}/findAllDto") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindAllDto(rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "ExecutionHttpService.find" should {
    "find workflow execution by id" in {
      startAutopilot(Left(Success(getWorkflowExecutionModel)))
      Get(s"/${HttpConstant.ExecutionsPath}/id") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindById("id", rootUser))
        responseAs[WorkflowExecution] should equal(getWorkflowExecutionModel)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException)))
      Get(s"/${HttpConstant.ExecutionsPath}/id") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindById("id", rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "ExecutionHttpService.update" should {
    "update a workflow execution  when the id exists" in {
      startAutopilot(Left(Try(getWorkflowExecutionModel)))
      Put(s"/${HttpConstant.ExecutionsPath}", getWorkflowExecutionModel) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[Update]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Try(throw new Exception)))
      Put(s"/${HttpConstant.ExecutionsPath}", getWorkflowExecutionModel) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[Update]
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
      startAutopilot(None, testProbe, executionActorAutoPilot)
      Post(s"/${HttpConstant.ExecutionsPath}", getWorkflowExecutionModel) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateExecution]
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
      startAutopilot(None, testProbe, executionActorAutoPilot)
      Post(s"/${HttpConstant.ExecutionsPath}", getWorkflowExecutionModel) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateExecution]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
