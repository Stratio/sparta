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

import java.io.File
import javax.ws.rs.Path

import akka.actor.ActorRef

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import akka.pattern.ask
import com.wordnik.swagger.annotations._
import org.json4s.jackson.Serialization.write
import spray.http.HttpHeaders.`Content-Disposition`
import spray.http.{HttpResponse, StatusCodes}
import spray.httpx.marshalling.ToResponseMarshallable
import spray.routing._
import com.stratio.sparta.serving.api.actor.PolicyActor._
import com.stratio.sparta.serving.api.actor.SparkStreamingContextActor
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.SpartaSerializer
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.helpers.FragmentsHelper
import com.stratio.sparta.serving.core.helpers.FragmentsHelper._
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.serving.core.policy.status.{PolicyStatusActor, PolicyStatusEnum}

@Api(value = HttpConstant.PolicyPath, description = "Operations over policies.")
trait PolicyHttpService extends BaseHttpService with SpartaSerializer {

  case class Result(message: String, desc: Option[String] = None)

  override def routes: Route =
    find ~ findAll ~ findByFragment ~ create ~ update ~ remove ~ run ~ download ~ findByName ~
      removeAll ~ deleteCheckpoint

  @Path("/find/{id}")
  @ApiOperation(value = "Find a policy from its id.",
    notes = "Find a policy from its id.",
    httpMethod = "GET",
    response = classOf[AggregationPoliciesModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the policy",
      dataType = "string",
      required = true,
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
          val future = supervisor ? new Find(id)
          Await.result(future, timeout.duration) match {
            case ResponsePolicy(Failure(exception)) =>
              throw exception
            case ResponsePolicy(Success(policy)) => policy
          }
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
      required = true,
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
          val future = supervisor ? new FindByName(name)
          Await.result(future, timeout.duration) match {
            case ResponsePolicy(Failure(exception)) =>
              throw exception
            case ResponsePolicy(Success(policy)) => policy
          }
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
  def findByFragment: Route = {
    path(HttpConstant.PolicyPath / "fragment" / Segment / Segment) { (fragmentType, id) =>
      get {
        complete {
          val future = supervisor ? new FindByFragment(fragmentType, id)
          Await.result(future, timeout.duration) match {
            case ResponsePolicies(Failure(exception)) => throw exception
            case ResponsePolicies(Success(policies)) => withStatus(policies)
          }
        }
      }
    }
  }

  @Path("/all")
  @ApiOperation(value = "Finds all policies.",
    notes = "Finds all policies.",
    httpMethod = "GET",
    response = classOf[PolicyWithStatus])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAll: Route = {
    path(HttpConstant.PolicyPath / "all") {
      get {
        complete {
          val future = supervisor ? new FindAll()
          Await.result(future, timeout.duration) match {
            case ResponsePolicies(Failure(exception)) => throw exception
            case ResponsePolicies(Success(policies)) => withStatus(policies)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a policy.",
    notes = "Creates a policy.",
    httpMethod = "POST",
    response = classOf[AggregationPoliciesModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy",
      defaultValue = "",
      value = "policy json",
      dataType = "AggregationPoliciesModel",
      required = true,
      paramType = "body")))
  def create: Route = {
    path(HttpConstant.PolicyPath) {
      post {
        entity(as[AggregationPoliciesModel]) { policy =>
          AggregationPoliciesValidator.validateDto(
            getPolicyWithFragments(policy, actors.get(AkkaConstant.FragmentActor).get)
          )
          complete {
            val future = supervisor ? new Create(policy)
            Await.result(future, timeout.duration) match {
              case ResponsePolicy(Failure(exception)) =>
                throw exception
              case ResponsePolicy(Success(policy)) => policy
            }
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
      dataType = "AggregationPoliciesModel",
      required = true,
      paramType = "body")))
  def update: Route = {
    path(HttpConstant.PolicyPath) {
      put {
        entity(as[AggregationPoliciesModel]) { policy =>
          AggregationPoliciesValidator.validateDto(
            getPolicyWithFragments(policy, actors.get(AkkaConstant.FragmentActor).get)
          )
          complete {
            val future = supervisor ? new Update(policy)
            Await.result(future, timeout.duration) match {
              case ResponsePolicy(Failure(exception)) => throw exception
              case ResponsePolicy(Success(policy)) => HttpResponse(StatusCodes.OK)
            }
          }
        }
      }
    }
  }


  @ApiOperation(value = "Deletes all policies.",
    notes = "Deletes all policies.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def removeAll: Route = {
    path(HttpConstant.PolicyPath) {
      delete {
        complete {
          val policyStatusActor = actors.get(AkkaConstant.PolicyStatusActor).get
          for {
            policies <- (supervisor ? DeleteAll()).mapTo[ResponsePolicies]
          } yield policies match {
            case ResponsePolicies(Failure(exception)) =>
              throw exception
            case ResponsePolicies(Success(policies: Seq[AggregationPoliciesModel])) =>
              Try{
                policyStatusActor ? PolicyStatusActor.DeleteAll
              } match {
                case Success(_) => StatusCodes.OK
                case Failure(exception) => throw exception
              }
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
      required = true,
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
            Response(Success(_)) <- (supervisor ? Delete(id)).mapTo[Response]
            policyStatusActor = actors.get(AkkaConstant.PolicyStatusActor).get
            deleteContextResponse <- (policyStatusActor ? PolicyStatusActor.Delete(id))
              .mapTo[PolicyStatusActor.ResponseDelete]
          } yield deleteContextResponse match {
            case PolicyStatusActor.ResponseDelete(Success(_)) => StatusCodes.OK
            case PolicyStatusActor.ResponseDelete(Failure(exception)) => throw exception
          }
        }
      }
    }
  }

  @Path("/checkpoint/{name}")
  @ApiOperation(value = "Delete checkpoint associated to one policy from its name.",
    notes = "Delete checkpoint associated to one policy from its name.",
    httpMethod = "DELETE",
    response = classOf[Result])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name",
      value = "name of the policy",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def deleteCheckpoint: Route = {
    path(HttpConstant.PolicyPath / "checkpoint" / Segment) { (name) =>
      delete {
        complete {
          val future = supervisor ? new FindByName(name)
          Await.result(future, timeout.duration) match {
            case ResponsePolicy(Failure(exception)) => throw exception
            case ResponsePolicy(Success(policy)) =>
              AggregationPoliciesValidator.validateDto(policy)
              val response = (supervisor ? DeleteCheckpoint(policy)).mapTo[Response]
              Await.result(response, timeout.duration) match {
                case Response(Failure(ex)) => throw ex
                case Response(Success(_)) => new Result("Checkpoint deleted from policy: " + policy.name)
              }
          }
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
      required = true,
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
          val future = supervisor ? new Find(id)
          Await.result(future, timeout.duration) match {
            case ResponsePolicy(Failure(exception)) => throw exception
            case ResponsePolicy(Success(policy)) =>
              AggregationPoliciesValidator.validateDto(policy)
              val response = actors.get(AkkaConstant.SparkStreamingContextActor).get ?
                SparkStreamingContextActor.Create(policy)
              Await.result(response, timeout.duration) match {
                case Failure(ex) => throw ex
                case Success(_) => new Result("Creating new context with name " + policy.name)
              }
          }
        }
      }
    }
  }

  @Path("/download/{id}")
  @ApiOperation(value = "Downloads a policy from its id.",
    notes = "Downloads a policy from its id.",
    httpMethod = "GET",
    response = classOf[AggregationPoliciesModel])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the policy",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def download: Route = {
    path(HttpConstant.PolicyPath / "download" / Segment) { (id) =>
      get {
        val future = supervisor ? new Find(id)
        Await.result(future, timeout.duration) match {
          case ResponsePolicy(Failure(exception)) =>
            throw exception
          case ResponsePolicy(Success(policy)) =>
            AggregationPoliciesValidator.validateDto(policy)
            val tempFile = File.createTempFile(s"${policy.id.get}-${policy.name}-", ".json")
            tempFile.deleteOnExit()
            respondWithHeader(`Content-Disposition`("attachment", Map("filename" -> s"${policy.name}.json"))) {
              scala.tools.nsc.io.File(tempFile).writeAll(
                write(policy.copy(fragments = Seq.empty[FragmentElementModel]))
              )
              getFromFile(tempFile)
            }
        }
      }
    }
  }

  // XXX Protected methods

  protected def withStatus(policies: Seq[AggregationPoliciesModel]): ToResponseMarshallable = {

    if (policies.nonEmpty) {
      val policyStatusActor = actors.get(AkkaConstant.PolicyStatusActor).get
      for {
        response <- (policyStatusActor ? PolicyStatusActor.FindAll)
          .mapTo[PolicyStatusActor.Response]
      } yield policies.map(policy => getPolicyWithStatus(policy, response.policyStatus.get.policiesStatus))
    } else Seq()
  }

  protected def getPolicyWithStatus(policy: AggregationPoliciesModel, statuses: Seq[PolicyStatusModel])
  : PolicyWithStatus = {
    val status = statuses.find(_.id == policy.id.get) match {
      case Some(statusPolicy) => statusPolicy.status
      case None => PolicyStatusEnum.NotStarted
    }
    PolicyWithStatus(status, policy)
  }
}
