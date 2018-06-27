/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.serving.api.actor.MetadataActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}
import com.stratio.sparta.serving.core.models.files.{BackupRequest, SpartaFile}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import spray.http._

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class MetadataHttpServiceTest extends WordSpec
  with MetadataHttpService
  with HttpServiceBaseTest
  with MockitoSugar {

  val metadataTestProbe= TestProbe()
  val dummyUser = Some(LoggedUserConstant.AnonymousUser)
  val rootUser = Some(LoggedUser("1234","root", "dummyMail","0",Seq.empty[String],Seq.empty[String]))


  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.MetadataActorName -> metadataTestProbe.ref
  )

  override val supervisor: ActorRef = testProbe.ref


  "MetadataHttpService.buildBackup" when {
    "everything goes right" should {
      "create a ZK backup" in {
        val fileResponse = Seq(SpartaFile("backup", "/etc/sds/sparta/backup","/etc/sds/sparta/backup"))
        startAutopilot(Left(Success(fileResponse)))
        Get(s"/${HttpConstant.MetadataPath}/backup/build") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[BuildBackup]
          status should be(StatusCodes.OK)
          responseAs[Seq[SpartaFile]] should equal(fileResponse)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(Failure(new MockException)))
        Get(s"/${HttpConstant.MetadataPath}/backup/build") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[BuildBackup]
          status should be(StatusCodes.InternalServerError)
        }

      }
    }
  }

  "MetadataHttpService.executeBackup" when {
    "everything goes right" should {
      "restore a ZK backup" in {
        startAutopilot(Left(Success("OK")))
        Post(s"/${HttpConstant.MetadataPath}/backup", BackupRequest("backup1")) ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[ExecuteBackup]
          status should be(StatusCodes.OK)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(Failure(new MockException())))
        Post(s"/${HttpConstant.MetadataPath}/backup", BackupRequest("backup1")) ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[ExecuteBackup]
          status should be(StatusCodes.InternalServerError)
        }
      }
    }
  }

  "MetadataHttpService.uploadBackup" when {
    "everything goes right" should {
      "upload a ZK backup" in {
        val fileResponse = Seq(SpartaFile("backup", "/etc/sds/sparta/backup","/etc/sds/sparta/backup"))
        startAutopilot(Left(Success(fileResponse)))
        Put(s"/${HttpConstant.MetadataPath}/backup") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[UploadBackups]
          status should be(StatusCodes.OK)
          responseAs[Seq[SpartaFile]] should equal(fileResponse)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(Failure(new MockException())))
        Put(s"/${HttpConstant.MetadataPath}/backup") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[UploadBackups]
          status should be(StatusCodes.InternalServerError)
        }
      }
    }
  }

  "MetadataHttpService.getAllBackups" when {
    "everything goes right" should {
      "retrieve all the ZK backups" in {
        val fileResponse = Seq(SpartaFile("a", "", ""), SpartaFile("b","",""))
        startAutopilot(Left(Success(fileResponse)))
        Get(s"/${HttpConstant.MetadataPath}/backup") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[ListBackups]
          status should be(StatusCodes.OK)
          responseAs[Seq[SpartaFile]] should equal(fileResponse)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(Failure(new MockException())))
        Get(s"/${HttpConstant.MetadataPath}/backup") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[ListBackups]
          status should be(StatusCodes.InternalServerError)
        }
      }
    }
  }

  "MetadataHttpService.deleteAllBackups" when {
    "everything goes right" should {
      "retrieve all the ZK backups" in {
        startAutopilot(Left(Success("Ok")))
        Delete(s"/${HttpConstant.MetadataPath}/backup") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[DeleteBackups]
          status should be(StatusCodes.OK)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(Failure(new MockException())))
        Delete(s"/${HttpConstant.MetadataPath}/backup") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[DeleteBackups]
          status should be(StatusCodes.InternalServerError)
        }
      }
    }
  }

  "MetadataHttpService.deleteBackup" when {
    "everything goes right" should {
      "delete the desired backup" in {
        val fileToDelete = "backup1"
        startAutopilot(Left(Success("Ok")))
        Delete(s"/${HttpConstant.MetadataPath}/backup/$fileToDelete") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[DeleteBackup]
          status should be(StatusCodes.OK)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        val fileToDelete = "backup1"
        startAutopilot(Left(Failure(new MockException())))
        Delete(s"/${HttpConstant.MetadataPath}/backup/$fileToDelete") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[DeleteBackup]
          status should be(StatusCodes.InternalServerError)
        }
      }
    }
  }


  "MetadataHttpService.cleanMetadata" when {
    "everything goes right" should {
      "clean all data in ZK" in {
        startAutopilot(Left(Success("Ok")))
        Delete(s"/${HttpConstant.MetadataPath}") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[CleanMetadata]
          status should be(StatusCodes.OK)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(Failure(new MockException())))
        Delete(s"/${HttpConstant.MetadataPath}") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[CleanMetadata]
          status should be(StatusCodes.InternalServerError)
        }
      }
    }
  }
}