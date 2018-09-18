/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.dao

import slick.ast.BaseTypedType
import com.stratio.sparta.serving.core.daoTables._
import com.stratio.sparta.serving.core.models.parameters.ParameterVariable
//scalastyle:off
trait GlobalParametersDao extends DaoUtils {

  import profile.api._

  type SpartaEntity = ParameterVariable
  type SpartaTable = GlobalParametersTable
  type Id = String

  lazy val table = TableQuery[GlobalParametersTable]

  def $id(table: GlobalParametersTable): Rep[Id] = table.name

  def baseTypedType: BaseTypedType[Id] = implicitly[BaseTypedType[Id]]

}
