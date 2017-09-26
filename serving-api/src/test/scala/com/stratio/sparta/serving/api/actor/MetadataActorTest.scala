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

import java.nio.file.{Files, Path}

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.api.actor.MetadataActor.{ExecuteBackup, _}
import com.stratio.sparta.serving.core.config.{SpartaConfig, SpartaConfigFactory}
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.files.{BackupRequest, SpartaFile, SpartaFilesResponse}
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import org.apache.curator.test.TestingCluster
import org.apache.curator.utils.CloseableUtils
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import spray.http.BodyPart
import com.stratio.sparta.serving.core.constants._
import com.stratio.sparta.serving.core.helpers.DummySecurityTestClass
import com.stratio.sparta.serving.core.models.dto.LoggedUser

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class MetadataActorTest extends TestKit(ActorSystem("PluginActorSpec"))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with MockitoSugar
  with SLF4JLogging
  with SpartaSerializer {

  val tempDir: Path = Files.createTempDirectory("test")
  tempDir.toFile.deleteOnExit()

  val localConfig: Config = ConfigFactory.parseString(
    s"""
       |sparta{
       |   api {
       |     host = local
       |     port= 7777
       |   }
       |   zookeeper: {
       |     connectionString = "localhost:2181",
       |     storagePath = "/stratio/sparta/sparta"
       |     connectionTimeout = 15000,
       |     sessionTimeout = 60000
       |     retryAttempts = 5
       |     retryInterval = 2000
       |   }
       |}
       |
       |sparta.config.backupsLocation = "$tempDir"
    """.stripMargin)

  val fileList = Seq(BodyPart("reference.conf", "file"))
  implicit val secManager = Option(new DummySecurityTestClass().asInstanceOf[SpartaSecurityManager])
  var zkTestServer: TestingCluster = _
  var clusterConfig: Option[Config] = None
  val rootUser = Some(LoggedUser("1234","root", "dummyMail","0",Seq.empty[String],Seq.empty[String]))
  val limitedUser = Some(LoggedUser("4321","limited", "dummyMail","0",Seq.empty[String],Seq.empty[String]))

  override def beforeEach(): Unit = {
    zkTestServer = new TestingCluster(1)
    zkTestServer.start()
    clusterConfig = Some(localConfig.withValue("sparta.zookeeper.connectionString",
      ConfigValueFactory.fromAnyRef(zkTestServer.getConnectString)))
    SpartaConfig.initMainConfig(clusterConfig, SpartaConfigFactory(localConfig))
    SpartaConfig.initApiConfig()
    val instance = CuratorFactoryHolder.getInstance()

    if (CuratorFactoryHolder.existsPath("/stratio"))
      instance.delete().deletingChildrenIfNeeded().forPath("/stratio")
    else log.debug("Test node not created. It is not necessary to delete it.")

    CuratorFactoryHolder.resetInstance()
  }

  override def afterAll: Unit = {
    shutdown()
    CuratorFactoryHolder.resetInstance()
    CloseableUtils.closeQuietly(zkTestServer)
  }

  override implicit val timeout: Timeout = Timeout(15 seconds)

  "MetadataActor " must {

    "Not save files with wrong extension" in {
      val metadataActor = system.actorOf(Props(new MetadataActor()))
      metadataActor ! (UploadBackups(fileList, rootUser))
      expectMsgPF() {
        case Left(SpartaFilesResponse(Success(f: Seq[SpartaFile]))) => f.isEmpty shouldBe true
      }
      metadataActor ! (DeleteBackups(rootUser))
      expectMsgPF() {
        case Left(BackupResponse(Success(_))) =>
      }
    }
    "Not upload empty files" in {
      val metadataActor = system.actorOf(Props(new MetadataActor()))
      metadataActor ! UploadBackups(Seq.empty, rootUser)
      expectMsgPF() {
        case Left(SpartaFilesResponse(Failure(f))) => f.getMessage shouldBe "At least one file is expected"
      }
      metadataActor ! DeleteBackups(rootUser)
      expectMsgPF() {
        case Left(BackupResponse(Success(_))) =>
      }
    }
    "Save a file" in {
      val metadataActor = system.actorOf(Props(new MetadataActor()))
      metadataActor ! UploadBackups(Seq(BodyPart("reference.conf", "file.json")), rootUser)
      expectMsgPF() {
        case Left(SpartaFilesResponse(Success(f: Seq[SpartaFile]))) =>
          f.head.fileName.endsWith("file.json") shouldBe true
      }
      metadataActor ! DeleteBackups(rootUser)
      expectMsgPF() {
        case Left(BackupResponse(Success(_))) =>
      }
    }

    "Build backup and response the uploaded file" in {
      val instance = CuratorFactoryHolder.getInstance()
      instance.create().creatingParentsIfNeeded().forPath(s"${AppConstant.DefaultZKPath}/test", "testData".getBytes)
      val metadataActor = system.actorOf(Props(new MetadataActor()))
      metadataActor ! BuildBackup(rootUser)
      expectMsgPF() {
        case Left(SpartaFilesResponse(Success(f: Seq[SpartaFile]))) => f.head.fileName.startsWith("backup-") shouldBe true
      }
      metadataActor ! DeleteBackups(rootUser)
      expectMsgPF() {
        case Left(BackupResponse(Success(_))) =>
      }
    }

    "Build backup and response error when path does not exists" in {
      val metadataActor = system.actorOf(Props(new MetadataActor()))
      metadataActor ! BuildBackup(rootUser)
      expectMsgPF() {
        case Left(SpartaFilesResponse(Failure(e: Exception))) => e.getLocalizedMessage shouldBe
          "org.apache.zookeeper.KeeperException$NoNodeException: KeeperErrorCode = NoNode for " +
            s"${AppConstant.DefaultZKPath}"
      }
      metadataActor ! DeleteBackups(rootUser)
      expectMsgPF() {
        case Left(BackupResponse(Success(_))) =>
      }
    }

    "Build backup, clean metadata and execute backup" in {
      val instance = CuratorFactoryHolder.getInstance()
      instance.create().creatingParentsIfNeeded().forPath(s"${AppConstant.DefaultZKPath}/test", "testData".getBytes)
      val metadataActor = system.actorOf(Props(new MetadataActor()))
      metadataActor ! BuildBackup(rootUser)
      var backupFile = ""
      expectMsgPF() {
        case Left(SpartaFilesResponse(Success(f: Seq[SpartaFile]))) =>
          f.foreach(file => if (file.fileName.startsWith("backup-")) backupFile = file.fileName)
      }
      metadataActor ! CleanMetadata(rootUser)
      expectMsgPF() {
        case Left(BackupResponse(Success(_))) =>
          CuratorFactoryHolder.existsPath(s"${AppConstant.DefaultZKPath}/test") shouldBe false
      }

      metadataActor ! ExecuteBackup(BackupRequest(backupFile), rootUser)
      expectMsgPF() {
        case Left(BackupResponse(Success(_))) =>
      }

      CuratorFactoryHolder.existsPath(s"${AppConstant.DefaultZKPath}/test") shouldBe true

      metadataActor ! DeleteBackups(rootUser)
      expectMsgPF() {
        case Left(BackupResponse(Success(_))) =>
      }
    }

    "Build backup and delete backup" in {
      val instance = CuratorFactoryHolder.getInstance()
      instance.create().creatingParentsIfNeeded().forPath(s"${AppConstant.DefaultZKPath}/test", "testData".getBytes)
      val metadataActor = system.actorOf(Props(new MetadataActor()))
      metadataActor ! BuildBackup(rootUser)
      var backupFile = ""
      expectMsgPF() {
        case Left(SpartaFilesResponse(Success(f: Seq[SpartaFile]))) =>
          f.foreach(file => if (file.fileName.startsWith("backup-")) backupFile = file.fileName)
      }

      metadataActor ! DeleteBackup(backupFile, rootUser)
      expectMsgPF() {
        case Left(BackupResponse(Success(_))) =>
      }

      metadataActor ! ListBackups(rootUser)
      expectMsgPF() {
        case Left(SpartaFilesResponse(Success(f: Seq[SpartaFile]))) => f.size shouldBe 0
      }

      metadataActor ! DeleteBackups(rootUser)
      expectMsgPF() {
        case Left(BackupResponse(Success(_))) =>
      }
    }
  }
}
