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
import com.stratio.sparkta.serving.api.actor.JobServerActor._
import com.stratio.sparkta.serving.api.constants.HttpConstant
import spray.routing._

import scala.concurrent.Await
import scala.util.{Failure, Success}

trait JobServerHttpService extends BaseHttpService {

  override def routes: Route = getJars ~ getJobs ~ getJob ~ getContexts ~ deleteContext ~ deleteJob ~ getJobConfig

  case class Result(message: String, desc: Option[String] = None)

  def getJars: Route = {
    path(HttpConstant.JobServerPath / "jars") {
      get {
        complete {
          val future = supervisor ? JsGetJars
          Await.result(future, timeout.duration) match {
            case JsResponseGetJars(Failure(exception)) => throw exception
            case JsResponseGetJars(Success(jarList)) => jarList
          }
        }
      }
    }
  }

  def getJobs: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.JobsPath) {
      get {
        complete {
          val future = supervisor ? JsGetJobs
          Await.result(future, timeout.duration) match {
            case JsResponseGetJobs(Failure(exception)) => throw exception
            case JsResponseGetJobs(Success(jobsList)) => jobsList
          }
        }
      }
    }
  }

  def getContexts: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.ContextsPath) {
      get {
        complete {
          val future = supervisor ? JsGetContexts
          Await.result(future, timeout.duration) match {
            case JsResponseGetContexts(Failure(exception)) => throw exception
            case JsResponseGetContexts(Success(contextsList)) => contextsList
          }
        }
      }
    }
  }

  def deleteContext: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.ContextsPath / Segment) { (contextId) =>
      delete {
        complete {
          val future = supervisor ? new JsDeleteContext(contextId)
          Await.result(future, timeout.duration) match {
            case JsResponseDeleteContext(Failure(exception)) => throw exception
            case JsResponseDeleteContext(Success(response)) => response
          }
        }
      }
    }
  }

  def deleteJob: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.JobsPath / Segment) { (jobId) =>
      delete {
        complete {
          val future = supervisor ? new JsDeleteJob(jobId)
          Await.result(future, timeout.duration) match {
            case JsResponseDeleteJob(Failure(exception)) => throw exception
            case JsResponseDeleteJob(Success(response)) => response
          }
        }
      }
    }
  }

  def getJob: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.JobsPath / Segment) { (jobId) =>
      get {
        complete {
          val future = supervisor ? new JsGetJob(jobId)
          Await.result(future, timeout.duration) match {
            case JsResponseGetJob(Failure(exception)) => throw exception
            case JsResponseGetJob(Success(jobInfo)) => jobInfo
          }
        }
      }
    }
  }

  def getJobConfig: Route = {
    path(HttpConstant.JobServerPath / HttpConstant.JobsPath / Segment / "config") { (jobId) =>
      get {
        complete {
          val future = supervisor ? new JsGetJobConfig(jobId)
          Await.result(future, timeout.duration) match {
            case JsResponseGetJobConfig(Failure(exception)) => throw exception
            case JsResponseGetJobConfig(Success(jobInfo)) => jobInfo
          }
        }
      }
    }
  }
}
