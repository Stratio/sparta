/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.enumerators

object SaveModeEnum extends Enumeration {

  type SpartaSaveMode = Value

  val Append = Value("Append")
  val ErrorIfExists = Value("ErrorIfExists")
  val Ignore = Value("Ignore")
  val Overwrite = Value("Overwrite")
  val Upsert = Value("Upsert")
  val Delete = Value("Delete")

  val allSaveModes = Seq(Append, ErrorIfExists, Ignore, Overwrite, Upsert, Delete)
}
