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

import java.util.regex.Pattern

import akka.actor.Actor
import com.stratio.sparta.serving.api.actor.DriverActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.utils.FileActorUtils
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.files.SpartaFilesResponse
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.util.{Failure, Try}

class DriverActor extends Actor with Json4sJacksonSupport with FileActorUtils with SpartaSerializer {

  //The dir where the jars will be saved
  val targetDir = Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.DriverPackageLocation))
    .getOrElse(AppConstant.DefaultDriverPackageLocation)
  override val apiPath = HttpConstant.DriverPath
  override val patternFileName = Option(Pattern.compile(""".*\.jar""").asPredicate())

  override def receive: Receive = {
    case UploadDrivers(files) => if (files.isEmpty) errorResponse() else uploadDrivers(files)
    case ListDrivers => browseDrivers()
    case DeleteDrivers => deleteDrivers()
    case DeleteDriver(fileName) => deleteDriver(fileName)
    case _ => log.info("Unrecognized message in Driver Actor")
  }

  def errorResponse(): Unit =
    sender ! SpartaFilesResponse(Failure(new IllegalArgumentException(s"At least one file is expected")))

  def deleteDrivers(): Unit = sender ! DriverResponse(deleteFiles())

  def deleteDriver(fileName: String): Unit = sender ! DriverResponse(deleteFile(fileName))

  def browseDrivers(): Unit = sender ! SpartaFilesResponse(browseDirectory())

  def uploadDrivers(files: Seq[BodyPart]): Unit = sender ! SpartaFilesResponse(uploadFiles(files))
}

object DriverActor {

  case class UploadDrivers(files: Seq[BodyPart])

  case class DriverResponse(status: Try[_])

  case object ListDrivers

  case object DeleteDrivers

  case class DeleteDriver(fileName: String)

}
