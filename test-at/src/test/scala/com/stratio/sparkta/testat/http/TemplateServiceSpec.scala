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

package com.stratio.sparkta.testat.http

import java.io.File

import akka.actor.ActorRefFactory
import akka.testkit.TestProbe
import akka.util.Timeout
import com.stratio.sparkta.driver.actor._
import com.stratio.sparkta.driver.helpers.sparkta.SparktaHelper
import com.stratio.sparkta.driver.service.http.TemplateHttpService
import com.stratio.sparkta.testat.SparktaATSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import spray.client.pipelining._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.ScalatestRouteTest

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class TemplateServiceSpec extends SparktaATSuite
  with WordSpecLike
  with ScalatestRouteTest
  with Matchers {

  override val policyFile = ""
  override val PathToCsv = ""

  "A TemplateService should" should {
    "Get a parsed template from a type and a name" in {
      startSparkta
      checkData(s"http://${Localhost}:${SparktaPort}/template/outputs/mongodb")
      checkData(s"http://${Localhost}:${SparktaPort}/template/outputs")
    }
  }

  def checkData(url: String): Unit = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val promise: Future[HttpResponse] =
      pipeline(Get(url))
    val response: HttpResponse = Await.result(promise, Timeout(200.seconds).duration)
    response.status should be(OK)
    log.info(response.entity.data.asString)
  }

  override def extraBefore: Unit = {}

  override def extraAfter: Unit = {}

}
