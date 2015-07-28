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
import com.stratio.sparkta.driver.models.StreamingContextStatusEnum
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.stratio.sparkta.serving.api.constants.HttpConstant
import com.stratio.sparkta.serving.core.messages.JobServerMessages._
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

    case JobServerSupervisorActor_getJars() => doGetJars
    case JobServerSupervisorActor_getJobs() => doGetJobs
    case JobServerSupervisorActor_getContexts() => doGetContexts
    case JobServerSupervisorActor_getJob(jobId) => doGetJob(jobId)
    case JobServerSupervisorActor_getJobConfig(jobId) => doGetJobConfig(jobId)
    case JobServerSupervisorActor_deleteContext(contextId) => doDeleteContext(contextId)
    case JobServerSupervisorActor_uploadJars(files) => doUploadJars(files)
    case JobServerSupervisorActor_uploadPolicy(jobName, classPath, policy, contextName) =>
      doUploadPolicy(jobName, classPath, policy, contextName)
    case JobServerSupervisorActor_createContext(contextName, cpuCores, memory) =>
      doCreateContext(contextName, cpuCores, memory)
  }

  def doGetJars: Unit = {
    if (ValidParameters) {
      val responseParsed = parse(Http(s"http://$host:$port/jars").asString.body)
      sender ! JobServerSupervisorActor_response_getJars(Try(responseParsed))
    } else {
      sender ! JobServerSupervisorActor_response_getJars(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doGetJobs: Unit = {
    if (ValidParameters) {
      val responseParsed = parse(Http(s"http://$host:$port/jobs").asString.body)
      sender ! JobServerSupervisorActor_response_getJobs(Try(responseParsed))
    } else {
      sender ! JobServerSupervisorActor_response_getJobs(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doGetContexts: Unit =
    if (ValidParameters) {
      val responseParsed = parse(Http(s"http://$host:$port/contexts").asString.body)
      sender ! JobServerSupervisorActor_response_getContexts(Try(responseParsed))
    } else {
      sender ! JobServerSupervisorActor_response_getContexts(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }

  def doGetJob(jobId: String): Unit = {
    if (ValidParameters) {
      val responseParsed = parse(Http(s"http://$host:$port/jobs/$jobId").asString.body)
      sender ! JobServerSupervisorActor_response_getJob(Try(responseParsed))
    } else {
      sender ! JobServerSupervisorActor_response_getJob(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doGetJobConfig(jobId: String): Unit = {
    if (ValidParameters) {
      val responseParsed = parse(Http(s"http://$host:$port/jobs/$jobId/config").asString.body)
      sender ! JobServerSupervisorActor_response_getJobConfig(Try(responseParsed))
    } else {
      sender ! JobServerSupervisorActor_response_getJobConfig(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doDeleteJob(jobId: String): Unit = {
    if (ValidParameters) {
      val response = Http(s"http://$host:$port/jobs/$jobId").method("DELETE").asString.body
      val responseParsed = if (response == HttpConstant.JobServerOkMessage) ResultOk else parse(response)
      sender ! JobServerSupervisorActor_response_deleteJob(Try(responseParsed))
    } else {
      sender ! JobServerSupervisorActor_response_deleteJob(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doCreateContext(contextName: String, cpuCores: String, memory: String): Unit = {
    if (ValidParameters) {
      doDeleteContext(contextName)
      val response = Http(s"http://$host:$port/contexts/$contextName?num-cpu-cores=$cpuCores&memory-per-node=$memory")
        .postData("")
        .asString.body
      val responseParsed = if (response == HttpConstant.JobServerOkMessage) ResultOk else parse(response)
      sender ! JobServerSupervisorActor_response_createContext(Try(responseParsed))
    } else {
      sender ! JobServerSupervisorActor_response_createContext(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doDeleteContext(contextId: String): Unit = {
    if (ValidParameters) {
      val response = Http(s"http://$host:$port/contexts/$contextId").method("DELETE").asString.body
      val responseParsed = if (response == HttpConstant.JobServerOkMessage) ResultOk else parse(response)
      sender ! JobServerSupervisorActor_response_deleteContext(Try(responseParsed))
    } else {
      sender ! JobServerSupervisorActor_response_deleteContext(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg))
      )
    }
  }

  def doUploadPolicy(jobName: String, classPath: String, policy: String, context: Option[String]): Unit = {
    if (ValidParameters) {
      val contextOptions = context match {
        case Some(contextName) => s"&context=$contextName&sync=true"
        case None => ""
      }
      val response = Http(s"http://$host:$port/jobs?appName=$jobName&classPath=$classPath$contextOptions")
        .postData(policy.getBytes).asString.body
      val responseParsed = if (response == HttpConstant.JobServerOkMessage) ResultOk else parse(response)
      sender ! JobServerSupervisorActor_response_uploadPolicy(Try(responseParsed))
    } else {
      sender ! JobServerSupervisorActor_response_uploadPolicy(
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
      sender ! JobServerSupervisorActor_response_uploadJars(Try(uploadsResults))
    } else {
      sender ! JobServerSupervisorActor_response_uploadJars(
        Try(throw new Exception(HttpConstant.JobServerHostPortExceptionMsg)))
    }
  }

  def uploadFile(fileName: String, filePath: String): String = {
    val fileBytes = Files.readAllBytes(Paths.get(filePath))
    val request = Http(s"http://$host:$port/jars/$fileName").postData(fileBytes).asString
    request.body
  }
}
