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
import java.util

import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.api.actor.GroupActor._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
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
import org.mockito.stubbing.OngoingStubbing

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
        |  "id" : "940800b2-6d81-44a8-84d9-26913a2faea4",
        |  "name": "/home"
        |  }
      """.stripMargin

    val groupWithoutID =
      """{
        |  "name": "/home"
        |}
      """.stripMargin

    val newGroupWithoutID =
      """{
        |  "name": "/home/test1"
        |}
      """.stripMargin

    val newGroupInstance = Group(Some("a1234"), "/home/test1")

    val newGroupInstanceJSON =
      """
        | {
        |  "id" : "a1234",
        |  "name": "/home/test1"
        |  }
      """.stripMargin

    val updateGroupInstanceJSON =
      """
        | {
        |  "id" : "a1234",
        |  "name": "/home/test2"
        |  }
      """.stripMargin


    val inexistentGroup =
      """{
        |"id" : "123456789",
        |"name": "/home/ghost"
        |}
      """.stripMargin

    implicit val secManager = Option(new DummySecurityTestClass().asInstanceOf[SpartaSecurityManager])
    val groupElementModel = read[Group](group)
    val groupElementModelWithoutId = read[Group](groupWithoutID)
    val newGroupElementModelWithoutId = read[Group](newGroupWithoutID)
    val updateGroupModel = read[Group](updateGroupInstanceJSON)
    val inexistentGroupElementModel = read[Group](inexistentGroup)
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

    def mockListOfGroups: OngoingStubbing[util.List[String]] = {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath(s"${AppConstant.GroupZkPath}"))
        .thenReturn(new util.ArrayList[String]() {
          add("940800b2-6d81-44a8-84d9-26913a2faea4")})
    }
  }

  override def afterAll: Unit = shutdown()

  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))
  val limitedUser = Some(LoggedUser("4321", "limited", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))
  val defaultGroup = Group(Option("940800b2-6d81-44a8-84d9-26913a2faea4"), "/home")

  "GroupActor" must {

    "FindGroup: returns an exception because the node of type does not exist yet" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"/stratio/sparta/sparta/group/${defaultGroup.id.get}"))
        .thenReturn(null)

      groupActor ! GroupActor.FindGroupByID(defaultGroup.id.get, rootUser)
      expectMsg(Left(Failure(new ServerException(s"No group found"))))
    }

    "FindGroupByID: returns a error message when limitedUser attempts to get group" in new TestData {
      groupActor ! GroupActor.FindGroupByID(defaultGroup.id.get, limitedUser)
      expectMsgAnyClassOf(classOf[Either[ResponseGroup, UnauthorizedResponse]])
    }

    "FindGroupByName: returns a error message when limitedUser attempts to get group" in new TestData {
      mockListOfGroups
      groupActor ! GroupActor.FindGroupByName(defaultGroup.name, limitedUser)
      expectMsgAnyClassOf(classOf[Either[ResponseGroup, UnauthorizedResponse]])
    }

    "FindGroupByID: returns the group" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"/stratio/sparta/sparta/group/${defaultGroup.id.get}"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"/stratio/sparta/sparta/group/${defaultGroup.id.get}"))
        .thenReturn(group.getBytes)

      groupActor ! GroupActor.FindGroupByID(defaultGroup.id.get, rootUser)
      expectMsg(Left(Success(groupElementModel)))
    }

    "FindGroupByName: returns the group" in new TestData {
      mockListOfGroups

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"/stratio/sparta/sparta/group/${defaultGroup.id.get}"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"/stratio/sparta/sparta/group/${defaultGroup.id.get}"))
        .thenReturn(group.getBytes)

      groupActor ! GroupActor.FindGroupByName(defaultGroup.name, rootUser)
      expectMsg(Left(Success(groupElementModel)))
    }

    "CreateGroup: creates a group and return the created group" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"/stratio/sparta/sparta/group/${newGroupInstance.id.get}"))
        .thenReturn(null)
      when(curatorFramework.create)
        .thenReturn(createBuilder)
      when(curatorFramework.create
        .creatingParentsIfNeeded)
        .thenReturn(protectedACL)
      when(curatorFramework.create
        .creatingParentsIfNeeded
        .forPath(s"/stratio/sparta/sparta/group/${newGroupInstance.id.get}"))
        .thenReturn(newGroupInstanceJSON)

      groupActor ! GroupActor.CreateGroup(newGroupElementModelWithoutId, rootUser)

      expectMsgPF() {
        case Left(Success(x: Group)) => x.name.equals(newGroupInstance.name)
      }
    }

    "CreateGroup: tries to create a group but raise an exception because the group exists" in
      new TestData {
        when(curatorFramework.getChildren)
          .thenReturn(getChildrenBuilder)
        when(curatorFramework.getChildren
          .forPath(s"${AppConstant.GroupZkPath}"))
          .thenReturn(new util.ArrayList[String]() {
            add(newGroupInstance.id.get)
          })
        when(curatorFramework.checkExists())
          .thenReturn(existsBuilder)
        when(curatorFramework.checkExists()
          .forPath(s"/stratio/sparta/sparta/group/${newGroupInstance.id.get}"))
          .thenReturn(new Stat())
        when(curatorFramework.getData)
          .thenReturn(getDataBuilder)
        when(curatorFramework.getData
          .forPath(s"/stratio/sparta/sparta/group/${newGroupInstance.id.get}"))
          .thenReturn(newGroupInstanceJSON.getBytes)

        groupActor ! GroupActor.CreateGroup(newGroupElementModelWithoutId, rootUser)
        expectMsg(Left(Failure(new ServerException(s"Unable to create group ${newGroupInstance.name}" +
          s" because it already exists"))))
      }

    "UpdateGroup: update a group" in
      new TestData {
        when(curatorFramework.getChildren)
          .thenReturn(getChildrenBuilder)
        when(curatorFramework.getChildren
          .forPath(s"${AppConstant.GroupZkPath}"))
          .thenReturn(new util.ArrayList[String]() {
            add(newGroupInstance.id.get)
          })
        when(curatorFramework.checkExists())
          .thenReturn(existsBuilder)
        when(curatorFramework.checkExists()
          .forPath(s"/stratio/sparta/sparta/group/${updateGroupModel.id.get}"))
          .thenReturn(new Stat())
        when(curatorFramework.getData)
          .thenReturn(getDataBuilder)
        when(curatorFramework.getData
          .forPath(s"/stratio/sparta/sparta/group/${newGroupInstance.id.get}"))
          .thenReturn(newGroupInstanceJSON.getBytes)
        when(curatorFramework.setData())
          .thenReturn(setDataBuilder)
        when(curatorFramework.setData()
          .forPath(s"/stratio/sparta/sparta/group/${updateGroupModel.id.get}", newGroupInstanceJSON.getBytes))
          .thenReturn(new Stat())


        groupActor ! GroupActor.UpdateGroup(updateGroupModel, rootUser)

        expectMsgPF() {
          case Left(Success(x: Group)) => x.name.equals(updateGroupModel.name)
        }
      }

    "UpdateGroup: tries to create a group but throw exception because the group not exists" in
      new TestData {
        when(curatorFramework.checkExists())
          .thenReturn(existsBuilder)
        when(curatorFramework.checkExists()
          .forPath("/stratio/sparta/sparta/group"))
          .thenReturn(null)

        groupActor ! GroupActor.UpdateGroup(inexistentGroupElementModel, rootUser)

        expectMsg(Left(Failure(
          new ServerException(s"Unable to update group ${inexistentGroupElementModel.id.get}: group not found"))))
      }

    "UpdateGroup: tries to create a group but throw exception because there is a group with the same name" in
      new TestData {
        when(curatorFramework.getChildren)
          .thenReturn(getChildrenBuilder)
        when(curatorFramework.getChildren
          .forPath(s"${AppConstant.GroupZkPath}"))
          .thenReturn(new util.ArrayList[String](
            util.Arrays.asList(newGroupInstance.id.get,updateGroupModel.id.get)))
        when(curatorFramework.checkExists())
          .thenReturn(existsBuilder)
        when(curatorFramework.checkExists()
          .forPath(s"/stratio/sparta/sparta/group/${updateGroupModel.id.get}"))
          .thenReturn(new Stat())
        when(curatorFramework.getData)
          .thenReturn(getDataBuilder)
        when(curatorFramework.getData
          .forPath(s"/stratio/sparta/sparta/group/${newGroupInstance.id.get}"))
          .thenReturn(newGroupInstanceJSON.getBytes)
        when(curatorFramework.getData)
          .thenReturn(getDataBuilder)
        when(curatorFramework.getData
          .forPath(s"/stratio/sparta/sparta/group/${updateGroupModel.id.get}"))
          .thenReturn(updateGroupInstanceJSON.getBytes)
        when(curatorFramework.setData())
          .thenReturn(setDataBuilder)
        when(curatorFramework.setData()
          .forPath(s"/stratio/sparta/sparta/group/${updateGroupModel.id.get}", newGroupInstanceJSON.getBytes))
          .thenReturn(new Stat())
        when(curatorFramework.checkExists())
          .thenReturn(existsBuilder)
        when(curatorFramework.checkExists()
          .forPath(s"/stratio/sparta/sparta/group/${newGroupInstance.id.get}"))
          .thenReturn(new Stat())


        groupActor ! GroupActor.UpdateGroup(updateGroupModel, rootUser)
        expectMsg(Left(Failure(
          new ServerException(s"Unable to update group ${updateGroupModel.id.get} " +
            s"with name ${updateGroupModel.name}:target group already exists"))))
      }

    "DeleteGroupById: delete a group by its ID" in new TestData {
      mockListOfGroups
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
      groupActor ! GroupActor.DeleteGroupByID(defaultGroup.id.get, rootUser)

      expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])
    }

    "DeleteGroupByName: delete a group by its name" in new TestData {
      mockListOfGroups

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
      groupActor ! GroupActor.DeleteGroupByName(defaultGroup.name, rootUser)

      expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])
    }

    "DeleteGroup By ID: delete a group and node not exists" in new TestData {
      mockListOfGroups
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/group"))
        .thenReturn(null)

      groupActor ! GroupActor.DeleteGroupByID(defaultGroup.id.get, rootUser)

      expectMsg(Left(Failure(new ServerException("No group available to delete"))))
    }
  }
}
