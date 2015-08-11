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
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.sys.process._
import scala.util.{Failure, Success, Try}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.Config
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._

import com.stratio.sparkta.driver.SparktaJob
import com.stratio.sparkta.driver.models.AggregationPoliciesModel
import com.stratio.sparkta.driver.models.StreamingContextStatusEnum._
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor.JobServerActor._
import com.stratio.sparkta.serving.api.actor.StreamingActor._
import com.stratio.sparkta.serving.api.constants._

class ClusterContextActor(policy: AggregationPoliciesModel,
                          streamingContextService: StreamingContextService,
                          jobServerRef: ActorRef,
                          jobServerConfig: Config) extends InstrumentedActor {

  implicit val timeout: Timeout = Timeout(90.seconds)
  implicit val json4sJacksonFormats = DefaultFormats
  implicit val formats = Serialization.formats(NoTypeHints)

  override def receive: PartialFunction[Any, Unit] = {
    case InitSparktaContext => doInitSparktaContext
    case JsResponseUploadPolicy(response) => doJobServerResponseUploadPolicy(response)
  }

  def doInitSparktaContext: Unit = {
    log.debug("Init new cluster streamingContext with name " + policy.name)

    val file = new Path(s"/user/stratio/${policy.name}.json")
    val fileSystem = FileSystem.get(new Configuration())
    val out = fileSystem.create(file)
    out.write(write(policy).getBytes)
    out.close
    val cmd = s"spark-submit --class com.stratio.sparkta.driver.SparktaJob --master  yarn-client " +
      s"--num-executors 2 driver/target/driver-plugin.jar ${policy.name}"
    cmd.!!
  }

  private def doJobServerResponseUploadPolicy(response: Try[JValue]): Unit =
    response match {
      case Failure(exception) => this.context.parent ! new StatusContextActor(context.self,
        policy.name,
        Error,
        Some(exception.getMessage))
      case Success(response) => {
        this.context.parent ! new ResponseCreateContext(new StatusContextActor(context.self,
          policy.name,
          Initialized,
          Some(response.toString)))
      }
    }

  private def sendPolicyToJobServerActor(activeJars: Either[Seq[String], Seq[String]]): Unit = {

    //TODO policy to string, we need spark configuration¿?

    val policyStr = "input.policy = " + write(policy)
    val activeJarsFilesToSend = SparktaJob.activeJarFiles(activeJars.right.get, streamingContextService.jars)

    //TODO validate correct result and send messages to sender

    doUploadJars(activeJarsFilesToSend, policyStr)
  }

  private def doUploadJars(jarFiles: Seq[File], policyStr: String): Unit = {

    (jobServerRef ? new JsUploadJars(jarFiles)).mapTo[JsResponseUploadJars].foreach {
      case JsResponseUploadJars(Failure(exception)) =>
        sender ! new ResponseCreateContext(new StatusContextActor(context.self,
          policy.name,
          Error,
          Some(exception.getMessage)))
      case JsResponseUploadJars(Success(response)) => doCreateContext(policyStr)
    }
  }

  private def doCreateContext(policyStr: String): Unit = {

    val cpuCores = Try(jobServerConfig.getInt(JobServerConstant.CpuCores)).getOrElse(JobServerConstant.DefaultCpuCores)
    val memory = Try(jobServerConfig.getString(JobServerConstant.Memory)).getOrElse(JobServerConstant.DefaultMemory)

    (jobServerRef ? new JsCreateContext(s"${policy.name}", cpuCores.toString, memory))
      .mapTo[JsResponseCreateContext].foreach {
      case JsResponseCreateContext(Failure(exception)) =>
        sender ! new ResponseCreateContext(new StatusContextActor(context.self,
          policy.name,
          Error,
          Some(exception.getMessage)))
      case JsResponseCreateContext(Success(response)) =>
        jobServerRef ! new JsUploadPolicy("driver-plugin.jar",
          "com.stratio.sparkta.driver.service.SparktaJob",
          policyStr,
          Some(s"${policy.name}-${policy.input.get.name}"))
    }
  }

  override def postStop(): Unit = {

    super.postStop()
  }
}
