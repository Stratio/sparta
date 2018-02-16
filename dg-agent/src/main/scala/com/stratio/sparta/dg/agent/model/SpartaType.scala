/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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