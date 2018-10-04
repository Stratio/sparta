/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow.migration

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.workflow.{GlobalSettings, Settings, Workflow}

object MigrationModelImplicits {

  implicit def cassiopeaWorkflowToAndromeda(workflowCassiopeia: WorkflowCassiopeia): WorkflowAndromeda =
    WorkflowAndromeda(
      id = workflowCassiopeia.id,
      name = workflowCassiopeia.name,
      description = workflowCassiopeia.description,
      settings = workflowCassiopeia.settings,
      pipelineGraph = workflowCassiopeia.pipelineGraph,
      executionEngine = workflowCassiopeia.executionEngine,
      uiSettings = workflowCassiopeia.uiSettings,
      creationDate = workflowCassiopeia.creationDate,
      lastUpdateDate = workflowCassiopeia.lastUpdateDate,
      version = workflowCassiopeia.version,
      group = workflowCassiopeia.group,
      tags = workflowCassiopeia.tags,
      status = None,
      execution = None,
      debugMode = None,
      versionSparta = Option(AppConstant.AndromedaVersion)
    )

  implicit def cassiopeaSettingsToAndromeda(cassiopeaSettings: SettingsCassiopea): SettingsAndromeda =
    SettingsAndromeda(
      global = cassiopeaSettings.global,
      streamingSettings = cassiopeaSettings.streamingSettings,
      sparkSettings = cassiopeaSettings.sparkSettings,
      errorsManagement = cassiopeaSettings.errorsManagement
    )

  implicit def andromedaWorkflowToOrion(workflowAndromeda: WorkflowAndromeda): Workflow =
    Workflow(
      id = workflowAndromeda.id,
      name = workflowAndromeda.name,
      description = workflowAndromeda.description,
      settings = workflowAndromeda.settings,
      pipelineGraph = workflowAndromeda.pipelineGraph,
      executionEngine = workflowAndromeda.executionEngine,
      uiSettings = workflowAndromeda.uiSettings,
      creationDate = workflowAndromeda.creationDate,
      lastUpdateDate = workflowAndromeda.lastUpdateDate,
      version = workflowAndromeda.version,
      group = workflowAndromeda.group,
      tags = workflowAndromeda.tags,
      debugMode = None,
      versionSparta = Option(AppConstant.version),
      parametersUsedInExecution = None,
      executionId = None,
      groupId = workflowAndromeda.group.id
    )

  implicit def andromedaSettingsToOrion(andromedaSettings: SettingsAndromeda): Settings =
    Settings(
      global = andromedaSettings.global,
      streamingSettings = andromedaSettings.streamingSettings,
      sparkSettings = andromedaSettings.sparkSettings,
      errorsManagement = andromedaSettings.errorsManagement
    )

  implicit def cassiopeaGlobalSettingsToOrion(cassiopeaGlobalSettings: GlobalSettingsCassiopea): GlobalSettings =
    GlobalSettings(
      executionMode = cassiopeaGlobalSettings.executionMode,
      userPluginsJars = cassiopeaGlobalSettings.userPluginsJars,
      preExecutionSqlSentences = cassiopeaGlobalSettings.initSqlSentences,
      postExecutionSqlSentences = Seq.empty,
      addAllUploadedPlugins = cassiopeaGlobalSettings.addAllUploadedPlugins,
      mesosConstraint = cassiopeaGlobalSettings.mesosConstraint,
      mesosConstraintOperator = cassiopeaGlobalSettings.mesosConstraintOperator,
      parametersLists = Seq(AppConstant.EnvironmentParameterListName),
      parametersUsed = Seq.empty,
      udafsToRegister = Seq.empty,
      udfsToRegister = Seq.empty
    )


}
