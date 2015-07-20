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
import com.stratio.sparkta.serving.core.messages.ActorsMessages._
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.JsonMethods._
import org.json4s.{DefaultFormats, _}

import scala.util.Try
import scalaj.http.Http

class JobServerActor(host: String, port: Int) extends InstrumentedActor {

  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()

  override def receive: Receive = {

    case JobServerSupervisorActor_getJars() => doGetJars
    case JobServerSupervisorActor_uploadJars(files) => doUploadJars(files)
    //case files : Seq[File] => doUploadJars(files)
  }

  def doGetJars: Unit = {
    if (!host.isEmpty && port > 0) {
      val responseParsed: JValue = parse(Http(s"http://${host}:${port}/jars").asString.body)
      sender ! JobServerSupervisorActor_response_getJars(Try(responseParsed))
    } else log.info("JobServer host and port is not defined in configuration file")
  }

  def doUploadJars(files: Seq[File]): Unit = {
    if (!host.isEmpty && port > 0) {
      val uploadsResults = files.map(file => {
        val resultUpload = uploadFile(file.getName, file.getAbsolutePath)
        s"${file.getName} : ${resultUpload}\n"
      }).mkString(",")
      sender ! JobServerSupervisorActor_response_uploadJars(Try(uploadsResults))
    } else log.info("JobServer host and port is not defined in configuration file")
  }

  def uploadFile(fileName: String, filePath: String): String = {
    val fileBytes = Files.readAllBytes(Paths.get(filePath))
    val request = Http(s"http://${host}:${port}/jars/${fileName}").postData(fileBytes).asString
    request.body
  }
}
