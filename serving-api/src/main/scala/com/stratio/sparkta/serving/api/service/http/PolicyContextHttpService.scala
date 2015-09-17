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
import com.stratio.sparkta.driver.constants.AkkaConstant
import com.stratio.sparkta.serving.api.actor.PolicyActor.ResponsePolicy
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.api.exception.ServingApiException
import com.stratio.sparkta.serving.api.helpers.PolicyHelper
import com.stratio.sparkta.serving.core.models._
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.{FindAll, Response, Update}
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing._

import scala.concurrent.Await
import scala.util.{Failure, Success}
import com.stratio.sparkta.serving.api.actor.SparkStreamingContextActor._

@Api(value = HttpConstant.PolicyContextPath, description = "Operations about policy contexts.", position = 0)
trait PolicyContextHttpService extends BaseHttpService {

  case class Result(message: String, desc: Option[String] = None)

  override def routes: Route = findAll ~ update ~ create

  @ApiOperation(value = "Finds all policy contexts",
    notes = "Returns a policies list",
    httpMethod = "GET",
    response = classOf[String],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAll: Route = {
    path(HttpConstant.PolicyContextPath) {
      get {
        complete {
          val policyStatusActor = actors.get(AkkaConstant.PolicyStatusActor).get
          val future = policyStatusActor ? FindAll
          Await.result(future, timeout.duration) match {
            case Response(Failure(exception)) => throw exception
            case Response(Success(policyStatuses)) => policyStatuses
          }

        }
      }
    }
  }

  @ApiOperation(value = "Updates a policy status.",
    notes = "Updates a policy status.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy status",
      value = "policy json",
      dataType = "PolicyStatusModel",
      required = true,
      paramType = "body")))
  def update: Route = {
    path(HttpConstant.PolicyContextPath) {
      put {
        entity(as[PolicyStatusModel]) { policyStatus =>
          complete {
            val policyStatusActor = actors.get(AkkaConstant.PolicyStatusActor).get
            for {
              response <- (policyStatusActor ? Update(policyStatus)).mapTo[Option[PolicyStatusModel]]
            } yield {
              if (response.isDefined)
                HttpResponse(StatusCodes.Created)
              else
                throw new ServingApiException(ErrorModel.toString(
                  ErrorModel(ErrorModel.CodeNotExistsPolicytWithId,
                    s"No policy with id ${policyStatus.id}.")
                ))
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a policy context.",
    notes = "Returns the result",
    httpMethod = "POST",
    response = classOf[Result])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy",
      value = "policy json",
      dataType = "AggregationPoliciesModel",
      required = true,
      paramType = "body")))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def create: Route = {
    path(HttpConstant.PolicyContextPath) {
      post {
        entity(as[AggregationPoliciesModel]) { p =>

          val parsedP = PolicyHelper.parseFragments(
            PolicyHelper.fillFragments(p, actors.get(AkkaConstant.FragmentActor).get, timeout))

          val isValidAndMessageTuple = AggregationPoliciesValidator.validateDto(parsedP)
          validate(isValidAndMessageTuple._1, isValidAndMessageTuple._2) {
            complete {
              val response = supervisor ? new Create(parsedP)
              Await.result(response,timeout.duration) match {
                case Failure(ex) => throw ex
                case Success(policy) => new Result("Creating new context with name " + p.name)
              }
            }
          }
        }
      }
    }
  }

}
