/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

object DtoModelImplicits {

  implicit def workFlowToDto(workflow: Workflow): WorkflowDto =
    WorkflowDto(
      id = workflow.id,
      name = workflow.name,
      description = workflow.description,
      settings = workflow.settings.global,
      nodes = workflow.pipelineGraph.nodes.map(nodeToDto),
      executionEngine = workflow.executionEngine,
      lastUpdateDate = workflow.lastUpdateDate,
      version = workflow.version,
      group = workflow.group.name,
      tags = workflow.tags
    )

  implicit def globalSettingsToDto(globalSettings: GlobalSettings): GlobalSettingsDto =
    GlobalSettingsDto(
      executionMode = globalSettings.executionMode,
      parametersLists = globalSettings.parametersLists,
      parametersUsed = globalSettings.parametersUsed
    )

  implicit def executionToDto(execution: WorkflowExecution): WorkflowExecutionDto =
    WorkflowExecutionDto(
      execution.id,
      execution.statuses,
      execution.genericDataExecution,
      execution.marathonExecution,
      execution.localExecution,
      execution.archived
    )

  implicit def marathonExecutionToDto(marathonExecution: Option[MarathonExecution]): Option[MarathonExecutionDto] = {
    marathonExecution.flatMap{marathonEx =>
      marathonEx.sparkURI.map(_ => MarathonExecutionDto(marathonEx.sparkURI))
    }
  }

  implicit def nodeToDto(node: NodeGraph): NodeGraphDto = NodeGraphDto(node.name, node.stepType)

  implicit def genericExecutionToDto(ge: GenericDataExecution): GenericDataExecutionDto =
    GenericDataExecutionDto(
      ge.executionMode,
      ge.executionContext,
      ge.workflow,
      ge.launchDate,
      ge.startDate,
      ge.endDate,
      ge.lastError,
      ge.name,
      ge.description
    )
}
