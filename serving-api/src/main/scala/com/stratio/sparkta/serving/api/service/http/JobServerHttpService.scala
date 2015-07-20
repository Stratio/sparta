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

package com.stratio.sparkta.serving.api.service.http

import akka.pattern.ask
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.core.messages.ActorsMessages._
import com.wordnik.swagger.annotations._
import spray.routing._

import scala.concurrent.Await
import scala.util.{Failure, Success}

@Api(value = "/jobServer", description = "Operations about JobServer.", position = 0)
trait JobServerHttpService extends BaseHttpService {

  override def routes: Route = getJars

  case class Result(message: String, desc: Option[String] = None)

  @ApiOperation(value = "Find all jars", notes = "Returns a jars list", httpMethod = "GET",
    response = classOf[String])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def getJars: Route = {
    path("jobServer" / "jars") {
      get {
        complete {
          val future = supervisor ? new JobServerSupervisorActor_getJars()
          Await.result(future, timeout.duration) match {
            case JobServerSupervisorActor_response_getJars(Failure(exception)) => throw exception
            case JobServerSupervisorActor_response_getJars(Success(jarList)) => jarList
          }
        }
      }
    }
  }
}
