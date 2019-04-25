/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.helpers

import com.stratio.sparta.core.models.SpartaQualityRuleThresholdActionType


object QualityRuleActionEnum extends Enumeration {

  type action = Value

  type QualityRuleActionEnum = Value

  val ActionPassthrough = Value(1, "ACT_PASS")
  val ActionCopy        = Value(2, "ACT_COP")
  val ActionMove        = Value(3, "ACT_MOV")

  def findOutputMode(mapPassedThresholds: Map[Long,(Boolean, String, SpartaQualityRuleThresholdActionType)]): QualityRuleActionEnum =
    mapPassedThresholds.map { case (_, (isPassing, _, action)) => {
      if (isPassing) ActionPassthrough else withName(action.`type`)
    } }.max
}

