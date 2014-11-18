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
package com.stratio.sparkta.driver.route

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.actor._
import com.stratio.sparkta.driver.dto.{JsoneyStringSerializer, AggregationPoliciesDto, StreamingContextStatusDto}
import com.stratio.sparkta.driver.exception.DriverException
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import spray.httpx.Json4sJacksonSupport
import spray.routing.{ExceptionHandler, HttpService}
import spray.util.LoggingContext

import scala.concurrent.duration.DurationDouble

trait PolicyRoutes extends HttpService with Json4sJacksonSupport {
  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()
  implicit val timeout: Timeout = 15 seconds

  implicit def executionContext = actorRefFactory.dispatcher

  val supervisor: ActorRef

  val policyRoutes = {
    path("policy") {
      get {
        complete {
          supervisor.ask(GetAllContextStatus).mapTo[List[StreamingContextStatusDto]]
        }
      }
    } ~
      post {
        entity(as[AggregationPoliciesDto]) { p =>
          complete {
            supervisor ! new CreateContext(p)
            new Result("Creating new context with name " + p.name)
          }
        }
      } ~
      pathPrefix("policy" / Segment) { name =>
        get {
          complete {
            supervisor.ask(new GetContextStatus(name)).mapTo[StreamingContextStatusDto]
          }
        } ~
          delete {
            complete {
              supervisor.ask(new DeleteContext(name)).mapTo[StreamingContextStatusDto]
            }
          }
      }
  }

  //TODO test
  implicit def driverExceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler {
      case e: DriverException =>
        requestUri { uri =>
          log.warning("Request to {} could not be handled normally", uri)
          complete(e, new Result("INTERNAL ERROR", Some(e.getMessage)))
        }
    }
}

case class Result(message: String, desc: Option[String] = None)
