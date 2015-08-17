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

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparkta.driver.constants.AkkaConstant
import com.stratio.sparkta.serving.api.actor.StreamingActor.CreateContext
import com.stratio.sparkta.serving.api.actor._
import com.stratio.sparkta.serving.core.models._
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.api.helpers.PolicyHelper
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing._

import scala.concurrent.Await
import scala.util.{Failure, Success}

@Api(value = HttpConstant.PolicyPath, description = "Operations over policies.")
trait PolicyHttpService extends BaseHttpService {

  case class Result(message: String, desc: Option[String] = None)

  override def routes: Route = find ~ findAll ~ findByFragment ~ create ~ update ~ remove ~ run

  @Path("/find/{name}")
  @ApiOperation(value      = "Find a policy from its name.",
                notes      = "Find a policy from its name.",
                httpMethod = "GET",
                response   = classOf[AggregationPoliciesModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name      = "name",
                         value     = "name of the policy",
                         dataType  = "string",
                         paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code    = HttpConstant.NotFound,
                    message = HttpConstant.NotFoundMessage)
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

  @Path("/fragment/{fragmentType}/{name}")
  @ApiOperation(value      = "Finds policies that contains a fragment.",
                notes      = "Finds policies that contains a fragment.",
                httpMethod = "GET",
                response = classOf[AggregationPoliciesModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name      = "fragmentType",
                         value     = "type of fragment (input/output)",
                         dataType  = "string",
                         paramType = "path"),
    new ApiImplicitParam(name      = "name",
                         value     = "name of the fragment",
                         dataType  = "string",
                         paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
                    message = HttpConstant.NotFoundMessage)
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

  @Path("/all")
  @ApiOperation(value      = "Finds all policies.",
                notes      = "Finds all policies.",
                httpMethod = "GET",
                response   = classOf[AggregationPoliciesModel])
  @ApiResponses(Array(
    new ApiResponse(code    = HttpConstant.NotFound,
                    message = HttpConstant.NotFoundMessage)
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

  @ApiOperation(value      = "Creates a policy.",
                notes      = "Creates a policy.",
                httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name         = "policy",
                         defaultValue = "",
                         value        = "policy json",
                         dataType     = "PolicyElementModel",
                         required     = true,
                         paramType    = "body")))
  def create: Route = {
    path(HttpConstant.PolicyPath) {
      post {
        entity(as[AggregationPoliciesModel]) { policy =>
          val parsedP = PolicyHelper.parseFragments(
            PolicyHelper.fillFragments(policy,actors.get(AkkaConstant.FragmentActor).get, timeout))
          val isValidAndMessageTuple = AggregationPoliciesValidator.validateDto(parsedP)
          validate(isValidAndMessageTuple._1, isValidAndMessageTuple._2)
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

  @ApiOperation(value = "Updates a policy.",
                notes = "Updates a policy.",
                httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy",
                         defaultValue = "",
                         value = "policy json",
                         dataType = "PolicyElementModel",
                         required = true,
                         paramType = "body")))
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

  @ApiOperation(value      = "Deletes a policy from its name.",
                notes      = "Deletes a policy from its name.",
                httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name      = "name",
                         value     = "name of the policy",
                         dataType  = "string",
                         paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code    = HttpConstant.NotFound,
                    message = HttpConstant.NotFoundMessage)
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

  @Path("/run/{name}")
  @ApiOperation(value      = "Runs a policy from its name.",
                notes      = "Runs a policy from its name.",
                httpMethod = "GET",
                response   = classOf[Result])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name      = "name",
                         value     = "name of the policy",
                         dataType  = "string",
                         paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code    = HttpConstant.NotFound,
                    message = HttpConstant.NotFoundMessage)
  ))
  def run: Route = {
    path(HttpConstant.PolicyPath / "run" / Segment) { (name) =>
      get {
        val future = supervisor ? new PolicySupervisorActor_find(name)
        Await.result(future, timeout.duration) match {
          case PolicySupervisorActor_response_policy(Failure(exception)) => throw exception
          case PolicySupervisorActor_response_policy(Success(policy)) => {
            val parsedP = PolicyHelper.parseFragments(
              PolicyHelper.fillFragments(policy,actors.get(AkkaConstant.FragmentActor).get, timeout))
            val isValidAndMessageTuple = AggregationPoliciesValidator.validateDto(parsedP)
            validate(isValidAndMessageTuple._1, isValidAndMessageTuple._2) {
              actors.get("streamingActor").get ! new CreateContext(parsedP)
              complete {
                new Result("Creating new context with name " + policy.name)
              }
            }
          }
        }
      }
    }
  }
}
