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
package com.stratio.sparta.serving.api.actor

import java.io.{BufferedOutputStream, FileOutputStream}

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.actor.PluginActor.{PluginResponse, UploadFile}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.util.{Failure, Try}

class PluginActor extends Actor
  with Json4sJacksonSupport
  with SLF4JLogging
  with SpartaSerializer {

  val targetDir: String = Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.DriverPackageLocation))
    .getOrElse(AppConstant.DefaultDriverPackageLocation)

  val targetUrl: String = "http://localhost:8080"

  val url = s"$targetUrl/$AppConstant.PluginsURLLocation/"

  override def receive: Receive = {
    case UploadFile(name, files) if name.matches(""".*\.jar""") => uploadFile(name, files)
    case UploadFile(name, _) =>
      sender ! PluginResponse(Failure(new IllegalArgumentException(s"$name is Not a valid file name")))
    case _ => log.info("Unrecognized message in Plugin Actor")
  }

  def uploadFile(fileName: String, files: Seq[BodyPart]): Unit = {
    sender ! PluginResponse(
      Try {
        val targetFile = s"$targetDir/$fileName"
        files.foreach(file => saveFile(file.entity.data.toByteArray, targetFile))
        s"$targetUrl/$targetFile"
      }
    )
  }

  protected def saveFile(array: Array[Byte], fileName: String): Unit = {
    log.info(s"Saving file to: $fileName")
    val bos = new BufferedOutputStream(new FileOutputStream(fileName))
    bos.write(array)
    bos.close()
  }

}

object PluginActor {

  case class UploadFile(t: String, files: Seq[BodyPart])

  case class PluginResponse(status: Try[_])

}
