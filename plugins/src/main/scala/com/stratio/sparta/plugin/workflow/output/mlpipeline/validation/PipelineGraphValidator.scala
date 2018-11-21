/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline.validation

import com.stratio.sparta.serving.core.models.workflow.{EdgeGraph, NodeGraph, PipelineGraph}

import scala.annotation.tailrec
import scala.util.Try

import ValidationErrorMessages.{moreThanOneEnd, moreThanOneStart, unconnectedNodes, moreThanOneOutput}
class PipelineGraphValidator(val graph: PipelineGraph) {

  private val nodes: Seq[NodeGraph] = graph.nodes
  private val edges: Seq[EdgeGraph] = graph.edges

  private val startNodes: Seq[NodeGraph] = nodes filter { inDegree(_) == 0 }

  private val endNodes: Seq[NodeGraph] = nodes filter { outDegree(_) == 0 }

  private def inDegree(node: NodeGraph): Int = edges count { _.destination == node.name }

  private def outDegree(node: NodeGraph): Int = edges count { _.origin == node.name }

  private def inputs(node: NodeGraph): Seq[NodeGraph] = {
    val names: Seq[String] = edges filter { _.destination == node.name } map { _.origin }
    nodes filter { names contains _.name }
  }

  private def outputs(node: NodeGraph): Seq[NodeGraph] = {
    val names: Seq[String] = edges filter { _.origin == node.name } map { _.destination }
    nodes filter { names contains _.name }
  }

  private def getNextNode(node: NodeGraph): Option[NodeGraph] = {
    val outs = outputs(node)
    if (outs.isEmpty)
      None
    else if (outs.size == 1)
      Some(outs.head)
    else
      throw new Exception(moreThanOneOutput(node.name))
  }

  @tailrec
  private def getOrderedNodeSeq(nodeSeq: Seq[NodeGraph]): Seq[NodeGraph] = {
      getNextNode(nodeSeq.head) match {
      // we have reached the end
      case None => {
        // sanity check: this is the unique end node
        if (nodeSeq.head.name == endNodes.head.name)
          nodeSeq.reverse
        else
          throw new Exception(moreThanOneEnd)
      }
      // we continue iterating
      case Some(next) => getOrderedNodeSeq(next +: nodeSeq)
    }
  }

  def validate: Try[Seq[NodeGraph]] = Try {
    // check there is only one start node
    if (startNodes.size != 1)
      throw new Exception(moreThanOneStart)

    // check there is only one end node
    else if (endNodes.size != 1)
      throw new Exception(moreThanOneEnd)

    // build ordered sequence of nodes. While building we validate there is only one output per node
    else {
        val nodeSeq = getOrderedNodeSeq(startNodes)
        // sanity check
        if (nodeSeq.size != nodes.size)
          throw new Exception(unconnectedNodes)
        else
          nodeSeq
      }
  }

}
