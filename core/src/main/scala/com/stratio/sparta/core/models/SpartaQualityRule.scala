/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.models

case class SpartaQualityRule(id: Long,
                             metadataPath: String,
                             name: String,
                             qualityRuleScope: String,
                             logicalOperator: String,
                             enable: Boolean,
                             threshold: SpartaQualityRuleThreshold,
                             predicates: Seq[SpartaQualityRulePredicate],
                             stepName: String,
                             outputName: String,
                             executionId: Option[String] = None
                            ){
  override def toString: String = {
    s"id : $id, metadataPath: $metadataPath, " +
      s"enable: $enable, stepName: $stepName, " +
      s"outputName: $outputName, predicates: ${predicates.mkString(",")} ," +
      s"threshold: $threshold" + s"${executionId.fold(""){ exId => s" executionId: $exId" }}"
  }
}

case class SpartaQualityRulePredicate(`type`: Option[String] = None,
                                      order: Int,
                                      operands: Seq[String],
                                      field: String,
                                      operation: String){
  override def toString: String = {
    s"IF column ($field) ${prettyPrintOperation(operation)} ${operands.mkString(",")}"
  }

  private def prettyPrintOperation(operation: String) : String = {
    val leaveUnchanged = Set("IS NOT NULL", "IS NULL", "IN", "NOT IN", "LIKE", "NOT LIKE", "REGEX", "IS DATE", "IS NOT DATE", "IS TIMESTAMP", "IS NOT TIMESTAMP")

    operation match{
      case op if leaveUnchanged.contains(op.toUpperCase) => op.toLowerCase
      case "=" => "is equal to"
      case ">" => "is greater than"
      case ">=" => "is greater than or equal to"
      case "<" => "is less than"
      case "<=" => "is less than or equal to"
      case "<>" => "is not equal to"
    }
  }
}

case class SpartaQualityRuleThreshold(value: Double,
                                      operation: String,
                                      `type`: String,
                                      actionType: SpartaQualityRuleThresholdActionType){
  override def toString: String = {
    s"passedEvents $operation $value"
  }
}

case class SpartaQualityRuleThresholdActionType(`type`: String, path: Option[String] = None)



