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
  val contextService = ContextsService(curatorFramework, system.actorOf(TestActors.echoActorProps))
  val inMemoryApiActors = InMemoryApiActors(ActorRef.noSender,ActorRef.noSender, ActorRef.noSender,ActorRef.noSender)

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
