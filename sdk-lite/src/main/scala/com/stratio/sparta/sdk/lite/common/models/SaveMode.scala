/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.sdk.lite.common.models

sealed trait SaveMode

// Overwrite to make explicit that toString should match with SaveModeEnum's toString
case object Append extends SaveMode{
  override def toString: String = "Append"
}
case object ErrorIfExists extends SaveMode{
  override def toString: String = "ErrorIfExists"
}
case object Overwrite extends SaveMode{
  override def toString: String = "Overwrite"
}
case object Ignore extends SaveMode{
  override def toString: String = "Ignore"
}
case object Upsert extends SaveMode{
  override def toString: String = "Upsert"
}
case object Delete extends SaveMode{
  override def toString: String = "Delete"
}