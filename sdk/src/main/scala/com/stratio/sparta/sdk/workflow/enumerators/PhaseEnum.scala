/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.sdk.workflow.enumerators

object PhaseEnum extends Enumeration {

  val Context = Value("Context")
  val Setup = Value("Setup")
  val Cleanup = Value("Cleanup")
  val Input = Value("Input")
  val Transform = Value("Transform")
  val Output = Value("Output")
  val Write = Value("Write")
  val Execution = Value("Execution")
  val Launch = Value("Launch")
  val Stop = Value("Stop")
  val Validate = Value("Validate")

}