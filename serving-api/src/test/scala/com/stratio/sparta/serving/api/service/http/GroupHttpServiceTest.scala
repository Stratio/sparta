/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.serving.api.actor.GroupActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.{SpartaConfig, SpartaConfigFactory}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.authorization.{GosecUser, GosecUserConstants, LoggedUser}
import com.stratio.sparta.serving.core.models.workflow.Group
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class GroupHttpServiceTest extends WordSpec
  with GroupHttpService
  with HttpServiceBaseTest {

  val workflowTestProbe = TestProbe()
  val groupTestProbe = TestProbe()
  val dummyUser = Some(GosecUserConstants.AnonymousUser)
  val rootUser = Some(GosecUser("1234", "root", "dummyMail", "0", Seq.empty, Seq.empty))

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.WorkflowActorName -> workflowTestProbe.ref,
    AkkaConstant.GroupActorName -> groupTestProbe.ref
  )
  override val supervisor: ActorRef = testProbe.ref

  override def beforeEach(): Unit = {
    SpartaConfig.getSpartaConfig(Option(localConfig))
    SpartaConfig.getZookeeperConfig(Option(localConfig))
    SpartaConfig.getDetailConfig(Option(localConfig))
  }

  "GroupHttpService.findAll" should {
    "findAll groups" in {
      startAutopilot(Left(Success(Seq(getGroupModel()))))
      Get(s"/${HttpConstant.GroupsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindAllGroups]
        responseAs[Seq[Group]] should equal(Seq(getGroupModel()))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.GroupsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindAllGroups]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GroupHttpService.findByID" should {
    "find a group" in {
      startAutopilot(Left(Success(Seq(getGroupModel()))))
      Get(s"/${HttpConstant.GroupsPath}/findById/940800b2-6d81-44a8-84d9-26913a2faea4") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindGroupByID]
        responseAs[Group] should equal(getGroupModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.GroupsPath}/findById/940800b2-6d81-44a8-84d9-26913a2faea4") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindGroupByID]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GroupHttpService.findByName" should {
    "find a group" in {
      startAutopilot(Left(Success(Seq(getGroupModel()))))
      Get(s"/${HttpConstant.GroupsPath}/findByName/home") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindGroupByName]
        responseAs[Group] should equal(getGroupModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.GroupsPath}/findByName/home") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindGroupByName]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GroupHttpService.deleteAll" should {
    "delete all groups" in {
      startAutopilot(Left(Success(None)))
      Delete(s"/${HttpConstant.GroupsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteAllGroups]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.GroupsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteAllGroups]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GroupHttpService.deleteById" should {
    "delete a group" in {
      startAutopilot(Left(Success(None)))
      Delete(s"/${HttpConstant.GroupsPath}/deleteById/940800b2-6d81-44a8-84d9-26913a2faea4") ~> routes(rootUser) ~>
        check {
        testProbe.expectMsgType[DeleteGroupByID]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.GroupsPath}/deleteById/940800b2-6d81-44a8-84d9-26913a2faea4") ~> routes(rootUser) ~>
        check {
        testProbe.expectMsgType[DeleteGroupByID]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GroupHttpService.deleteByName" should {
    "delete a group" in {
      startAutopilot(Left(Success(None)))
      Delete(s"/${HttpConstant.GroupsPath}/deleteByName/home") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteGroupByName]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.GroupsPath}/deleteByName/home") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteGroupByName]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GroupHttpService.update" should {
    "update a group" in {
      startAutopilot(Left(Success(getGroupModel())))
      Put(s"/${HttpConstant.GroupsPath}", getGroupModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UpdateGroup]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Put(s"/${HttpConstant.GroupsPath}", getGroupModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UpdateGroup]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GroupHttpService.create" should {
    "create a group" in {
      startAutopilot(Left(Success(getGroupModel())))
      Post(s"/${HttpConstant.GroupsPath}", getGroupModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateGroup]
        responseAs[Group] should equal(getGroupModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.GroupsPath}", getGroupModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateGroup]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
  
}
