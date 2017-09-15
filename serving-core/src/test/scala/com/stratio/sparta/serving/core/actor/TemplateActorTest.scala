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

import java.util

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.core.actor.TemplateActor
import com.stratio.sparta.serving.core.actor.TemplateActor.{Response, ResponseTemplate, ResponseTemplates}
import com.stratio.sparta.serving.core.helpers.DummySecurityTestClass
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.TemplateElement
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api._
import org.apache.zookeeper.KeeperException.NoNodeException
import org.apache.zookeeper.data.Stat
import org.json4s.jackson.Serialization.read
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Success

@RunWith(classOf[JUnitRunner])
class TemplateActorTest extends TestKit(ActorSystem("TemplateActorSpec"))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with MockitoSugar with SpartaSerializer {

  trait TestData {

    val template =
      """
        |{
        |  "id": "id",
        |  "templateType": "input",
        |  "className": "TestInputStep",
        |  "classPrettyName": "Test",
        |  "name": "inputname",
        |  "description": "input description",
        |  "configuration": {}
        |}
      """.stripMargin
    val otherTemplate =
      """
        |{
        |  "id": "id2",
        |  "templateType": "input",
        |  "className": "TestInputStep",
        |  "classPrettyName": "Test",
        |  "name": "inputname",
        |  "description": "input description",
        |  "configuration": {}
        |}
      """.stripMargin

    val outputTemplate =
      """
        |{
        |  "id": "id3",
        |  "templateType": "output",
        |  "className": "TestInputStep",
        |  "classPrettyName": "Test",
        |  "name": "outputname",
        |  "description": "output description",
        |  "configuration": {}
        |}rootUser
      """.stripMargin

    val secManager = Option(new DummySecurityTestClass().asInstanceOf[SpartaSecurityManager])
    val templateElementModel = read[TemplateElement](template)
    val templateElementModel2 = read[TemplateElement](outputTemplate)
    val curatorFramework = mock[CuratorFramework]
    val getChildrenBuilder = mock[GetChildrenBuilder]
    val getDataBuilder = mock[GetDataBuilder]
    val existsBuilder = mock[ExistsBuilder]
    val createBuilder = mock[CreateBuilder]
    val deleteBuilder = mock[DeleteBuilder]
    val protectedACL = mock[ProtectACLCreateModeStatPathAndBytesable[String]]
    val setDataBuilder = mock[SetDataBuilder]
    val templateActor = system.actorOf(Props(new TemplateActor(curatorFramework, secManager)))
    implicit val timeout: Timeout = Timeout(15.seconds)
  }

