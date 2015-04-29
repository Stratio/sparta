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
package com.stratio.sparkta.driver.route



import scala.concurrent.ExecutionContextExecutor

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.wordnik.swagger.annotations._
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import spray.httpx.Json4sJacksonSupport
import spray.routing.{ExceptionHandler, HttpService}
import spray.util.LoggingContext

import com.stratio.sparkta.driver.actor._
import com.stratio.sparkta.driver.dto.{AggregationPoliciesDto, AggregationPoliciesValidator, StreamingContextStatusDto}
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import scala.concurrent.duration.DurationDouble

@Api(value = "/policy", description = "Operations about policies.", position = 0)
trait PolicyRoutes extends HttpService with Json4sJacksonSupport {

  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()

  implicit val timeout: Timeout = 15 seconds

  implicit def executionContext: ExecutionContextExecutor = actorRefFactory.dispatcher

  val supervisor: ActorRef
  val policyRoutes = policyGet ~ policyPost ~ policyByNameGet ~ deletePolicy



  @ApiOperation(value = "Find all policies", notes = "Returns a policies list", httpMethod = "GET",
    response =
      classOf[String])
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "not found")

  ))
  private def policyGet = {
    path("policy") {
      get {
        complete {
          supervisor.ask(GetAllContextStatus).mapTo[List[StreamingContextStatusDto]]
        }
      }
    }
  }

  @ApiOperation(value = "post a policy", notes = "Returns the result", httpMethod = "POST",
    response =
      classOf[Result])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy", defaultValue = "", value = "policy json", dataType =
      "AggregationPoliciesDto",
      required =
        true,
      paramType = "json")
  ))
  private def policyPost = {
    post {
      entity(as[AggregationPoliciesDto]) { p =>
        val isValidAndMessageTuple = AggregationPoliciesValidator.validateDto(p)
        validate(isValidAndMessageTuple._1, isValidAndMessageTuple._2) {
          complete {
            supervisor ! new CreateContext(p)
            new Result("Creating new context with name " + p.name)
          }
        }
      }
    }
  }

  @ApiOperation(value = "get policy status by name", notes = "Returns the status of a policy", httpMethod = "GET",
    response =
      classOf[StreamingContextStatusDto])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy name", defaultValue = "", value = "policy name", dataType =
      "String",
      required =
        true
    )
  ))
  private def policyByNameGet = {
    pathPrefix("policy" / Segment) { name =>
      get {
        complete {
          supervisor.ask(new GetContextStatus(name)).mapTo[StreamingContextStatusDto]
        }
      }
    }
  }

  @ApiOperation(value = "delete a policy by name", notes = "Returns the status of the policy", httpMethod = "DELETE",
    response =
      classOf[StreamingContextStatusDto])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy name", defaultValue = "", value = "policy name", dataType =
      "String",
      required =
        true
    )
  ))
  private def deletePolicy = {
    pathPrefix("policy" / Segment) { name =>
      delete {
        complete {
          supervisor.ask(new DeleteContext(name)).mapTo[StreamingContextStatusDto]
        }
      }
    }
  }

  //TODO test
  implicit def driverExceptionHandler(implicit log: LoggingContext): ExceptionHandler =
    ExceptionHandler {
      case e: DriverException =>
        requestUri { uri =>
          log.warning("Request to {} could not be handled normally", uri)
          complete(e, new Result("INTERNAL ERROR", Some(e.getMessage)))
        }
    }
}

case class Result(message: String, desc: Option[String] = None)
