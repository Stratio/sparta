/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.security

import com.typesafe.config.ConfigFactory

trait SpartaSecurityManager {

  lazy val dyplonConfig = ConfigFactory.load().getConfig("api")

  def start(): Unit

  def stop(): Unit

  def authorize(userId: String, resource: Resource, action: Action, hierarchy: Boolean): Boolean

  def audit(auditEvent: AuditEvent): Unit
}