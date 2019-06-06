/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.authorization

object GosecUserConstants {
  val InfoNameTag = "cn"
  val InfoIdTag = "id"
  val InfoMailTag = "mail"
  val InfoRolesTag = "roles"
  val InfoGroupIDTag = "gidNumber"
  val InfoGroupsTag = "groups"
  val TenantTag = "tenant"
  val DummyMail = "email@email.com"
  val AnonymousUser = GosecUser("*", "Anonymous", DummyMail,"0", Seq.empty, Seq.empty)
  val AllowedRoles = Seq("FullAdministrator","management_admin","sparta","sparta_zk")
}
