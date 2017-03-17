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
import com.stratio.sparta.serving.api.actor.PolicyActor.{DeletePolicy, FindByFragment, FindByFragmentName, FindByFragmentType, ResponsePolicies}
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.FragmentActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.policy.fragment.FragmentElementModel
import com.stratio.sparta.serving.core.models.policy.{PolicyElementModel, PolicyModel, ResponsePolicy}
import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing.Route

import scala.util.{Failure, Success}

@Api(value = HttpConstant.FragmentPath, description = "Operations over fragments: inputs and outputs that will be " +
  "included in a policy")
trait FragmentHttpService extends BaseHttpService with OauthClient {

  override def routes(user: Option[LoggedUser] = None): Route =
    findAll(user) ~ findByTypeAndId(user) ~ findByTypeAndName(user) ~
      findAllByType(user) ~ create(user) ~ update(user) ~ deleteByTypeAndId(user) ~
      deleteByType(user) ~ deleteByTypeAndName(user) ~ deleteAll(user)

  @Path("/{fragmentType}/id/{fragmentId}")
  @ApiOperation(value = "Finds a fragment depending on its type and id.",
    notes = "Finds a fragment depending on its type and id.",
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
  def findByTypeAndId(user: Option[LoggedUser]): Route = {
    path(HttpConstant.FragmentPath / Segment / "id" / Segment) { (fragmentType, id) =>
      get {
        complete {
          for {
            responseFragment <- (supervisor ? FindByTypeAndId(fragmentType, id, user))
              .mapTo[Either[ResponseFragment,UnauthorizedResponse]]
          } yield responseFragment match {
            case Left(ResponseFragment(Failure(exception))) => throw exception
            case Left(ResponseFragment(Success(fragment))) => fragment
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in fragments")
          }
        }
      }
    }
  }

  @Path("/{fragmentType}/name/{fragmentName}")
  @ApiOperation(value = "Finds a fragment depending on its type and name.",
    notes = "Finds a fragment depending on its type and name.",
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
  def findByTypeAndName(user: Option[LoggedUser]): Route = {
    path(HttpConstant.FragmentPath / Segment / "name" / Segment) { (fragmentType, name) =>
      get {
        complete {
          for {
            responseFragment <- (supervisor ? FindByTypeAndName(fragmentType, name, user))
              .mapTo[Either[ResponseFragment,UnauthorizedResponse]]
          } yield responseFragment match {
            case Left(ResponseFragment(Failure(exception))) => throw exception
            case Left(ResponseFragment(Success(fragment))) => fragment
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in fragments")
          }
        }
      }
    }
  }

  @Path("/{fragmentType}")
  @ApiOperation(value = "Finds a list of fragments depending on its type.",
    notes = "Finds a list of fragments depending on its type.",
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
  def findAllByType(user: Option[LoggedUser]): Route = {
    path(HttpConstant.FragmentPath / Segment) { (fragmentType) =>
      get {
        complete {
          for {
            responseFragments <- (supervisor ? FindByType(fragmentType, user))
              .mapTo[Either[ResponseFragments,UnauthorizedResponse]]
          } yield responseFragments match {
            case Left(ResponseFragments(Failure(exception))) => throw exception
            case Left(ResponseFragments(Success(fragments))) => fragments
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in fragments")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Find all fragments",
    notes = "Finds all fragments",
    httpMethod = "GET",
    response = classOf[FragmentElementModel],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.FragmentPath) {
      get {
        complete {
          for {
            responseFragments <- (supervisor ? FindAllFragments(user))
              .mapTo[Either[ResponseFragments,UnauthorizedResponse]]
          } yield responseFragments match {
            case Left(ResponseFragments(Failure(exception))) => throw exception
            case Left(ResponseFragments(Success(fragments))) => fragments
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in fragments")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a fragment depending of its type.",
    notes = "Creates a fragment depending on its type.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragment",
      value = "fragment to save",
      dataType = "FragmentElementModel",
      required = true,
      paramType = "body")
  ))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.FragmentPath) {
      post {
        entity(as[FragmentElementModel]) { fragment =>
          complete {
            for {
              responseFragment <- (supervisor ? CreateFragment(fragment, user))
               .mapTo[Either[ResponseFragment,UnauthorizedResponse]]
            } yield responseFragment match {
              case Left(ResponseFragment(Failure(exception))) => throw exception
              case Left(ResponseFragment(Success(fragment: FragmentElementModel))) => fragment
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in fragments")
            }
          }
        }
      }
    }
  }

  // scalastyle:off cyclomatic.complexity
  @ApiOperation(value = "Updates a fragment.",
    notes = "Updates a fragment.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fragment",
      value = "fragment json",
      dataType = "FragmentElementModel",
      required = true,
      paramType = "body")))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.FragmentPath) {
      put {
        entity(as[FragmentElementModel]) { fragment =>
          complete {
            val policyActor = actors(AkkaConstant.PolicyActorName)
            for {
              responseFragments <- (supervisor ? Update(fragment, user))
                .mapTo[Either[Response,UnauthorizedResponse]]
            } yield responseFragments match {
              case Left(Response(Success(_))) =>
                for {
                  responsePolicies <- (policyActor ? PolicyActor.FindAll(user))
                    .mapTo[Either[ResponsePolicies,UnauthorizedResponse]]
                } yield responsePolicies match {
                  case Left(ResponsePolicies(Success(policies))) =>
                    val policiesInFragments = policies.flatMap(policy => {
                      if (policy.fragments.exists(policyFragment =>
                        policyFragment.fragmentType == fragment.fragmentType &&
                          policyFragment.id == fragment.id))
                        Some(policy)
                      else None
                    })
                    updatePoliciesWithUpdatedFragments(policiesInFragments, user)
                    HttpResponse(StatusCodes.OK)
                  case Left(ResponsePolicies(Failure(exception))) =>
                    throw exception
                  case Right(UnauthorizedResponse(exception)) =>
                    throw exception
                  case _ =>
                    throw new RuntimeException("Unexpected behaviour in policies")
                }
              case Left(Response(Failure(exception))) =>
                throw exception
              case Right(UnauthorizedResponse(exception)) =>
                throw exception
              case _ =>
                throw new RuntimeException("Unexpected behaviour in fragments")
            }
          }
        }
      }
    }
  }

  @Path("/{fragmentType}/id/{fragmentId}")
  @ApiOperation(value = "Deletes a fragment depending of its type and id and their policies related",
    notes = "Deletes a fragment depending on its type and id.",
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
  def deleteByTypeAndId(user: Option[LoggedUser]): Route = {
    path(HttpConstant.FragmentPath / Segment / "id" / Segment) { (fragmentType, id) =>
      delete {
        complete {
          val policyActor = actors(AkkaConstant.PolicyActorName)
          for {
            responseFragments <- (supervisor ? DeleteByTypeAndId(fragmentType, id, user))
              .mapTo[Either[Response,UnauthorizedResponse]]
          } yield responseFragments match {
            case Left(Response(Success(_))) =>
              for {
                responsePolicies <- (policyActor ? FindByFragment(fragmentType, id, user))
                  .mapTo[Either[ResponsePolicies,UnauthorizedResponse]]
              } yield responsePolicies match {
                case Left(ResponsePolicies(Success(policies))) =>
                  policies.foreach(policy => policyActor ! DeletePolicy(policy.id.get, user))
                  HttpResponse(StatusCodes.OK)
                case Left(ResponsePolicies(Failure(exception))) =>
                  throw exception
                case Right(UnauthorizedResponse(exception)) =>
                  throw exception
                case _ =>
                  throw new RuntimeException("Unexpected behaviour in policies")
              }
            case Left(Response(Failure(exception))) =>
              throw exception
            case Right(UnauthorizedResponse(exception)) =>
              throw exception
            case _ =>
              throw new RuntimeException("Unexpected behaviour in fragments")
          }
        }
      }
    }
  }

  @Path("/{fragmentType}/name/{fragmentName}")
  @ApiOperation(value = "Deletes a fragment depending on its type and name and its related policies",
    notes = "Deletes a fragment depending on its type and name.",
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
  def deleteByTypeAndName(user: Option[LoggedUser]): Route = {
    path(HttpConstant.FragmentPath / Segment / "name" / Segment) { (fragmentType, name) =>
      delete {
        complete {
          val policyActor = actors(AkkaConstant.PolicyActorName)
          for {
            responseFragments <- (supervisor ? DeleteByTypeAndName(fragmentType, name, user))
              .mapTo[Either[Response,UnauthorizedResponse]]
          } yield responseFragments match {
            case Left(Response(Success(_))) =>
              for {
                responsePolicies <- (policyActor ? FindByFragmentName(fragmentType, name, user))
                  .mapTo[Either[ResponsePolicies,UnauthorizedResponse]]
              } yield responsePolicies match {
                case Left(ResponsePolicies(Success(policies))) =>
                  policies.foreach(policy => policyActor ! DeletePolicy(policy.id.get, user))
                  HttpResponse(StatusCodes.OK)
                case Left(ResponsePolicies(Failure(exception))) =>
                  throw exception
                case Right(UnauthorizedResponse(exception)) =>
                  throw exception
                case _ =>
                  throw new RuntimeException("Unexpected behaviour in policies")
              }
            case Left(Response(Failure(exception))) =>
              throw exception
            case Right(UnauthorizedResponse(exception)) =>
              throw exception
            case _ =>
              throw new RuntimeException("Unexpected behaviour in fragments")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes a fragment depending on its type and its policies related",
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
  def deleteByType(user: Option[LoggedUser]): Route = {
    path(HttpConstant.FragmentPath / Segment) { (fragmentType) =>
      delete {
        complete {
          val policyActor = actors(AkkaConstant.PolicyActorName)
          for {
            responseFragments <- (supervisor ? DeleteByType(fragmentType, user))
              .mapTo[Either[Response,UnauthorizedResponse]]
          } yield responseFragments match {
            case Left(Response(Success(_))) =>
              for {
                responsePolicies <- (policyActor ? FindByFragmentType(fragmentType, user))
                  .mapTo[Either[ResponsePolicies,UnauthorizedResponse]]
              } yield responsePolicies match {
                case Left(ResponsePolicies(Success(policies))) =>
                  policies.foreach(policy => policyActor ! DeletePolicy(policy.id.get, user))
                  HttpResponse(StatusCodes.OK)
                case Left(ResponsePolicies(Failure(exception))) =>
                  throw exception
                case Right(UnauthorizedResponse(exception)) =>
                  throw exception
                case _ =>
                  throw new RuntimeException("Unexpected behaviour in policies")
              }
            case Left(Response(Failure(exception))) =>
              throw exception
            case Right(UnauthorizedResponse(exception)) =>
              throw exception
            case _ =>
              throw new RuntimeException("Unexpected behaviour in fragments")
          }
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
  def deleteAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.FragmentPath) {
      delete {
        complete {
          for {
            fragmentsResponse <- (supervisor ? DeleteAllFragments(user))
              .mapTo[Either[ResponseFragments,UnauthorizedResponse]]
          } yield fragmentsResponse match {
            case Left(ResponseFragments(Failure(exception))) =>
              throw exception
            case Left(ResponseFragments(Success(fragments: List[FragmentElementModel]))) =>
              val fragmentsTypes = fragments.map(fragment => fragment.fragmentType).distinct
              val policyActor = actors(AkkaConstant.PolicyActorName)

              fragmentsTypes.foreach(fragmentType =>
                for {
                  responsePolicies <- (policyActor ? FindByFragmentType(fragmentType, user))
                    .mapTo[Either[ResponsePolicies,UnauthorizedResponse]]
                } yield responsePolicies match {
                  case Left(ResponsePolicies(Failure(exception))) =>
                    throw exception
                  case Left(ResponsePolicies(Success(policies))) =>
                    policies.foreach(policy => policyActor ! DeletePolicy(policy.id.get, user))
                    HttpResponse(StatusCodes.OK)
                  case Right(UnauthorizedResponse(exception)) =>
                    throw exception
                  case _ =>
                    throw new RuntimeException("Unexpected behaviour in policies")
                })
            case Right(UnauthorizedResponse(exception)) =>
              throw exception
            case _ =>
              throw new RuntimeException("Unexpected behaviour in fragments")
          }
        }
      }
    }
  }

  protected def updatePoliciesWithUpdatedFragments(policies: Seq[PolicyModel], user: Option[LoggedUser]): Unit =
    policies.foreach(policy => {
      val policyActor = actors(AkkaConstant.PolicyActorName)
      for {
      responsePolicies <- (policyActor ? PolicyActor
        .Update(policy.copy(input = None, outputs = Seq.empty[PolicyElementModel]), user))
          .mapTo[Either[ResponsePolicy,UnauthorizedResponse]]
      } yield responsePolicies match {
        case Left(ResponsePolicy(Failure(exception))) => throw exception
        case Left(ResponsePolicy(Success(policies))) => policies
        case Right(UnauthorizedResponse(exception)) => throw exception
        case _ => throw new RuntimeException("Unexpected behaviour in policies")
      }
    }
      )
}