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
package com.stratio.sparta.serving.core.actor

import java.util

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import com.stratio.sparta.serving.core.actor.FragmentActor.{Response, ResponseFragment, ResponseFragments}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.policy.fragment.FragmentElementModel
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
class FragmentActorTest extends TestKit(ActorSystem("FragmentActorSpec"))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with MockitoSugar with SpartaSerializer {

  trait TestData {

    val fragment =
      """
        |{
        |  "id": "id",
        |  "fragmentType": "input",
        |  "name": "inputname",
        |  "description": "input description",
        |  "shortDescription": "input description",
        |  "element": {
        |    "name": "input",
        |    "type": "input",
        |    "configuration": {
        |      "configKey": "configValue"
        |    }
        |  }
        |}
      """.stripMargin
    val otherFragment =
      """
        |{
        |  "id": "id2",
        |  "fragmentType": "input",
        |  "name": "inputname",
        |  "description": "input description",
        |  "shortDescription": "input description",
        |  "element": {
        |    "name": "input",
        |    "type": "input",
        |    "configuration": {
        |      "configKey": "configValue"
        |    }
        |  }
        |}
      """.stripMargin

    val fragmentElementModel = read[FragmentElementModel](fragment)
    val curatorFramework = mock[CuratorFramework]
    val getChildrenBuilder = mock[GetChildrenBuilder]
    val getDataBuilder = mock[GetDataBuilder]
    val existsBuilder = mock[ExistsBuilder]
    val createBuilder = mock[CreateBuilder]
    val deleteBuilder = mock[DeleteBuilder]
    val protectedACL = mock[ProtectACLCreateModeStatPathAndBytesable[String]]
    val setDataBuilder = mock[SetDataBuilder]
    val fragmentActor = system.actorOf(Props(new FragmentActor(curatorFramework)))
    implicit val timeout: Timeout = Timeout(15.seconds)
  }

  override def afterAll: Unit = shutdown()

  "FragmentActor" must {

    // XXX findByType
    "findByType: returns all fragments by type" in new TestData {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/fragments/input"))
        .thenReturn(util.Arrays.asList("element"))
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/fragments/input/element"))
        .thenReturn(fragment.getBytes)

      fragmentActor ! FragmentActor.FindByType("input")

      expectMsg(new ResponseFragments(Success(Seq(fragmentElementModel))))
    }

    "findByType: returns an empty Seq because the node of type not exists yet" in new TestData {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/fragments/input"))
        .thenThrow(new NoNodeException)

      fragmentActor ! FragmentActor.FindByType("input")

      expectMsg(new ResponseFragments(Success(Seq())))
    }

    // XXX findByTypeAndId
    "findByTypeAndId: returns a fragments by type and id" in new TestData {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/fragments/id"))
        .thenReturn(util.Arrays.asList("element"))
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/fragments/input/id"))
        .thenReturn(fragment.getBytes)

      fragmentActor ! FragmentActor.FindByTypeAndId("input", "id")

      expectMsg(new ResponseFragment(Success(read[FragmentElementModel](fragment))))
    }

    "findByTypeAndId: returns a failure holded by a ResponseFragment when the node does not exist" in new TestData {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/fragments/input"))
        .thenThrow(new NoNodeException)

      fragmentActor ! FragmentActor.FindByTypeAndId("input", "id")

      expectMsgAnyClassOf(classOf[ResponseFragment])
    }

    // XXX findByTypeAndName
    "findByTypeAndName: returns a fragments by type and name" in new TestData {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/fragments/input"))
        .thenReturn(util.Arrays.asList("element"))
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/fragments/input/element"))
        .thenReturn(fragment.getBytes)

      fragmentActor ! FragmentActor.FindByTypeAndName("input", "inputname")

      expectMsg(new ResponseFragment(Success(read[FragmentElementModel](fragment))))
    }

    "findByTypeAndName: returns a failure holded by a ResponseFragment when the node does not exist" in new TestData {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/fragments/input"))
        .thenThrow(new NoNodeException)

      fragmentActor ! FragmentActor.FindByTypeAndName("input", "inputname")

      expectMsgAnyClassOf(classOf[ResponseFragment])
    }

    "findByTypeAndName: returns a failure holded by a ResponseFragment when no such element" in new TestData {
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/fragments/input"))
        .thenThrow(new NoSuchElementException)

      fragmentActor ! FragmentActor.FindByTypeAndName("input", "inputname")

      expectMsgAnyClassOf(classOf[ResponseFragment])
    }

    // XXX create
    "create: creates a fragment and return the created fragment" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      // scalastyle:off null
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/fragments/input"))
        .thenReturn(null)
      // scalastyle:on null
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/fragments/input"))
        .thenReturn(util.Arrays.asList("id"))
      when(curatorFramework.create)
        .thenReturn(createBuilder)
      when(curatorFramework.create
        .creatingParentsIfNeeded)
        .thenReturn(protectedACL)
      when(curatorFramework.create
        .creatingParentsIfNeeded
        .forPath("/stratio/sparta/fragments/input/element"))
        .thenReturn(fragment)

      fragmentActor ! FragmentActor.Create(fragmentElementModel)

      expectMsgAnyClassOf(classOf[ResponseFragment])
    }

