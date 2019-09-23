/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.constants

import com.stratio.sparta.serving.core.constants.{AppConstant, MarathonConstant}

import scala.util.{Properties, Try}

object HttpConstant {

  final val SpartaRootPath = AppConstant.tenantIdInstanceNameWithDefault
  final val DebugWorkflowsPath = "debug"
  final val DriverPath = "driver"
  final val PluginsPath = "plugins"
  final val WorkflowsPath = "workflows"
  final val TemplatePath = "template"
  final val ParameterListPath = "paramList"
  final val WorkflowStatusesPath = "workflowStatuses"
  final val ExecutionsPath = "workflowExecutions"
  final val GroupsPath = "groups"
  final val ScheduledWorkflowTasksPath = "scheduledWorkflowTasks"
  final val GlobalParametersPath = "globalParameters"
  final val SwaggerPath = "swagger"
  final val ConfigPath = "config"
  final val AppStatus = "appStatus"
  final val CrossdataPath = "crossdata"
  final val AppInfoPath = "appInfo"
  final val MetadataPath = "metadata"
  final val MlModelsPath = "mlModels"
  final val QualityRuleResultsPath = "qualityRuleResults"
  final val NotFound = 400
  final val NotFoundMessage = "Not Found"


  type Response = Try[Unit]
  type ResponseAny = Try[Any]
  type ResponseBoolean = Try[Boolean]

}