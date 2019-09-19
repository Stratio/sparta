/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.dao

import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.serving.core.daoTables.PlannedQualityRuleTable
import org.apache.ignite.IgniteCache
import slick.ast.BaseTypedType

//scalastyle:off
trait PlannedQualityRuleDao extends DaoUtils{

  import profile.api._

  type SpartaEntity = SpartaQualityRule
  type SpartaTable = PlannedQualityRuleTable
  type Id = Long

  lazy val table = TableQuery[PlannedQualityRuleTable]

  override val initializationOrder: Int = 4

  def $id(table: PlannedQualityRuleTable): Rep[Id] = table.id

  def baseTypedType: BaseTypedType[Id] = implicitly[BaseTypedType[Id]]

  implicit lazy val cache: IgniteCache[String, SpartaQualityRule] = throw new UnsupportedOperationException("Cache not enabled")

  override def getSpartaEntityId(entity: SpartaEntity): String = entity.id.toString
}
