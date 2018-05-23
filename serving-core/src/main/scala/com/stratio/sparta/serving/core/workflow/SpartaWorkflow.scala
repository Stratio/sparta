/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.workflow

import java.io.Serializable

import akka.event.Logging
import akka.util.Timeout
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.models.{ErrorValidations, OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.utils.AggregationTimeUtils
import com.stratio.sparta.sdk.enumerators.PhaseEnum
import com.stratio.sparta.sdk.workflow.step._
import com.stratio.sparta.sdk.{ContextBuilder, DistributedMonad, WorkflowContext}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant.UserNameEnv
import com.stratio.sparta.serving.core.error.ErrorManager
import com.stratio.sparta.serving.core.exception.DriverException
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.helpers.GraphHelper._
import com.stratio.sparta.serving.core.helpers.{JarsHelper, WorkflowHelper}
import com.stratio.sparta.serving.core.models.enumerators.DataType
import com.stratio.sparta.serving.core.models.enumerators.DataType.DataType
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow, WorkflowRelationSettings}
import com.stratio.sparta.serving.core.utils.CheckpointUtils
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.{Duration, StreamingContext}

import scala.concurrent.duration._
import scala.util.{Properties, Try}
import scalax.collection.Graph
import scalax.collection.GraphTraversal.{Parameters, Predecessors}
import scalax.collection.edge.LDiEdge

