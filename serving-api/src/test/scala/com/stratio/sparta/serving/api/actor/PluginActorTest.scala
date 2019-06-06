/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.nio.file.{Files, Path}

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import akka.util.Timeout
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.api.actor.PluginActor._
import com.stratio.sparta.serving.core.config.{SpartaConfig, SpartaConfigFactory}
import com.stratio.sparta.serving.core.helpers.DummySecurityTestClass
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.authorization.{GosecUser, LoggedUser}
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
class PluginActorTest extends TestKit(ActorSystem("PluginActorSpec"))
  with DefaultTimeout
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with MockitoSugar with SpartaSerializer with SLF4JLogging {

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
       |sparta.config.pluginsLocation = "$tempDir"
    """.stripMargin)


  val fileList = Seq(BodyPart("reference.conf", "file"))
  implicit val secManager = Option(new DummySecurityTestClass().asInstanceOf[SpartaSecurityManager])
  val rootUser = Some(GosecUser("1234", "root", "dummyMail", "0", Seq.empty, Seq.empty))
  val limitedUser = Some(GosecUser("4321", "limited", "dummyMail","0",Seq.empty ,Seq.empty))

  override def beforeEach(): Unit = {
    SpartaConfig.getApiConfig(Option(localConfig))
  }

  override def afterAll: Unit = {
    shutdown()
  }

  override implicit val timeout: Timeout = Timeout(15 seconds)

  "PluginActor " must {

    "Not upload empty files" in {
      val pluginActor = system.actorOf(Props(new PluginActor()))
      pluginActor ! UploadPlugins(Seq.empty, rootUser)
      expectMsgPF() {
        case Left(Failure(f)) => f.getMessage shouldBe "At least one file is expected"
      }
    }
  }

}
