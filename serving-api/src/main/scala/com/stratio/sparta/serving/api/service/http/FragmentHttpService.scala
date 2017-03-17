/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.PolicyActor
import com.stratio.sparta.serving.api.actor.PolicyActor.{Delete, FindByFragment, FindByFragmentName, FindByFragmentType, ResponsePolicies}
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.actor.FragmentActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.policy.fragment.FragmentElementModel
import com.stratio.sparta.serving.core.models.policy.{PolicyElementModel, PolicyModel}
import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing.Route

import scala.util.{Failure, Success}

@Api(value = HttpConstant.FragmentPath, description = "Operations over fragments: inputs and outputs that will be " +
  "included in a policy")
trait FragmentHttpService extends BaseHttpService with OauthClient {

  override def routes: Route =
    findAll ~ findByTypeAndId ~ findByTypeAndName ~ findAllByType ~ create ~ update ~ deleteByTypeAndId ~
      deleteByType ~ deleteByTypeAndName ~ deleteAll

  @Path("/{fragmentType}/id/{fragmentId}")
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
    new ApiImplicitParam(name = "fragmentId",
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
    path(HttpConstant.FragmentPath / Segment / "id" / Segment) { (fragmentType, id) =>
      get {
        complete {
          for {
            responseFragment <- (supervisor ? new FindByTypeAndId(fragmentType, id)).mapTo[ResponseFragment]
          } yield responseFragment match {
            case ResponseFragment(Failure(exception)) => throw exception
            case ResponseFragment(Success(fragment)) => fragment
          }
        }
      }
    }
  }

