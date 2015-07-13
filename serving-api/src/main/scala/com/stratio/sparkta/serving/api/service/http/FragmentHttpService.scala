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

package com.stratio.sparkta.driver.service.http


import akka.pattern.ask
import com.stratio.sparkta.driver.actor._
import com.stratio.sparkta.driver.constants.HttpConstant
import com.stratio.sparkta.driver.models.FragmentElementModel
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing.Route

import scala.concurrent.Await
import scala.util.{Failure, Success}

@Api(value = HttpConstant.FragmentPath, description = "Operations about fragments.", position = 0)
trait FragmentHttpService extends BaseHttpService {

  override def routes: Route = findByTypeAndName ~ findAllByType ~ create ~ deleteByTypeAndName

  @ApiOperation(value = "Find a segment depending of its type", notes = "Returns a segment", httpMethod = "GET",
    response = classOf[FragmentElementModel])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
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

  @ApiOperation(value = "Find a list of segments depending of its type", notes = "Returns a list of segments",
    httpMethod = "GET", response = classOf[FragmentElementModel])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def findAllByType: Route = {
    path(HttpConstant.FragmentPath / Segment ) { (fragmentType) =>
      get {
        complete {
          val future = supervisor ? new FragmentSupervisorActor_findAllByType(fragmentType)
          Await.result(future, timeout.duration) match {
            case FragmentSupervisorActor_response_fragments(Failure(exception)) => throw exception
            case FragmentSupervisorActor_response_fragments(Success(fragments)) => fragments
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a fragment depending of its type", notes = "Creates a fragment", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragment", defaultValue = "", value = "fragment json", dataType = "FragmentElementDto",
      required = true, paramType = "json")))
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

  @ApiOperation(value = "Deletes a fragment depending of its type", notes = "Deletes a fragment", httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteByTypeAndName: Route = {
    path(HttpConstant.FragmentPath / Segment / Segment) { (fragmentType, name) =>
      delete {
        complete {
          val future = supervisor ? new FragmentSupervisorActor_deleteByTypeAndName(fragmentType, name)
          Await.result(future, timeout.duration) match {
            case FragmentSupervisorActor_response(Failure(exception)) => throw exception
            case FragmentSupervisorActor_response(Success(_)) => HttpResponse(StatusCodes.OK)
          }
        }
      }
    }
  }
}
