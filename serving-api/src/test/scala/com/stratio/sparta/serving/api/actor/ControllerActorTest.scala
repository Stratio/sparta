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
import akka.testkit.{ImplicitSender, TestKit}
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.core.actor.{ExecutionActor, FragmentActor, StatusActor}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant
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

  val curatorFramework = mock[CuratorFramework]
  val statusActor = _system.actorOf(Props(new StatusActor(curatorFramework)))
  val executionActor = _system.actorOf(Props(new ExecutionActor(curatorFramework)))
  val streamingContextService = new StreamingContextService(statusActor)
  val fragmentActor = _system.actorOf(Props(new FragmentActor(curatorFramework)))
  val policyActor = _system.actorOf(Props(new PolicyActor(curatorFramework, statusActor)))
  val sparkStreamingContextActor = _system.actorOf(
    Props(new LauncherActor(streamingContextService, policyActor, statusActor, executionActor, curatorFramework)))
  val pluginActor = _system.actorOf(Props(new PluginActor()))

  def this() =
    this(ActorSystem("ControllerActorSpec", SpartaConfig.daemonicAkkaConfig))

  implicit val actors = Map(
    AkkaConstant.statusActor -> statusActor,
    AkkaConstant.FragmentActor -> fragmentActor,
    AkkaConstant.PolicyActor -> policyActor,
    AkkaConstant.LauncherActor -> sparkStreamingContextActor,
    AkkaConstant.PluginActor -> pluginActor,
    AkkaConstant.ExecutionActor -> executionActor
  )

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "ControllerActor" should {
    "set up the controller actor that contains all sparta's routes without any error" in {
      _system.actorOf(Props(new ControllerActor(actors, curatorFramework)))
    }
  }
}
