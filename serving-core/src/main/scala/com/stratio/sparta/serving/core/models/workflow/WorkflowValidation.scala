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

case class WorkflowValidation(valid: Boolean, messages: Seq[String]) {

  def this(valid: Boolean) = this(valid, messages = Seq.empty[String])

  def this() = this(valid = true)

  def validateNonEmptyNodes(implicit workflow: Workflow): WorkflowValidation =
    if (workflow.pipelineGraph.nodes.size >= 2 && valid) this
    else this.copy(valid = false, messages = messages :+ "The workflow must contains almost two nodes")

  def validateNonEmptyEdges(implicit workflow: Workflow): WorkflowValidation =
    if (workflow.pipelineGraph.edges.nonEmpty && valid) this
    else this.copy(valid = false, messages = messages :+ "The workflow must contains almost one relation")

  def validateEdgesNodesExists(implicit workflow: Workflow): WorkflowValidation = {
    val nodesNames = workflow.pipelineGraph.nodes.map(_.name)
    val wrongEdges = workflow.pipelineGraph.edges.flatMap(edge =>
      if(nodesNames.contains(edge.origin) && nodesNames.contains(edge.destination)) None
      else Option(edge)
    )

    if(wrongEdges.isEmpty && valid) this
    else this.copy(
      valid = false,
      messages = messages :+ s"The workflow has relations that not exists in nodes: ${wrongEdges.mkString(" , ")}"
    )
  }
}