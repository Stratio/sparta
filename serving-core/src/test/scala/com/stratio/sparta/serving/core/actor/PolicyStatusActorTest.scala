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

import scala.util.{Failure, Success}
import akka.actor.{ActorSystem, Props}
import akka.testkit._
import akka.util.Timeout
import com.stratio.sparta.serving.core.actor.PolicyStatusActor
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api._
import org.apache.zookeeper.data.Stat
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

import scala.concurrent.duration._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.exception.ServingCoreException
import PolicyStatusActor.ResponseDelete

@RunWith(classOf[JUnitRunner])
class PolicyStatusActorTest extends TestKit(ActorSystem("FragmentActorSpec"))
  with WordSpecLike
  with Matchers
  with ImplicitSender
  with MockitoSugar {

  val curatorFramework = mock[CuratorFramework]
  val getChildrenBuilder = mock[GetChildrenBuilder]
  val getDataBuilder = mock[GetDataBuilder]
  val existsBuilder = mock[ExistsBuilder]
  val createBuilder = mock[CreateBuilder]
  val deleteBuilder = mock[DeleteBuilder]

  val actor = system.actorOf(Props(new PolicyStatusActor(curatorFramework)))
  implicit val timeout: Timeout = Timeout(15.seconds)
  val id = "existingID"

  "PolicyStatusActor" must {

    "delete: returns success when deleting an existing ID " in {
      when(curatorFramework
        .checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.ContextPath}/$id"))
        .thenReturn(new Stat)
      // scalastyle:off null

      when(curatorFramework.delete())
        .thenReturn(deleteBuilder)
      when(curatorFramework.delete()
        .forPath(s"${AppConstant.ContextPath}/$id"))
        .thenReturn(null)

      actor ! PolicyStatusActor.Delete(id)

      expectMsg(ResponseDelete(Success(null)))
      // scalastyle:on null

    }

    "delete: returns failure when deleting an unexisting ID " in {
      // scalastyle:off null
      when(curatorFramework
        .checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.ContextPath}/$id"))
        .thenReturn(null)

     actor ! PolicyStatusActor.Delete(id)

      expectMsgAnyClassOf(classOf[ResponseDelete])
      // scalastyle:on null

    }

    "delete: returns failure when deleting an existing ID and an error occurs while deleling" in {
      // scalastyle:off null
      when(curatorFramework
        .checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.ContextPath}/$id"))
        .thenReturn(new Stat())

      when(curatorFramework.delete())
        .thenReturn(deleteBuilder)
      when(curatorFramework.delete()
        .forPath(s"${AppConstant.ContextPath}/$id"))
        .thenThrow(new RuntimeException())
      actor ! PolicyStatusActor.Delete(id)

      expectMsgAnyClassOf(classOf[ResponseDelete])
      // scalastyle:on null

    }
  }
}
