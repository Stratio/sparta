/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.driver.test.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.stratio.sparkta.driver.actor.StreamingContextStatusEnum._
import com.stratio.sparkta.driver.actor._
import com.stratio.sparkta.driver.dto.{AggregationPoliciesDto, StreamingContextStatusDto}
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.service.StreamingContextService
import com.typesafe.config.ConfigFactory
import org.apache.spark.streaming.StreamingContext
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.DurationInt

/**
 * Created by ajnavarro on 8/10/14.
 */
class SupervisorActorSpec
  extends TestKit(ActorSystem("SupervisorActorSpec",
    ConfigFactory.parseString(SupervisorActorSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll with MockitoSugar {

  var streamingContextService: StreamingContextService = null

  before {
    streamingContextService = mock[StreamingContextService]
    val ssc = mock[StreamingContext]
    doNothing().when(ssc).start()
    when(streamingContextService.createStreamingContext(any[AggregationPoliciesDto])).thenReturn(ssc)
  }

  after {
    streamingContextService = null
  }

  override protected def afterAll(): Unit = {
    shutdown(system)
  }

  "An SupervisorActor" should {
    "Init a StreamingContextActor and DriverException error is thrown" in {
      val supervisorRef = createSupervisorActor
      val errorMessage = "An error ocurred"

      when(streamingContextService.createStreamingContext(any[AggregationPoliciesDto]))
        .thenThrow(new DriverException(errorMessage))

      within(5000 millis) {
        supervisorRef ! new CreateContext(createPolicyConfiguration("test-1"))
        expectNoMsg
      }

      within(5000 millis) {
        supervisorRef ! new GetContextStatus("test-1")
        expectMsg(new StreamingContextStatusDto(ConfigurationError, errorMessage))
      }

    }
    "Init a StreamingContextActor and any unexpected error is thrown" in {
      val supervisorRef = createSupervisorActor

      when(streamingContextService.createStreamingContext(any[AggregationPoliciesDto]))
        .thenThrow(new NullPointerException)
      within(5000 millis) {
        supervisorRef ! new CreateContext(createPolicyConfiguration("test-1"))
        expectNoMsg
      }

      within(5000 millis) {
        supervisorRef ! new GetContextStatus("test-1")
        expectMsg(new StreamingContextStatusDto(Error, null))
      }
    }
    //TODO test when creating a streamingContextActor unexpected error occurs
    "Init a StreamingContextActor" in {

      val supervisorRef = createSupervisorActor

      within(5000 millis) {
        supervisorRef ! new CreateContext(createPolicyConfiguration("test-1"))
        expectNoMsg
      }

      within(5000 millis) {
        supervisorRef ! new GetContextStatus("test-1")
        expectMsg(new StreamingContextStatusDto(Initialized, null))
      }

    }
    "Get a context status for a created context" in {
      val supervisorRef = createSupervisorActor

      within(5000 millis) {
        supervisorRef ! new CreateContext(createPolicyConfiguration("test-1"))
        expectNoMsg
      }

      within(5000 millis) {
        supervisorRef ! new GetContextStatus("test-1")
        expectMsg(new StreamingContextStatusDto(Initialized, null))
      }
    }
    "Delete a previously created context" in {

      val supervisorRef = createSupervisorActor

      within(5000 millis) {
        supervisorRef ! new CreateContext(createPolicyConfiguration("test-1"))
        expectNoMsg
      }

      within(5000 millis) {
        supervisorRef ! new DeleteContext("test-1")
        expectMsg(new StreamingContextStatusDto(Removed, null))
      }
    }
    "Get all context statuses" in {

      val supervisorRef = createSupervisorActor

      within(5000 millis) {

        supervisorRef ! new CreateContext(createPolicyConfiguration("test-1"))
        expectNoMsg

        supervisorRef ! new CreateContext(createPolicyConfiguration("test-2"))
        expectNoMsg

        supervisorRef ! new CreateContext(createPolicyConfiguration("test-3"))
        expectNoMsg

        supervisorRef ! GetAllContextStatus
        val contextData = receiveWhile(5000 millis) {
          case msg: Map[String, StreamingContextStatusDto] =>
            msg
        }
        contextData.size should be(1)

        contextData.map(d => {
          d.size should be(3)
          d.keys should contain("test-1")
          d.keys should contain("test-2")
          d.keys should contain("test-3")
        })
      }
    }
  }

  private def createSupervisorActor: ActorRef = {
    system.actorOf(Props(new SupervisorActor(streamingContextService)))
  }

  private def createPolicyConfiguration(name: String): AggregationPoliciesDto = {
    val policyConfiguration = mock[AggregationPoliciesDto]
    when(policyConfiguration.name).thenReturn(name)
    policyConfiguration
  }
}

object SupervisorActorSpec {
  val config = """
               akka {
                 loglevel ="OFF"
               }
               """
}
