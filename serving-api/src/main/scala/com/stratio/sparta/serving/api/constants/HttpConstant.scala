/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.constants

import scala.util.Properties

object HttpConstant {

  final val SpartaRootPath = Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME", "sparta")
  final val DebugWorkflowsPath = "debug"
  final val DriverPath = "driver"
  final val PluginsPath = "plugins"
  final val WorkflowsPath = "workflows"
  final val TemplatePath = "template"
  final val WorkflowStatusesPath = "workflowStatuses"
  final val ExecutionsPath = "workflowExecutions"
  final val GroupsPath = "groups"
  final val EnvironmentPath = "environment"
  final val SwaggerPath = "swagger"
  final val ConfigPath = "config"
  final val AppStatus = "appStatus"
  final val CrossdataPath = "crossdata"
  final val AppInfoPath = "appInfo"
  final val MetadataPath = "metadata"
  final val ExecutionsHistoryPath = "workflowExecutionsHistory"
  final val StatusHistoryPath = "workflowStatusHistory"
  final val NotFound = 400
  final val NotFoundMessage = "Not Found"
}