/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.join

import com.stratio.sparta.plugin.enumerations.TableSide
import com.stratio.sparta.plugin.enumerations.TableSide.TableSide
import com.stratio.sparta.core.properties.ValidatingPropertyMap._

case class JoinReturnColumn(tableSide: TableSide, column: String, alias: Option[String]) {

  def toSql(leftTable: String, rightTable: String): String =
    s"${if (tableSide == TableSide.LEFT) leftTable else rightTable}.$column" +
      s" ${alias.notBlank.map(a => s"AS $a").getOrElse("")}"
}
