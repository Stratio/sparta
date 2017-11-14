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

import com.stratio.sparta.serving.core.models.enumerators.ArityValueEnum.{ArityValue, _}
import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum.{NodeArity, _}

import scalax.collection.Graph
import scalax.collection.GraphEdge.DiEdge

case class WorkflowValidation(valid: Boolean, messages: Seq[String]) {

  def this(valid: Boolean) = this(valid, messages = Seq.empty[String])

  def this() = this(valid = true)

  val InputMessage = "input"
  val OutputMessage = "output"

  def validateNonEmptyNodes(implicit workflow: Workflow): WorkflowValidation =
    if (workflow.pipelineGraph.nodes.size >= 2) this
    else copy(valid = false, messages = messages :+ "The workflow must contains almost two nodes")

  def validateNonEmptyEdges(implicit workflow: Workflow): WorkflowValidation =
    if (workflow.pipelineGraph.edges.nonEmpty) this
    else copy(valid = false, messages = messages :+ "The workflow must contains almost one relation")

  def validateEdgesNodesExists(implicit workflow: Workflow): WorkflowValidation = {
    val nodesNames = workflow.pipelineGraph.nodes.map(_.name)
    val wrongEdges = workflow.pipelineGraph.edges.flatMap(edge =>
      if (nodesNames.contains(edge.origin) && nodesNames.contains(edge.destination)) None
      else Option(edge)
    )

    if (wrongEdges.isEmpty || workflow.pipelineGraph.edges.isEmpty) this
    else copy(
      valid = false,
      messages = messages :+ s"The workflow has relations that not exists in nodes: ${wrongEdges.mkString(" , ")}"
    )
  }

  def validateGraphIsAcyclic(implicit workflow: Workflow, graph: Graph[NodeGraph, DiEdge]): WorkflowValidation = {
    val cycle = graph.findCycle

    if (cycle.isEmpty || workflow.pipelineGraph.edges.isEmpty) this
    else copy(
      valid = false,
      messages = messages :+ s"The workflow contains one or more cycles" + {
        if (cycle.isDefined)
          s"${": " + cycle.get.nodes.toList.map(node => node.value.asInstanceOf[NodeGraph].name).mkString(",")}"
        else "!"
      }
    )
  }

  def validateArityOfNodes(implicit workflow: Workflow, graph: Graph[NodeGraph, DiEdge]): WorkflowValidation = {
    val arityNodesValidation = workflow.pipelineGraph.nodes.foldLeft(this) { case (lastValidation, node) =>
      val nodeInGraph = graph.get(node)
      val inDegree = nodeInGraph.inDegree
      val outDegree = nodeInGraph.outDegree
      val validation = {
        if (node.arity.nonEmpty)
          node.arity.foldLeft(new WorkflowValidation(valid = false)) { case (lastArityValidation, arity) =>
            combineWithOr(lastArityValidation, validateArityDegrees(node, inDegree, outDegree, arity))
          }
        else new WorkflowValidation()
      }

      combineWithAnd(lastValidation, validation)
    }

    combineWithAnd(this, arityNodesValidation)
  }

  private[workflow] def combineWithAnd(first: WorkflowValidation, second: WorkflowValidation): WorkflowValidation =
    if (first.valid && second.valid) new WorkflowValidation()
    else WorkflowValidation(valid = false, messages = first.messages ++ second.messages)

  private[workflow] def combineWithOr(first: WorkflowValidation, second: WorkflowValidation): WorkflowValidation =
    if (first.valid || second.valid) new WorkflowValidation()
    else WorkflowValidation(valid = false, messages = first.messages ++ second.messages)

  private[workflow] def validateArityDegrees(
                                              nodeGraph: NodeGraph,
                                              inDegree: Int,
                                              outDegree: Int,
                                              arity: NodeArity
                                            ): WorkflowValidation =
    arity match {
      case NullaryToNary =>
        combineWithAnd(
          validateDegree(inDegree, Nullary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
      case UnaryToUnary =>
        combineWithAnd(
          validateDegree(inDegree, Unary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Unary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
      case UnaryToNary =>
        combineWithAnd(
          validateDegree(inDegree, Unary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
      case BinaryToNary =>
        combineWithAnd(
          validateDegree(inDegree, Binary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
      case NaryToNullary =>
        combineWithAnd(
          validateDegree(inDegree, Nary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nullary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
      case NaryToNary =>
        combineWithAnd(
          validateDegree(inDegree, Nary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nary, InvalidMessage(nodeGraph.name, OutputMessage))
        )
    }

  private[workflow] def validateDegree(
                                        degree: Int,
                                        arityDegree: ArityValue,
                                        invalidMessage: InvalidMessage
                                      ): WorkflowValidation =
    arityDegree match {
      case Nullary =>
        validateDegreeValue(degree, 0, invalidMessage)
      case Unary =>
        validateDegreeValue(degree, 1, invalidMessage)
      case Binary =>
        validateDegreeValue(degree, 2, invalidMessage)
      case Nary =>
        if (degree > 0) new WorkflowValidation()
        else WorkflowValidation(
          valid = false,
          messages = Seq(s"Invalid number of relations, the node ${invalidMessage.nodeName} has $degree" +
            s" ${invalidMessage.relationType} relations and support 1 to N")
        )
    }

  private[workflow] def validateDegreeValue(
                                             degree: Int,
                                             arityDegree: Int,
                                             invalidMessage: InvalidMessage
                                           ): WorkflowValidation =
    if (degree == arityDegree) new WorkflowValidation()
    else WorkflowValidation(
      valid = false,
      messages = Seq(s"Invalid number of relations, the node ${invalidMessage.nodeName} has $degree" +
        s" ${invalidMessage.relationType} relations and support $arityDegree")
    )

  case class InvalidMessage(nodeName: String, relationType: String)

}