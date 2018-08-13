/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit}

import com.stratio.sparta.driver.services.ContextsService
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.helpers.DummySecurityClass
import org.apache.curator.framework.CuratorFramework
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ControllerActorTest(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with MockFactory {

  SpartaConfig.initMainConfig()
  SpartaConfig.initApiConfig()

  implicit val secManager = Option(new DummySecurityClass().asInstanceOf[SpartaSecurityManager])
  val curatorFramework = mock[CuratorFramework]
  val contextService = ContextsService(curatorFramework)
  val inMemoryApiActors = InMemoryApiActors(ActorRef.noSender,ActorRef.noSender, ActorRef.noSender,ActorRef.noSender,
    ActorRef.noSender, ActorRef.noSender, ActorRef.noSender)

  def this() =
    this(ActorSystem("ControllerActorSpec", SpartaConfig.daemonicAkkaConfig))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "ControllerActor" should {
    "set up the controller actor that contains all Sparta's routes without any error" in {
      _system.actorOf(Props(new ControllerActor(
        curatorFramework,ActorRef.noSender,ActorRef.noSender, inMemoryApiActors)))
    }
  }
}
