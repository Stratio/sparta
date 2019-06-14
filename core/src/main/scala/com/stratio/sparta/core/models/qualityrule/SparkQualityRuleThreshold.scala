/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.models.qualityrule

import com.stratio.sparta.core.models.SpartaQualityRuleThreshold

import scala.util.Try

class SparkQualityRuleThreshold(spartaQualityRuleThreshold: SpartaQualityRuleThreshold, rowsSatisfyingQR: Long, totalRows: Long) {

  lazy val valid: Boolean  = allowedOperations.contains(spartaQualityRuleThreshold.operation) && Try(rowsSatisfyingQR.toDouble/totalRows).isSuccess

  val hundredDouble = 100.00

  val allowedOperations: Set[String] = Set("=", ">", ">=", "<", "<=")

  def isThresholdSatisfied : Boolean = {
    spartaQualityRuleThreshold.`type` match {
      case "%" => applyOperations(spartaQualityRuleThreshold.operation,
        rowsSatisfyingQR.toDouble/totalRows,
        spartaQualityRuleThreshold.value/hundredDouble)
      case "abs" => applyOperations(spartaQualityRuleThreshold.operation,
        rowsSatisfyingQR.toDouble,
        spartaQualityRuleThreshold.value)
    }
  }

   def applyOperations(operation: String, firstOperand: Double, thresholdValue: Double): Boolean = {
     operation match {
       case "=" => roundTwoDecimalPositions(firstOperand) == roundTwoDecimalPositions(thresholdValue)
       case ">" => roundTwoDecimalPositions(firstOperand) > roundTwoDecimalPositions(thresholdValue)
       case ">=" => roundTwoDecimalPositions(firstOperand) >= roundTwoDecimalPositions(thresholdValue)
       case "<" => roundTwoDecimalPositions(firstOperand) < roundTwoDecimalPositions(thresholdValue)
       case "<=" => roundTwoDecimalPositions(firstOperand) <= roundTwoDecimalPositions(thresholdValue)
     }
   }


  override def toString: String = {
    val commonString = s"${spartaQualityRuleThreshold.operation} ${spartaQualityRuleThreshold.value}"
    spartaQualityRuleThreshold.`type` match {
      case "abs" => commonString
      case "%" => s"$commonString %"
    }
  }

  private def roundTwoDecimalPositions(numberToRound: Double): Double = math.round(numberToRound * hundredDouble * hundredDouble)/hundredDouble * hundredDouble
}
