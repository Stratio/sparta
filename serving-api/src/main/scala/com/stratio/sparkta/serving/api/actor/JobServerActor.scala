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
import java.nio.file.{Files, Paths}

import akka.actor._
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.stratio.sparkta.serving.api.actor.JobServerActor._
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.core.models.StreamingContextStatusEnum
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.JsonMethods._
import org.json4s.{DefaultFormats, _}

import scala.util.Try
import scalaj.http.Http

class JobServerActor(host: String, port: Int) extends InstrumentedActor {

  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()

  final val ResultOk = parse( """{"result": "OK" }""")
  final val ValidParameters = !host.isEmpty && port > 0

  override def receive: Receive = {

    case JsGetJars => doGetJars
    case JsGetJobs => doGetJobs
    case JsGetContexts => doGetContexts
    case JsGetJob(jobId) => doGetJob(jobId)
    case JsGetJobConfig(jobId) => doGetJobConfig(jobId)
    case JsDeleteContext(contextId) => doDeleteContext(contextId)
    case JsUploadJars(files) => doUploadJars(files)
    case JsUploadPolicy(jobName, classPath, policy, contextName) =>
      doUploadPolicy(jobName, classPath, policy, contextName)
    case JsCreateContext(contextName, cpuCores, memory) =>
      doCreateContext(contextName, cpuCores, memory)
  }

  def doGetJars: Unit = {
    if (ValidParameters) {
      val responseParsed = parse(Http(s"http://$host:$port/jars").asString.body)
      sender ! JsResponseGetJars(Try(responseParsed))
    } else {
      sender ! JsResponseGetJars(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doGetJobs: Unit = {
    if (ValidParameters) {
      val responseParsed = parse(Http(s"http://$host:$port/jobs").asString.body)
      sender ! JsResponseGetJobs(Try(responseParsed))
    } else {
      sender ! JsResponseGetJobs(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doGetContexts: Unit =
    if (ValidParameters) {
      val responseParsed = parse(Http(s"http://$host:$port/contexts").asString.body)
      sender ! JsResponseGetContexts(Try(responseParsed))
    } else {
      sender ! JsResponseGetContexts(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }

  def doGetJob(jobId: String): Unit = {
    if (ValidParameters) {
      val responseParsed = parse(Http(s"http://$host:$port/jobs/$jobId").asString.body)
      sender ! JsResponseGetJob(Try(responseParsed))
    } else {
      sender ! JsResponseGetJob(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doGetJobConfig(jobId: String): Unit = {
    if (ValidParameters) {
      val responseParsed = parse(Http(s"http://$host:$port/jobs/$jobId/config").asString.body)
      sender ! JsResponseGetJobConfig(Try(responseParsed))
    } else {
      sender ! JsResponseGetJobConfig(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doDeleteJob(jobId: String): Unit = {
    if (ValidParameters) {
      val response = Http(s"http://$host:$port/jobs/$jobId").method("DELETE")
        .timeout(HttpConstant.ConnectionTimeout, HttpConstant.ReadTimeout).asString.body
      if (response == HttpConstant.JobServerOkMessage) {
        sender ! JsResponseDeleteJob(Try(ResultOk))
      } else {
        sender ! JsResponseDeleteJob(
          Try(throw new Exception(parse(response).toString)))
      }
    } else {
      sender ! JsResponseDeleteJob(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doCreateContext(contextName: String, cpuCores: String, memory: String): Unit = {
    if (ValidParameters) {
      deleteContextRequest(contextName)
      val response = Http(s"http://$host:$port/contexts/$contextName?num-cpu-cores=$cpuCores&memory-per-node=$memory")
        .postData("").timeout(HttpConstant.ConnectionTimeout, HttpConstant.ReadTimeout).asString.body
      if (response == HttpConstant.JobServerOkMessage) {
        sender ! JsResponseCreateContext(Try(ResultOk))
      } else {
        sender ! JsResponseCreateContext(
          Try(throw new Exception(parse(response).toString)))
      }
    } else {
      sender ! JsResponseCreateContext(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doDeleteContext(contextId: String): Unit = {
    if (ValidParameters) {
      val response = deleteContextRequest(contextId)
      if (response == HttpConstant.JobServerOkMessage) {
        sender ! JsResponseDeleteContext(Try(ResultOk))
      } else {
        sender ! JsResponseDeleteContext(
          Try(throw new Exception(parse(response).toString)))
      }
    } else {
      sender ! JsResponseDeleteContext(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def deleteContextRequest(contextId: String): String =
    Http(s"http://$host:$port/contexts/$contextId").method("DELETE")
      .timeout(HttpConstant.ConnectionTimeout, HttpConstant.ReadTimeout).asString.body

  def doUploadPolicy(jobName: String, classPath: String, policy: String, context: Option[String]): Unit = {
    if (ValidParameters) {
      val contextOptions = context match {
        case Some(contextName) => s"&context=$contextName&sync=true"
        case None => ""
      }
      val response = Http(s"http://$host:$port/jobs?appName=$jobName&classPath=$classPath$contextOptions")
        .postData(policy.getBytes).timeout(HttpConstant.ConnectionTimeout, HttpConstant.ReadTimeout).asString.body
      if (response == HttpConstant.JobServerOkMessage) {
        sender ! JsResponseUploadPolicy(Try(ResultOk))
      } else {
        sender ! JsResponseUploadPolicy(
          Try(throw new Exception(parse(response).toString)))
      }
    } else {
      sender ! JsResponseUploadPolicy(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doUploadJars(files: Seq[File]): Unit = {
    if (ValidParameters) {
      val uploadsResults = files.map(file => {
        val resultUpload = uploadFile(file.getName, file.getAbsolutePath)
        s"${file.getName} : $resultUpload\n"
      }).mkString(",")
      sender ! JsResponseUploadJars(Try(uploadsResults))
    } else {
      sender ! JsResponseUploadJars(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg)))
    }
  }

  def uploadFile(fileName: String, filePath: String): String = {
    val fileBytes = Files.readAllBytes(Paths.get(filePath))
    Http(s"http://$host:$port/jars/$fileName").postData(fileBytes)
      .timeout(HttpConstant.ConnectionTimeout, HttpConstant.ReadTimeout).asString.body
  }
}

object JobServerActor {

  case object JsGetJars

  case object JsGetJobs

  case object JsGetContexts

  case class JsGetJob(jobId: String)

  case class JsGetJobConfig(jobId: String)

  case class JsDeleteContext(contextId: String)

  case class JsDeleteJob(jobId: String)

  case class JsUploadJars(files: Seq[File])

  case class JsUploadPolicy(jobName: String,
                            classPath: String,
                            policy: String,
                            context: Option[String])

  case class JsCreateContext(contextName: String,
                             cpuCores: String,
                             memory: String)

  case class JsResponseGetJars(jarResult: Try[JValue])

  case class JsResponseGetJobs(jobsResult: Try[JValue])

  case class JsResponseGetContexts(contextsResult: Try[JValue])

  case class JsResponseGetJob(jobInfoResult: Try[JValue])

  case class JsResponseGetJobConfig(jobInfoResult: Try[JValue])

  case class JsResponseDeleteContext(deleteContextResult: Try[JValue])

  case class JsResponseDeleteJob(deleteJobResult: Try[JValue])

  case class JsResponseUploadJars(uploadJarResult: Try[String])

  case class JsResponseUploadPolicy(uploadPolicyResult: Try[JValue])

  case class JsResponseCreateContext(createContextResult: Try[JValue])

}
