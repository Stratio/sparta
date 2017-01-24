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
import java.util.function.Predicate
import java.util.regex.Pattern

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.actor.PluginActor.{PluginResponse, UploadFile}
import com.stratio.sparta.serving.api.constants.HttpConstant
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

  //The dir where the jars will be saved
  val targetDir = Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.PluginsPackageLocation))
    .getOrElse(AppConstant.DefaultPluginsPackageLocation)
  //Configuration of the app
  val host: String = SpartaConfig.apiConfig.get.getString("host")
  val port: Int = SpartaConfig.apiConfig.get.getInt("port")
  val targetUrl: String = s"$host:$port"
  //Url of the download endpoint
  val url = s"$targetUrl/${HttpConstant.PluginsPath}"
  //Regexp for jar name validation
  val jarFileName: Predicate[String] = Pattern.compile(""".*\.jar""").asPredicate()

  override def receive: Receive = {
    case UploadFile(_, files) if files.isEmpty =>
      sender ! PluginResponse(Failure(new IllegalArgumentException(s"A file is expected")))
    case UploadFile(_, files) if files.size != 1 =>
      sender ! PluginResponse(Failure(new IllegalArgumentException(s"More than one file is not supported")))
    case UploadFile(name, files) if jarFileName.test(name) => uploadFile(name, files)
    case UploadFile(name, _) =>
      sender ! PluginResponse(Failure(new IllegalArgumentException(s"$name is Not a valid file name")))
    case _ => log.info("Unrecognized message in Plugin Actor")
  }

  def uploadFile(fileName: String, files: Seq[BodyPart]): Unit = {
    sender ! PluginResponse(
      Try {
        saveFile(files.head.entity.data.toByteArray, s"$targetDir/$fileName")
        s"$url/$fileName"
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
