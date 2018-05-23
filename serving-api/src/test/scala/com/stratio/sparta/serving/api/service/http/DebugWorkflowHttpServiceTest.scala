/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe

import scala.concurrent.duration._
import akka.testkit._
import akka.util.Timeout
import com.stratio.sparta.sdk.exception.MockException
import com.stratio.sparta.sdk.workflow.step.{DebugResults, ResultStep}
import com.stratio.sparta.serving.api.actor.DebugWorkflowActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}
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
  val rootUser = Some(LoggedUser("1234","root", "dummyMail","0",Seq.empty[String],Seq.empty[String]))
  val dummyUser = Some(LoggedUserConstant.AnonymousUser)


  "DebugWorkflowHttpService.findById" should {
    "ask for a specific workflow" in {
      val initWorkflow: DebugWorkflow= DebugWorkflow(getWorkflowModel(), None, None)
      startAutopilot(Left(Success(initWorkflow)))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/findById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
      }
    }
    "return workflow" in {
      val initWorkflow: DebugWorkflow = DebugWorkflow(getWorkflowModel(), None, None)
      startAutopilot(Left(Success(initWorkflow)))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/findById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Find]
        responseAs[DebugWorkflow] should equal(initWorkflow)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/findById/$id") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[Find]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "DebugWorkflowHttpService.findAll" should {
    "find all workflows" in {
      startAutopilot(Left(Success(Seq(DebugWorkflow(getWorkflowModel(),None, None)))))
      Get(s"/${HttpConstant.DebugWorkflowsPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindAll]
        responseAs[Seq[DebugWorkflow]] should equal(Seq(DebugWorkflow(getWorkflowModel(),None, None)))
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
      val initWorkflow: DebugWorkflow= DebugWorkflow(getWorkflowModel(), None, None)
      startAutopilot(Left(Success(initWorkflow)))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/resultsById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[GetResults]
      }
    }
    "return the debug results of a workflow" ignore {
      val schemaFake = StructType(StructField("name", StringType, false) ::
        StructField("age", IntegerType, false) ::
        StructField("year", IntegerType, true) :: Nil)
      val fakedResults = DebugResults(true, Map("Kafka" -> ResultStep("Kafka", 0, Option(schemaFake),
        Option("Gregor Samza, 28, 1915"))))
      startAutopilot(Left(Success(fakedResults)))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/resultsById/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[GetResults]
        responseAs[DebugResults].debugSuccessful should equal(true)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.DebugWorkflowsPath}/resultsById/$id") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[GetResults]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "DebugWorkflowHttpService.run" should {
    "return a true if the run was launched" in {
      startAutopilot(Left(Success(true)))
      Post(s"/${HttpConstant.DebugWorkflowsPath}/run/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Run]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.DebugWorkflowsPath}/run/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[Run]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
