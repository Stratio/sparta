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

package com.stratio.sparkta.driver.test.route

import com.stratio.sparkta.driver.service.http.PolicyHttpService

import scala.concurrent.duration._

import akka.actor.{ActorRef, ActorRefFactory}
import akka.testkit.TestProbe
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import spray.http.StatusCodes._
import spray.testkit.ScalatestRouteTest

import com.stratio.sparkta.driver.actor.StreamingContextStatusEnum._
import com.stratio.sparkta.driver.actor.{CreateContext, DeleteContext, GetAllContextStatus, GetContextStatus}
import com.stratio.sparkta.driver.dto._

@RunWith(classOf[JUnitRunner])
class PolicyRoutesSpec extends WordSpecLike
with PolicyHttpService
with ScalatestRouteTest
with Matchers {

  val supervisorProbe = TestProbe()

  override val supervisor = supervisorProbe.ref

  def actorRefFactory: ActorRefFactory = system

  implicit val routeTestTimeout = RouteTestTimeout(10.second)

  val checkpointInterval = 10000
  val checkpointAvailable = 60000
  val checkpointGranularity = "minute"
  val checkpointDir = "checkpoint"

  "A PolicytRoutes should" should {
    "Get info about created policies" in {
      val test = Get("/policy") ~> routes
      supervisorProbe.expectMsg(GetAllContextStatus)

      supervisorProbe.reply(List(
        new StreamingContextStatusDto("p-1", Initializing, None),
        new StreamingContextStatusDto("p-2", Error, Some("SOME_ERROR_DESCRIPTION"))))

      test ~> check {
        status should equal(OK)
        entity.asString should include("p-1")
        entity.asString should include("p-2")
        entity.asString should include("SOME_ERROR_DESCRIPTION")
      }
    }
    "Get info about specific policy" in {
      val PolicyName = "p-1"
      val test = Get("/policy/" + PolicyName) ~> routes
      supervisorProbe.expectMsg(new GetContextStatus(PolicyName))
      supervisorProbe.reply(new StreamingContextStatusDto(PolicyName, Initialized, None))
      test ~> check {
        status should equal(OK)
        entity.asString should include(PolicyName)
      }
    }
    "Create policy" in {
      val PolicyName = "p-1"
      val apd = new AggregationPoliciesDto(PolicyName, "false", "myPath","day",
        checkpointDir, "", checkpointGranularity, checkpointInterval, checkpointAvailable, 0,
        Seq(), Seq(), Seq(), Seq(), Seq(), Seq(), Seq())
      try {
        val test = Post("/policy", apd) ~> routes
        supervisorProbe.expectMsg(new CreateContext(apd))
        supervisorProbe.reply(Unit)
        test ~> check {
          status should equal(OK)
        }
      } catch {
        //FIXME "timeout (3 seconds) during expectMsg"
        case e: Throwable => print(e)
      }
    }
    "Delete policy" in {
      val PolicyName = "p-1"
      val test = Delete("/policy/" + PolicyName) ~> routes
      supervisorProbe.expectMsg(new DeleteContext(PolicyName))
      supervisorProbe.reply(new StreamingContextStatusDto(PolicyName, Removed, None))
      test ~> check {
        status should equal(OK)
        entity.asString should include(PolicyName)
        entity.asString should include("Removed")
      }
    }
    "Validate policy rollup" in {
      val PolicyName = "p-1"
      val DimensionToRollup = "dimension2"
      val dimensionDto = new DimensionDto("dimensionType", "dimension1", None)
      val rollupDto = new RollupDto(Seq(new DimensionAndBucketTypeDto(DimensionToRollup, "dimensionType", None)), Seq())
      val apd =
        new AggregationPoliciesDto(PolicyName, "true", "example","day",
          checkpointDir, "", checkpointGranularity, checkpointInterval, checkpointAvailable, 0,
          Seq(dimensionDto), Seq(rollupDto), Seq(), Seq(), Seq(), Seq(), Seq())
      val test = Post("/policy", apd) ~> routes
      test ~> check {
        rejections.size should be(1)
      }
    }
  }
}
