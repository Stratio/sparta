/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.helpers

import com.stratio.crossdata.security.CrossdataSecurityManager
import com.stratio.gosec.dyplon.plugins.crossdata.GoSecCrossdataSecurityManager
import com.stratio.gosec.dyplon.plugins.sparta.GoSecSpartaSecurityManager
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.config.SpartaConfig._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.typesafe.config.Config
import spray.http.StatusCodes

import scala.util.{Failure, Success, Try}

object SecurityManagerHelper {

  lazy val securityManager: Option[SpartaSecurityManager] =
    if (!isSecurityManagerEnabled) {
      log.info("Authorization is not enabled, configure a security manager if needed")
      None
    } else {
      log.debug("Starting Gosec Sparta Dyplon security manager")
      val secManager = new GoSecSpartaSecurityManager().asInstanceOf[SpartaSecurityManager]
      secManager.start
      Some(secManager)
    }

  def initCrossdataSecurityManager(): Unit =
    if (!isCrossdataSecurityManagerEnabled) {
      log.info("Crossdata authorization is not enabled, configure a security manager if needed")
      None
    } else{
      log.debug("Starting Gosec Crossdata Dyplon security manager")
      new GoSecCrossdataSecurityManager().asInstanceOf[CrossdataSecurityManager].start
    }


  def isSecurityManagerEnabled: Boolean = Try(getSecurityConfig.get.getBoolean("manager.enabled")) match {
    case Success(value) =>
      value
    case Failure(e) =>
      log.error("Incorrect value in security manager option, setting enabled value by default", e)
      true
  }

  def isCrossdataSecurityManagerEnabled: Boolean =
    Try(crossdataConfig.get.getBoolean("security.enable-manager")) match {
      case Success(value) =>
        value
      case Failure(e) =>
        log.error("Incorrect value in crossdata security manager option, setting enabled value by default", e)
        true
    }

  def getSecurityConfig: Option[Config] = mainConfig.flatMap(config => getOptionConfig(ConfigSecurity, config))

  def errorResponseAuthorization(userId: String, resource: String): UnauthorizedResponse = {
    val msg = s"Unauthorized action on resource: $resource. User $userId doesn't have enough permissions."
    log.warn(msg)
    UnauthorizedResponse(ServerException(ErrorModel.toString(ErrorModel(
      StatusCodes.Unauthorized.intValue,
      StatusCodes.Unauthorized.intValue.toString,
      ErrorCodesMessages.getOrElse(StatusCodes.Unauthorized.intValue.toString, UnAuthorizedError),
      Option(msg)
    ))))
  }

  def errorNoUserFound(actions: Seq[Action]): UnauthorizedResponse = {
    val msg = s"Authorization rejected for actions: $actions. No user was found."
    log.warn(msg)
    UnauthorizedResponse(ServerException(ErrorModel.toString(ErrorModel(
      StatusCodes.InternalServerError.intValue,
      UserNotFound,
      ErrorCodesMessages.getOrElse(UserNotFound, UnAuthorizedError),
      Option(msg)
    ))))
  }

  case class UnauthorizedResponse(exception: ServerException)

  //scalastyle:off

  implicit def resourceParser(resource: String): Resource = {
    resource match {
      case "backup" => Resource(BackupResource, resource)
      case "catalog" => Resource(CatalogResource, resource)
      case "checkpoint" => Resource(CheckpointResource, resource)
      case "configuration" => Resource(ConfigurationResource, resource)
      case "driver" => Resource(DriverResource, resource)
      case "environment" => Resource(EnvironmentResource, resource)
      case "execution" => Resource(ExecutionResource, resource)
      case "group" => Resource(GroupResource, resource)
      case "plugin" => Resource(PluginResource, resource)
      case "status" => Resource(StatusResource, resource)
      case "template" => Resource(TemplateResource, resource)
      case "workflow" => Resource(WorkflowResource, resource)
    }
  }
}
