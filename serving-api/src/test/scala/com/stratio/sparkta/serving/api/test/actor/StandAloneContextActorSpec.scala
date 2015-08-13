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

package com.stratio.sparkta.driver.test.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor.StreamingActor.{CreateContext, GetAllContextStatus, GetContextStatus}
import com.stratio.sparkta.serving.api.actor.{StreamingActor, SupervisorContextActor}
import com.stratio.sparkta.serving.core.models.StreamingContextStatusEnum._
import com.stratio.sparkta.serving.core.models._
import com.typesafe.config.ConfigFactory
import org.apache.spark.streaming.StreamingContext
import org.junit.runner.RunWith
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global

@RunWith(classOf[JUnitRunner])
class StandAloneContextActorSpec
  extends TestKit(ActorSystem("SupervisorActorSpec",
    ConfigFactory.parseString(StandAloneContextActorSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll with MockitoSugar {

  var streamingContextService: Option[StreamingContextService] = None
  var supervisorContextActor: ActorRef = _

  before {
    streamingContextService = Some(mock[StreamingContextService])
    supervisorContextActor = createSupervisorContextActor
    val ssc = Some(mock[StreamingContext])
    doNothing().when(ssc.get).start()
    when(streamingContextService.get.standAloneStreamingContext(any[AggregationPoliciesModel])).thenReturn(ssc)
  }

  after {
    streamingContextService = None
  }

  override protected def afterAll(): Unit = {
    shutdown(system)
  }

  "An SupervisorActor" should {
    "Init a StreamingContextActor " in {
      val supervisorRef = createSupervisorActorStandalone

      supervisorRef ? new CreateContext(createPolicyConfiguration("test-1"))
      (supervisorRef ? new GetContextStatus("test-1")).mapTo[StreamingContextStatus].foreach(scs =>
        scs should be(new StreamingContextStatus("test-1", Initializing, None)))
    }

    "Delete a previously created context" in {

      val supervisorRef = createSupervisorActorStandalone

      supervisorRef ? new CreateContext(createPolicyConfiguration("test-1"))
      (supervisorRef ? new GetContextStatus("test-1")).mapTo[StreamingContextStatus].foreach(scs =>
        scs should be(new StreamingContextStatus("test-1", Removed, None)))
    }

    "Get all context statuses" in {

      val supervisorRef = createSupervisorActorStandalone

      supervisorRef ? new CreateContext(createPolicyConfiguration("test-1"))
      supervisorRef ? new CreateContext(createPolicyConfiguration("test-2"))
      supervisorRef ? new CreateContext(createPolicyConfiguration("test-3"))

      (supervisorRef ? GetAllContextStatus).mapTo[Seq[StreamingContextStatus]].foreach(scs => {
        scs.size should be(3)
        val names = scs.map(_.name)
        names should contain("test-1")
        names should contain("test-2")
        names should contain("test-3")
      })
    }
  }

  private def createSupervisorActorStandalone: ActorRef = {
    system.actorOf(Props(new StreamingActor(streamingContextService.get, None, None, supervisorContextActor)))
  }

  private def createSupervisorContextActor: ActorRef = {
    system.actorOf(Props(new SupervisorContextActor))
  }

  private def createPolicyConfiguration(name: String): AggregationPoliciesModel = {
    val policyConfiguration = mock[AggregationPoliciesModel]
    when(policyConfiguration.name).thenReturn(name)
    policyConfiguration
  }
}

object StandAloneContextActorSpec {

  val config = """
               akka {
                 loglevel ="OFF"
               }
               """
}
