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
import com.stratio.sparta.serving.api.actor.EnvironmentActor._
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.helpers.DummySecurityTestClass
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.env.{Environment, EnvironmentVariable}
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
class EnvironmentActorTest extends TestKit(ActorSystem("EnvironmentActorSpec"))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with MockitoSugar with SpartaSerializer {

  trait TestData {

    val environment =
      """{
        |"variables":
        |[
        | {
        |  "name": "foo",
        |  "value": "var"
        |  }
        |]
        |}
      """.stripMargin

    val environmentTwo =
      """{
        |"variables":
        |[
        | {
        |  "name": "foo",
        |  "value": "var"
        |  },
        |  {
        |  "name": "sparta",
        |  "value": "stratio"
        |  }
        |]
        |}
      """.stripMargin


    implicit val secManager = Option(new DummySecurityTestClass().asInstanceOf[SpartaSecurityManager])
    val environmentElementModel = read[Environment](environment)
    val environmentTwoElementModel = read[Environment](environmentTwo)
    val curatorFramework = mock[CuratorFramework]
    val getChildrenBuilder = mock[GetChildrenBuilder]
    val getDataBuilder = mock[GetDataBuilder]
    val existsBuilder = mock[ExistsBuilder]
    val createBuilder = mock[CreateBuilder]
    val deleteBuilder = mock[DeleteBuilder]
    val protectedACL = mock[ProtectACLCreateModeStatPathAndBytesable[String]]
    val setDataBuilder = mock[SetDataBuilder]

    val groupRaw =
      """
        |{
        |"name": "default"
        |}
      """.stripMargin

    lazy val environmentActor = system.actorOf(Props(new EnvironmentActor(curatorFramework)))
    implicit val timeout: Timeout = Timeout(15.seconds)
    CuratorFactoryHolder.setInstance(curatorFramework)
  }

