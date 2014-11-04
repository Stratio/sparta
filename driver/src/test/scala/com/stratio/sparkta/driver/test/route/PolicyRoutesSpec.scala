/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.driver.test.route

/**
 * Created by ajnavarro on 3/11/14.
 */

import akka.actor.{ActorRef, ActorRefFactory}
import akka.testkit.TestProbe
import com.stratio.sparkta.driver.actor.StreamingContextStatusEnum._
import com.stratio.sparkta.driver.actor.{CreateContext, DeleteContext, GetAllContextStatus, GetContextStatus}
import com.stratio.sparkta.driver.dto.{AggregationPoliciesDto, StreamingContextStatusDto}
import com.stratio.sparkta.driver.route.PolicyRoutes
import org.scalatest.{Matchers, WordSpecLike}
import spray.http.StatusCodes._
import spray.testkit.ScalatestRouteTest

import scala.concurrent.duration._

class PolicyRoutesSpec extends WordSpecLike
with PolicyRoutes
with ScalatestRouteTest
with Matchers {

  val supervisorProbe = TestProbe()

  override val supervisor: ActorRef = supervisorProbe.ref

  def actorRefFactory: ActorRefFactory = system

  implicit val routeTestTimeout = RouteTestTimeout(10.second)

  "A PolicytRoutes should" should {
    "Get info about created policies" in {
      val test = Get("/policy") ~> policyRoutes
      supervisorProbe.expectMsg(GetAllContextStatus)

      supervisorProbe.reply(List(
        new StreamingContextStatusDto("p-1", Initializing, null),
        new StreamingContextStatusDto("p-2", Error, "SOME_ERROR_DESCRIPTION")))

      test ~> check {
        status should equal(OK)
        entity.asString should include("p-1")
        entity.asString should include("p-2")
        entity.asString should include("SOME_ERROR_DESCRIPTION")
        entity.asString should include("null")
      }
    }
    "Get info about specific policy" in {
      val POLICY_NAME = "p-1"
      val test = Get("/policy/" + POLICY_NAME) ~> policyRoutes
      supervisorProbe.expectMsg(new GetContextStatus(POLICY_NAME))
      supervisorProbe.reply(new StreamingContextStatusDto(POLICY_NAME, Initialized, null))
      test ~> check {
        status should equal(OK)
        entity.asString should include(POLICY_NAME)
        entity.asString should include("null")
      }
    }
    "Create policy" in {
      val POLICY_NAME = "p-1"
      val apd = new AggregationPoliciesDto(Seq(), POLICY_NAME, 0, Seq(), Seq(), Seq(), Seq(), Seq(), Seq())
      val test = Post("/policy", apd) ~> policyRoutes
      supervisorProbe.expectMsg(new CreateContext(apd))
      supervisorProbe.reply(Unit)
      test ~> check {
        status should equal(OK)
        entity.asString should include("Creating new context with name")
        entity.asString should include(POLICY_NAME)
      }
    }
    "Delete policy" in {
      val POLICY_NAME = "p-1"
      val test = Delete("/policy/" + POLICY_NAME) ~> policyRoutes
      supervisorProbe.expectMsg(new DeleteContext(POLICY_NAME))
      supervisorProbe.reply(new StreamingContextStatusDto(POLICY_NAME, Removed, null))
      test ~> check {
        status should equal(OK)
        entity.asString should include(POLICY_NAME)
        entity.asString should include("null")
        entity.asString should include("Removed")
      }
    }
    //TODO test error cases
  }
}
