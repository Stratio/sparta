/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.enumerators

object QualityRuleTypeEnum extends Enumeration {

  type QualityRuleType = Value

  val Planned = Value("Planned")
  val Reactive = Value("Reactive")

  implicit def extractQRType(qrType: String): QualityRuleType =
    qrType match {
      case s if s matches "(i?)EXE_PRO" => Planned
      case _ => Reactive
    }
}
