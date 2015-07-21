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
import com.stratio.sparkta.serving.api.actor._
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing._

import scala.concurrent.Await
import scala.util.{Failure, Success}

@Api(value = HttpConstant.PolicyPath, description = "Operations about policies.", position = 0)
trait PolicyHttpService extends BaseHttpService {

  case class Result(message: String, desc: Option[String] = None)

  override def routes: Route = find ~ findAll ~ findByFragment ~ create ~ update ~ remove

  @ApiOperation(value = "Find a policy from its name", notes = "Returns a policy", httpMethod = "GET",
    response = classOf[AggregationPoliciesModel])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def find: Route = {
    path(HttpConstant.PolicyPath / "find" / Segment) { (name) =>
      get {
        complete {
          val future = supervisor ? new PolicySupervisorActor_find(name)
          Await.result(future, timeout.duration) match {
            case PolicySupervisorActor_response_policy(Failure(exception)) => throw exception
            case PolicySupervisorActor_response_policy(Success(policy)) => policy
          }
        }
      }
    }
  }

  @ApiOperation(value = "Finds policies that contains a fragment", notes = "Returns a list of policies",
    httpMethod = "GET", response = classOf[AggregationPoliciesModel])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def findByFragment: Route = {
    path(HttpConstant.PolicyPath / "fragment" / Segment / Segment) { (fragmentType, name) =>
      get {
        complete {
          val future = supervisor ? new PolicySupervisorActor_findByFragment(fragmentType, name)
          Await.result(future, timeout.duration) match {
            case PolicySupervisorActor_response_policies(Failure(exception)) => throw exception
            case PolicySupervisorActor_response_policies(Success(policies)) => policies
          }
        }
      }
    }
  }

  @ApiOperation(value = "Finds all policies", notes = "Returns a list of policies",
    httpMethod = "GET", response = classOf[AggregationPoliciesModel])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def findAll: Route = {
    path(HttpConstant.PolicyPath / "all") {
      get {
        complete {
          val future = supervisor ? new PolicySupervisorActor_findAll()
          Await.result(future, timeout.duration) match {
            case PolicySupervisorActor_response_policies(Failure(exception)) => throw exception
            case PolicySupervisorActor_response_policies(Success(policies)) => policies
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a policy", notes = "Creates a policy", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy", defaultValue = "", value = "policy json", dataType = "PolicyElementDto",
      required = true, paramType = "json")))
  def create: Route = {
    path(HttpConstant.PolicyPath) {
      post {
        entity(as[AggregationPoliciesModel]) { policy =>
          complete {
            val future = supervisor ? new PolicySupervisorActor_create(policy)
            Await.result(future, timeout.duration) match {
              case PolicySupervisorActor_response(Failure(exception)) => throw exception
              case PolicySupervisorActor_response(Success(_)) => HttpResponse(StatusCodes.Created)
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates a policy", notes = "Creates a policy", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy", defaultValue = "", value = "policy json", dataType = "PolicyElementDto",
      required = true, paramType = "json")))
  def update: Route = {
    path(HttpConstant.PolicyPath) {
      put {
        entity(as[AggregationPoliciesModel]) { policy =>
          complete {
            val future = supervisor ? new PolicySupervisorActor_update(policy)
            Await.result(future, timeout.duration) match {
              case PolicySupervisorActor_response(Failure(exception)) => throw exception
              case PolicySupervisorActor_response(Success(_)) => HttpResponse(StatusCodes.Created)
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes a policy from its name", notes = "Deletes a fragment", httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def remove: Route = {
    path(HttpConstant.PolicyPath / Segment) { (name) =>
      delete {
        complete {
          val future = supervisor ? new PolicySupervisorActor_delete(name)
          Await.result(future, timeout.duration) match {
            case PolicySupervisorActor_response(Failure(exception)) => throw exception
            case PolicySupervisorActor_response(Success(_)) => HttpResponse(StatusCodes.OK)
          }
        }
      }
    }
  }
}
