/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.EntityAuthorization

import scala.annotation.meta.field
import org.apache.ignite.cache.query.annotations.QuerySqlField

object Group {
  def isValid(group: Group): Boolean = {
    val regexGroups= "^(?!.*[/]{2}.*$)(^(/home)+(/)*([a-z0-9-/]*)$)"
    group.name.matches(regexGroups)
  }

  def isSystemGroup(group: Group): Boolean = group.equals(AppConstant.DefaultSystemGroup)

}

case class Group(
                  id: Option[String],
                  name: String) extends EntityAuthorization {

  def authorizationId: String = name

}