case class SpartaWorkflow[Underlying[Row] : ContextBuilder](workflow: Workflow, errorManager: ErrorManager)
  extends CheckpointUtils with DistributedMonadImplicits {

  private val apiTimeout = Try(SpartaConfig.getDetailConfig.get.getInt("timeout")).getOrElse(DefaultApiTimeout) - 1

  implicit val timeout: Timeout = Timeout(apiTimeout.seconds)

  private val classpathUtils = WorkflowHelper.classpathUtils
  private var steps = Seq.empty[GraphStep]

  /**
    * Execute the setup function associated to all the steps. Previously is mandatory execute the stages
    * function because the steps variable is mutable and is initialized to empty value.
    */
  def setup(): Unit = {
    val phaseEnum = PhaseEnum.Setup
    val errorMessage = s"An error was encountered while executing the setup steps"
    val okMessage = s"Setup steps executed successfully"

    errorManager.traceFunction(phaseEnum, okMessage, errorMessage) {
      steps.foreach(step => step.setUp())
    }
  }

  /**
    * Execute the cleanUp function associated to all the steps. Previously is mandatory execute the stages
    * function because the steps variable is mutable and is initialized to empty value.
    */
  def cleanUp(): Unit = {
    val phaseEnum = PhaseEnum.Cleanup
    val errorMessage = s"An error was encountered while executing the cleanup steps."
    val okMessage = s"Cleanup steps executed successfully"

    errorManager.traceFunction(phaseEnum, okMessage, errorMessage) {
      steps.foreach(step => step.cleanUp())
    }
  }

  /**
    * Execute the validate function associated to all the steps. Previously is mandatory execute the stages
    * function because the steps variable is mutable and is initialized to empty value.
    */
  def validate(): Seq[ErrorValidations] = {
    val phaseEnum = PhaseEnum.Validate
    val errorMessage = s"An error was encountered while executing the validate steps"
    val okMessage = s"Validate steps executed successfully"

    errorManager.traceFunction(phaseEnum, okMessage, errorMessage, Logging.DebugLevel) {
      steps.map(step => step.validate())
    }
  }

  //scalastyle:off
  /**
    * Initialize the Spark contexts, create the steps for setup and cleanup functions and execute the workflow.
    *
    * @return The streaming context created, is used by the desing pattern in the Spark Streaming Context creation
    */
  def stages(execute: Boolean = true): Unit = {

    log.debug("Creating workflow stages")

    if (execute) errorManager.clearError()

    val withStandAloneExtraConf = !execute || workflow.settings.global.executionMode == local
    val initSqlSentences = {
      if (execute)
        workflow.settings.global.initSqlSentences.map(modelSentence => modelSentence.sentence.toString)
      else Seq.empty[String]
    }
    val userId = Properties.envOrNone(UserNameEnv)
    val xDSession = getOrCreateXDSession(withStandAloneExtraConf, initSqlSentences, userId)

    implicit val workflowContext = implicitly[ContextBuilder[Underlying]].buildContext(classpathUtils, xDSession) {
      /*
      Prepare Workflow Context variables with the Spark Contexts used in steps.

      NOTE that his block will only run when the context builder for the concrete Underlying entity requires it,
      thus, DStreams won't cause the execution of this block.
      */
      val workflowCheckpointPath = Option(checkpointPathFromWorkflow(workflow))
        .filter(_ => workflow.settings.streamingSettings.checkpointSettings.enableCheckpointing)
      val window = AggregationTimeUtils.parseValueToMilliSeconds(workflow.settings.streamingSettings.window.toString)
      getOrCreateStreamingContext(Duration(window),
        workflowCheckpointPath.notBlank,
        workflow.settings.streamingSettings.remember.notBlank
      )
    }

    implicit val customClasspathClasses = workflow.pipelineGraph.nodes.filter(_.className.matches("Custom[\\w]*Step")) match {
      case Nil => Map[String, String]()
      case x :: xs => {
        val pluginsFiles = workflow.settings.global.userPluginsJars.map(_.jarPath.toString)
        JarsHelper.addJarsToClassPath(pluginsFiles)
        (x :: xs).map(jar => (jar.configuration.getString("customClassType"),
          s"com.stratio.sparta.${jar.configuration.getString("customClassType")}")).toMap
      }
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

    if (execute) executeWorkflow
  }

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
  def executeWorkflow(implicit workflowContext: WorkflowContext, customClasspathClasses: Map[String, String]): Unit = {

    log.debug("Executing workflow")

    val nodesModel = workflow.pipelineGraph.nodes
    val graph: Graph[NodeGraph, LDiEdge] = createGraph(workflow)
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

    nodesModel.filter(_.stepType.toLowerCase == OutputStep.StepType)
      .sortBy(node => node.name)
      .foreach { outputNode =>
        val newOutput = createOutputStep(outputNode)
        val outNodeGraph = graph.get(outputNode)
        outNodeGraph.diPredecessors.foreach { predecessor =>
          predecessor.outerNodeTraverser(parameters).withOrdering(nodeOrdering)
            .toList.reverse.foreach { node =>
            createStep(node)
          }

          if (predecessor.stepType.toLowerCase == InputStep.StepType) {
            val phaseEnum = PhaseEnum.Write
            val errorMessage = s"An error was encountered while writing input step ${predecessor.name}"
            val okMessage = s"Input step ${predecessor.name} written successfully"

            errorManager.traceFunction(phaseEnum, okMessage, errorMessage, Logging.DebugLevel, Option(predecessor.name)) {
              inputs.find(_._1 == predecessor.name).foreach {
                case (_, InputStepData(step, data, _)) =>
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
            val errorMessage = s"An error was encountered while writing transform step ${predecessor.name}"
            val okMessage = s"Transform step ${predecessor.name} written successfully"

            errorManager.traceFunction(phaseEnum, okMessage, errorMessage, Logging.DebugLevel, Option(predecessor.name)) {
              val relationSettings = Try {
                predecessor.findOutgoingTo(outNodeGraph).get.value.edge.label.asInstanceOf[WorkflowRelationSettings]
              }.getOrElse(defaultWorkflowRelationSettings)

              /*
              When one transformation is saved, we need to check if the data is the discarded. This situation is produced when:
                       discard
                 step ---------> output (where the name contains _Discard and is used by the save and the errors management in order to find the schema)
              */
              val stepName = nodeName(predecessor.name, relationSettings.dataType)
              transformations.filterKeys(_ == stepName).foreach { case (_, transform) =>
                newOutput.writeTransform(
                  transform.data,
                  transform.step.outputOptions.copy(
                    stepName = stepName,
                    tableName = nodeName(transform.step.outputOptions.tableName, relationSettings.dataType)
                  ),
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

  /**
    * Create the step associated to the node passed as parameter.
    *
    * @param node            The node of the graph
    * @param workflowContext The Spark contexts are contained into this parameter
    * @param graphContext    The context contains the graph and the steps created
    */
  private[core] def createStep(node: NodeGraph)
                              (implicit workflowContext: WorkflowContext, graphContext: GraphContext[Underlying], customClasspathClasses: Map[String, String])
  : Unit =
    node.stepType.toLowerCase match {
      case value if value == InputStep.StepType =>
        if (!graphContext.inputs.contains(node.name)) {
          val phaseEnum = PhaseEnum.Input
          val errorMessage = s"An error was encountered while executing input step ${node.name}"
          val okMessage = s"Input step ${node.name} executed successfully"

          errorManager.traceFunction(phaseEnum, okMessage, errorMessage, Logging.DebugLevel, Option(node.name)) {
            val input = createInputStep(node)
            val (data, schema) = input.initWithSchema()
            val inputStepData = InputStepData(input, data, schema)

            schema.foreach(sc => data.registerAsTable(workflowContext.xDSession, sc, node.name))
            data.setStepName(inputIdentificationName(input), forced = true)
            graphContext.inputs += (input.name -> inputStepData)
          }
        }
      case value if value == TransformStep.StepType =>
        if (!graphContext.transformations.contains(node.name)) {
          val phaseEnum = PhaseEnum.Transform
          val errorMessage = s"An error was encountered while executing transform step ${node.name}"
          val okMessage = s"Transform step ${node.name} executed successfully"

          errorManager.traceFunction(phaseEnum, okMessage, errorMessage, Logging.DebugLevel, Option(node.name)) {
            val tPredecessors = findTransformPredecessors(node)
            val iPredecessors = findInputPredecessors(node)
            val transform = createTransformStep(node)
            val (validData, validSchema, discardedData, discardedSchema) = transform.transformWithDiscards(
              iPredecessors.mapValues(_.data).toMap ++ tPredecessors.mapValues(_.data))
            val iPredecessorsNames = iPredecessors.map { case (_, pInput) => inputIdentificationName(pInput.step) }.toSeq
            val tPredecessorsNames = tPredecessors.map { case (name, pTransform) =>
              transformIdentificationName(pTransform.step, relationDataTypeFromName(name))
            }.toSeq
            val discardedDataName = SdkSchemaHelper.discardTableName(node.name)

            validSchema.foreach(sc => validData.registerAsTable(workflowContext.xDSession, sc, node.name))
            discardedSchema.foreach(sc => discardedData.foreach(data => data.registerAsTable(workflowContext.xDSession, sc, discardedDataName)))

            validData.setStepName(transformIdentificationName(transform, DataType.ValidData), forced = false)
            discardedData.foreach(data => data.setStepName(transformIdentificationName(transform, DataType.DiscardedData), forced = false))

            graphContext.transformations += (node.name -> TransformStepData(
              transform, validData, iPredecessorsNames ++ tPredecessorsNames, validSchema))
            discardedData.foreach(data => graphContext.transformations += (discardedDataName -> TransformStepData(
              transform, data, iPredecessorsNames ++ tPredecessorsNames, discardedSchema)))
          }
        }
      case _ =>
        log.warn(s"Invalid node step type, the predecessor nodes must be input or transformation. Node: ${node.name} " +
          s"\tWrong type: ${node.stepType}")
    }

  private[core] def relationDataTypeFromName(nodeName: String): DataType =
    if (nodeName.contains(SdkSchemaHelper.discardExtension)) DataType.DiscardedData
    else DataType.ValidData

  private[core] def nodeName(name: String, relationDataType: DataType): String =
    if (relationDataType == DataType.ValidData) name
    else SdkSchemaHelper.discardTableName(name)

  private[core] def inputIdentificationName(step: InputStep[Underlying]): String =
    s"${InputStep.StepType}-${step.outputOptions.errorTableName.getOrElse(step.name)}"

  private[core] def transformIdentificationName(step: TransformStep[Underlying], relationDataType: DataType): String = {
    val name = nodeName(step.outputOptions.errorTableName.getOrElse(step.name), relationDataType)
    s"${TransformStep.StepType}-$name"
  }


  /**
    * Find the input steps that are predecessors to the node passed as parameter.
    *
    * @param node    The node to find predecessors
    * @param context The context that contains the graph and the steps created
    * @return The predecessors steps
    */
  private[core] def findInputPredecessors(node: NodeGraph)(implicit context: GraphContext[Underlying])
  : scala.collection.mutable.HashMap[String, InputStepData[Underlying]] =
    context.inputs.filter { input =>
      context.graph.get(node).diPredecessors
        .filter(_.stepType.toLowerCase == InputStep.StepType)
        .map(_.name)
        .contains(input._1)
    }

  /**
    * Find the transform steps that are predecessors to the node passed as parameter.
    *
    * @param node    The node to find predecessors
    * @param context The context that contains the graph and the steps created
    * @return The predecessors steps
    */
  private[core] def findTransformPredecessors(node: NodeGraph)(implicit context: GraphContext[Underlying])
  : scala.collection.mutable.HashMap[String, TransformStepData[Underlying]] =
    context.transformations.filter { transform =>
      val outNodeGraph = context.graph.get(node)
      outNodeGraph.diPredecessors
        .filter(_.stepType.toLowerCase == TransformStep.StepType)
        .map { step =>
          val relationSettings = Try {
            step.findOutgoingTo(outNodeGraph).get.value.edge.label.asInstanceOf[WorkflowRelationSettings]
          }.getOrElse(defaultWorkflowRelationSettings)

          nodeName(step.name, relationSettings.dataType)
        }.contains(transform._1)
    }

  /**
    * Create the Transform step and trace the error if appears.
    *
    * @param node                   The node to create as transform step
    * @param workflowContext        The Spark contexts are contained into this parameter
    * @param customClasspathClasses Custom classes to load from external jars
    * @return The new transform step
    */
  private[core] def createTransformStep(node: NodeGraph)
                                       (implicit workflowContext: WorkflowContext, customClasspathClasses: Map[String, String]): TransformStep[Underlying] = {
    val phaseEnum = PhaseEnum.Transform
    val errorMessage = s"An error was encountered while creating transform step ${node.name}"
    val okMessage = s"Transform step ${node.name} created successfully"

    errorManager.traceFunction(phaseEnum, okMessage, errorMessage, Logging.DebugLevel, Option(node.name)) {
      val className = WorkflowHelper.getClassName(node, workflow.executionEngine)
      val classType = node.configuration.getOrElse(CustomTypeKey, className).toString
      val tableName = node.writer.tableName.notBlank.getOrElse(node.name)
      val outputOptions = OutputOptions(
        node.writer.saveMode,
        node.name,
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
          .asInstanceOf[TransformStep[Underlying]],
        customClasspathClasses
      )
    }
  }

  /**
    * Create the Input step and trace the error if appears.
    *
    * @param node                   The node to create as input step
    * @param workflowContext        The Spark contexts are contained into this parameter
    * @param customClasspathClasses Custom classes to load from external jars
    * @return The new input step
    */
  private[core] def createInputStep(node: NodeGraph)
                                   (implicit workflowContext: WorkflowContext, customClasspathClasses: Map[String, String]): InputStep[Underlying] = {
    val phaseEnum = PhaseEnum.Input
    val errorMessage = s"An error was encountered while creating input step ${node.name}."
    val okMessage = s"Input step ${node.name} created successfully."

    errorManager.traceFunction(phaseEnum, okMessage, errorMessage, Logging.DebugLevel, Option(node.name)) {
      val className = WorkflowHelper.getClassName(node, workflow.executionEngine)
      val classType = node.configuration.getOrElse(CustomTypeKey, className).toString
      val tableName = node.writer.tableName.notBlank.getOrElse(node.name)
      val outputOptions = OutputOptions(
        node.writer.saveMode,
        node.name,
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
          .asInstanceOf[InputStep[Underlying]],
        customClasspathClasses
      )
    }
  }

  /**
    * Create the Output step and trace the error if appears.
    *
    * @param node                   The node to create as Output step
    * @param workflowContext        The Spark contexts are contained into this parameter
    * @param customClasspathClasses Custom classes to load from external jars
    * @return The new Output step
    */
  private[core] def createOutputStep(node: NodeGraph)
                                    (implicit workflowContext: WorkflowContext, customClasspathClasses: Map[String, String]): OutputStep[Underlying] = {
    val phaseEnum = PhaseEnum.Output
    val errorMessage = s"An error was encountered while creating output step ${node.name}"
    val okMessage = s"Output step ${node.name} created successfully"

    errorManager.traceFunction(phaseEnum, okMessage, errorMessage, Logging.DebugLevel, Option(node.name)) {
      val classType = node.configuration.getOrElse(CustomTypeKey, node.className).toString
      workflowContext.classUtils.tryToInstantiate[OutputStep[Underlying]](classType, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[XDSession],
          classOf[Map[String, Serializable]]
        ).newInstance(node.name, workflowContext.xDSession, node.configuration).asInstanceOf[OutputStep[Underlying]],
        customClasspathClasses
      )
    }
  }
}

case class TransformStepData[Underlying[Row]](
                                               step: TransformStep[Underlying],
                                               data: DistributedMonad[Underlying],
                                               predecessors: Seq[String],
                                               schema: Option[StructType]
                                             )

case class InputStepData[Underlying[Row]](
                                           step: InputStep[Underlying],
                                           data: DistributedMonad[Underlying],
                                           schema: Option[StructType]
                                         )

case class GraphContext[Underlying[Row]](graph: Graph[NodeGraph, LDiEdge],
                                         inputs: scala.collection.mutable.HashMap[String, InputStepData[Underlying]],
                                         transformations: scala.collection.mutable.HashMap[String, TransformStepData[Underlying]])
