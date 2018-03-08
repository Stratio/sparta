/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.dg.agent.model
import scala.util.Try

import com.stratio.governance.commons.agent.model.metadata.CustomType
import com.stratio.sparta.serving.core.helpers.InfoHelper

object SpartaType {

    lazy val UNDEFINED = "UNDEFINED"
    val agentVersion = Try(s"dg-sparta-agent-${InfoHelper.getAppInfo.pomVersion}").getOrElse(UNDEFINED)
    val serverVersion = Try(InfoHelper.getAppInfo.pomVersion).getOrElse(UNDEFINED)

    val TENANT =  SpartaType(SpartaTenantMetadata.getClass.getTypeName)
    val WORKFLOW =  SpartaType(SpartaWorkflowMetadata.getClass.getTypeName)
    val INPUT =  SpartaType(SpartaInputMetadata.getClass.getTypeName)
    val OUTPUT =  SpartaType(SpartaOutputMetadata.getClass.getTypeName)
    val TRANSFORMATION =  SpartaType(SpartaTransformationMetadata.getClass.getTypeName)
    val STATUS =  SpartaType(SpartaWorkflowStatusMetadata.getClass.getTypeName)
}


case class SpartaType(value: String) extends CustomType {
  override val provider = "SPARTA"
}