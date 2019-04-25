/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.governance

import com.stratio.sparta.core.models.{SpartaQualityRule, SpartaQualityRulePredicate, SpartaQualityRuleThreshold, SpartaQualityRuleThresholdActionType}

object QualityRuleParser {

  implicit class Parse2QualityRule(governanceQualityRule: GovernanceQualityRule) {

    def parse(stepName: String, outputName: String): Seq[SpartaQualityRule] = {
      governanceQualityRule.content.map(x => {
        val id = x.id
        val metadataPath = x.metadataPath
        val name = x.name
        val qualityRuleScope = x.catalogAttributeType
        val logicalOperator = x.parameters.filter.`type`
        val enabled = x.active
        val thresholdValue = x.resultUnit
        val thresholdOperation = x.resultOperation
        val thresholdOperationType = x.resultOperationType
        val thresholdActionType = SpartaQualityRuleThresholdActionType(`type` = x.resultAction.`type`)

        val threshold = SpartaQualityRuleThreshold(thresholdValue, thresholdOperation, thresholdOperationType, thresholdActionType)

        val predicates: Seq[SpartaQualityRulePredicate] = x.parameters.filter.cond.map(x => {
          SpartaQualityRulePredicate(
            x.`type`,
            x.order,
            x.param.getOrElse(Seq.empty[String]),
            x.attribute,
            x.operation)
        })

        SpartaQualityRule(
          id,
          metadataPath,
          name,
          qualityRuleScope,
          logicalOperator,
          enabled,
          threshold,
          predicates,
          stepName,
          outputName)
      })
    }
  }
}
