/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import java.util.UUID

import akka.actor.ActorRef
import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.serving.api.actor.WorkflowActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.models.authorization.{GosecUser, GosecUserConstants, LoggedUser}
import com.stratio.sparta.serving.core.models.workflow._
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.concurrent.Future
import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class WorkflowHttpServiceTest extends WordSpec
  with WorkflowHttpService
  with HttpServiceBaseTest {

  import DtoModelImplicits._

  override val supervisor: ActorRef = testProbe.ref
  val id = UUID.randomUUID.toString
  val group = "default"
  val rootUser = Some(GosecUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))
  val dummyUser = Some(GosecUserConstants.AnonymousUser)

  override implicit val actors: Map[String, ActorRef] = Map()

  "WorkflowHttpService.findById" should {
    "burn workflow" in {
      val initWorkflow = getWorkflowModel()
      startAutopilot(Left(Success(initWorkflow)))
      Get(s"/${HttpConstant.WorkflowsPath}/findById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
      }
    }
    "return workflow" in {
      val initWorkflow = getWorkflowModel()
      startAutopilot(Left(Success(initWorkflow)))
      Get(s"/${HttpConstant.WorkflowsPath}/findById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        responseAs[Workflow] should equal(initWorkflow)
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

  "WorkflowHttpService.findAllDto" should {
    "return a workflow list" in {
      val seqDto: Seq[WorkflowDto] = Seq(getWorkflowModel())
      startAutopilot(Left(Success(seqDto)))
      Get(s"/${HttpConstant.WorkflowsPath}/findAllDto") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindAllDto]
        responseAs[Seq[WorkflowDto]] should equal(seqDto)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.WorkflowsPath}/findAllDto") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindAllDto]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.findAllByGroup" should {
    "return a workflow list" in {
      val seqDto: Seq[WorkflowDto] = Seq(getWorkflowModel())
      startAutopilot(Left(Success(seqDto)))
      Get(s"/${HttpConstant.WorkflowsPath}/findAllByGroup/$group") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindAllByGroup]
        responseAs[Seq[WorkflowDto]] should equal(seqDto)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.WorkflowsPath}/findAllByGroup/$group") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindAllByGroup]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.find" should {
    "return a workflow" in {
      startAutopilot(Left(Success(Seq(getWorkflowModel()))))
      Post(s"/${HttpConstant.WorkflowsPath}/find", getWorkflowQueryModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[Query]
        responseAs[Array[Workflow]] should equal(Seq(getWorkflowModel()))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.WorkflowsPath}/find", getWorkflowQueryModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[Query]
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
      Delete(s"/${HttpConstant.WorkflowsPath}/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteWorkflow]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.removeWithAllVersions" should {
    "return an OK if the workflows were deleted" in {
      startAutopilot(Left(Success(getWorkflowDeleteModel())))
      Delete(s"/${HttpConstant.WorkflowsPath}/removeWithAllVersions", getWorkflowDeleteModel()) ~>
        routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteWorkflowWithAllVersions]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.WorkflowsPath}/removeWithAllVersions", getWorkflowDeleteModel()) ~>
        routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteWorkflowWithAllVersions]
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

  "WorkflowHttpService.version" should {
    "create a new workflow version" in {
      startAutopilot(Left(Success(getWorkflowModel())))
      Post(s"/${HttpConstant.WorkflowsPath}/version", getWorkflowVersionModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateWorkflowVersion]
        responseAs[Workflow] should equal(getWorkflowModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.WorkflowsPath}/version", getWorkflowVersionModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateWorkflowVersion]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.rename" should {
    "return an OK because the workflows was updated" in {
      startAutopilot(Left(Success(getWorkflowRenameModel())))
      Put(s"/${HttpConstant.WorkflowsPath}/rename", getWorkflowRenameModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[RenameWorkflow]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Put(s"/${HttpConstant.WorkflowsPath}/rename", getWorkflowRenameModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[RenameWorkflow]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowHttpService.move" should {
    "return an OK because the workflow was moved" in {
      val seqDto: Seq[WorkflowDto] = Seq(getWorkflowModel())
      startAutopilot(Left(Success(seqDto)))
      Put(s"/${HttpConstant.WorkflowsPath}/move", getWorkflowMoveModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[MoveWorkflow]
        val a = responseAs[Seq[WorkflowDto]]
        a.head
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Put(s"/${HttpConstant.WorkflowsPath}/move", getWorkflowMoveModel()) ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[MoveWorkflow]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

}
