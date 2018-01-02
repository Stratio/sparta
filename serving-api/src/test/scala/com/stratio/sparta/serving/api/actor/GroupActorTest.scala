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

package com.stratio.sparta.serving.api.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.api.actor.GroupActor._
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.helpers.DummySecurityTestClass
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.Group
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api._
import org.apache.zookeeper.data.Stat
import org.json4s.jackson.Serialization.read
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class GroupActorTest extends TestKit(ActorSystem("GroupActorSpec"))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with MockitoSugar with SpartaSerializer {

  trait TestData {

    val group =
      """
        | {
        |  "name": "default"
        |  }
      """.stripMargin

    val groupTwo =
      """{
        |  "name": "default2"
        |}
      """.stripMargin

    implicit val secManager = Option(new DummySecurityTestClass().asInstanceOf[SpartaSecurityManager])
    val groupElementModel = read[Group](group)
    val groupTwoElementModel = read[Group](groupTwo)
    val curatorFramework = mock[CuratorFramework]
    val getChildrenBuilder = mock[GetChildrenBuilder]
    val getDataBuilder = mock[GetDataBuilder]
    val existsBuilder = mock[ExistsBuilder]
    val createBuilder = mock[CreateBuilder]
    val deleteBuilder = mock[DeleteBuilder]
    val protectedACL = mock[ProtectACLCreateModeStatPathAndBytesable[String]]
    val setDataBuilder = mock[SetDataBuilder]

    lazy val groupActor = system.actorOf(Props(new GroupActor(curatorFramework)))
    implicit val timeout: Timeout = Timeout(15.seconds)
    CuratorFactoryHolder.setInstance(curatorFramework)
  }

  override def afterAll: Unit = shutdown()

  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))
  val limitedUser = Some(LoggedUser("4321", "limited", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))
  val defaultGroup = "default"

  "GroupActor" must {

    "FindGroup: returns an exception because the node of type does not exist yet" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/group/default"))
        .thenReturn(null)

      groupActor ! GroupActor.FindGroup(defaultGroup, rootUser)
      expectMsg(Left(Failure(new ServerException(s"No group found"))))
    }

    "FindGroup: returns a error message when limitedUser attempts to get group" in new TestData {
      groupActor ! GroupActor.FindGroup(defaultGroup, limitedUser)

      expectMsgAnyClassOf(classOf[Either[ResponseGroup, UnauthorizedResponse]])
    }

    "FindGroup: returns the group" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/group/default"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/sparta/group/default"))
        .thenReturn(group.getBytes)

      groupActor ! GroupActor.FindGroup(defaultGroup, rootUser)
      expectMsg(Left(Success(groupElementModel)))
    }

    "CreateGroup: creates a group and return the created group" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/group/default"))
        .thenReturn(null)
      when(curatorFramework.create)
        .thenReturn(createBuilder)
      when(curatorFramework.create
        .creatingParentsIfNeeded)
        .thenReturn(protectedACL)
      when(curatorFramework.create
        .creatingParentsIfNeeded
        .forPath("/stratio/sparta/sparta/group/default"))
        .thenReturn(group)

      groupActor ! GroupActor.CreateGroup(groupElementModel, rootUser)

      expectMsg(Left(Success(groupElementModel)))
    }

    "CreateGroup: tries to create a group but execute update because the group exists" in
      new TestData {
        when(curatorFramework.checkExists())
          .thenReturn(existsBuilder)
        when(curatorFramework.checkExists()
          .forPath("/stratio/sparta/sparta/group/default"))
          .thenReturn(new Stat())
        when(curatorFramework.setData())
          .thenReturn(setDataBuilder)
        when(curatorFramework.setData()
          .forPath("/stratio/sparta/sparta/group/default"))
          .thenReturn(new Stat())

        groupActor ! GroupActor.CreateGroup(groupElementModel, rootUser)

        expectMsg(Left(Success(groupElementModel)))
      }

    "UpdateGroup: update a group" in
      new TestData {
        when(curatorFramework.checkExists())
          .thenReturn(existsBuilder)
        when(curatorFramework.checkExists()
          .forPath("/stratio/sparta/sparta/group/default"))
          .thenReturn(new Stat())
        when(curatorFramework.setData())
          .thenReturn(setDataBuilder)
        when(curatorFramework.setData()
          .forPath("/stratio/sparta/sparta/group/default", group.getBytes))
          .thenReturn(new Stat())

        groupActor ! GroupActor.UpdateGroup(groupElementModel, rootUser)

        expectMsg(Left(Success(groupElementModel)))
      }

    "UpdateGroup: tries to create a group but throw exception because the group not exists" in
      new TestData {
        when(curatorFramework.checkExists())
          .thenReturn(existsBuilder)
        when(curatorFramework.checkExists()
          .forPath("/stratio/sparta/sparta/group"))
          .thenReturn(null)

        groupActor ! GroupActor.UpdateGroup(groupElementModel, rootUser)

        expectMsg(Left(Failure(new ServerException("Unable to create group"))))
      }

    "DeleteGroup: delete a group" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/group"))
        .thenReturn(new Stat())
      when(curatorFramework.delete)
        .thenReturn(deleteBuilder)
      when(curatorFramework.delete
        .forPath("/stratio/sparta/sparta/group"))
        .thenReturn(null)

      groupActor ! GroupActor.DeleteGroup(defaultGroup, rootUser)

      expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])
    }

    "DeleteGroup: delete a group and node not exists" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/group"))
        .thenReturn(null)

      groupActor ! GroupActor.DeleteGroup(defaultGroup, rootUser)

      expectMsg(Left(Failure(new ServerException("No group available to delete"))))
    }
  }
}
