/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.core.enumerators.WhenError
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{DebugResults, ErrorsManagement}
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.workflow.step.{InputStep, OutputStep}
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.GraphHelper.createGraph
import com.stratio.sparta.serving.core.models.EntityAuthorization
import com.stratio.sparta.serving.core.models.enumerators.{DataType, NodeArityEnum, WorkflowExecutionMode}

import scalax.collection.Graph
import scalax.collection.GraphTraversal.{Parameters, Successors}
import scalax.collection.edge.LDiEdge

//scalastyle:off
case class DebugWorkflow(
                          id: Option[String] = None,
                          workflowOriginal: Workflow,
                          workflowDebug: Option[Workflow],
                          result: Option[DebugResults]
                        ) extends EntityAuthorization {

  private val inputClassName = "DummyDebugInputStep"
  private val inputPrettyClassName = "DummyDebug"
  private val outputDebugName = "output_debug"
  private val outputDebugNameDiscards = "output_debug_discards"
  private val sparkWindow = s"${AppConstant.DebugSparkWindow}ms"
  private val debugOutputNode = NodeGraph(
    name = outputDebugName,
    stepType = OutputStep.StepType,
    className = "DebugOutputStep",
    classPrettyName = "Debug",
    arity = Seq(NodeArityEnum.NullaryToNullary, NodeArityEnum.NaryToNullary),
    writer = Option(WriterGraph()),
    configuration = Map.empty
  )
  private val debugOutputNodeDiscard = NodeGraph(
    name = outputDebugNameDiscards,
    stepType = OutputStep.StepType,
    className = "DebugOutputStep",
    classPrettyName = "Debug",
    arity = Seq(NodeArityEnum.NullaryToNullary, NodeArityEnum.NaryToNullary),
    writer = Option(WriterGraph()),
    configuration = Map.empty
  )

  def authorizationId: String = workflowOriginal.authorizationId

  def transformToWorkflowRunnable: Workflow = {
    workflowOriginal.copy(
      settings = workflowOriginal.settings.copy(
        global = globalDebugSettings(),
        streamingSettings = streamingDebugSettings(),
        errorsManagement = errorManagementDebugSettings(),
        sparkSettings = sparkDebugSettings()
      ),
      name = s"${workflowOriginal.name}-debug",
      uiSettings = None,
      pipelineGraph = pipelineGraphDebug(),
      debugMode = Option(true)
    )
  }

  private[workflow] def globalDebugSettings(): GlobalSettings =
    workflowOriginal.settings.global.copy(
      mesosConstraint = None,
      mesosConstraintOperator = None,
      executionMode = WorkflowExecutionMode.local)

  private[workflow] def streamingDebugSettings(): StreamingSettings = {
    import workflowOriginal.settings.streamingSettings

    streamingSettings.copy(
      window = JsoneyString(sparkWindow),
      remember = None,
      backpressure = None,
      backpressureInitialRate = None,
      backpressureMaxRate = None,
      blockInterval = Option(JsoneyString(sparkWindow)),
      stopGracefully = Option(false),
      checkpointSettings = streamingSettings.checkpointSettings.copy(
        checkpointPath = JsoneyString(s"file:///tmp/debug-checkpoint-${workflowOriginal.name}-${System.currentTimeMillis()}"),
        autoDeleteCheckpoint = true
      )
    )
  }

  private[workflow] def errorManagementDebugSettings(): ErrorsManagement = {
    import workflowOriginal.settings.errorsManagement

    errorsManagement.copy(
      genericErrorManagement = errorsManagement.genericErrorManagement.copy(WhenError.Error),
      transactionsManagement = errorsManagement.transactionsManagement.copy(
        sendToOutputs = Seq.empty,
        sendStepData = false,
        sendPredecessorsData = false,
        sendInputData = false
      )
    )
  }

  private[workflow] def sparkDebugSettings(): SparkSettings = {
    import workflowOriginal.settings.sparkSettings

    sparkSettings.copy(
      sparkKerberos = false,
      sparkMesosSecurity = false,
      killUrl = None,
      submitArguments = SubmitArguments(),
      sparkConf = sparkSettings.sparkConf
    )
  }

  private[workflow] def pipelineGraphDebug(): PipelineGraph = {
    import com.stratio.sparta.serving.core.helpers.GraphHelperImplicits._
    import workflowOriginal.pipelineGraph

    val graph: Graph[NodeGraph, LDiEdge] = createGraph(workflowOriginal)
    val nodeOrdering = graph.NodeOrdering((nodeX, nodeY) =>
      if (nodeX.priority != nodeY.priority)
        nodeX.priority.compare(nodeY.priority) * -1
      else nodeY.name.compare(nodeX.name) * -1
    )
    val parameters = Parameters(direction = Successors)
    val nodesInGraph = workflowOriginal.pipelineGraph.nodes.filter(_.stepType.toLowerCase == InputStep.StepType)
      .sorted
      .flatMap { inputNode =>
        val inNodeGraph = graph.get(inputNode)
        inNodeGraph.outerNodeTraverser(parameters).withOrdering(nodeOrdering).toList.reverse.flatMap { node =>
          node.stepType.toLowerCase match {
            case InputStep.StepType => Option(node.copy(
              className = s"$inputClassName${workflowOriginal.executionEngine}",
              classPrettyName = inputPrettyClassName)
            )
            case OutputStep.StepType => None
            case _ => Option(node)
          }
        }
      }.reverse
    var index = "a"
    val nodesNames = nodesInGraph.flatMap { node =>
      val nodes = Seq(
        node.name -> s"${index}_${debugOutputNode.name}_${node.name}",
        SdkSchemaHelper.discardTableName(node.name) -> s"${index}_${debugOutputNodeDiscard.name}_${node.name}")
      index = index + "a"
      nodes
    }.toMap
    val outputNodes = nodesInGraph.flatMap { node =>
      Seq(
        debugOutputNode.copy(name = nodesNames(node.name)),
        debugOutputNodeDiscard.copy(name = nodesNames(SdkSchemaHelper.discardTableName(node.name)))
      )
    }
    val newNodes = nodesInGraph ++ outputNodes
    val outputs = pipelineGraph.nodes.flatMap { node =>
      if (node.stepType.equalsIgnoreCase(OutputStep.StepType)) Option(node.name)
      else None
    }
    val outputEdges = newNodes.flatMap { node =>
      node.stepType.toLowerCase match {
        case OutputStep.StepType => Seq.empty
        case _ =>
          node.supportedDataRelations match {
            case Some(relations) => relations.map { dataType =>
              if (dataType == DataType.ValidData)
                EdgeGraph(
                  origin = node.name,
                  destination = nodesNames(node.name),
                  dataType = Option(dataType)
                )
              else EdgeGraph(
                origin = node.name,
                destination = nodesNames(SdkSchemaHelper.discardTableName(node.name)),
                dataType = Option(dataType)
              )
            }
            case None => Seq(EdgeGraph(
              origin = node.name,
              destination = nodesNames(node.name),
              dataType = Option(DataType.ValidData)
            ))
          }
      }
    }
    val newEdges = pipelineGraph.edges.filter(edge =>
      !outputs.contains(edge.origin) && !outputs.contains(edge.destination)) ++ outputEdges

    pipelineGraph.copy(nodes = newNodes, edges = newEdges)
  }

}