/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.enumerations

object TrimType extends Enumeration{

  type TrimType = Value

  val TRIM_LEFT = Value("TRIM_LEFT")
  val TRIM_RIGHT = Value("TRIM_RIGHT")
  val TRIM_BOTH = Value("TRIM_BOTH")
}
