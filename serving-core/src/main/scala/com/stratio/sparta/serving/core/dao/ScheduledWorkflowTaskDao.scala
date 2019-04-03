/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.dao

import com.stratio.sparta.serving.core.daoTables._
import com.stratio.sparta.serving.core.models.orchestrator.ScheduledWorkflowTask
import org.apache.ignite.IgniteCache
import slick.ast.BaseTypedType

//scalastyle:off
trait ScheduledWorkflowTaskDao extends DaoUtils {

  import profile.api._
  import CustomColumnTypes._

  type SpartaEntity = ScheduledWorkflowTask
  type SpartaTable = ScheduledWorkflowTaskTable
  type Id = String

  lazy val table = TableQuery[ScheduledWorkflowTaskTable]

  override val initializationOrder = 4

  def $id(table: ScheduledWorkflowTaskTable): Rep[Id] = table.id

  def baseTypedType: BaseTypedType[Id] = implicitly[BaseTypedType[Id]]

  implicit lazy val cache: IgniteCache[String, ScheduledWorkflowTask] = throw new UnsupportedOperationException("Cache not enabled")

}
