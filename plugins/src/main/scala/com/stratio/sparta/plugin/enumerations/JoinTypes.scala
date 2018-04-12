/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.enumerations

import org.apache.spark.sql.catalyst.plans._

object JoinTypes extends Enumeration {

  type JoinTypes = Value
  val CROSS, INNER, LEFT, RIGHT, FULL, LEFT_ONLY, RIGHT_ONLY, LEFT_RIGHT_ONLY = Value

  def joinTypeToSql(joinType: JoinTypes) : String = {
    joinType match {
      case INNER => Inner.sql
      case LEFT => LeftOuter.sql
      case RIGHT => RightOuter.sql
      case FULL => FullOuter.sql
      case CROSS => Cross.sql
      case LEFT_RIGHT_ONLY => FullOuter.sql
      case LEFT_ONLY => LeftAnti.sql
      case RIGHT_ONLY => LeftAnti.sql
    }
  }
}

