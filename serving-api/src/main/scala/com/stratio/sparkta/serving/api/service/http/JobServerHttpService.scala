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
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.core.messages.JobServerMessages._
import com.wordnik.swagger.annotations._
import spray.routing._

import scala.concurrent.Await
import scala.util.{Failure, Success}

@Api(value = "/jobServer", description = "Operations about JobServer.", position = 0)
trait JobServerHttpService extends BaseHttpService {

  override def routes: Route = getJars ~ getJobs ~ getJob ~ getContexts ~ deleteContext ~ deleteJob ~ getJobConfig

  case class Result(message: String, desc: Option[String] = None)

  @ApiOperation(value = "Find all jars", notes = "Returns a jars list", httpMethod = "GET",
    response = classOf[String])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def getJars: Route = {
    path(HttpConstant.JobServerPath / "jars") {
      get {
        complete {
          val future = supervisor ? new JobServerSupervisorActor_getJars()
          Await.result(future, timeout.duration) match {
            case JobServerSupervisorActor_response_getJars(Failure(exception)) => throw exception
            case JobServerSupervisorActor_response_getJars(Success(jarList)) => jarList
          }
        }
      }
    }
  }

  @ApiOperation(value = "Find all jobs", notes = "Returns a jobs list", httpMethod = "GET",
    response = classOf[String])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def getJobs: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.JobsPath) {
      get {
        complete {
          val future = supervisor ? new JobServerSupervisorActor_getJobs()
          Await.result(future, timeout.duration) match {
            case JobServerSupervisorActor_response_getJobs(Failure(exception)) => throw exception
            case JobServerSupervisorActor_response_getJobs(Success(jobsList)) => jobsList
          }
        }
      }
    }
  }

  @ApiOperation(value = "Find all contexts", notes = "Returns a contexts list", httpMethod = "GET",
    response = classOf[String])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def getContexts: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.ContextsPath) {
      get {
        complete {
          val future = supervisor ? new JobServerSupervisorActor_getContexts()
          Await.result(future, timeout.duration) match {
            case JobServerSupervisorActor_response_getContexts(Failure(exception)) => throw exception
            case JobServerSupervisorActor_response_getContexts(Success(contextsList)) => contextsList
          }
        }
      }
    }
  }

  @ApiOperation(value = "Delete context", notes = "Delete a context", httpMethod = "DELETE",
    response = classOf[String])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def deleteContext: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.ContextsPath / Segment) { (contextId) =>
      delete {
        complete {
          val future = supervisor ? new JobServerSupervisorActor_deleteContext(contextId)
          Await.result(future, timeout.duration) match {
            case JobServerSupervisorActor_response_deleteContext(Failure(exception)) => throw exception
            case JobServerSupervisorActor_response_deleteContext(Success(response)) => response
          }
        }
      }
    }
  }

  @ApiOperation(value = "Delete job", notes = "Delete a job", httpMethod = "DELETE",
    response = classOf[String])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def deleteJob: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.JobsPath / Segment) { (jobId) =>
      delete {
        complete {
          val future = supervisor ? new JobServerSupervisorActor_deleteJob(jobId)
          Await.result(future, timeout.duration) match {
            case JobServerSupervisorActor_response_deleteJob(Failure(exception)) => throw exception
            case JobServerSupervisorActor_response_deleteJob(Success(response)) => response
          }
        }
      }
    }
  }

  @ApiOperation(value = "Find job info by id", notes = "Return job information", httpMethod = "GET",
    response = classOf[String])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def getJob: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.JobsPath / Segment) { (jobId) =>
      get {
        complete {
          val future = supervisor ? new JobServerSupervisorActor_getJob(jobId)
          Await.result(future, timeout.duration) match {
            case JobServerSupervisorActor_response_getJob(Failure(exception)) => throw exception
            case JobServerSupervisorActor_response_getJob(Success(jobInfo)) => jobInfo
          }
        }
      }
    }
  }

  @ApiOperation(value = "Find job config by id", notes = "Return job configuration", httpMethod = "GET",
    response = classOf[String])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def getJobConfig: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.JobsPath / Segment / "config") { (jobId) =>
      get {
        complete {
          val future = supervisor ? new JobServerSupervisorActor_getJobConfig(jobId)
          Await.result(future, timeout.duration) match {
            case JobServerSupervisorActor_response_getJobConfig(Failure(exception)) => throw exception
            case JobServerSupervisorActor_response_getJobConfig(Success(jobInfo)) => jobInfo
          }
        }
      }
    }
  }
}
