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
import com.stratio.sparkta.driver.models.{AggregationPoliciesModel, FragmentElementModel}
import com.stratio.sparkta.serving.api.actor._
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.wordnik.swagger.annotations._
import spray.http.{StatusCodes, HttpResponse}
import spray.routing._

import scala.concurrent.Await
import scala.util.{Success, Failure}

@Api(value = "/policy", description = "Operations about policies.", position = 0)
trait PolicyHttpService extends BaseHttpService {

  override def routes: Route = find ~ findAll ~ create ~ update ~ remove

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

  @ApiOperation(value = "Find a list of policies depending of its type", notes = "Returns a list of policies",
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

  @ApiOperation(value = "Creates a policy", notes = "Creates a policy", httpMethod = "POST")
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


  /*
  override def routes: Route = findAll ~ create ~ findById ~ deleteById
  case class Result(message: String, desc: Option[String] = None)

  @ApiOperation(value = "Find all policies", notes = "Returns a policies list", httpMethod = "GET",
    response = classOf[String])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def findAll: Route = {
    path("policy") {
      get {
        complete {
          supervisor.ask(GetAllContextStatus).mapTo[List[StreamingContextStatus]]
        }
      }
    }
  }

  @ApiOperation(value = "post a policy", notes = "Returns the result", httpMethod = "POST", response = classOf[Result])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy", defaultValue = "", value = "policy json", dataType = "AggregationPoliciesDto",
      required = true, paramType = "json")))
  def create: Route = {
    post {
      entity(as[AggregationPoliciesModel]) { p =>
        val parsedP = (PolicyHelper.parseFragments _ compose fillFragments _)(p)
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

  @ApiOperation(value = "get policy status by name", notes = "Returns the status of a policy", httpMethod = "GET",
    response = classOf[StreamingContextStatus])
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "policy name", defaultValue = "", value = "policy name",
    dataType = "String", required = true)))
  def findById: Route = {
    pathPrefix("policy" / Segment) { name =>
      get {
        complete {
          supervisor.ask(new GetContextStatus(name)).mapTo[StreamingContextStatus]
        }
      }
    }
  }

  @ApiOperation(value = "delete a policy by name", notes = "Returns the status of the policy", httpMethod = "DELETE",
    response = classOf[StreamingContextStatus])
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "policy name", defaultValue = "", value = "policy name",
    dataType = "String", required = true)))
  def deleteById: Route = {
    pathPrefix("policy" / Segment) { name =>
      delete {
        complete {
          supervisor.ask(new DeleteContext(name)).mapTo[StreamingContextStatus]
        }
      }
    }
  }

  ///////////////////////////////////////////// XXX PRIVATE METHODS ////////////////////////////////////////////////////

  /**
   * The policy only has fragments with its name and type. When this method is called it finds the full fragment in
   * ZK and fills the rest of the fragment.
   * @param apConfig with the policy.
   * @return a fragment with all fields filled.
   */
  private def fillFragments(apConfig: AggregationPoliciesModel): AggregationPoliciesModel = {
    val actor = actorRefFactory.actorSelection(AkkaConstant.FragmentActorAkkaPath)

    val currentFragments: Seq[FragmentElementModel] = apConfig.fragments.map(fragment => {
      val future = actor ? new FragmentSupervisorActor_findByTypeAndName(fragment.fragmentType, fragment.name)
      Await.result(future, timeout.duration) match {
        case FragmentSupervisorActor_response_fragment(Failure(exception)) => throw exception
        case FragmentSupervisorActor_response_fragment(Success(fragment)) => fragment
      }
    })
    apConfig.copy(fragments = currentFragments)
  }
  */
}
