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

package com.stratio.sparta.serving.core.helpers

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.workflow.step.{InputStep, TransformStep}
import com.stratio.sparta.serving.core.models.workflow.{EdgeGraph, NodeGraph}

import scalax.collection.Graph
import scalax.collection.GraphEdge.DiEdge
import scalax.collection.GraphPredef._

object GraphHelper extends SLF4JLogging {

  def creteEdges(nodes: Seq[NodeGraph], edges: Seq[EdgeGraph]): Seq[DiEdge[NodeGraph]] =
    edges.flatMap { edge =>
      (nodes.find(_.name == edge.origin), nodes.find(_.name == edge.destination)) match {
        case (Some(nodeOrigin), Some(nodeDestination)) => Option(nodeOrigin ~> nodeDestination)
        case _ =>
          log.warn(s"Impossible to create relation in graph, $edge. Origin or destination are not present in nodes.")
          None
      }
    }

  //scalastyle:off
  def getGraphOrdering(graph: Graph[NodeGraph, DiEdge]): graph.NodeOrdering =
    graph.NodeOrdering((nodeX, nodeY) => (nodeX.`type`, nodeY.`type`) match {
      case (x, _) if x.contains(InputStep.ClassSuffix) => 1
      case (x, y) if !x.contains(InputStep.ClassSuffix) && y.contains(InputStep.ClassSuffix) => -1
      case (x, y) if x.contains(TransformStep.ClassSuffix) && y.contains(TransformStep.ClassSuffix) =>
        if (graph.get(nodeX).diPredecessors.forall(_.`type`.contains(InputStep.ClassSuffix))) 1
        else if (graph.get(nodeY).diPredecessors.forall(_.`type`.contains(InputStep.ClassSuffix))) -1
        else {
          val xPredecessors = graph.get(nodeX).diPredecessors.count(_.`type`.contains(TransformStep.ClassSuffix))
          val yPredecessors = graph.get(nodeY).diPredecessors.count(_.`type`.contains(TransformStep.ClassSuffix))

          xPredecessors.compare(yPredecessors) * -1
        }
      case _ => 0
    })

  //scalastyle:on
}
