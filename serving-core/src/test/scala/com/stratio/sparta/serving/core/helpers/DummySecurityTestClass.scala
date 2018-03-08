/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.helpers

import com.stratio.sparta.security._

class DummySecurityTestClass extends SpartaSecurityManager {

  override def start(): Unit = {}

  override def stop(): Unit = {}

  override def authorize(userId: String, resource: Resource, action: Action): Boolean =
    (userId, resource, action) match {

      case ("1234", _, _) => true
      case ("1111", Resource(WorkflowResource, "workflow"), Create) => false
      case ("1111", Resource(WorkflowResource, "workflow"), _) => false
      case ("1111", Resource(PluginResource, "plugin"), _) => false
      case (_, _, _) => true
    }

  override def audit(auditEvent: AuditEvent): Unit = {}

}
