/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.serving.api.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.stratio.sparkta.serving.api.actor.TemplateActor
import com.stratio.sparkta.serving.api.actor.TemplateActor.FindByType
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class TemplateActorSpec extends TestKit(ActorSystem("TemplateActorSpec"))
                                with DefaultTimeout
                                with ImplicitSender
                                with WordSpecLike
                                with Matchers
                                with BeforeAndAfterAll {

  val templateActor = system.actorOf(Props(classOf[TemplateActor]))

  override def afterAll {
    shutdown()
  }


  "A template actor" must {

    "return to the sender a full list of existing  templates depending ot its type" in {
      //within(500 millis) {
        templateActor ! FindByType("input")
        //expectMsg("hello world")
      //}
    }
  }
}