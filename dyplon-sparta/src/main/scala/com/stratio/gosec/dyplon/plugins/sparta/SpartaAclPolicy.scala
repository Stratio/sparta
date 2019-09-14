/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.gosec.dyplon.plugins.sparta

import com.stratio.gosec.api.auth.acl.FileSystemAclPolicy
import com.stratio.gosec.api.config.{ConfigComponent, Constant}
import com.stratio.gosec.dyplon.model._

trait SpartaAclPolicy extends FileSystemAclPolicy {

  this: ConfigComponent =>
  lazy val posixEnable : Boolean = config.getString(Constant.PosixEnableProperty).toBoolean

  //scalastyle:off
  override def checkByAction(
                              resourceHierarchy: Seq[com.stratio.gosec.dyplon.model.Resource],
                              aclsUser: Seq[Acl]
                            ): PartialFunction[Action, Boolean] = {
    case View => checkGenericAction(resourceHierarchy, aclsUser, View)
    case Manage => checkGenericAction(resourceHierarchy, aclsUser, Manage)
    case Status => checkGenericAction(resourceHierarchy, aclsUser, Status)
    case Download => checkGenericAction(resourceHierarchy, aclsUser, Download)
    case Upload => checkGenericAction(resourceHierarchy, aclsUser, Upload)
    case Describe => checkGenericAction(resourceHierarchy, aclsUser, Describe)
    case Select => checkGenericAction(resourceHierarchy, aclsUser, Select)
    case Edit => checkGenericAction(resourceHierarchy, aclsUser, Edit)
    case Write => checkGenericAction(resourceHierarchy, aclsUser, Write)
    case Delete => checkGenericAction(resourceHierarchy, aclsUser, Delete)
    case Create => checkGenericAction(resourceHierarchy, aclsUser, Create)
  }

  //scalastyle:off

  override def hasValidInstance(resource: com.stratio.gosec.dyplon.model.Resource, acl: Acl): Boolean =
    resource == acl.resource ||
      (!posixEnable && acl.recursive && resource.resourceType == acl.resource.resourceType && checkNameWithAclName(resource.name, acl.resource.name))

  private def checkNameWithAclName(resourceName: String, aclName: String): Boolean = {
    if(aclName.length == resourceName.length)
      aclName == resourceName
    else if(aclName.length > resourceName.length)
      false
    else resourceName.startsWith(aclName) && (resourceName(aclName.length) == '/' || resourceName(aclName.length) == '*')
  }

  private def checkGenericAction(
                                  resourceHierarchy: Seq[com.stratio.gosec.dyplon.model.Resource],
                                  acls: Seq[Acl],
                                  action: com.stratio.gosec.dyplon.model.Action
                                ): Boolean =
    (!posixEnable || checkPermissionAllResources(resourceHierarchy.filter(r => r.name != "/"), acls, View)) &&
      getPermission(acls, resourceHierarchy.filter(r => r.name != "/"), action)
}