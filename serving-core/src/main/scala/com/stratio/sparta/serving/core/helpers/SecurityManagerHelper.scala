/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.helpers

import scala.util.{Failure, Success, Try}
import spray.http.StatusCodes
import com.stratio.crossdata.security.CrossdataSecurityManager
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.config.SpartaConfig._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._

object SecurityManagerHelper {

  private var crossdataSecurityManager: Option[CrossdataSecurityManager] = None
  private val spartaSecurityManagerPool = scala.collection.mutable.ArrayBuffer.empty[SpartaSecurityManager]
  private val randomManager = new scala.util.Random

  private lazy val spartaSecurityManagerPoolSize = Try(SpartaConfig.getSecurityConfig().get.getInt("manager.poolSize")).getOrElse(3)

  lazy val dyplonFacadeEnabled = Try(SpartaConfig.getSecurityConfig().get.getBoolean("manager.http.enabled")).getOrElse(false)

  def initSpartaSecurityManager(): Unit = {
    if (!isSecurityManagerEnabled) {
      log.info("Authorization is not enabled, configure a security manager if needed")
    } else if (spartaSecurityManagerPool.nonEmpty) {
      log.info("Sparta security manager was already initialized")
    } else {
      JarsHelper.addDyplonSpartaPluginsToClassPath()
      log.info(s"Starting Sparta Dyplon security managers with a pool size of $spartaSecurityManagerPoolSize instances")
      for (instanceNumber <- 0 until spartaSecurityManagerPoolSize) {
        val finalClazzToInstance = if (dyplonFacadeEnabled) {
          "com.stratio.gosec.dyplon.plugins.sparta.GoSecSpartaSecurityManagerFacade"
        } else "com.stratio.gosec.dyplon.plugins.sparta.GoSecSpartaSecurityManager"
        val securityManagerClass = Class.forName(finalClazzToInstance, true, Thread.currentThread().getContextClassLoader)
        val constructor = securityManagerClass.getConstructor()
        val newSecManager = constructor.newInstance().asInstanceOf[SpartaSecurityManager]
        newSecManager.start()
        spartaSecurityManagerPool += newSecManager

        log.info(s"Sparta Dyplon security manager instance $instanceNumber started")

        Runtime.getRuntime.addShutdownHook(new Thread() {
          override def run(): Unit = {
            stopSpartaSecurityManager()
          }
        })
      }
    }
  }

  def stopSpartaSecurityManager(): Unit = {
    log.debug("Stopping Sparta Security managers")
    spartaSecurityManagerPool.foreach(_.stop())
  }


  def stopCrossdataSecurityManager(): Unit = {
    log.debug("Stopping Crossdata Security manager")
    crossdataSecurityManager.foreach(_.stop())
  }

  def securityManager: Option[SpartaSecurityManager] = {
    if (spartaSecurityManagerPool.nonEmpty)
      Some(spartaSecurityManagerPool(randomManager.nextInt(spartaSecurityManagerPool.size)))
    else None
  }

  def initCrossdataSecurityManager(): Unit =
    if (!isCrossdataSecurityManagerEnabled) {
      log.info("Crossdata authorization is not enabled, configure a security manager if needed")
      None
    } else {
      log.info("Starting Crossdata Dyplon security manager")
      JarsHelper.addDyplonCrossdataPluginsToClassPath()
      val finalClazzToInstance = Try(SpartaConfig.getCrossdataConfig().get.getString("security.manager.class"))
        .getOrElse("com.stratio.gosec.dyplon.plugins.crossdata.GoSecCrossdataSecurityManager")
      val securityManagerClass =
        Class.forName(finalClazzToInstance, true, Thread.currentThread().getContextClassLoader)
      val constr = securityManagerClass.getConstructor()
      val secManager = constr.newInstance().asInstanceOf[CrossdataSecurityManager]

      secManager.start()
      crossdataSecurityManager = Option(secManager)

      log.info("Crossdata Dyplon security manager started")

      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run(): Unit = {
          stopCrossdataSecurityManager()
        }
      })
    }


  def isSecurityManagerEnabled: Boolean = Try(getSecurityConfig().get.getBoolean("manager.enabled")) match {
    case Success(value) =>
      value
    case Failure(e) =>
      log.error("Incorrect value in security manager option, setting enabled value to false", e)
      true
  }

  def isCrossdataSecurityManagerEnabled: Boolean =
    Try(getCrossdataConfig().get.getBoolean("security.enable-manager")) match {
      case Success(value) =>
        value
      case Failure(e) =>
        log.error("Incorrect value in Crossdata security manager option, setting enabled value to false", e)
        true
    }

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
      case "Catalog" => Resource(CatalogResource, resource)
      case "Configuration" => Resource(ConfigurationResource, resource)
      case "Dashboard" => Resource(DashboardResource, resource)
      case "Files" => Resource(FilesResource, resource)
      case "GlobalParameters" => Resource(GlobalParametersResource, resource)
      case "Groups" => Resource(GroupsResource, resource)
      case "MlModels" => Resource(MlModelsResource, resource)
      case "ParameterList" => Resource(ParameterListResource, resource)
      case "Template" => Resource(TemplateResource, resource)
      case "Workflows" => Resource(WorkflowsResource, resource)
      case "QRResult" => Resource(QRResultResource, resource)
    }
  }

  implicit def resourceTupleParser(resource: (String, String)): Resource = {
    resource._1 match {
      case "Catalog" => Resource(CatalogResource, resource._2)
      case "Configuration" => Resource(ConfigurationResource, resource._2)
      case "Dashboard" => Resource(DashboardResource, resource._2)
      case "Files" => Resource(FilesResource, resource._2)
      case "GlobalParameters" => Resource(GlobalParametersResource, resource._2)
      case "Groups" => Resource(GroupsResource, resource._2)
      case "MlModels" => Resource(MlModelsResource, resource._2)
      case "ParameterList" => Resource(ParameterListResource, resource._2)
      case "Template" => Resource(TemplateResource, resource._2)
      case "Workflows" => Resource(WorkflowsResource, resource._2)
      case "QRResult" => Resource(QRResultResource, resource._2)
    }
  }
}
