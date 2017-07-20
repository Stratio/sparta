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

package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.stratio.sparta.sdk.exception.MockException
import com.stratio.sparta.serving.api.actor.MetadataActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}
import com.stratio.sparta.serving.core.models.files.{BackupRequest, SpartaFile, SpartaFilesResponse}
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
        val fileResponse = Seq(SpartaFile("backup",
          "/etc/sds/sparta/backup","/etc/sds/sparta/backup","251"))
        startAutopilot(Left(SpartaFilesResponse(Success(fileResponse))))
        Get(s"/${HttpConstant.MetadataPath}/backup/build") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[BuildBackup]
          status should be(StatusCodes.OK)
          responseAs[Seq[SpartaFile]] should equal(fileResponse)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(BackupResponse(Failure(new MockException))))
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
        startAutopilot(Left(BackupResponse(Success("OK"))))
        Post(s"/${HttpConstant.MetadataPath}/backup", BackupRequest("backup1")) ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[ExecuteBackup]
          status should be(StatusCodes.OK)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(BackupResponse(Failure(new MockException()))))
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
        val fileResponse = Seq(SpartaFile("backup",
          "/etc/sds/sparta/backup","/etc/sds/sparta/backup","251"))
        startAutopilot(Left(SpartaFilesResponse(Success(fileResponse))))
        Put(s"/${HttpConstant.MetadataPath}/backup") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[UploadBackups]
          status should be(StatusCodes.OK)
          responseAs[Seq[SpartaFile]] should equal(fileResponse)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(SpartaFilesResponse(Failure(new MockException()))))
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
        val fileResponse = Seq(SpartaFile("a", "", "", ""), SpartaFile("b","","",""))
        startAutopilot(Left(SpartaFilesResponse(Success(fileResponse))))
        Get(s"/${HttpConstant.MetadataPath}/backup") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[ListBackups]
          status should be(StatusCodes.OK)
          responseAs[Seq[SpartaFile]] should equal(fileResponse)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(SpartaFilesResponse(Failure(new MockException()))))
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
        startAutopilot(Left(BackupResponse(Success("Ok"))))
        Delete(s"/${HttpConstant.MetadataPath}/backup") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[DeleteBackups]
          status should be(StatusCodes.OK)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(BackupResponse(Failure(new MockException()))))
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
        startAutopilot(Left(BackupResponse(Success("Ok"))))
        Delete(s"/${HttpConstant.MetadataPath}/backup/$fileToDelete") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[DeleteBackup]
          status should be(StatusCodes.OK)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        val fileToDelete = "backup1"
        startAutopilot(Left(BackupResponse(Failure(new MockException()))))
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
        startAutopilot(Left(BackupResponse(Success("Ok"))))
        Delete(s"/${HttpConstant.MetadataPath}") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[CleanMetadata]
          status should be(StatusCodes.OK)
        }
      }
    }
    "there is an error" should {
      "return a 500 error" in {
        startAutopilot(Left(BackupResponse(Failure(new MockException()))))
        Delete(s"/${HttpConstant.MetadataPath}") ~> routes(rootUser) ~> check {
          testProbe.expectMsgType[CleanMetadata]
          status should be(StatusCodes.InternalServerError)
        }
      }
    }
  }
}