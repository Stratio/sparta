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

import java.io.File
import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparkta.driver.constants.AkkaConstant
import com.stratio.sparkta.serving.api.actor.PolicyActor._
import com.stratio.sparkta.serving.api.actor.SparkStreamingContextActor
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.api.helpers.PolicyHelper
import com.stratio.sparkta.serving.core.models._
import com.wordnik.swagger.annotations._
import org.json4s.jackson.Serialization.write
import spray.http.HttpHeaders.`Content-Disposition`
import spray.http.{HttpResponse, StatusCodes}
import spray.routing._

@Api(value = HttpConstant.PolicyPath, description = "Operations over policies.")
trait PolicyHttpService extends BaseHttpService with SparktaSerializer {

  case class Result(message: String, desc: Option[String] = None)

  override def routes: Route = find ~ findAll ~ findByFragment ~ create ~ update ~ remove ~ run ~ download ~ findByName

  @Path("/find/{id}")
  @ApiOperation(value = "Find a policy from its id.",
    notes = "Find a policy from its id.",
    httpMethod = "GET",
    response = classOf[AggregationPoliciesModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the policy",
      dataType = "string",
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def find: Route = {
    path(HttpConstant.PolicyPath / "find" / Segment) { (id) =>
      get {
        complete {
          for {
            response <- (supervisor ? new Find(id)).mapTo[ResponsePolicy]
          } yield response.policy.get
        }
      }
    }
  }

  @Path("/findByName/{name}")
  @ApiOperation(value = "Find a policy from its name.",
    notes = "Find a policy from its name.",
    httpMethod = "GET",
    response = classOf[AggregationPoliciesModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name",
      value = "name of the policy",
      dataType = "string",
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByName: Route = {
    path(HttpConstant.PolicyPath / "findByName" / Segment) { (name) =>
      get {
        complete {
          for {
            response <- (supervisor ? new FindByName(name)).mapTo[ResponsePolicy]
          } yield response.policy.get
        }
      }
    }
  }

  @Path("/fragment/{fragmentType}/{id}")
  @ApiOperation(value = "Finds policies that contains a fragment.",
    notes = "Finds policies that contains a fragment.",
    httpMethod = "GET",
    response = classOf[AggregationPoliciesModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragmentType",
      value = "type of fragment (input/output)",
      dataType = "string",
      paramType = "path"),
    new ApiImplicitParam(name = "id",
      value = "id of the fragment",
      dataType = "string",
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByFragment: Route = {
    path(HttpConstant.PolicyPath / "fragment" / Segment / Segment) { (fragmentType, id) =>
      get {
        complete {
          for {
            response <- (supervisor ? new FindByFragment(fragmentType, id)).mapTo[ResponsePolicies]
          } yield response.policies.get
        }
      }
    }
  }

  @Path("/all")
  @ApiOperation(value = "Finds all policies.",
    notes = "Finds all policies.",
    httpMethod = "GET",
    response = classOf[AggregationPoliciesModel])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAll: Route = {
    path(HttpConstant.PolicyPath / "all") {
      get {
        complete {
          for {
            response <- (supervisor ? new FindAll()).mapTo[ResponsePolicies]
          } yield response.policies.get
        }
      }
    }
  }

  @ApiOperation(value = "Creates a policy.",
    notes = "Creates a policy.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy",
      defaultValue = "",
      value = "policy json",
      dataType = "PolicyElementModel",
      required = true,
      paramType = "body")))
  def create: Route = {
    path(HttpConstant.PolicyPath) {
      post {
        entity(as[AggregationPoliciesModel]) { policy =>
          complete {
            for {
              parsedP <- PolicyHelper.parseFragments(
                PolicyHelper.fillFragments(policy, actors.get(AkkaConstant.FragmentActor).get, timeout)
              )
              if PolicyHelper.validateAggregationPolicy(parsedP)
              response <- (supervisor ? new Create(policy)).mapTo[ResponsePolicy]
            } yield response.policy.get
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
            for {
              response <- (supervisor ? new Update(policy)).mapTo[Response]
              _ = response.status.get
            } yield HttpResponse(StatusCodes.Created)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes a policy from its id.",
    notes = "Deletes a policy from its id.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the policy",
      dataType = "string",
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def remove: Route = {
    path(HttpConstant.PolicyPath / Segment) { (id) =>
      delete {
        complete {
          for {
            response <- (supervisor ? new Delete(id)).mapTo[Response]
            _ = response.status.get
          } yield HttpResponse(StatusCodes.OK)
        }
      }
    }
  }

  @Path("/run/{id}")
  @ApiOperation(value = "Runs a policy from its name.",
    notes = "Runs a policy from its name.",
    httpMethod = "GET",
    response = classOf[Result])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the policy",
      dataType = "string",
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def run: Route = {
    path(HttpConstant.PolicyPath / "run" / Segment) { (id) =>
      get {
        complete {
          for {
            responsePolicy <- (supervisor ? Find(id)).mapTo[ResponsePolicy]
            policy = responsePolicy.policy.get
            parsedP <- PolicyHelper.parseFragments(
              PolicyHelper.fillFragments(policy, actors.get(AkkaConstant.FragmentActor).get, timeout)
            )
            if PolicyHelper.validateAggregationPolicy(parsedP)
          } yield {
            val sscActor = actors.get(AkkaConstant.SparkStreamingContextActor).get
            sscActor ! SparkStreamingContextActor.Create(parsedP)
            Result("Creating new context with name " + policy.name)
          }
        }
      }
    }
  }

  @Path("/download/{id}")
  @ApiOperation(value = "Downloads a policy from its id.",
    notes = "Downloads a policy from its id.",
    httpMethod = "GET",
    response = classOf[Result])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the policy",
      dataType = "string",
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def download: Route =
    path(HttpConstant.PolicyPath / "download" / Segment) { (id) =>
      get {
        complete {
          for {
            response <- (supervisor ? Find(id)).mapTo[ResponsePolicy]
            policy = response.policy.get
            parsedP <- PolicyHelper.parseFragments(
              PolicyHelper.fillFragments(policy, actors.get(AkkaConstant.FragmentActor).get, timeout)
            )
            if PolicyHelper.validateAggregationPolicy(parsedP)
          } yield {
            val tempFile = File.createTempFile(s"${parsedP.id.get}-${parsedP.name}-", ".json")
            tempFile.deleteOnExit()
            respondWithHeader(`Content-Disposition`("attachment", Map("filename" -> s"${parsedP.name}.json"))) {
              scala.tools.nsc.io.File(tempFile).writeAll(write(parsedP))
              getFromFile(tempFile)
            }
          }
        }
      }
    }
}