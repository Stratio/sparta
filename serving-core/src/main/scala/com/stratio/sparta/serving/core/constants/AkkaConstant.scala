/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.constants


object AkkaConstant {

  val TemplateActorName = "templateActor"
  val WorkflowActorName = "workflowActor"
  val ExecutionActorName = "executionActor"
  val ClusterLauncherActorName = "clusterLauncherActor"
  val LauncherActorName = "launcherActor"
  val MarathonLauncherActorName = "marathonLauncherActor"
  val PluginActorName = "pluginActor"
  val DriverActorName = "driverActor"
  val ControllerActorName = "controllerActor"
  val StatusActorName = "statusActor"
  val MarathonAppActorName = "marathonAppActor"
  val UpDownMarathonActor = "upDownMarathonActor"
  val ConfigActorName = "configurationActor"
  val MetadataActorName = "metadataActor"
  val CrossdataActorName = "crossdataActor"
  val NginxActorName = "nginxActor"
  val EnvironmentActorName = "environmentActor"
  val GroupActorName = "groupActor"
  val GroupApiActorName = "GroupApiActor"
  val ExecutionApiActorName = "ExecutionApiActor"
  val WorkflowApiActorName = "WorkflowApiActor"
  val DebugWorkflowApiActorName = "DebugWorkflowApiActor"
  val StatusApiActorName = "StatusApiActor"
  val StatusChangeActorName = "StatusChangeActor"
  val EnvironmentCleanerActorName= "EnvironmentCleanerActor"
  val InconsistentStatusCheckerActorName = "InconsistentStatusCheckerActor"
  val WorkflowHistoryExecutionApiActorName = "WorkflowHistoryExecutionApiActorName"

  val DefaultInstances = 3

  def cleanActorName(initialName: String): String = initialName.replace(" ", "_")
}
