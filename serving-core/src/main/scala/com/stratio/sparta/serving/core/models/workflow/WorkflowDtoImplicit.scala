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

package com.stratio.sparta.serving.core.models.workflow

object WorkflowDtoImplicit {

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
      tags = workflow.tags,
      status = workflow.status)

  private[sparta] def nodeToDto(node: NodeGraph): NodeGraphDto = NodeGraphDto(node.name, node.stepType)
}
