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

import com.stratio.sparta.sdk.workflow.step.OutputStep
import com.stratio.sparta.serving.core.helpers.WorkflowHelper
import com.stratio.sparta.serving.core.models.enumerators.ArityValueEnum.{ArityValue, _}
import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum.{NodeArity, _}
import com.stratio.sparta.serving.core.services.GroupService
import org.apache.curator.framework.CuratorFramework

import scala.util.Try
import scalax.collection.{Graph, GraphTraversal}
import scalax.collection.GraphPredef
import scalax.collection.GraphEdge.DiEdge
import scalax.collection.GraphTraversal.Visitor
import scalax.collection.edge.WDiEdge

case class WorkflowValidation(valid: Boolean, messages: Seq[String]) {

  def this(valid: Boolean) = this(valid, messages = Seq.empty[String])

  def this() = this(valid = true)

  val InputMessage = "input"
  val OutputMessage = "output"

  /** A group name is correct if and only if:
    * 1) there are no two or more consecutives /
    * 2) it starts with /home (the default root)
    * 3) the group name has no Uppercase letters or other special characters (except / and -)
    */
  val regexGroups = "^(?!.*[/]{2}.*$)(^(/home)+(/)*([a-z0-9-/]*)$)"

  // A Workflow name should not contain special characters and Uppercase letters (because of DCOS deployment)
  val regexName = "^[a-z0-9-]*"

  def validateGroupName(implicit workflow: Workflow, curator: Option[CuratorFramework]): WorkflowValidation = {
    if (curator.isEmpty) this
    else {
      val groupService = new GroupService(curator.get)
      val groupInZk = groupService.findByID(workflow.group.id.get).toOption
      if (groupInZk.isDefined && groupInZk.get.name.matches(regexGroups))
        this
      else {
        val msg = messages :+ "The workflow group not exists or is invalid"
        copy(valid = false, messages = msg)
      }
    }
  }

  def validateErrorOutputs(implicit workflow: Workflow): WorkflowValidation = {
    val errorOutputNodes = workflow.pipelineGraph.nodes.filter { node =>
      val isSinkOutput = Try(node.configuration(WorkflowHelper.OutputStepErrorProperty).toString.toBoolean)
        .getOrElse(false)
      node.stepType.toLowerCase == OutputStep.StepType && isSinkOutput
    }.map(_.name)

    val saveErrors = workflow.settings.errorsManagement.transactionsManagement.sendToOutputs.flatMap { action =>
      if (!errorOutputNodes.contains(action.outputStepName)) {
        Option(action.outputStepName)
      } else None
    }

    if (saveErrors.nonEmpty) {
      copy(
        valid = false,
        messages = messages :+ s"The workflow has output errors that not exists in nodes. ${saveErrors.mkString(", ")}"
      )
    } else this
  }

  def validateName(implicit workflow: Workflow): WorkflowValidation = {
    if (workflow.name.nonEmpty && workflow.name.matches(regexName)) this
    else copy(valid = false, messages = messages :+ "The workflow name is empty or invalid")
  }

  def validateNonEmptyNodes(implicit workflow: Workflow): WorkflowValidation =
    if (workflow.pipelineGraph.nodes.size >= 2) this
    else copy(valid = false, messages = messages :+ "The workflow must contains at least two nodes")

  def validateNonEmptyEdges(implicit workflow: Workflow): WorkflowValidation =
    if (workflow.pipelineGraph.edges.nonEmpty) this
    else copy(valid = false, messages = messages :+ "The workflow must contains at least one relation")

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

  def validateExistenceCorrectPath(implicit workflow: Workflow, graph: Graph[NodeGraph, DiEdge]): WorkflowValidation = {

    def node(outer: NodeGraph): graph.NodeT = (graph get outer).asInstanceOf[graph.NodeT]

    val inputNodes: Seq[graph.NodeT] = workflow.pipelineGraph.nodes
      .filter(node => node.stepType.equals("Input")).map(node(_))
    val outputNodes: Seq[graph.NodeT] = workflow.pipelineGraph.nodes
      .filter(node => node.stepType.equals("Output")).map(node(_))

    val path = {
      for {in <- inputNodes.toStream
           out <- outputNodes.toStream
      } yield {
        in.pathTo(out)(Visitor.empty)
      }
    } exists (_.isDefined)


    if (path) this else this.copy(
      valid = false,
      messages = messages :+ s"The workflow has no I->O path"
    )
  }


  def validateArityOfNodes(implicit workflow: Workflow, graph: Graph[NodeGraph, DiEdge]): WorkflowValidation = {
    workflow.pipelineGraph.nodes.foldLeft(this) { case (lastValidation, node) =>
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
      case NullaryToNullary =>
        combineWithAnd(
          validateDegree(inDegree, Nullary, InvalidMessage(nodeGraph.name, InputMessage)),
          validateDegree(outDegree, Nullary, InvalidMessage(nodeGraph.name, OutputMessage))
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