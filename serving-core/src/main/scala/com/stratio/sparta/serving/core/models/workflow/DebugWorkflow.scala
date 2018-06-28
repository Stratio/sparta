/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.core.models.{DebugResults, ErrorsManagement}
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.enumerators.WhenError
import com.stratio.sparta.core.workflow.step.{InputStep, OutputStep}
import com.stratio.sparta.serving.core.constants.{AppConstant, SparkConstant}
import com.stratio.sparta.serving.core.models.EntityAuthorization
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowExecutionMode}

case class DebugWorkflow(
                          workflowOriginal: Workflow,
                          workflowDebug: Option[Workflow],
                          result: Option[DebugResults]
                        ) extends EntityAuthorization {

  private val inputClassName = "DummyDebugInputStep"
  private val inputPrettyClassName = "DummyDebug"
  private val outputDebugName = "output_debug"
  private val sparkWindow = s"${AppConstant.DebugSparkWindow}ms"
  private val debugOutputNode = NodeGraph(
    name = outputDebugName,
    stepType = OutputStep.StepType,
    className = "DebugOutputStep",
    classPrettyName = "Debug",
    arity = Seq(NodeArityEnum.NullaryToNullary, NodeArityEnum.NaryToNullary),
    writer = WriterGraph(),
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
      status = None,
      execution = None,
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
      stopGracefully = Option(true),
      stopGracefulTimeout = Option(JsoneyString(AppConstant.maxDebugTimeout.toString)),
      checkpointSettings = streamingSettings.checkpointSettings.copy(
        checkpointPath = JsoneyString("/tmp/debug-checkpoint"),
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
    import workflowOriginal.pipelineGraph

    val newNodes = pipelineGraph.nodes.flatMap { node =>
      node.stepType.toLowerCase match {
        case InputStep.StepType => Option(node.copy(
          className = s"$inputClassName${workflowOriginal.executionEngine}",
          classPrettyName = inputPrettyClassName)
        )
        case OutputStep.StepType => None
        case _ => Option(node)
      }
    } :+ debugOutputNode
    val outputs = pipelineGraph.nodes.flatMap { node =>
      if (node.stepType.equalsIgnoreCase(OutputStep.StepType)) Option(node.name)
      else None
    }
    val outputEdges = newNodes.flatMap { node =>
      node.stepType.toLowerCase match {
        case OutputStep.StepType => None
        case _ => Option(EdgeGraph(origin = node.name, destination = outputDebugName))
      }
    }
    val newEdges = pipelineGraph.edges.filter(edge =>
      !outputs.contains(edge.origin) && !outputs.contains(edge.destination)) ++ outputEdges

    pipelineGraph.copy(nodes = newNodes, edges = newEdges)
  }

}