  @Path("/{fragmentType}/name/{fragmentName}")
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
    new ApiImplicitParam(name = "fragmentName",
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
          for {
            responseFragment <- (supervisor ? new FindByTypeAndName(fragmentType, name)).mapTo[ResponseFragment]
          } yield responseFragment match {
            case ResponseFragment(Failure(exception)) => throw exception
            case ResponseFragment(Success(fragment)) => fragment
          }
        }
      }
    }
  }

  @Path("/{fragmentType}")
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
          for {
            responseFragments <- (supervisor ? new FindByType(fragmentType)).mapTo[ResponseFragments]
          } yield responseFragments match {
            case ResponseFragments(Failure(exception)) => throw exception
            case ResponseFragments(Success(fragments)) => fragments
          }
        }
      }
    }
  }

  @ApiOperation(value = "Find all fragments",
    notes = "Find all fragments",
    httpMethod = "GET",
    response = classOf[FragmentElementModel],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAll: Route = {
    path(HttpConstant.FragmentPath) {
      get {
        complete {
          for {
            responseFragments <- (supervisor ? new FindAllFragments()).mapTo[ResponseFragments]
          } yield responseFragments match {
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
          complete {
            for {
              responseFragment <- (supervisor ? new Create(fragment)).mapTo[ResponseFragment]
            } yield responseFragment match {
              case ResponseFragment(Failure(exception)) => throw exception
              case ResponseFragment(Success(fragment: FragmentElementModel)) => fragment
            }
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
            val policyActor = actors.get(AkkaConstant.PolicyActorName).get
            val fragmentStatusActor = actors.get(AkkaConstant.FragmentActorName).get
            for {
              allPolicies <- policyActor ? PolicyActor.FindAll()
              updateResponse <- fragmentStatusActor ? FragmentActor.Update(fragment)
            } yield (updateResponse, allPolicies) match {
              case (FragmentActor.Response(Success(_)), PolicyActor.ResponsePolicies(Success(policies))) =>
                val policiesInFragments = policies.flatMap(policy => {
                  if (policy.fragments.exists(policyFragment =>
                    policyFragment.fragmentType == fragment.fragmentType &&
                      policyFragment.id == fragment.id))
                    Some(policy)
                  else None
                })
                updatePoliciesWithUpdatedFragments(policiesInFragments)
                HttpResponse(StatusCodes.OK)
              case (Response(Failure(exception)), _) =>
                throw exception
            }
          }
        }
      }
    }
  }

  @Path("/{fragmentType}/id/{fragmentId}")
  @ApiOperation(value = "Deletes a fragment depending of its type and id and their policies related",
    notes = "Deletes a fragment depending of its type and id.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragmentType",
      value = "type of fragment (input/output)",
      dataType = "string",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "fragmentId",
      value = "id of the fragment",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteByTypeAndId: Route = {
    path(HttpConstant.FragmentPath / Segment / "id" / Segment) { (fragmentType, id) =>
      delete {
        complete {
          val policyActor = actors.get(AkkaConstant.PolicyActorName).get
          supervisor ! new DeleteByTypeAndId(fragmentType, id)
          for {
            responsePolicies <- (policyActor ? FindByFragment(fragmentType, id)).mapTo[ResponsePolicies]
          } responsePolicies match {
            case ResponsePolicies(Failure(exception)) => throw exception
            case ResponsePolicies(Success(policies)) =>
              policies.foreach(policy => policyActor ! Delete(policy.id.get))
          }
          HttpResponse(StatusCodes.OK)
        }
      }
    }
  }

  @Path("/{fragmentType}/name/{fragmentName}")
  @ApiOperation(value = "Deletes a fragment depending of its type and name and their policies related",
    notes = "Deletes a fragment depending of its type and name.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragmentType",
      value = "type of fragment (input/output)",
      dataType = "string",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "fragmentName",
      value = "name of the fragment",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteByTypeAndName: Route = {
    path(HttpConstant.FragmentPath / Segment / "name" / Segment) { (fragmentType, name) =>
      delete {
        complete {
          val policyActor = actors.get(AkkaConstant.PolicyActorName).get
          supervisor ! new DeleteByTypeAndName(fragmentType, name)
          for {
            responsePolicies <- (policyActor ? FindByFragmentName(fragmentType, name)).mapTo[ResponsePolicies]
          } yield responsePolicies match {

            case ResponsePolicies(Failure(exception)) => throw exception
            case ResponsePolicies(Success(policies)) =>
              policies.foreach(policy => policyActor ! Delete(policy.id.get))
          }
          HttpResponse(StatusCodes.OK)
        }
      }
    }
  }

  @ApiOperation(value = "Deletes a fragment depending of its type and their policies related",
    notes = "Deletes a fragment depending of its type.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragmentType",
      value = "type of fragment (input/output)",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteByType: Route = {
    path(HttpConstant.FragmentPath / Segment) { (fragmentType) =>
      delete {
        complete {
          val policyActor = actors.get(AkkaConstant.PolicyActorName).get
          supervisor ! new DeleteByType(fragmentType)
          for {
            responsePolicies <- (policyActor ? FindByFragmentType(fragmentType)).mapTo[ResponsePolicies]
          } yield responsePolicies match {
                case ResponsePolicies(Failure(exception)) => throw exception
                case ResponsePolicies(Success(policies)) =>
                  policies.foreach(policy => policyActor ! Delete(policy.id.get))
              }
              HttpResponse(StatusCodes.OK)
          }
        }
      }
    }

  @ApiOperation(value = "Deletes all fragments and their policies related",
    notes = "Deletes all fragments.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteAll: Route = {
    path(HttpConstant.FragmentPath) {
      delete {
        complete {
          for {
            fragmentsResponse <- (supervisor ? new DeleteAllFragments()).mapTo[ResponseFragments]
          } yield fragmentsResponse match {
            case ResponseFragments(Failure(exception)) =>
              throw exception
            case ResponseFragments(Success(fragments: List[FragmentElementModel])) =>
              val fragmentsTypes = fragments.map(fragment => fragment.fragmentType).distinct
              val policyActor = actors.get(AkkaConstant.PolicyActorName).get

              fragmentsTypes.foreach(fragmentType =>
                for {
                  responsePolicies <- (policyActor ? FindByFragmentType(fragmentType)).mapTo[ResponsePolicies]
                } yield responsePolicies match {
                  case ResponsePolicies(Failure(exception)) =>
                    throw exception
                  case ResponsePolicies(Success(policies)) =>
                    policies.foreach(policy => policyActor ! Delete(policy.id.get))
                })
              HttpResponse(StatusCodes.OK)
          }
        }
      }
    }
  }

  protected def updatePoliciesWithUpdatedFragments(policies: Seq[PolicyModel]): Unit =
    policies.foreach(policy => {
      val policyActor = actors.get(AkkaConstant.PolicyActorName).get

      policyActor ! PolicyActor.Update(policy.copy(input = None, outputs = Seq.empty[PolicyElementModel]))
    })
}