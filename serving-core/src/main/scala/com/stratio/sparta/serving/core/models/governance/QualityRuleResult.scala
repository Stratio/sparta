/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.governance

import org.joda.time.DateTime

case class QualityRuleResult(
                              id: Option[String] = None,
                              executionId: String,
                              dataQualityRuleId: String,
                              numTotalEvents: Long,
                              numPassedEvents: Long,
                              numDiscardedEvents: Long,
                              metadataPath: String,
                              transformationStepName: String,
                              outputStepName: String,
                              satisfied: Boolean,
                              successfulWriting: Boolean,
                              conditionThreshold: String,
                              sentToApi: Boolean = false,
                              warning: Boolean = true,
                              qualityRuleName: String,
                              conditionsString: String,
                              globalAction: String,
                              creationDate: Option[DateTime] = None
                             )