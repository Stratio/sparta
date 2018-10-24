/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.dao

import org.apache.ignite.IgniteCache
import slick.ast.BaseTypedType
import slick.jdbc.PostgresProfile

import com.stratio.sparta.core.models.ResultStep
import com.stratio.sparta.serving.core.daoTables._
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection

//scalastyle:off
trait DebugResultDao extends DaoUtils {
  import profile.api._

  type SpartaEntity = ResultStep
  type SpartaTable = DebugResultStepTable
  type Id = String

  lazy val table = TableQuery[DebugResultStepTable]

  def $id(table: DebugResultStepTable): Rep[Id] = table.id

  def baseTypedType: BaseTypedType[Id] = implicitly[BaseTypedType[Id]]

  lazy val resultTable = TableQuery[DebugResultStepTable]

  implicit lazy val cache: IgniteCache[String, ResultStep] = throw new UnsupportedOperationException("")

  override def getSpartaEntityId(entity: ResultStep): String = entity.id

}

class DebugResultPostgresDao extends DebugResultDao {

  override val profile = PostgresProfile

  override val db = JdbcSlickConnection.getDatabase

}
