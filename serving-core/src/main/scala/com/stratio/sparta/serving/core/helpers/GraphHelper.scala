/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.helpers

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.models.workflow.{EdgeGraph, NodeGraph, Workflow}

import scalax.collection.Graph
import scalax.collection.GraphEdge.DiEdge
import scalax.collection.GraphPredef._

object GraphHelper extends SLF4JLogging {

  def createGraph(workflow: Workflow): Graph[NodeGraph, DiEdge] =
    Graph.from(workflow.pipelineGraph.nodes, creteEdges(workflow.pipelineGraph.nodes, workflow.pipelineGraph.edges))

  def creteEdges(nodes: Seq[NodeGraph], edges: Seq[EdgeGraph]): Seq[DiEdge[NodeGraph]] =
    edges.flatMap { edge =>
      (nodes.find(_.name == edge.origin), nodes.find(_.name == edge.destination)) match {
        case (Some(nodeOrigin), Some(nodeDestination)) =>
          Option(nodeOrigin ~> nodeDestination)
        case _ =>
          log.warn(s"Impossible to create relation in graph, $edge. Origin or destination are not present in nodes.")
          None
      }
    }

}
