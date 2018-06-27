/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.service.http

import java.util.UUID
import scala.util.Failure

import akka.actor.ActorRef
import akka.testkit.TestProbe
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}

@RunWith(classOf[JUnitRunner])
class ExecutionHistoryHttpServiceTest extends WordSpec
  with ExecutionHistoryHttpService
  with HttpServiceBaseTest with SpartaSerializer {

  import com.stratio.sparta.serving.api.actor.ExecutionHistoryActor._

  override val supervisor: ActorRef = testProbe.ref
  val id = UUID.randomUUID.toString
  val group = "default"
  val statusActorTestProbe = TestProbe()
  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))
  val dummyUser = Some(LoggedUserConstant.AnonymousUser)

  override implicit val actors: Map[String, ActorRef] = Map(
    //AkkaConstant.LauncherActorName -> sparkStreamingTestProbe.ref,
    AkkaConstant.ExecutionHistoryApiActorName -> statusActorTestProbe.ref
  )

  "historyExecutionHttpService.findByWorkflowId" should {
    "return history execution" in {
      Get(s"/${HttpConstant.ExecutionsHistoryPath}/findByWorkflowId/$id") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[QueryByWorkflowId]
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.ExecutionsHistoryPath}/findByWorkflowId/$id") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[QueryByWorkflowId]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "historyExecutionHttpService.findByUserId" should {
    "return history execution" in {
      Get(s"/${HttpConstant.ExecutionsHistoryPath}/findByUserId/1234") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[QueryByUserId]
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.ExecutionsHistoryPath}/findByUserId/$id") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[QueryByUserId]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}