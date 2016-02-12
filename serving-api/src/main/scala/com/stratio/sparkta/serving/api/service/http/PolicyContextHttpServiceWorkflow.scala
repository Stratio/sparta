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

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparkta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.core.constants.AkkaConstant
import com.stratio.sparkta.serving.core.exception.ServingCoreException
import com.stratio.sparkta.serving.core.helpers.{ParseWorkflowToCommonModel, PolicyHelper}
import com.stratio.sparkta.serving.core.models._
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.{FindAll, Response, Update}
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing._

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.PolicyContextWorkflow, description = "Operations about policy contexts.", position = 0)
trait PolicyContextHttpServiceWorkflow extends BaseHttpService {

  override def routes: Route =  createWorkflow

  @ApiOperation(value = "Creates a policy context with the new version of the policy and launch it.",
    notes = "Returns the result",
    httpMethod = "POST",
    response = classOf[PolicyResult])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy",
      value = "policy json",
      dataType = "WorkflowPoliciesModel",
      required = true,
      paramType = "body")))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def createWorkflow: Route = {
    path(HttpConstant.PolicyContextWorkflow) {
      post {
        entity(as[WorkflowPoliciesModel]) { p =>
          val policy = ParseWorkflowToCommonModel.parsePolicyToCommonPolicy(p)
          val parsedP = getPolicyWithFragments(policy)
          val isValidAndMessageTuple = CommonPoliciesValidator.validateDto(parsedP)

          validate(isValidAndMessageTuple._1, isValidAndMessageTuple._2) {
            complete {
              for {
                response <- (supervisor ? Create(parsedP)).mapTo[Try[CommonPoliciesModel]]
              } yield {
                response match {
                  case Success(policy) =>
                    PolicyResult(policy.id.getOrElse(""), p.name)
                  case Failure(e) => throw e
                }
              }
            }
          }
        }
      }
    }
  }

  def getPolicyWithFragments(policy: CommonPoliciesModel): CommonPoliciesModel =
    PolicyHelper.parseFragments(PolicyHelper.fillFragments(policy, actors.get(AkkaConstant.FragmentActor).get, timeout))

}






