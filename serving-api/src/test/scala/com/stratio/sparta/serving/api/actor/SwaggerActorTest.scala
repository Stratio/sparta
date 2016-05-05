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
import org.apache.curator.framework.CuratorFramework
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor

@RunWith(classOf[JUnitRunner])
class SwaggerActorTest(_system: ActorSystem) extends TestKit(_system)
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll
with MockFactory {

  def this() = this(ActorSystem("SwaggerActorSpec"))

  val curatorFramework = mock[CuratorFramework]
  val streamingContextService = mock[StreamingContextService]

  val policyStatusActor = _system.actorOf(Props(new PolicyStatusActor(curatorFramework)))
  val fragmentActor = _system.actorOf(Props(new FragmentActor(curatorFramework)))
  val templateActor = _system.actorOf(Props(new TemplateActor()))
  val policyActor = _system.actorOf(Props(new PolicyActor(curatorFramework, policyStatusActor)))
  val sparkStreamingContextActor = _system.actorOf(
    Props(new SparkStreamingContextActor(streamingContextService, policyStatusActor, curatorFramework)))

  implicit val actors = Map(
    AkkaConstant.PolicyStatusActor -> policyStatusActor,
    AkkaConstant.FragmentActor -> fragmentActor,
    AkkaConstant.TemplateActor -> templateActor,
    AkkaConstant.PolicyActor -> policyActor,
    AkkaConstant.SparkStreamingContextActor -> sparkStreamingContextActor)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "ControllerActor" should {
    "set up the controller actor that contains all sparta's routes without any error" in {
      _system.actorOf(Props(new SwaggerActor(actors, curatorFramework)))
    }
  }
}
