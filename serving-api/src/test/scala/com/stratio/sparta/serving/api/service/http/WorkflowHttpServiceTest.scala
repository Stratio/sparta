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

import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.{TestActor, TestProbe}
import com.stratio.sparta.sdk.exception.MockException
import com.stratio.sparta.serving.api.actor.WorkflowActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
import com.stratio.sparta.serving.core.actor.StatusActor
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
  val id = UUID.randomUUID.toString
  val statusActorTestProbe = TestProbe()
  val rootUser = Some(LoggedUser("1234","root", "dummyMail","0",Seq.empty[String],Seq.empty[String]))
  val dummyUser = Some(LoggedUserConstant.AnonymousUser)

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.LauncherActorName -> sparkStreamingTestProbe.ref,
    AkkaConstant.StatusActorName -> statusActorTestProbe.ref
  )

  "WorkflowHttpService.find" should {
    "return workflow" in {
      startAutopilot(Left(Success(getWorkflowModel())))
      Get(s"/${HttpConstant.WorkflowsPath}/findById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        responseAs[Workflow] should equal(getWorkflowModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.WorkflowsPath}/findById/$id") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.findByName" should {
    "return a workflow" in {
      startAutopilot(Left(Success(getWorkflowModel())))
      Get(s"/${HttpConstant.WorkflowsPath}/findByName/name") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindByName]
        responseAs[Workflow] should equal(getWorkflowModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.WorkflowsPath}/findByName/name") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindByName]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.findByIds" should {
    "find all workflows" in {
      startAutopilot(Left(Success(Seq(getWorkflowModel()))))
      Post(s"/${HttpConstant.WorkflowsPath}/findByIds", Seq(id)) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindByIdList]
        responseAs[Seq[Workflow]] should equal(Seq(getWorkflowModel()))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.WorkflowsPath}/findByIds", Seq(id)) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindByIdList]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.findAll" should {
    "find all workflows" in {
      startAutopilot(Left(Success(Seq(getWorkflowModel()))))
      Get(s"/${HttpConstant.WorkflowsPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindAll]
        responseAs[Seq[Workflow]] should equal(Seq(getWorkflowModel()))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.WorkflowsPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindAll]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.create" should {
    "return the workflow created" in {
      startAutopilot(Left(Success(getWorkflowModel())))
      Post(s"/${HttpConstant.WorkflowsPath}", getWorkflowModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[CreateWorkflow]
        responseAs[Workflow] should equal(getWorkflowModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.WorkflowsPath}", getWorkflowModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[CreateWorkflow]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.validate" should {
    "return the validation result with valid workflow" in {
      startAutopilot(Left(Success(getValidWorkflowValidation())))
      Post(s"/${HttpConstant.WorkflowsPath}/validate", getWorkflowModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[ValidateWorkflow]
        responseAs[WorkflowValidation] should equal(getValidWorkflowValidation())
      }
    }

    "return the validation result with not valid workflow" in {
      startAutopilot(Left(Success(getNotValidWorkflowValidation())))
      Post(s"/${HttpConstant.WorkflowsPath}/validate", getWorkflowModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[ValidateWorkflow]
        responseAs[WorkflowValidation] should equal(getNotValidWorkflowValidation())
      }
    }

    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.WorkflowsPath}/validate", getWorkflowModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[ValidateWorkflow]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.createList" should {
    "return the workflow created" in {
      startAutopilot(Left(Success(Seq(getWorkflowModel()))))
      Post(s"/${HttpConstant.WorkflowsPath}/list", Seq(getWorkflowModel())) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[CreateWorkflows]
        responseAs[Seq[Workflow]] should equal(Seq(getWorkflowModel()))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.WorkflowsPath}/list", Seq(getWorkflowModel())) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[CreateWorkflows]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.update" should {
    "return an OK because the workflow was updated" in {
      startAutopilot(Left(Success(getWorkflowModel())))
      Put(s"/${HttpConstant.WorkflowsPath}", getWorkflowModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Update]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Put(s"/${HttpConstant.WorkflowsPath}", getWorkflowModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Update]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.updateList" should {
    "return an OK because the workflows was updated" in {
      startAutopilot(Left(Success(Seq(getWorkflowModel()))))
      Put(s"/${HttpConstant.WorkflowsPath}/list", Seq(getWorkflowModel())) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[UpdateList]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Put(s"/${HttpConstant.WorkflowsPath}/list", Seq(getWorkflowModel())) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[UpdateList]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.remove" should {
    "return an OK because the workflow was deleted" in {
      startAutopilot(Left(Success(getFragmentModel())))
      Delete(s"/${HttpConstant.WorkflowsPath}/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteWorkflow]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case StatusActor.DeleteStatus(id, user) =>
              sender ! Left(Success(true))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(Left(Failure(new MockException())))
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Delete(s"/${HttpConstant.WorkflowsPath}/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteWorkflow]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.removeList" should {
    "return an OK because the workflows was deleted" in {
      startAutopilot(Left(Success(getFragmentModel())))
      Delete(s"/${HttpConstant.WorkflowsPath}/list", Seq(id)) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteList]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.WorkflowsPath}/list", Seq(id)) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteList]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.run" should {
    "return an OK and the name of the workflow run" in {
      val workflowAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Launch(id, user) =>
              sender ! Left(Success(getWorkflowModel()))
              TestActor.NoAutoPilot
            case Delete => TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, sparkStreamingTestProbe, workflowAutoPilot)
      Get(s"/${HttpConstant.WorkflowsPath}/run/$id") ~> routes(dummyUser) ~> check {
        sparkStreamingTestProbe.expectMsgType[Launch]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      val workflowAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Launch(workflow, user) =>
              sender ! Left(Failure(new MockException()))
              TestActor.NoAutoPilot
            case Delete => TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, sparkStreamingTestProbe, workflowAutoPilot)
      Get(s"/${HttpConstant.WorkflowsPath}/run/$id") ~> routes(dummyUser) ~> check {
        sparkStreamingTestProbe.expectMsgType[Launch]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.download" should {
    "return an OK and the attachment filename" in {
      startAutopilot(Left(Success(getWorkflowModel())))
      Get(s"/${HttpConstant.WorkflowsPath}/download/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.OK)
        header("Content-Disposition").isDefined should be(true)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.WorkflowsPath}/download/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
