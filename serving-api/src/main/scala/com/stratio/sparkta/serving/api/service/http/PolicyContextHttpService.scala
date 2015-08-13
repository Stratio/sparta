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
import com.stratio.sparkta.driver.models._
import com.stratio.sparkta.serving.api.actor.StreamingActor._
import com.stratio.sparkta.serving.api.constants.{AkkaConstant, HttpConstant}
import com.stratio.sparkta.serving.api.helpers.PolicyHelper
import com.wordnik.swagger.annotations._
import spray.routing._

@Api(value = HttpConstant.PolicyContextPath, description = "Operations about policy contexts.", position = 0)
trait PolicyContextHttpService extends BaseHttpService {

  case class Result(message: String, desc: Option[String] = None)

  override def routes: Route = find ~ findAll ~ create ~ remove

  @ApiOperation(value      = "Gets a policy context from its name.",
                notes      = "Returns the status of a policy",
                httpMethod =  "GET",
                response   = classOf[StreamingContextStatus])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name     = "name",
                         value    = "policy name",
                         dataType = "String",
                         required = true)))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def find: Route = {
    pathPrefix(HttpConstant.PolicyContextPath / Segment) { name =>
      get {
        complete {
          supervisor.ask(new GetContextStatus(name)).mapTo[StreamingContextStatus]
        }
      }
    }
  }

  @ApiOperation(value      = "Finds all policy contexts",
                notes      = "Returns a policies list",
                httpMethod = "GET",
                response   = classOf[String],
                responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
                          message = HttpConstant.NotFoundMessage)))
  def findAll: Route = {
    path(HttpConstant.PolicyContextPath) {
      get {
        complete {
          supervisor.ask(GetAllContextStatus).mapTo[List[StreamingContextStatus]]
        }
      }
    }
  }

  @ApiOperation(value      = "Creates a policy context.",
                notes      = "Returns the result",
                httpMethod = "POST",
                response   = classOf[Result])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name         = "policy",
                         value        = "policy json",
                         dataType     = "AggregationPoliciesModel",
                         required     = true,
                         paramType    = "body")))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
                          message = HttpConstant.NotFoundMessage)))
  def create: Route = {
    path(HttpConstant.PolicyContextPath) {
      post {
        entity(as[AggregationPoliciesModel]) { p =>
          val parsedP = PolicyHelper.parseFragments(
            PolicyHelper.fillFragments(p,actors.get(AkkaConstant.FragmentActor).get, timeout))
          val isValidAndMessageTuple = AggregationPoliciesValidator.validateDto(parsedP)
          validate(isValidAndMessageTuple._1, isValidAndMessageTuple._2) {
            complete {
              supervisor ! new CreateContext(parsedP)
              new Result("Creating new context with name " + p.name)
            }
          }
        }
      }
    }
  }

  @ApiOperation(value      = "Deletes a policy context by its name",
                notes      = "Returns the status of the policy",
                httpMethod = "DELETE",
                response   = classOf[StreamingContextStatus])
  @ApiImplicitParams(Array(new ApiImplicitParam(
    name     = "name",
    value    = "policy name",
    dataType = "String",
    required = true)))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
                          message = HttpConstant.NotFoundMessage)))
  def remove: Route = {
    pathPrefix(HttpConstant.PolicyContextPath / Segment) { name =>
      delete {
        complete {
          supervisor.ask(new DeleteContext(name)).mapTo[StreamingContextStatus]
        }
      }
    }
  }
}