  override def afterAll: Unit = shutdown()

  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))
  val limitedUser = Some(LoggedUser("4321", "limited", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))

  "TemplateActor" must {

    "findByType: returns an empty Seq because the node of type does not exist yet" in new TestData {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/templates/input"))
        .thenThrow(new NoNodeException)

      templateActor ! TemplateActor.FindByType("input", rootUser)
      expectMsg(Left(ResponseTemplates(Success(List()))))
    }

    "findByTypeAndId: returns a failure holded by a ResponseTemplate when the node does not exist" in new TestData {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/templates/input"))
        .thenThrow(new NoNodeException)

      templateActor ! TemplateActor.FindByTypeAndId("input", "id", rootUser)
      expectMsgAnyClassOf(classOf[Either[ResponseTemplate, UnauthorizedResponse]])
    }

    "findByTypeAndName: returns a failure holded by a ResponseTemplate when the node does not exist" in new TestData {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/templates/input"))
        .thenThrow(new NoNodeException)

      templateActor ! TemplateActor.FindByTypeAndName("input", "inputname", rootUser)

      expectMsgAnyClassOf(classOf[Either[ResponseTemplate, UnauthorizedResponse]])
    }

    "findByTypeAndName: returns a failure holded by a ResponseTemplate when no such element" in new TestData {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/templates/input"))
        .thenThrow(new NoSuchElementException)

      templateActor ! TemplateActor.FindByTypeAndName("input", "inputname", rootUser)

      expectMsgAnyClassOf(classOf[Either[ResponseTemplate, UnauthorizedResponse]])
    }

    "findAll: returns a error message when limitedUser attempts to list all the templates" in new TestData {
      templateActor ! TemplateActor.FindAllTemplates(limitedUser)

      expectMsgAnyClassOf(classOf[Either[ResponseTemplates, UnauthorizedResponse]])
    }

    // XXX create
    "create: creates a template and return the created template" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      // scalastyle:off null
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/templates/input"))
        .thenReturn(null)
      // scalastyle:on null
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/templates/input"))
        .thenReturn(util.Arrays.asList("id"))
      when(curatorFramework.create)
        .thenReturn(createBuilder)
      when(curatorFramework.create
        .creatingParentsIfNeeded)
        .thenReturn(protectedACL)
      when(curatorFramework.create
        .creatingParentsIfNeeded
        .forPath("/stratio/sparta/templates/input/element"))
        .thenReturn(template)

      templateActor ! TemplateActor.CreateTemplate(templateElementModel, rootUser)

      expectMsgAnyClassOf(classOf[Either[ResponseTemplate, UnauthorizedResponse]])
    }

    "create: tries to create a template but it is impossible because the template exists" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/templates/input"))
        .thenReturn(new Stat())
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/templates/input"))
        .thenReturn(util.Arrays.asList("id"))

      templateActor ! TemplateActor.CreateTemplate(templateElementModel, rootUser)

      expectMsgAnyClassOf(classOf[Either[ResponseTemplate, UnauthorizedResponse]])
    }

    "create: an output template but an exception is encountered " +
      "because the user doesn't have enough permissions" in new TestData {

      templateActor ! TemplateActor.CreateTemplate(templateElementModel2, limitedUser)

      expectMsgAnyClassOf(classOf[Either[ResponseTemplate, UnauthorizedResponse]])
    }
  }

  // XXX update
  "update: updates a template" in new TestData {
    when(curatorFramework.checkExists())
      .thenReturn(existsBuilder)
    // scalastyle:off null
    when(curatorFramework.checkExists()
      .forPath("/stratio/sparta/templates/input"))
      .thenReturn(null)
    when(curatorFramework.getChildren)
      .thenReturn(getChildrenBuilder)
    when(curatorFramework.getChildren
      .forPath("/stratio/sparta/templates/input"))
      .thenReturn(util.Arrays.asList("id"))
    when(curatorFramework.setData())
      .thenReturn(setDataBuilder)
    when(curatorFramework.setData()
      .forPath("/stratio/sparta/templates/input/element"))
      .thenReturn(new Stat())

    templateActor ! TemplateActor.Update(templateElementModel, rootUser)

    expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])

    // scalastyle:on null
  }

  "update: tries to update a template but the user doesn't have enough permission to do so" in new TestData {

    templateActor ! TemplateActor.Update(templateElementModel, limitedUser)

    expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])
  }

  "update: tries to update a template but it is impossible because the template exists" in new TestData {
    when(curatorFramework.checkExists())
      .thenReturn(existsBuilder)
    when(curatorFramework.checkExists()
      .forPath("/stratio/sparta/templates/input"))
      .thenReturn(new Stat())
    when(curatorFramework.getChildren)
      .thenReturn(getChildrenBuilder)
    when(curatorFramework.getChildren
      .forPath("/stratio/sparta/templates/input"))
      .thenReturn(util.Arrays.asList("id"))
    when(curatorFramework.getData)
      .thenReturn(getDataBuilder)
    when(curatorFramework.getData
      .forPath("/stratio/sparta/templates/input/id"))
      .thenReturn(otherTemplate.getBytes)

    templateActor ! TemplateActor.Update(templateElementModel, rootUser)

    expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])
  }

  "update: tries to update a template but it is impossible because the template does not exist" in new TestData {
    when(curatorFramework.checkExists())
      .thenReturn(existsBuilder)
    when(curatorFramework.checkExists()
      .forPath("/stratio/sparta/templates/input"))
      .thenReturn(new Stat())
    when(curatorFramework.getChildren)
      .thenReturn(getChildrenBuilder)
    when(curatorFramework.getChildren
      .forPath("/stratio/sparta/templates/input"))
      .thenReturn(util.Arrays.asList("id"))
    when(curatorFramework.getData)
      .thenReturn(getDataBuilder)
    when(curatorFramework.getData
      .forPath("/stratio/sparta/templates/input/id"))
      .thenReturn(template.getBytes)

    when(curatorFramework.setData())
      .thenReturn(setDataBuilder)
    when(curatorFramework.setData()
      .forPath("/stratio/sparta/templates/input/element"))
      .thenThrow(new NoNodeException)

    templateActor ! TemplateActor.Update(templateElementModel, rootUser)

    expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])
  }

  // XXX deleteByTypeAndId
  "deleteByTypeAndId: deletes a template by its type and its id" in new TestData {
    // scalastyle:off null
    when(curatorFramework.delete)
      .thenReturn(deleteBuilder)
    when(curatorFramework.delete
      .forPath("/stratio/sparta/templates/input/id"))
      .thenReturn(null)

    templateActor ! TemplateActor.DeleteByTypeAndId("input", "id", rootUser)

    expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])
    // scalastyle:on null
  }

  "deleteByTypeAndId: deletes a template but it is impossible because the template does not exists" in new TestData {
    when(curatorFramework.delete)
      .thenReturn(deleteBuilder)
    when(curatorFramework.delete
      .forPath("/stratio/sparta/templates/input/id"))
      .thenThrow(new NoNodeException)

    templateActor ! TemplateActor.DeleteByTypeAndId("input", "id", rootUser)

    expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])
  }

  "delete: tries to delete a template but the user doesn't have enough permissions" in new TestData {

    templateActor ! TemplateActor.DeleteAllTemplates(limitedUser)

    expectMsgAnyClassOf(classOf[Either[ResponseTemplates, UnauthorizedResponse]])
  }
}
