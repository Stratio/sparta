/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.models.qualityrule

case class SparkQualityRuleResults(
                                    dataQualityRuleId: String,
                                    numTotalEvents: Long,
                                    numPassedEvents: Long,
                                    numDiscardedEvents: Long,
                                    metadataPath: String,
                                    transformationStepName: String,
                                    outputStepName: String,
                                    satisfied: Boolean,
                                    condition : String,
                                    successfulWriting: Boolean,
                                    qualityRuleName: String,
                                    conditionsString: String,
                                    globalAction :String
                                  ){

  override def toString: String = {
    s"dataQualityRuleId: $dataQualityRuleId,  qualityRuleName: $qualityRuleName, numTotalEvents: $numTotalEvents, numPassedEvents: $numPassedEvents, numDiscardedEvents: $numDiscardedEvents, metadataPath: $metadataPath, transformationStepName: $transformationStepName, outputStepName: $outputStepName, satisfied: $satisfied, condition : $condition , successfulWriting: $successfulWriting, conditionsString : $conditionsString, globalAction: $globalAction"
  }
}
