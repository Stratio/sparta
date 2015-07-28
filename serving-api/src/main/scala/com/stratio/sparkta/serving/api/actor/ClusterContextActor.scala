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

package com.stratio.sparkta.serving.api.actor

import java.io.File

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.models.AggregationPoliciesModel
import com.stratio.sparkta.driver.models.StreamingContextStatusEnum._
import com.stratio.sparkta.driver.service.{SparktaJob, StreamingContextService}
import JobServerActor._
import StreamingActor._
import scala.concurrent.ExecutionContext.Implicits.global
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class ClusterContextActor(policy: AggregationPoliciesModel,
                          streamingContextService: StreamingContextService,
                          jobServerRef: ActorRef) extends InstrumentedActor {

  implicit val timeout: Timeout = Timeout(60.seconds)
  implicit val json4sJacksonFormats = DefaultFormats
  implicit val formats = Serialization.formats(NoTypeHints)

  override def receive: PartialFunction[Any, Unit] = {
    case InitSparktaContext => doInitSparktaContext
    case JsResponseUploadPolicy(response) => doJobServerResponseUploadPolicy(response)
  }

  def doInitSparktaContext: Unit = {
    log.debug("Init new cluster streamingContext with name " + policy.name)

    //TODO validate policy
    val activeJars: Either[Seq[String], Seq[String]] = SparktaJob.activeJars(policy, streamingContextService.jars)
    if (activeJars.isLeft) {
      val msg = s"The policy have jars witch cannot be found in classpath:" +
        s" ${activeJars.left.get.mkString(",")}"
      sender ! new ResponseCreateContext(new ContextActorStatus(context.self,
        policy.name,
        ConfigurationError,
        Some(msg)))
    } else sendPolicyToJobServerActor(activeJars)
  }

  private def doJobServerResponseUploadPolicy(response: Try[JValue]): Unit =
    response match {
      case Failure(exception) => this.context.parent ! new ContextActorStatus(context.self,
        policy.name,
        Error,
        Some(exception.getMessage))
      case Success(response) => {
        this.context.parent ! new ResponseCreateContext(new ContextActorStatus(context.self,
          policy.name,
          Initialized,
          Some(response.toString)))
      }
    }

  private def sendPolicyToJobServerActor(activeJars: Either[Seq[String], Seq[String]]): Unit = {

    val policyStr = write(policy)
    val activeJarsFilesToSend = SparktaJob.activeJarFiles(activeJars.right.get, streamingContextService.jars)

    //TODO validate correct result and send messages to sender

    doUploadJars(activeJarsFilesToSend, policyStr)
  }

  private def doUploadJars(jarFiles: Seq[File], policyStr: String): Unit = {
    (jobServerRef ? new JsUploadJars(jarFiles)).mapTo[JsResponseUploadJars].foreach {
      case JsResponseUploadJars(Failure(exception)) =>
        sender ! new ResponseCreateContext(new ContextActorStatus(context.self,
          policy.name,
          Error,
          Some(exception.getMessage)))
      case JsResponseUploadJars(Success(response)) => doCreateContext(policyStr)
    }
  }

  private def doCreateContext(policyStr: String): Unit = {
    (jobServerRef ? new JsCreateContext(s"${policy.name}-${policy.input.name}", "4", "512m"))
      .mapTo[JsResponseCreateContext].foreach {
      case JsResponseCreateContext(Failure(exception)) =>
        sender ! new ResponseCreateContext(new ContextActorStatus(context.self,
          policy.name,
          Error,
          Some(exception.getMessage)))
      case JsResponseCreateContext(Success(response)) =>
        jobServerRef ! new JsUploadPolicy("driver-plugin.jar",
          "com.stratio.sparkta.driver.service.SparktaJob",
          policyStr,
          Some(s"${policy.name}-${policy.input.name}"))
    }
  }

  override def postStop(): Unit = {

    super.postStop()
  }
}
