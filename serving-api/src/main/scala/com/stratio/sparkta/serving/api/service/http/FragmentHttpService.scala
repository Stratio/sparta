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
import com.stratio.sparkta.driver.models.FragmentElementModel
import com.stratio.sparkta.serving.api.actor._
import com.stratio.sparkta.serving.api.constants.{AkkaConstant, HttpConstant}
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing.Route

import scala.concurrent.Await
import scala.util.{Failure, Success}

@Api(value = HttpConstant.FragmentPath, description = "Operations over fragments: inputs and outputs that will be " +
  "included in a policy")
trait FragmentHttpService extends BaseHttpService {

  override def routes: Route = findByTypeAndName ~ findAllByType ~ create ~ deleteByTypeAndName

  @ApiOperation(value    = "Find a fragment depending of its type.",
                notes    = "Find a fragment depending of its type.",
                httpMethod = "GET",
                response = classOf[FragmentElementModel])
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
    new ApiResponse(code    = HttpConstant.NotFound,
                    message = HttpConstant.NotFoundMessage)
  ))
  def findByTypeAndName: Route = {
    path(HttpConstant.FragmentPath / Segment / Segment) { (fragmentType, name) =>
      get {
        complete {
          val future = supervisor ? new FragmentSupervisorActor_findByTypeAndName(fragmentType, name)
          Await.result(future, timeout.duration) match {
            case FragmentSupervisorActor_response_fragment(Failure(exception)) => throw exception
            case FragmentSupervisorActor_response_fragment(Success(fragment)) => fragment
          }
        }
      }
    }
  }

  @ApiOperation(value             = "Find a list of fragments depending of its type.",
                notes             = "Find a list of fragments depending of its type.",
                httpMethod        = "GET",
                response          = classOf[FragmentElementModel],
                responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name      = "fragmentType",
                         value     = "type of fragment (input|output)",
                         dataType  = "string",
                         paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code    = HttpConstant.NotFound,
                    message = HttpConstant.NotFoundMessage)
  ))
  def findAllByType: Route = {
    path(HttpConstant.FragmentPath / Segment ) { (fragmentType) =>
      get {
        complete {
          val future = supervisor ? new FragmentSupervisorActor_findByType(fragmentType)
          Await.result(future, timeout.duration) match {
            case FragmentSupervisorActor_response_fragments(Failure(exception)) => throw exception
            case FragmentSupervisorActor_response_fragments(Success(fragments)) => fragments
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a fragment depending of its type.",
                notes = "Creates a fragment depending of its type.",
                httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam( name         = "fragment",
                          value        = "fragment to save",
                          dataType     = "FragmentElementModel",
                          required     = true,
                          paramType    = "body")
  ))
  def create: Route = {
    path(HttpConstant.FragmentPath) {
      post {
        entity(as[FragmentElementModel]) { fragment =>
          complete {
            val future = supervisor ? new FragmentSupervisorActor_create(fragment)
            Await.result(future, timeout.duration) match {
              case FragmentSupervisorActor_response(Failure(exception)) => throw exception
              case FragmentSupervisorActor_response(Success(_)) => HttpResponse(StatusCodes.Created)
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
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteByTypeAndName: Route = {
    path(HttpConstant.FragmentPath / Segment / Segment) { (fragmentType, name) =>
      delete {
        complete {
          val policyActor = actors.get(AkkaConstant.PolicyActor).get
          val future = supervisor ? new FragmentSupervisorActor_deleteByTypeAndName(fragmentType, name)
          Await.result(future, timeout.duration) match {
            case FragmentSupervisorActor_response(Failure(exception)) => throw exception
            case FragmentSupervisorActor_response(Success(_)) => {
              Await.result(
                policyActor ? PolicySupervisorActor_findByFragment(fragmentType, name), timeout.duration) match {
                  case PolicySupervisorActor_response_policies(Failure(exception)) => throw exception
                  case PolicySupervisorActor_response_policies(Success(policies)) => {
                    policies.map(policy => policyActor ! PolicySupervisorActor_delete(policy.name))
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
