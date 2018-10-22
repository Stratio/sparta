/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.mlpipeline
import com.stratio.sparta.plugin.workflow.output.mlpipeline.validation.PipelineGraphValidator
import com.stratio.sparta.serving.core.models.workflow.{EdgeGraph, NodeGraph, PipelineGraph, WriterGraph}
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class MlPipelineGraphValidationTests extends FlatSpec with Matchers {
  import ExampleGraphs._


  trait WithPipelineGraphValidation {
    val graph: PipelineGraph
    lazy val validationResult: Try[Seq[NodeGraph]] = new PipelineGraphValidator(graph).validate
  }


  "PipelineGraphValidator" should "return success from a valid graph" in
    new WithPipelineGraphValidation {
      val graph: PipelineGraph = validGraph
      assert(validationResult.isSuccess)
    }

  "PipelineGraphValidator" should "return the nodes in the correct order from a valid graph" in
    new WithPipelineGraphValidation {
      val graph: PipelineGraph = validGraph
      assert(validationResult.isSuccess)
      val nodeSeq: Seq[NodeGraph] = validationResult.get
      nodeSeq.zipWithIndex.foreach {
        case (node, i) => assert(node.name == (i + 1).toString)
      }
    }

  "PipelineGraphValidator" should "return success from a graph with a single node" in
    new WithPipelineGraphValidation {
      val graph: PipelineGraph = singleNodeGraph
      assert(validationResult.isSuccess)
      assert(validationResult.get.size == 1)
    }

  "PipelineGraphValidator" should "return success from a graph with a two nodes" in
    new WithPipelineGraphValidation {
      val graph: PipelineGraph = twoNodesGraph
      assert(validationResult.isSuccess)
      assert(validationResult.get.size == 2)
    }

  "PipelineGraphValidator" should "return a failure from a graph with two start nodes" in
    new WithPipelineGraphValidation {
      val graph: PipelineGraph = twoStartsGraph
      assert(validationResult.isFailure)
      assert(validationResult.failed.get.getMessage ==
        "Pipeline Graph has more than one start node"
      )
    }

  "PipelineGraphValidator" should "return a failure from a graph with two end nodes" in
    new WithPipelineGraphValidation {
      val graph: PipelineGraph = twoEndsGraph
      assert(validationResult.isFailure)
      assert(validationResult.failed.get.getMessage ==
        "Pipeline Graph has more than one end node"
      )
    }


  "PipelineGraphValidator" should "return a failure from a graph with diamond shape" in
    new WithPipelineGraphValidation {
      val graph: PipelineGraph = diamondGraph
      assert(validationResult.isFailure)
      assert(validationResult.failed.get.getMessage ==
        "node '1' has more than one output"
      )
    }

  "PipelineGraphValidator" should "return a failure from a graph with a floating node" in
    new WithPipelineGraphValidation {
      val graph: PipelineGraph = floatingNodeGraph
      assert(validationResult.isFailure)
      assert(validationResult.failed.get.getMessage ==
        "Pipeline Graph has more than one start node"
      )
    }

  "PipelineGraphValidator"should
    "return a failure from a graph with a floating node with a loop" in
    new WithPipelineGraphValidation {
      val graph: PipelineGraph = floatingSelfConnected
      assert(validationResult.isFailure)
      assert(validationResult.failed.get.getMessage ==
        "There are some not connected nodes"
      )
    }

  "PipelineGraphValidator" should "return a failure from a graph with two linear pipelines" in
    new WithPipelineGraphValidation {
      val graph: PipelineGraph = doubleGraph
      assert(validationResult.isFailure)
      assert(validationResult.failed.get.getMessage ==
        "Pipeline Graph has more than one start node"
      )
    }

  "PipelineGraphValidator" should "return a failure from a graph containing a loop" in
    new WithPipelineGraphValidation {
      val graph: PipelineGraph = loopGraph
      assert(validationResult.isFailure)
      assert(validationResult.failed.get.getMessage ==
        "node '3' has more than one output"
      )
    }
}


object ExampleGraphs {

  private def nodeGraph(name: String): NodeGraph =
    NodeGraph(name,"", "", "", Seq(), WriterGraph())

  private def edgeGraph(origin: String, destination: String) =
    EdgeGraph(origin, destination, None)

  private def nodesFromNames(names: Seq[String]) = names.map(nodeGraph)
  private def edgesFromTuples(edges: Seq[(String, String)]) =
    edges.map(edge => edgeGraph(edge._1, edge._2))
  private def makeGraph(names: Seq[String], edges: Seq[(String, String)]): PipelineGraph =
    PipelineGraph(nodesFromNames(names), edgesFromTuples(edges))

  val validGraph: PipelineGraph = makeGraph(
    Seq("1", "2", "3", "4"),
    Seq("1"->"2", "2"->"3", "3"->"4")
  )
  val singleNodeGraph: PipelineGraph = makeGraph(Seq("1"), Seq())
  val twoNodesGraph: PipelineGraph = makeGraph(Seq("1", "2"), Seq("1" -> "2"))
  val twoStartsGraph: PipelineGraph = makeGraph(
    Seq("start1", "start2", "end"),
    Seq("start1" -> "end", "start2" -> "end")
  )
  val twoEndsGraph: PipelineGraph = makeGraph(
    Seq("end1", "end2", "start"),
    Seq("start" -> "end1", "start" -> "end2")
  )
  val diamondGraph: PipelineGraph = makeGraph(
    Seq("start", "1", "2a", "3a", "2b", "3b", "4", "end"),
    Seq("start" -> "1",
      "1"->"2a", "2a"->"3a", "3a" -> "4",
      "1"->"2b", "2b"->"3b", "3b" -> "4",
      "4"->"end"
    )
  )
  val floatingNodeGraph: PipelineGraph = makeGraph(
    Seq("1", "2", "3", "floating"),
    Seq("1" -> "2", "2" -> "3")
  )
  val floatingSelfConnected: PipelineGraph = makeGraph(
    Seq("1", "2", "3", "floating"),
    Seq("1" -> "2", "2" -> "3", "floating" -> "floating")
  )
  val doubleGraph: PipelineGraph = makeGraph(
    Seq("1", "2", "3", "a", "b", "c"),
    Seq("1"->"2", "2"->"3", "a"->"b", "b"->"c")
  )
  val loopGraph: PipelineGraph = makeGraph(
    Seq("1", "2", "3", "4"),
    Seq("1"->"2", "2"->"3", "3"->"2", "3"->"4")
  )
}