/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.nio.file.{Files, Path}

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.api.actor.DriverActor.UploadDrivers
import com.stratio.sparta.serving.core.config.{SpartaConfig, SpartaConfigFactory}
import com.stratio.sparta.serving.core.helpers.DummySecurityTestClass
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.files.SpartaFile
import com.typesafe.config.{Config, ConfigFactory}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import spray.http.BodyPart

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class DriverActorTest extends TestKit(ActorSystem("PluginActorSpec"))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with MockitoSugar with SpartaSerializer {

  val tempDir: Path = Files.createTempDirectory("test")
  tempDir.toFile.deleteOnExit()

  val localConfig: Config = ConfigFactory.parseString(
    s"""
       |sparta{
       |   api {
       |     host = local
       |     port= 7777
       |   }
       |}
       |
       |sparta.config.driverPackageLocation = "$tempDir"
    """.stripMargin)

  val fileList = Seq(BodyPart("reference.conf", "file"))
  implicit val secManager = Option(new DummySecurityTestClass().asInstanceOf[SpartaSecurityManager])
  val rootUser = Some(LoggedUser("1234","root", "dummyMail","0",Seq.empty[String],Seq.empty[String]))
  val limitedUser = Some(LoggedUser("4321","limited", "dummyMail","0",Seq.empty[String],Seq.empty[String]))

  override def beforeEach(): Unit = {
    SpartaConfig.initMainConfig(Option(localConfig), SpartaConfigFactory(localConfig))
    SpartaConfig.initApiConfig()
  }

  override def afterAll: Unit = {
    shutdown()
  }

  override implicit val timeout: Timeout = Timeout(15 seconds)

  "DriverActor " must {

    "Not upload empty files" in {
      val driverActor = system.actorOf(Props(new DriverActor()))
      driverActor ! UploadDrivers(Seq.empty, rootUser)
      expectMsgPF() {
        case Left(Failure(f)) => f.getMessage shouldBe "At least one file is expected"
      }
    }
    "Save a file" in {
      val driverActor = system.actorOf(Props(new DriverActor()))
      driverActor ! UploadDrivers(Seq(BodyPart("reference.conf", "file.jar")), rootUser)
      expectMsgPF() {
        case Left(Success(f: Seq[SpartaFile])) =>
          f.head.fileName.endsWith("file.jar") shouldBe true
      }
    }
  }
}