  override def afterAll: Unit = shutdown()

  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))
  val limitedUser = Some(LoggedUser("4321", "limited", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))

  "EnvironmentActor" must {

    "FindEnvironment: returns an exception because the node of type does not exist yet" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(null)

      environmentActor ! EnvironmentActor.FindEnvironment(rootUser)
      expectMsg(Left(Failure(new ServerException(s"No environment found"))))
    }

    "FindEnvironment: returns a error message when limitedUser attempts to get environment" in new TestData {
      environmentActor ! EnvironmentActor.FindEnvironment(limitedUser)

      expectMsgAnyClassOf(classOf[Either[ResponseEnvironment, UnauthorizedResponse]])
    }

    "FindEnvironment: returns the environment" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(environment.getBytes)

      environmentActor ! EnvironmentActor.FindEnvironment(rootUser)
      expectMsg(Left(Success(environmentElementModel)))
    }

    "FindEnvironmentVariable: returns an exception because the node of type does not exist yet" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(null)

      environmentActor ! EnvironmentActor.FindEnvironmentVariable("foo", rootUser)
      expectMsg(Left(Failure(new ServerException(s"Impossible to find variable, No environment found"))))
    }

    "FindEnvironmentVariable: returns a error message when limitedUser attempts to get environment" in new TestData {
      environmentActor ! EnvironmentActor.FindEnvironmentVariable("foo", limitedUser)

      expectMsgAnyClassOf(classOf[Either[ResponseEnvironmentVariable, UnauthorizedResponse]])
    }

    "FindEnvironmentVariable: returns exception with not found the environment variable" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(environment.getBytes)

      environmentActor ! EnvironmentActor.FindEnvironmentVariable("other", rootUser)

      expectMsg(Left(Failure(new ServerException(s"The environment variable doesn't exist"))))
    }

    "FindEnvironmentVariable: returns the environment" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(environment.getBytes)

      environmentActor ! EnvironmentActor.FindEnvironmentVariable("foo", rootUser)

      val expectedVariable = EnvironmentVariable("foo", "var")
      expectMsg(Left(Success(expectedVariable)))
    }

    "CreateEnvironment: creates a environment and return the created environment" in new TestData {
      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(null)
      when(curatorFramework.create)
        .thenReturn(createBuilder)
      when(curatorFramework.create
        .creatingParentsIfNeeded)
        .thenReturn(protectedACL)
      when(curatorFramework.create
        .creatingParentsIfNeeded
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(environment)

      environmentActor ! EnvironmentActor.CreateEnvironment(environmentElementModel, rootUser)

      expectMsg(Left(Success(environmentElementModel)))
    }

    "CreateEnvironment: tries to create a environment but execute update because the environment exists" in
      new TestData {
        when(curatorFramework.checkExists())
          .thenReturn(existsBuilder)
        when(curatorFramework.checkExists()
          .forPath("/stratio/sparta/sparta/environment"))
          .thenReturn(new Stat())
        when(curatorFramework.setData())
          .thenReturn(setDataBuilder)
        when(curatorFramework.setData()
          .forPath("/stratio/sparta/sparta/environment"))
          .thenReturn(new Stat())

        environmentActor ! EnvironmentActor.CreateEnvironment(environmentElementModel, rootUser)

        expectMsg(Left(Success(environmentElementModel)))
      }

    "CreateEnvironmentVariable: creates a environment variable and return the created variable" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(environment.getBytes)
      when(curatorFramework.setData())
        .thenReturn(setDataBuilder)
      when(curatorFramework.setData()
        .forPath("/stratio/sparta/sparta/environment", environmentTwo.getBytes))
        .thenReturn(new Stat())

      val spartaVariable = EnvironmentVariable("sparta", "stratio")
      environmentActor ! EnvironmentActor.CreateEnvironmentVariable(spartaVariable, rootUser)

      expectMsg(Left(Success(spartaVariable)))
    }

    "CreateEnvironmentVariable: creates a environment variable and return the modified variable" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(environmentTwo.getBytes)
      when(curatorFramework.setData())
        .thenReturn(setDataBuilder)
      when(curatorFramework.setData()
        .forPath("/stratio/sparta/sparta/environment", environmentTwo.getBytes))
        .thenReturn(new Stat())

      val spartaVariable = EnvironmentVariable("sparta", "stratio2")
      environmentActor ! EnvironmentActor.CreateEnvironmentVariable(spartaVariable, rootUser)

      expectMsg(Left(Success(spartaVariable)))
    }

    "UpdateEnvironment: update a environment" in
      new TestData {
        when(curatorFramework.checkExists())
          .thenReturn(existsBuilder)
        when(curatorFramework.checkExists()
          .forPath("/stratio/sparta/sparta/environment"))
          .thenReturn(new Stat())
        when(curatorFramework.setData())
          .thenReturn(setDataBuilder)
        when(curatorFramework.setData()
          .forPath("/stratio/sparta/sparta/environment", environmentTwo.getBytes))
          .thenReturn(new Stat())

        environmentActor ! EnvironmentActor.UpdateEnvironment(environmentTwoElementModel, rootUser)

        expectMsg(Left(Success(environmentTwoElementModel)))
      }

    "UpdateEnvironment: tries to create a environment but throw exception because the environment not exists" in
      new TestData {
        when(curatorFramework.checkExists())
          .thenReturn(existsBuilder)
        when(curatorFramework.checkExists()
          .forPath("/stratio/sparta/sparta/environment"))
          .thenReturn(null)

        environmentActor ! EnvironmentActor.UpdateEnvironment(environmentElementModel, rootUser)

        expectMsg(Left(Failure(new ServerException("Unable to create environment"))))
      }

    "UpdateEnvironmentVariable: update a environment variable and return the exception" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(environment.getBytes)

      val spartaVariable = EnvironmentVariable("sparta", "stratio")
      environmentActor ! EnvironmentActor.UpdateEnvironmentVariable(spartaVariable, rootUser)

      expectMsg(Left(Failure(new ServerException("The environment variable doesn't exist"))))
    }

    "UpdateEnvironmentVariable: update a environment variable and return the modified variable" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(environmentTwo.getBytes)
      when(curatorFramework.setData())
        .thenReturn(setDataBuilder)
      when(curatorFramework.setData()
        .forPath("/stratio/sparta/sparta/environment", environmentTwo.getBytes))
        .thenReturn(new Stat())

      val spartaVariable = EnvironmentVariable("sparta", "stratio2")
      environmentActor ! EnvironmentActor.UpdateEnvironmentVariable(spartaVariable, rootUser)

      expectMsg(Left(Success(spartaVariable)))
    }

    "DeleteEnvironment: delete a environment" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(new Stat())
      when(curatorFramework.delete)
        .thenReturn(deleteBuilder)
      when(curatorFramework.delete
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(null)

      environmentActor ! EnvironmentActor.DeleteEnvironment(rootUser)

      expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])
    }

    "DeleteEnvironment: delete a environment and node not exists" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(null)

      environmentActor ! EnvironmentActor.DeleteEnvironment(rootUser)

      expectMsg(Left(Failure(new ServerException("No environment available to delete"))))
    }

    "DeleteEnvironmentVariable: delete a environment variable" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(environmentTwo.getBytes)
      when(curatorFramework.setData())
        .thenReturn(setDataBuilder)
      when(curatorFramework.setData()
        .forPath("/stratio/sparta/sparta/environment", environment.getBytes))
        .thenReturn(new Stat())

      environmentActor ! EnvironmentActor.DeleteEnvironmentVariable("sparta", rootUser)

      expectMsg(Left(Success(environmentElementModel)))
    }

    "DeleteEnvironmentVariable: delete a environment variable but not environment available" in new TestData {

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath("/stratio/sparta/sparta/environment"))
        .thenReturn(null)

      environmentActor ! EnvironmentActor.DeleteEnvironmentVariable("sparta", rootUser)

      expectMsg(Left(Failure(new ServerException("Impossible to delete variable, No environment found"))))
    }
  }
}
