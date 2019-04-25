/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.dao

import com.stratio.sparta.serving.core.daoTables.QualityRuleResultTable
import com.stratio.sparta.serving.core.models.governance.QualityRuleResult
import org.apache.ignite.IgniteCache
import slick.ast.BaseTypedType

//scalastyle:off
trait QualityRuleResultDao extends DaoUtils{

  import profile.api._

  type SpartaEntity = QualityRuleResult
  type SpartaTable = QualityRuleResultTable
  type Id = String

  lazy val table = TableQuery[QualityRuleResultTable]

  override val initializationOrder = 4

  def $id(table: QualityRuleResultTable): Rep[Id] = table.id

  def baseTypedType: BaseTypedType[Id] = implicitly[BaseTypedType[Id]]

  implicit lazy val cache: IgniteCache[String, QualityRuleResult] = throw new UnsupportedOperationException("Cache not enabled")

  override def getSpartaEntityId(entity: SpartaEntity): String = entity.id.get
}
