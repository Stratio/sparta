/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit._
import akka.util.Timeout
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.core.actor.StatusActor.Response
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.DummySecurityTestClass
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api._
import org.apache.zookeeper.data.Stat
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

import scala.concurrent.duration._
import scala.util.Try

@RunWith(classOf[JUnitRunner])
class StatusActorTest extends TestKit(ActorSystem("FragmentActorSpec", SpartaConfig.daemonicAkkaConfig))
  with WordSpecLike
  with Matchers
  with ImplicitSender
  with MockitoSugar {

  implicit val secManager = Option(new DummySecurityTestClass().asInstanceOf[SpartaSecurityManager])
  val curatorFramework = mock[CuratorFramework]
  val getChildrenBuilder = mock[GetChildrenBuilder]
  val getDataBuilder = mock[GetDataBuilder]
  val existsBuilder = mock[ExistsBuilder]
  val createBuilder = mock[CreateBuilder]
  val deleteBuilder = mock[DeleteBuilder]

  SpartaConfig.initMainConfig()


  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))

  val statusListenerActor = system.actorOf(Props(new StatusListenerActor))
  val statusInMemoryApi = system.actorOf(Props(new StatusInMemoryApi))

  val actor = system.actorOf(Props(new StatusActor(curatorFramework, statusListenerActor, statusInMemoryApi)))
  implicit val timeout: Timeout = Timeout(15.seconds)
  val id = "existingID"
  val status = WorkflowStatus("existingID", WorkflowStatusEnum.Launched)
  val statusRaw =
    """
      |{
      |  "id": "existingID",
      |  "status": "Launched"
      |}
    """.stripMargin

  "statusActor" must {

    "find: returns success when find an existing ID " in {
      when(curatorFramework
        .checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowStatusesZkPath}/$id"))
        .thenReturn(new Stat)
      // scalastyle:off null

      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"${AppConstant.WorkflowStatusesZkPath}/$id"))
        .thenReturn(statusRaw.getBytes)

      actor ! StatusActor.FindById(id, rootUser)

      expectMsgAnyClassOf(classOf[Either[Try[WorkflowStatus], UnauthorizedResponse]])

      // scalastyle:on null

    }

    "delete: returns success when deleting an existing ID " in {
      when(curatorFramework
        .checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowStatusesZkPath}/$id"))
        .thenReturn(new Stat)
      // scalastyle:off null

      when(curatorFramework.delete())
        .thenReturn(deleteBuilder)
      when(curatorFramework.delete()
        .forPath(s"${AppConstant.WorkflowStatusesZkPath}/$id"))
        .thenReturn(null)

      actor ! StatusActor.DeleteStatus(id, rootUser)

      expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])

      // scalastyle:on null

    }

    "delete: returns failure when deleting an unexisting ID " in {
      // scalastyle:off null
      when(curatorFramework
        .checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowStatusesZkPath}/$id"))
        .thenReturn(null)

      actor ! StatusActor.DeleteStatus(id, rootUser)

      expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])
      // scalastyle:on null

    }

    "delete: returns failure when deleting an existing ID and an error occurs while deleting" in {
      // scalastyle:off null
      when(curatorFramework
        .checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"${AppConstant.WorkflowStatusesZkPath}/$id"))
        .thenReturn(new Stat())

      when(curatorFramework.delete())
        .thenReturn(deleteBuilder)
      when(curatorFramework.delete()
        .forPath(s"${AppConstant.WorkflowStatusesZkPath}/$id"))
        .thenThrow(new RuntimeException())
      actor ! StatusActor.DeleteStatus(id, rootUser)

      expectMsgAnyClassOf(classOf[Either[Response, UnauthorizedResponse]])
      // scalastyle:on null

    }
  }
}
