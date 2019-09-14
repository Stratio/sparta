/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.security

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.typesafe.config.ConfigFactory

import scala.util.{Properties, Try}

trait SpartaSecurityManager {

  lazy val spartaVersion = AppConstant.version
  lazy val serviceName = AppConstant.ConfigAppName
  lazy val spartaAuthorizer = AppConstant.ConfigAppName
  lazy val spartaInstance = Try(dyplonApiConfig.getString("instance")).getOrElse {
    Properties.envOrElse(SpartaDyplonPluginInstance, AppConstant.spartaServerMarathonAppId)
  }
  lazy val spartaTenant = Try(dyplonPluginConfig.getString("tenant")).toOption.orElse {
    Properties.envOrNone(SpartaDyplonTenantName)
  }

  lazy val dyplonApiConfig = ConfigFactory.load().getConfig("api")
  lazy val dyplonPluginConfig = ConfigFactory.load().getConfig("plugin")
  lazy val dyplonFacadeConfig = ConfigFactory.load().getConfig("facade")
  lazy val dyplonConfig = ConfigFactory.load().getConfig("dyplon")

  def start(): Unit

  def stop(): Unit

  def authorize(userId: String, resource: Resource, action: Action, hierarchy: Boolean): Boolean

  def audit(auditEvent: AuditEvent): Unit
}