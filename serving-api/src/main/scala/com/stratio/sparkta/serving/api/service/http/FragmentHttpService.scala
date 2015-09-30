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
import com.stratio.sparkta.serving.api.actor.FragmentActor._
import com.stratio.sparkta.serving.api.actor.PolicyActor.{Delete, FindByFragment, ResponsePolicies}
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.core.models.FragmentElementModel
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing.Route

import scala.concurrent.Await
import scala.util.{Failure, Success}

@Api(value = HttpConstant.FragmentPath, description = "Operations over fragments: inputs and outputs that will be " +
  "included in a policy")
trait FragmentHttpService extends BaseHttpService {

  override def routes: Route = findByTypeAndId ~ findAllByType ~ create ~ update ~ deleteByTypeAndId ~ findByTypeAndName

  @ApiOperation(value = "Find a fragment depending of its type and id.",
    notes = "Find a fragment depending of its type and id.",
    httpMethod = "GET",
    response = classOf[FragmentElementModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragmentType",
      value = "type of fragment (input/output)",
      dataType = "string",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "id",
      value = "id of the fragment",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByTypeAndId: Route = {
    path(HttpConstant.FragmentPath / Segment / Segment) { (fragmentType, id) =>
      get {
        complete {
          val future = supervisor ? new FindByTypeAndId(fragmentType, id)
          Await.result(future, timeout.duration) match {
            case ResponseFragment(Failure(exception)) => throw exception
            case ResponseFragment(Success(fragment)) => fragment
          }
        }
      }
    }
  }

  @Path("/{fragmentType}/name/{name}")
  @ApiOperation(value = "Find a fragment depending of its type and name.",
    notes = "Find a fragment depending of its type and name.",
    httpMethod = "GET",
    response = classOf[FragmentElementModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragmentType",
      value = "type of fragment (input/output)",
      dataType = "string",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "name",
      value = "name of the fragment",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByTypeAndName: Route = {
    path(HttpConstant.FragmentPath / Segment / "name" / Segment) { (fragmentType, name) =>
      get {
        complete {
          val future = supervisor ? new FindByTypeAndName(fragmentType, name)
          Await.result(future, timeout.duration) match {
            case ResponseFragment(Failure(exception)) => throw exception
            case ResponseFragment(Success(fragment)) => fragment
          }
        }
      }
    }
  }

  @ApiOperation(value = "Find a list of fragments depending of its type.",
    notes = "Find a list of fragments depending of its type.",
    httpMethod = "GET",
    response = classOf[FragmentElementModel],
    responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragmentType",
      value = "type of fragment (input|output)",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAllByType: Route = {
    path(HttpConstant.FragmentPath / Segment) { (fragmentType) =>
      get {
        complete {
          val future = supervisor ? new FindByType(fragmentType)
          Await.result(future, timeout.duration) match {
            case ResponseFragments(Failure(exception)) => throw exception
            case ResponseFragments(Success(fragments)) => fragments
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a fragment depending of its type.",
    notes = "Creates a fragment depending of its type.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragment",
      value = "fragment to save",
      dataType = "FragmentElementModel",
      required = true,
      paramType = "body")
  ))
  def create: Route = {
    path(HttpConstant.FragmentPath) {
      post {
        entity(as[FragmentElementModel]) { fragment =>
            val future = supervisor ? new Create(fragment)
            Await.result(future, timeout.duration) match {
              case ResponseFragment(Failure(exception)) => throw exception
              case ResponseFragment(Success(fragment)) => complete(fragment)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates a fragment.",
    notes = "Updates a fragment.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragment",
      value = "fragment json",
      dataType = "FragmentElementModel",
      required = true,
      paramType = "body")))
  def update: Route = {
    path(HttpConstant.FragmentPath) {
      put {
        entity(as[FragmentElementModel]) { fragment =>
          complete {
            val future = supervisor ? new Update(fragment)
            Await.result(future, timeout.duration) match {
              case Response(Failure(exception)) => throw exception
              case Response(Success(fragment)) => HttpResponse(StatusCodes.OK)
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes a fragment depending of its type.",
    notes = "Deletes a fragment depending of its type.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragmentType",
      value = "type of fragment (input/output)",
      dataType = "string",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "id",
      value = "id of the fragment",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteByTypeAndId: Route = {
    path(HttpConstant.FragmentPath / Segment / Segment) { (fragmentType, id) =>
      delete {
        complete {
          val policyActor = actors.get(AkkaConstant.PolicyActor).get
          val future = supervisor ? new DeleteByTypeAndId(fragmentType, id)
          Await.result(future, timeout.duration) match {
            case Response(Failure(exception)) => throw exception
            case Response(Success(_)) => {
              Await.result(
                policyActor ? FindByFragment(fragmentType, id), timeout.duration) match {
                  case ResponsePolicies(Failure(exception)) => throw exception
                  case ResponsePolicies(Success(policies)) => {
                    policies.map(policy => policyActor ! Delete(policy.id.get))
                  }
                }
              HttpResponse(StatusCodes.OK)
            }
          }
        }
      }
    }
  }
}
