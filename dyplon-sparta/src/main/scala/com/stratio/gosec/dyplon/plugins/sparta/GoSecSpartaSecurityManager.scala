/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.gosec.dyplon.plugins.sparta

import java.io.InputStream
import scala.io.Source
import scala.util.Try

import com.typesafe.config.ConfigRenderOptions

import com.stratio.gosec.api.Systems._
import com.stratio.gosec.api.audit.repository.{AuditRepositoryComponentImpl, LogAuditRepositoryComponentImpl}
import com.stratio.gosec.api.audit.service.AuditServiceComponentImpl
import com.stratio.gosec.api.auth.acl.DefaultAclPolicy
import com.stratio.gosec.api.config.{ConfigComponentImpl, PluginConfig}
import com.stratio.gosec.dyplon.audit.Authorizer
import com.stratio.gosec.dyplon.core.ZkRegisterCheckerComponentImpl
import com.stratio.gosec.dyplon.model.PluginInstance
import com.stratio.gosec.dyplon.plugins.sparta.SpartaToGoSecConversions._
import com.stratio.sparta.security.{Action, AuditEvent, Resource, SpartaSecurityManager}

class GoSecSpartaSecurityManager extends ConfigComponentImpl
  with SpartaSecurityManager
  with Authorizer
  with DefaultPolicyService
  with DefaultGroupService
  with DefaultUserService
  with DefaultPluginService
  with ZkRegisterCheckerComponentImpl
  with AuditServiceComponentImpl
  with LogAuditRepositoryComponentImpl
  with AuditRepositoryComponentImpl
  with DefaultAclPolicy
  with DefaultInstanceService{

  import com.stratio.gosec.dyplon.model.dsl.PluginInstanceDsl._

  logger.debug(s"[DYPLON_SPARTA] Sparta properties: ${config.root().render(ConfigRenderOptions.concise())}")

  override lazy val instance = parseSpartaPlugin

  private def parseSpartaPlugin: PluginInstance = {
    val stream: InputStream = getClass.getResourceAsStream("/manifest-sparta.json")
    val json: String = Source.fromInputStream(stream).mkString
    val plugin = json.toPlugin
    val tenantName = Try(config.getString(PluginConfig.Tenant)).toOption
    plugin.copy(instance = Some(config.getString(PluginConfig.PluginInstance)), tenant = tenantName)
  }

  override def start: Unit = {

    logger.info(s"[DYPLON_SPARTA] Starting ${instance.`type`} authorizer")
    auditService.start
    SpartaSystem.SpartaLifeCycleSystem(instance).init
  }

  override def stop: Unit = {
    logger.info(s"Stopping ${instance.`type`} authorizer")
    auditService.stop
  }

  override def authorize(userId: String,
                         resource: Resource,
                         action: Action): Boolean = {
    auth(userId, resource, action, EmptyAuditAddresses, hierarchy = false)
  }

  override def audit(auditEvent: AuditEvent): Unit = auditService.save(auditEvent)
}
