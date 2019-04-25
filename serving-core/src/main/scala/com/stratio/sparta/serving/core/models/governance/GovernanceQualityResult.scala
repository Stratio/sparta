/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.governance

case class GovernanceQualityResult (actor: Actor,
                                    countRows: Long,
                                    id: Long = -1,
                                    metadataPath: String,
                                    quality_id: Long,
                                    queryRows: Long,
                                    result: Boolean,
                                    tenant: String
                                   )

case class Actor(executionId: String, actorType: String = "SPARTA")

case class Quality(id: Long)

object GovernanceQualityResult{

  def parseSpartaResult(spartaResult: QualityRuleResult, tenant: Option[String] = Some("NONE")) : GovernanceQualityResult =
    new GovernanceQualityResult(
      actor = Actor(spartaResult.executionId),
      countRows = spartaResult.numTotalEvents,
      metadataPath = spartaResult.metadataPath,
      quality_id = spartaResult.dataQualityRuleId.toLong,
      queryRows = spartaResult.numPassedEvents,
      result = spartaResult.satisfied,
      tenant = tenant.getOrElse("NONE")
    )
}