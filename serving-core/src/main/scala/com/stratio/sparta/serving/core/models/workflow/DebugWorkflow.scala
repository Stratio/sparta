/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.sdk.models.{DebugResults, ErrorsManagement}
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.sdk.enumerators.WhenError
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputStep}
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum

case class DebugWorkflow(
                          workflowOriginal: Workflow,
                          workflowDebug: Option[Workflow],
                          result: Option[DebugResults]
                        ) {

  //private val inputClassName = "DummyDebugInputStep"
  private val inputClassName = "TestInputStep"
  //private val inputPrettyClassName = "DummyDebug"
  private val inputPrettyClassName = "Test"
  private val outputDebugName = "output_debug"
  private val sparkWindow = s"${AppConstant.DebugSparkWindow}ms"
  private val maxDebugTimeout = "5000"
  private val debugOutputNode = NodeGraph(
    name = outputDebugName,
    stepType = OutputStep.StepType,
    className = "DebugOutputStep",
    classPrettyName = "Debug",
    arity = Seq(NodeArityEnum.NullaryToNullary, NodeArityEnum.NaryToNullary),
    writer = WriterGraph(),
    configuration = Map(
      "printMetadata" -> JsoneyString("true"),
      "printData" -> JsoneyString("true")
    )
  )

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
    workflowOriginal.settings.global.copy(mesosConstraint = None, mesosConstraintOperator = None)

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
      stopGracefulTimeout = Option(JsoneyString(maxDebugTimeout)),
      checkpointSettings = streamingSettings.checkpointSettings.copy(enableCheckpointing = false)
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
      master = JsoneyString(AppConstant.SparkLocalMaster),
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
          classPrettyName = inputPrettyClassName,
          configuration = Map(
            "event" -> JsoneyString("""{"name":"jc", "age":33}""""),
            "numEvents" -> JsoneyString("1")
          )
        )
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
        case OutputStep.StepType | InputStep.StepType => None
        case _ => Option(EdgeGraph(origin = node.name, destination = outputDebugName))
      }
    }
    val newEdges = pipelineGraph.edges.filter(edge =>
      !outputs.contains(edge.origin) && !outputs.contains(edge.destination)) ++ outputEdges

    pipelineGraph.copy(nodes = newNodes, edges = newEdges)
  }

}