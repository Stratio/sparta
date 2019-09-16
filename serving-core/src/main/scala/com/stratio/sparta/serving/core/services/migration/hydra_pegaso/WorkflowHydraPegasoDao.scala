/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration.hydra_pegaso

import com.stratio.sparta.serving.core.dao.DaoUtils
import com.stratio.sparta.serving.core.daoTables._
import com.stratio.sparta.serving.core.models.workflow.migration.WorkflowHydraPegaso
import org.apache.ignite.IgniteCache
import slick.ast.BaseTypedType

//scalastyle:off
trait WorkflowHydraPegasoDao extends DaoUtils {

  import profile.api._

  type SpartaEntity = WorkflowHydraPegaso
  type SpartaTable = WorkflowHydraPegasoTable
  type Id = String

  lazy val table = TableQuery[WorkflowHydraPegasoTable]

  def $id(table: WorkflowHydraPegasoTable): Rep[Id] = table.id

  def baseTypedType: BaseTypedType[Id] = implicitly[BaseTypedType[Id]]

  implicit lazy val cache: IgniteCache[String, WorkflowHydraPegaso] = throw new UnsupportedOperationException("Cache not enabled")

}
