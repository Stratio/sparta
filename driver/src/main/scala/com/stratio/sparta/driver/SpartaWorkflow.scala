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

package com.stratio.sparta.driver

import java.io.Serializable

import akka.util.Timeout
import com.stratio.sparta.driver.error._
import com.stratio.sparta.driver.exception.DriverException
import com.stratio.sparta.driver.factory.SparkContextFactory._
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.utils.AggregationTimeUtils
import com.stratio.sparta.sdk.workflow.step._
import com.stratio.sparta.sdk.{ContextBuilder, DistributedMonad}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.helpers.GraphHelper._
import com.stratio.sparta.serving.core.helpers.WorkflowHelper
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, PhaseEnum, Workflow}
import com.stratio.sparta.serving.core.utils.CheckpointUtils
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.{Duration, StreamingContext}

import scala.concurrent.duration._
import scala.util.Try
import scalax.collection.Graph
import scalax.collection.GraphEdge.DiEdge
import scalax.collection.GraphTraversal.{Parameters, Predecessors}

case class SpartaWorkflow[Underlying[Row] : ContextBuilder](workflow: Workflow, curatorFramework: CuratorFramework)
  extends CheckpointUtils with ZooKeeperError with DistributedMonadImplicits {

  // Clear last error if it was saved in Zookeeper
  clearError()

  private val apiTimeout = Try(SpartaConfig.getDetailConfig.get.getInt("timeout"))
    .getOrElse(AppConstant.DefaultApiTimeout) - 1

  implicit val timeout: Timeout = Timeout(apiTimeout.seconds)

  private val classpathUtils = WorkflowHelper.classpathUtils
  private var steps = Seq.empty[GraphStep]

  /**
    * Execute the setup function associated to all the steps. Previously is mandatory execute the streamingStages
    * function because the steps variable is mutable and is initialized to empty value.
    */
  def setup(): Unit = {
    val phaseEnum = PhaseEnum.Setup
    val errorMessage = s"An error was encountered while executing the setup steps."
    val okMessage = s"Setup steps executed successfully."

    traceFunction(phaseEnum, okMessage, errorMessage) {
      steps.foreach(step => step.setUp())
    }
  }

  /**
    * Execute the cleanUp function associated to all the steps. Previously is mandatory execute the streamingStages
    * function because the steps variable is mutable and is initialized to empty value.
    */
  def cleanUp(): Unit = {
    val phaseEnum = PhaseEnum.Cleanup
    val errorMessage = s"An error was encountered while executing the cleanup steps."
    val okMessage = s"Cleanup steps executed successfully."

    traceFunction(phaseEnum, okMessage, errorMessage) {
      steps.foreach(step => step.cleanUp())
    }
  }

  /**
    * Initialize the Spark contexts, create the steps for setup and cleanup functions and execute the workflow.
    *
    * @return The streaming context created, is used by the desing pattern in the Spark Streaming Context creation
    */
  def stages(): Unit = {
    clearError()

    val xDSession = xdSessionInstance

    implicit val workflowContext = implicitly[ContextBuilder[Underlying]].buildContext(classpathUtils, xDSession) {
      /* Prepare Workflow Context variables with the Spark Contexts used in steps

        NOTE that his block will only run when the context builder for the concrete Underlying entity requires it,
        thus, DStreams won't cause the execution of this block. */
      val workflowCheckpointPath = Option(checkpointPathFromWorkflow(workflow))
        .filter(_ => workflow.settings.streamingSettings.checkpointSettings.enableCheckpointing)
      val window = AggregationTimeUtils.parseValueToMilliSeconds(workflow.settings.streamingSettings.window.toString)
      sparkStreamingInstance(Duration(window),
        workflowCheckpointPath.notBlank,
        workflow.settings.streamingSettings.remember.notBlank
      ) get
    }

    steps = workflow.pipelineGraph.nodes.map { node =>
      node.stepType.toLowerCase match {
        case value if value == InputStep.StepType =>
          createInputStep(node)
        case value if value == TransformStep.StepType =>
          createTransformStep(node)
        case value if value == OutputStep.StepType =>
          createOutputStep(node)
        case _ =>
          throw new DriverException(s"Incorrect node step ${node.stepType}. Review the nodes in pipelineGraph")
      }
    }

    executeWorkflow
  }

  //scalastyle:off

  /**
    * Execute the workflow and use the context with the Spark contexts, this function create the graph associated with
    * the workflow, in this graph the nodes are the steps and the edges are the relations.
    *
    * The function create all the nodes that they are implicated in the paths that ends in one output node. The
    * creation is ordered from the beginning to the end because the input data and the schema in one node is the output
    * data and the schema associated to the predecessor node.
    *
    * @param workflowContext The Spark Contexts used in the steps creation
    */
  private[driver] def executeWorkflow(implicit workflowContext: WorkflowContext): Unit = {
    val nodesModel = workflow.pipelineGraph.nodes
    val graph: Graph[NodeGraph, DiEdge] = createGraph(workflow)
    val nodeOrdering = graph.NodeOrdering((nodeX, nodeY) => (nodeX.stepType.toLowerCase, nodeY.stepType.toLowerCase) match {
      case (x, _) if x == InputStep.StepType => 1
      case (x, y) if x != InputStep.StepType && y == InputStep.StepType => -1
      case (x, y) if x == TransformStep.StepType && y == TransformStep.StepType =>
        if (graph.get(nodeX).diPredecessors.forall(_.stepType.toLowerCase == InputStep.StepType)) 1
        else if (graph.get(nodeY).diPredecessors.forall(_.stepType.toLowerCase == InputStep.StepType)) -1
        else {
          val xPredecessors = graph.get(nodeX).diPredecessors.count(_.stepType.toLowerCase == TransformStep.StepType)
          val yPredecessors = graph.get(nodeY).diPredecessors.count(_.stepType.toLowerCase == TransformStep.StepType)

          xPredecessors.compare(yPredecessors) * -1
        }
      case _ => 0
    })
    val parameters = Parameters(direction = Predecessors)
    val transformations = scala.collection.mutable.HashMap.empty[String, TransformStepData[Underlying]]
    val inputs = scala.collection.mutable.HashMap.empty[String, InputStepData[Underlying]]
    val errorOutputs: Seq[OutputStep[Underlying]] = nodesModel.filter { node =>
      val isSinkOutput = Try(node.configuration(WorkflowHelper.OutputStepErrorProperty).toString.toBoolean)
        .getOrElse(false)
      node.stepType.toLowerCase == OutputStep.StepType && isSinkOutput
    }.map(errorOutputNode => createOutputStep(errorOutputNode))

    implicit val graphContext = GraphContext(graph, inputs, transformations)

    nodesModel.filter(_.stepType.toLowerCase == OutputStep.StepType).foreach { outputNode =>
      val newOutput = createOutputStep(outputNode)
      graph.get(outputNode).diPredecessors.foreach { predecessor =>
        predecessor.outerNodeTraverser(parameters).withOrdering(nodeOrdering)
          .toList.reverse.foreach(node => createStep(node))

        if (predecessor.stepType.toLowerCase == InputStep.StepType) {
          val phaseEnum = PhaseEnum.Write
          val errorMessage = s"An error was encountered while writing input step ${predecessor.name}."
          val okMessage = s"Input step ${predecessor.name} written successfully."

          traceFunction(phaseEnum, okMessage, errorMessage) {
            inputs.find(_._1 == predecessor.name).foreach {
              case (_, InputStepData(step, data)) =>
                newOutput.writeTransform(
                  data,
                  step.outputOptions,
                  workflow.settings.errorsManagement,
                  errorOutputs,
                  Seq.empty[String]
                )
            }
          }
        }
        if (predecessor.stepType.toLowerCase == TransformStep.StepType) {
          val phaseEnum = PhaseEnum.Write
          val errorMessage = s"An error was encountered while writing transform step ${predecessor.name}."
          val okMessage = s"Transform step ${predecessor.name} written successfully."

          traceFunction(phaseEnum, okMessage, errorMessage) {
            transformations.find(_._1 == predecessor.name).foreach { case (_, transform) =>
              newOutput.writeTransform(
                transform.data,
                transform.step.outputOptions,
                workflow.settings.errorsManagement,
                errorOutputs,
                transform.predecessors
              )
            }
          }
        }
      }
    }
  }

  //scalastyle:on

  /**
    * Create the step associated to the node passed as parameter.
    *
    * @param node            The node of the graph
    * @param workflowContext The Spark contexts are contained into this parameter
    * @param graphContext    The context contains the graph and the steps created
    */
  private[driver] def createStep(node: NodeGraph)
                                (implicit workflowContext: WorkflowContext, graphContext: GraphContext[Underlying])
  : Unit =
    node.stepType.toLowerCase match {
      case value if value == InputStep.StepType =>
        if (!graphContext.inputs.contains(node.name)) {
          val input = createInputStep(node)
          val data = input.init()
          val inputStepData = InputStepData(input, data)

          data.setStepName(inputIdentificationName(input), forced = true)
          graphContext.inputs += (input.name -> inputStepData)
        }
      case value if value == TransformStep.StepType =>
        if (!graphContext.transformations.contains(node.name)) {
          val tPredecessors = findTransformPredecessors(node)
          val iPredecessors = findInputPredecessors(node)
          val transform = createTransformStep(node)
          val data = transform.transform(iPredecessors.mapValues(_.data).toMap ++ tPredecessors.mapValues(_.data))
          val iPredecessorsNames = iPredecessors.map { case (_, pInput) => inputIdentificationName(pInput.step) }.toSeq
          val tPredecessorsNames = tPredecessors.map { case (_, pTransform) =>
            transformIdentificationName(pTransform.step)
          }.toSeq

          data.setStepName(transformIdentificationName(transform), forced = false)
          graphContext.transformations += (transform.name -> TransformStepData(
            transform, data, iPredecessorsNames ++ tPredecessorsNames))
        }
      case _ =>
        log.warn(s"Invalid node step type, the predecessor nodes must be input or transformation. Node: ${node.name} " +
          s"\tWrong type: ${node.stepType}")
    }

  private[driver] def inputIdentificationName(step: InputStep[Underlying]): String =
    s"${InputStep.StepType}-${step.outputOptions.errorTableName.getOrElse(step.name)}"

  private[driver] def transformIdentificationName(step: TransformStep[Underlying]): String =
    s"${TransformStep.StepType}-${step.outputOptions.errorTableName.getOrElse(step.name)}"

  /**
    * Find the input steps that are predecessors to the node passed as parameter.
    *
    * @param node    The node to find predecessors
    * @param context The context that contains the graph and the steps created
    * @return The predecessors steps
    */
  private[driver] def findInputPredecessors(node: NodeGraph)(implicit context: GraphContext[Underlying])
  : scala.collection.mutable.HashMap[String, InputStepData[Underlying]] =
    context.inputs.filter(input =>
      context.graph.get(node).diPredecessors
        .filter(_.stepType.toLowerCase == InputStep.StepType)
        .map(_.name)
        .contains(input._1))

  /**
    * Find the transform steps that are predecessors to the node passed as parameter.
    *
    * @param node    The node to find predecessors
    * @param context The context that contains the graph and the steps created
    * @return The predecessors steps
    */
  private[driver] def findTransformPredecessors(node: NodeGraph)(implicit context: GraphContext[Underlying])
  : scala.collection.mutable.HashMap[String, TransformStepData[Underlying]] =
    context.transformations.filter(transform =>
      context.graph.get(node).diPredecessors
        .filter(_.stepType.toLowerCase == TransformStep.StepType)
        .map(_.name)
        .contains(transform._1)
    )

  /**
    * Create the Transform step and trace the error if appears.
    *
    * @param node            The node to create as transform step
    * @param workflowContext The Spark contexts are contained into this parameter
    * @return The new transform step
    */
  private[driver] def createTransformStep(node: NodeGraph)
                                         (implicit workflowContext: WorkflowContext): TransformStep[Underlying] = {
    val phaseEnum = PhaseEnum.Transform
    val errorMessage = s"An error was encountered while creating transform step ${node.name}."
    val okMessage = s"Transform step ${node.name} created successfully."

    traceFunction(phaseEnum, okMessage, errorMessage) {
      val className = WorkflowHelper.getClassName(node, workflow.executionEngine)
      val classType = node.configuration.getOrElse(AppConstant.CustomTypeKey, className).toString
      val tableName = node.writer.tableName.notBlank.getOrElse(node.name)
      val outputOptions = OutputOptions(
        node.writer.saveMode,
        tableName,
        node.writer.partitionBy.notBlank,
        node.writer.primaryKey.notBlank,
        node.writer.errorTableName.notBlank.orElse(Option(tableName))
      )
      workflowContext.classUtils.tryToInstantiate[TransformStep[Underlying]](classType, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[OutputOptions],
          classOf[TransformationStepManagement],
          classOf[Option[StreamingContext]],
          classOf[XDSession],
          classOf[Map[String, Serializable]]
        ).newInstance(node.name, outputOptions, workflow.settings.errorsManagement.transformationStepsManagement,
          workflowContext.ssc, workflowContext.xDSession, node.configuration)
          .asInstanceOf[TransformStep[Underlying]]
      )
    }
  }

  /**
    * Create the Input step and trace the error if appears.
    *
    * @param node            The node to create as input step
    * @param workflowContext The Spark contexts are contained into this parameter
    * @return The new input step
    */
  private[driver] def createInputStep(node: NodeGraph)
                                     (implicit workflowContext: WorkflowContext): InputStep[Underlying] = {
    val phaseEnum = PhaseEnum.Input
    val errorMessage = s"An error was encountered while creating input step ${node.name}."
    val okMessage = s"Input step ${node.name} created successfully."

    traceFunction(phaseEnum, okMessage, errorMessage) {
      val className = WorkflowHelper.getClassName(node, workflow.executionEngine)
      val classType = node.configuration.getOrElse(AppConstant.CustomTypeKey, className).toString
      val tableName = node.writer.tableName.notBlank.getOrElse(node.name)
      val outputOptions = OutputOptions(
        node.writer.saveMode,
        tableName,
        node.writer.partitionBy.notBlank,
        node.writer.primaryKey.notBlank,
        node.writer.errorTableName.notBlank.orElse(Option(tableName))
      )
      workflowContext.classUtils.tryToInstantiate[InputStep[Underlying]](classType, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[OutputOptions],
          classOf[Option[StreamingContext]],
          classOf[XDSession],
          classOf[Map[String, Serializable]]
        ).newInstance(node.name, outputOptions, workflowContext.ssc, workflowContext.xDSession, node.configuration)
          .asInstanceOf[InputStep[Underlying]]
      )
    }
  }

  /**
    * Create the Output step and trace the error if appears.
    *
    * @param node            The node to create as Output step
    * @param workflowContext The Spark contexts are contained into this parameter
    * @return The new Output step
    */
  private[driver] def createOutputStep(node: NodeGraph)
                                      (implicit workflowContext: WorkflowContext): OutputStep[Underlying] = {
    val phaseEnum = PhaseEnum.Output
    val errorMessage = s"An error was encountered while creating output step ${node.name}."
    val okMessage = s"Output step ${node.name} created successfully."

    traceFunction(phaseEnum, okMessage, errorMessage) {
      val classType = node.configuration.getOrElse(AppConstant.CustomTypeKey, node.className).toString
      workflowContext.classUtils.tryToInstantiate[OutputStep[Underlying]](classType, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[XDSession],
          classOf[Map[String, Serializable]]
        ).newInstance(node.name, workflowContext.xDSession, node.configuration).asInstanceOf[OutputStep[Underlying]]
      )
    }
  }

}

case class TransformStepData[Underlying[Row]](
                                               step: TransformStep[Underlying],
                                               data: DistributedMonad[Underlying],
                                               predecessors: Seq[String]
                                             )

case class InputStepData[Underlying[Row]](step: InputStep[Underlying], data: DistributedMonad[Underlying])

case class GraphContext[Underlying[Row]](graph: Graph[NodeGraph, DiEdge],
                                         inputs: scala.collection.mutable.HashMap[String, InputStepData[Underlying]],
                                         transformations: scala.collection.mutable.HashMap[String, TransformStepData[Underlying]])
