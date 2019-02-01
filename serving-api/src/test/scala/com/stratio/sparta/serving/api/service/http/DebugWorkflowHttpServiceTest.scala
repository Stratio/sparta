/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import java.util.UUID

import akka.actor.ActorRef
import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.core.models.{DebugResults, ResultStep}
import com.stratio.sparta.serving.api.actor.DebugWorkflowActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.authorization.GosecUserConstants
import com.stratio.sparta.serving.core.models.files.SpartaFile
import com.stratio.sparta.serving.core.models.workflow.DebugWorkflow
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class DebugWorkflowHttpServiceTest extends WordSpec
  with DebugWorkflowHttpService
  with HttpServiceBaseTest {

  val id = UUID.randomUUID.toString

  override val supervisor: ActorRef = testProbe.ref
  override implicit val actors: Map[String, ActorRef] = Map.empty[String, ActorRef]
  val dummyUser = Some(GosecUserConstants.AnonymousUser)


  "DebugWorkflowHttpService.findById" should {
    "ask for a specific workflow" in {
      val initWorkflow: DebugWorkflow = DebugWorkflow(getWorkflowModel().id,getWorkflowModel(), None, None)
      startAutopilot(Left(Success(initWorkflow)))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/findById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
      }
    }
    "return workflow" in {
      val initWorkflow: DebugWorkflow = DebugWorkflow(getWorkflowModel().id,getWorkflowModel(), None, None)
      startAutopilot(Left(Success(initWorkflow)))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/findById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        responseAs[DebugWorkflow] should equal(initWorkflow)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/findById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "DebugWorkflowHttpService.findAll" should {
    "find all workflows" in {
      startAutopilot(Left(Success(Seq(DebugWorkflow(getWorkflowModel().id,getWorkflowModel(), None, None)))))
      Get(s"/${HttpConstant.DebugWorkflowsPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindAll]
        responseAs[Seq[DebugWorkflow]] should equal(Seq(DebugWorkflow(getWorkflowModel().id,getWorkflowModel(), None, None)))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.DebugWorkflowsPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindAll]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "DebugWorkflowHttpService.resultsById" should {
    "ask for the debug results of a specific workflow" in {
      val initWorkflow: DebugWorkflow = DebugWorkflow(getWorkflowModel().id,getWorkflowModel(), None, None)
      startAutopilot(Left(Success(initWorkflow)))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/resultsById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[GetResults]
      }
    }
    "return the debug results of a workflow" in {
      val schemaFake = StructType(Seq(StructField("name", StringType, false),
        StructField("age", IntegerType, false),
        StructField("year", IntegerType, true))).json
      val fakedResults = DebugResults(true, Map("Kafka" -> ResultStep(id, id, "Kafka", 0, Option(schemaFake),
        Option(Seq("Gregor Samza, 28, 1915")))))
      startAutopilot(Left(Success(fakedResults)))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/resultsById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[GetResults]
        responseAs[DebugResults].debugSuccessful should be(true)
      }
    }
    "send that the results are unavailable" in {
      startAutopilot(Left(Failure(new ServerException("no results yet"))))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/resultsById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[GetResults]
        status should be(StatusCodes.EnhanceYourCalm)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/resultsById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[GetResults]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "DebugWorkflowHttpService.deleteById" should {
    "return an OK because the workflow was deleted" in {
      startAutopilot(Left(Success(true)))
      Delete(s"/${HttpConstant.DebugWorkflowsPath}/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteById]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.DebugWorkflowsPath}/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteById]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "DebugWorkflowHttpService.deleteAll" should {
    "return an OK because the workflow was deleted" in {
      startAutopilot(Left(Success(true)))
      Delete(s"/${HttpConstant.DebugWorkflowsPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteAll]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.DebugWorkflowsPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteAll]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "DebugWorkflowHttpService.uploadFile" should {
    "Upload a file" in {
      startAutopilot(Left(Success(Seq(SpartaFile("", "", "")))))
      Post(s"/${HttpConstant.DebugWorkflowsPath}/uploadFile/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[UploadFile]
        status should be(StatusCodes.OK)
      }
    }
    "Fail when service is not available" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.DebugWorkflowsPath}/uploadFile/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[UploadFile]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "DebugWorkflowHttpService.deleteFile" should {
    "return an OK because the sparta mock file was deleted" in {
      startAutopilot(Left(Success("")))
      Delete(s"/${HttpConstant.DebugWorkflowsPath}/deleteFile//home/path/file.csv") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteFile]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.DebugWorkflowsPath}/deleteFile//home/path/file.csv") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteFile]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
