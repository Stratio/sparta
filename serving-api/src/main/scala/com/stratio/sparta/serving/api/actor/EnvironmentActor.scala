/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.actor.EnvironmentActor._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.env.{Environment, EnvironmentData, EnvironmentVariable}
import com.stratio.sparta.serving.core.services.EnvironmentService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework

import scala.util.Try

class EnvironmentActor(val curatorFramework: CuratorFramework)
                      (implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize with SpartaSerializer {

  implicit val system = Option(context.system)
  private val environmentService = new EnvironmentService(curatorFramework)
  private val ResourceType = "Environment"

  //scalastyle:off
  override def receive: Receive = {
    case CreateEnvironment(request, user) => createEnvironment(request, user)
    case CreateEnvironmentVariable(request, user) => createEnvironmentVariable(request, user)
    case UpdateEnvironment(request, user) => updateEnvironment(request, user)
    case UpdateEnvironmentVariable(request, user) => updateEnvironmentVariable(request, user)
    case FindEnvironment(user) => findEnvironment(user)
    case FindEnvironmentVariable(name, user) => findEnvironmentVariable(name, user)
    case DeleteEnvironmentVariable(name, user) => deleteEnvironmentVariable(name, user)
    case DeleteEnvironment(user) => deleteEnvironment(user)
    case ExportData(user) => exportData(user)
    case ImportData(data, user) => importData(data, user)
    case _ => log.info("Unrecognized message in Environment Actor")
  }

  //scalastyle:on

  def exportData(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Download)) {
      environmentService.exportData()
    }

  def importData(data: EnvironmentData, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Upload)) {
      environmentService.importData(data)
    }

  def createEnvironment(request: Environment, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Create)) {
      environmentService.create(request)
    }

  def createEnvironmentVariable(request: EnvironmentVariable, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Create)) {
      environmentService.createVariable(request)
    }

  def updateEnvironment(request: Environment, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Edit)) {
      environmentService.update(request)
    }

  def updateEnvironmentVariable(request: EnvironmentVariable, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Edit)) {
      environmentService.updateVariable(request)
    }

  def findEnvironment(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      environmentService.find()
    }

  def findEnvironmentVariable(name: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      environmentService.findVariable(name)
    }

  def deleteEnvironment(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Delete)) {
      environmentService.delete()
    }

  def deleteEnvironmentVariable(name: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Delete)) {
      environmentService.deleteVariable(name)
    }
}

object EnvironmentActor {

  case class UpdateEnvironment(request: Environment, user: Option[LoggedUser])

  case class UpdateEnvironmentVariable(request: EnvironmentVariable, user: Option[LoggedUser])

  case class CreateEnvironmentVariable(request: EnvironmentVariable, user: Option[LoggedUser])

  case class CreateEnvironment(request: Environment, user: Option[LoggedUser])

  case class DeleteEnvironment(user: Option[LoggedUser])

  case class ExportData(user: Option[LoggedUser])

  case class ImportData(data: EnvironmentData, user: Option[LoggedUser])

  case class FindEnvironment(user: Option[LoggedUser])

  case class FindEnvironmentVariable(name: String, user: Option[LoggedUser])

  case class DeleteEnvironmentVariable(name: String, user: Option[LoggedUser])

  type Response = Try[Unit]

  type ResponseEnvironment = Try[Environment]

  type ResponseEnvironmentData = Try[EnvironmentData]

  type ResponseEnvironmentVariable = Try[EnvironmentVariable]
}
