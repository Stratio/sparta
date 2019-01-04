/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration.orion

import com.stratio.sparta.serving.core.dao.DaoUtils
import org.apache.ignite.IgniteCache
import slick.ast.BaseTypedType
import com.stratio.sparta.serving.core.daoTables._
import com.stratio.sparta.serving.core.models.workflow.migration.TemplateElementOrion


//scalastyle:off
trait TemplateOrionDao extends DaoUtils {

  import profile.api._

  type SpartaEntity = TemplateElementOrion
  type SpartaTable = TemplateOrionTable
  type Id = String

  lazy val table = TableQuery[TemplateOrionTable]

  def $id(table: TemplateOrionTable): Rep[Id] = table.id

  def baseTypedType: BaseTypedType[Id] = implicitly[BaseTypedType[Id]]

  implicit lazy val cache: IgniteCache[String, TemplateElementOrion] = throw new UnsupportedOperationException("Cache not enabled")
}