    "create: tries to create a fragment but it is impossible because the fragment exists" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/fragments/input"))
        .thenReturn(new Stat())
      when(curatorFramework.getChildren)
        .thenReturn(getChildrenBuilder)
      when(curatorFramework.getChildren
        .forPath("/stratio/sparta/fragments/input"))
        .thenReturn(util.Arrays.asList("id"))

      fragmentActor ! FragmentActor.Create(fragmentElementModel)

      expectMsgAnyClassOf(classOf[ResponseFragment])
    }
  }

  // XXX update
  "update: updates a fragment" in new TestData {
    when(curatorFramework.checkExists())
      .thenReturn(existsBuilder)
    // scalastyle:off null
    when(curatorFramework.checkExists()
      .forPath("/stratio/sparta/fragments/input"))
      .thenReturn(null)
    when(curatorFramework.getChildren)
      .thenReturn(getChildrenBuilder)
    when(curatorFramework.getChildren
      .forPath("/stratio/sparta/fragments/input"))
      .thenReturn(util.Arrays.asList("id"))
    when(curatorFramework.setData)
      .thenReturn(setDataBuilder)
    when(curatorFramework.setData
      .forPath("/stratio/sparta/fragments/input/element"))
      .thenReturn(new Stat())

    fragmentActor ! FragmentActor.Update(fragmentElementModel)

    expectMsg(new Response(Success(null)))
    // scalastyle:on null
  }

  "update: tries to update a fragment but it is impossible because the fragment exists" in new TestData {
    when(curatorFramework.checkExists())
      .thenReturn(existsBuilder)
    when(curatorFramework.checkExists()
      .forPath("/stratio/sparta/fragments/input"))
      .thenReturn(new Stat())
    when(curatorFramework.getChildren)
      .thenReturn(getChildrenBuilder)
    when(curatorFramework.getChildren
      .forPath("/stratio/sparta/fragments/input"))
      .thenReturn(util.Arrays.asList("id"))
    when(curatorFramework.getData)
      .thenReturn(getDataBuilder)
    when(curatorFramework.getData
      .forPath("/stratio/sparta/fragments/input/id"))
      .thenReturn(otherFragment.getBytes)

    fragmentActor ! FragmentActor.Update(fragmentElementModel)

    expectMsgAnyClassOf(classOf[FragmentActor.Response])
  }

  "update: tries to update a fragment but it is impossible because the fragment does not exist" in new TestData {
    when(curatorFramework.checkExists())
      .thenReturn(existsBuilder)
    when(curatorFramework.checkExists()
      .forPath("/stratio/sparta/fragments/input"))
      .thenReturn(new Stat())
    when(curatorFramework.getChildren)
      .thenReturn(getChildrenBuilder)
    when(curatorFramework.getChildren
      .forPath("/stratio/sparta/fragments/input"))
      .thenReturn(util.Arrays.asList("id"))
    when(curatorFramework.getData)
      .thenReturn(getDataBuilder)
    when(curatorFramework.getData
      .forPath("/stratio/sparta/fragments/input/id"))
      .thenReturn(fragment.getBytes)

    when(curatorFramework.setData)
      .thenReturn(setDataBuilder)
    when(curatorFramework.setData
      .forPath("/stratio/sparta/fragments/input/element"))
      .thenThrow(new NoNodeException)

    fragmentActor ! FragmentActor.Update(fragmentElementModel)

    expectMsgAnyClassOf(classOf[FragmentActor.Response])
  }

  // XXX deleteByTypeAndId
  "deleteByTypeAndId: deletes a fragment by its type and its id" in new TestData {
    // scalastyle:off null
    when(curatorFramework.delete)
      .thenReturn(deleteBuilder)
    when(curatorFramework.delete
      .forPath("/stratio/sparta/fragments/input/id"))
      .thenReturn(null)

    fragmentActor ! FragmentActor.DeleteByTypeAndId("input", "id")

    expectMsg(new Response(Success(null)))
    // scalastyle:on null
  }

  "deleteByTypeAndId: deletes a fragment but it is impossible because the fragment does not exists" in new TestData {
    when(curatorFramework.delete)
      .thenReturn(deleteBuilder)
    when(curatorFramework.delete
      .forPath("/stratio/sparta/fragments/input/id"))
      .thenThrow(new NoNodeException)

    fragmentActor ! FragmentActor.DeleteByTypeAndId("input", "id")

    expectMsgAnyClassOf(classOf[FragmentActor.Response])
  }
}
