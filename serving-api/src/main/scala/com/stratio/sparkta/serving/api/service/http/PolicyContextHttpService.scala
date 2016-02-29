/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

import akka.actor.ActorRef
import akka.pattern.ask
import com.stratio.sparkta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.core.actor.FragmentActor
import com.stratio.sparkta.serving.core.constants.AkkaConstant
import com.stratio.sparkta.serving.core.exception.ServingCoreException
import com.stratio.sparkta.serving.core.helpers.PolicyHelper
import com.stratio.sparkta.serving.core.models._
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.{FindAll, Response, Update}
import com.stratio.spray.oauth2.client.OauthClient
import com.stratio.spray.oauth2.client.OauthClientHelper._
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing._

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.PolicyContextPath, description = "Operations about policy contexts.", position = 0)
trait PolicyContextHttpService extends BaseHttpService with OauthClient {

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
    secured { user =>
      path(HttpConstant.PolicyContextPath) {
        authorize(hasRole(Seq("*"), user)) {
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
    secured { user =>
      path(HttpConstant.PolicyContextPath) {
        authorize(hasRole(Seq("*"), user)) {
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
                    throw new ServingCoreException(ErrorModel.toString(
                      ErrorModel(ErrorModel.CodeNotExistsPolicyWithId,
                        s"No policy with id ${policyStatus.id}.")
                    ))
                }
              }
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a policy context and launch the policy.",
    notes = "Returns the result",
    httpMethod = "POST",
    response = classOf[PolicyResult])
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
    secured { user =>
      path(HttpConstant.PolicyContextPath) {
        authorize(hasRole(Seq("*"), user)) {
          post {
            entity(as[AggregationPoliciesModel]) { p =>
              val parsedP = getPolicyWithFragments(p)
              val isValidAndMessageTuple = AggregationPoliciesValidator.validateDto(parsedP)
              val fragmentActor: ActorRef = actors.getOrElse(AkkaConstant.FragmentActor, throw new ServingCoreException
              (ErrorModel.toString(ErrorModel(ErrorModel.CodeUnknown, s"Error getting fragmentActor"))))
              validate(isValidAndMessageTuple._1, isValidAndMessageTuple._2) {
                complete {
                  for {
                    policyResponseTry <- (supervisor ? Create(parsedP)).mapTo[Try[AggregationPoliciesModel]]
                  } yield {
                    policyResponseTry match {
                      case Success(policy) =>
                        val inputs = PolicyHelper.populateFragmentFromPolicy(policy, FragmentType.input)
                        val outputs = PolicyHelper.populateFragmentFromPolicy(policy, FragmentType.output)
                        createFragments(fragmentActor, outputs.toList ::: inputs.toList)
                        PolicyResult(policy.id.getOrElse(""), p.name)
                      case Failure(ex: Throwable) => throw new ServingCoreException(ErrorModel.toString(
                        ErrorModel(ErrorModel.CodeErrorCreatingPolicy, s"Can't create policy")
                      ))
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  def createFragments(fragmentActor: ActorRef, fragments: Seq[FragmentElementModel]): Unit = {
    fragments.foreach(fragment => fragmentActor ! FragmentActor.Create(fragment))
  }

  def getPolicyWithFragments(policy: AggregationPoliciesModel): AggregationPoliciesModel =
    PolicyHelper.parseFragments(PolicyHelper.fillFragments(policy, actors.get(AkkaConstant.FragmentActor).get, timeout))
}
