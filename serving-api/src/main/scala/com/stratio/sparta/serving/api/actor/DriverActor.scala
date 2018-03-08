/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.actor.DriverActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.utils.FileActorUtils
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.files.SpartaFile
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.util.{Failure, Try}

class DriverActor(implicit val secManagerOpt: Option[SpartaSecurityManager]) extends Actor
  with Json4sJacksonSupport with FileActorUtils with SpartaSerializer with ActionUserAuthorize {

  //The dir where the jars will be saved
  val targetDir = Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.DriverPackageLocation))
    .getOrElse(AppConstant.DefaultDriverPackageLocation)
  val temporalDir = "/tmp/sparta/drivers"

  override val apiPath = s"${HttpConstant.DriverPath}/download"

  val ResourceType = "driver"

  override def receive: Receive = {
    case UploadDrivers(files, user) => if (files.isEmpty) errorResponse() else uploadDrivers(files, user)
    case ListDrivers(user) => browseDrivers(user)
    case DeleteDrivers(user) => deleteDrivers(user)
    case DeleteDriver(fileName, user) => deleteDriver(fileName, user)
    case _ => log.info("Unrecognized message in Driver Actor")
  }

  def errorResponse(): Unit =
    sender ! Left(Failure(new IllegalArgumentException(s"At least one file is expected")))

  def deleteDrivers(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Try[Unit]](user, Map(ResourceType -> Delete)) {
      deleteFiles()
    }

  def deleteDriver(fileName: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Try[Unit]](user, Map(ResourceType -> Delete)) {
      deleteFile(fileName)
    }

  def browseDrivers(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[SpartaFilesResponse](user, Map(ResourceType -> View)) {
      browseDirectory()
    }

  def uploadDrivers(files: Seq[BodyPart], user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[SpartaFilesResponse](user, Map(ResourceType -> Upload)) {
      uploadFiles(files)
    }
}

object DriverActor {

  type SpartaFilesResponse = Try[Seq[SpartaFile]]

  case class UploadDrivers(files: Seq[BodyPart], user: Option[LoggedUser])

  case class ListDrivers(user: Option[LoggedUser])

  case class DeleteDrivers(user: Option[LoggedUser])

  case class DeleteDriver(fileName: String, user: Option[LoggedUser])

}
