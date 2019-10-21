/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.gosec.dyplon.plugins.sparta

import java.net.InetAddress
import java.util.Date
import scala.language.implicitConversions
import scala.util.Try

import com.stratio.gosec.api.Systems.UserSystem
import com.stratio.gosec.api.config.ConfigComponentImpl
import com.stratio.gosec.dyplon.model._
import com.stratio.sparta.security.{AuditResult, FailAR, SuccessAR, Select => SpartaSelect, Action => SpartaAction, AuditEvent => SpartaAuditEvent, Create => SpartaCreate, Delete => SpartaDelete, Describe => SpartaDescribe, Download => SpartaDownload, Edit => SpartaEdit, Resource => SpartaResource, Status => SpartaStatus, Upload => SpartaUpload, View => SpartaView, Manage => SpartaManage}


object SpartaToGoSecConversions {

  val SpartaServiceName = "sparta"
  val SpartaPolicy = None
  val EmptyAuditAddresses = AuditAddresses(getLocalIp, "unknown")
  lazy val userSystem = new UserSystem()

  implicit def resourceConversion(spartaResource: SpartaResource): Resource =
    Resource(
      service = SpartaServiceName,
      instances = Seq(RetrieveSpartaInstance.SpartaInstanceName),
      resourceType = spartaResource.resourceType.name(),
      name = spartaResource.name
    )

  implicit def actionConversion(spartaAction: SpartaAction): Action = spartaAction match {
    case SpartaView => View
    case SpartaCreate => Create
    case SpartaDelete => Delete
    case SpartaEdit => Edit
    case SpartaStatus => Status
    case SpartaDownload => Download
    case SpartaUpload => Upload
    case SpartaDescribe => Describe
    case SpartaSelect => Select
    case SpartaManage => Manage
  }

  implicit def resultConversion(auditResult: AuditResult): Result = auditResult match {
    case FailAR => Fail
    case SuccessAR => Success
  }

  implicit def auditConversion(spartaEvent: SpartaAuditEvent): AuditEvent =
    AuditEvent(
      currentDate(),
      getUserFromLDAP(spartaEvent.userId),
      spartaEvent.resource,
      spartaEvent.action.toString,
      spartaEvent.result,
      EmptyAuditAddresses,
      Auth,
      SpartaPolicy,
      spartaEvent.impersonation
    )

  private def currentDate() = Time(new Date())

  private def getLocalIp: String = {
    if (RetrieveSpartaInstance.localHostNameConfig.isSuccess)
      RetrieveSpartaInstance.localHostNameConfig.get
    else
      InetAddress.getLocalHost.getHostAddress
  }

  private def getUserFromLDAP(userId: String): User = userSystem.userService.getUserWithGroups(userId)
}

object RetrieveSpartaInstance extends ConfigComponentImpl {

  val SpartaInstanceName = config.getString("api.instance")
  val localHostNameConfig = Try(config.getString("api.local.hostname"))
}
