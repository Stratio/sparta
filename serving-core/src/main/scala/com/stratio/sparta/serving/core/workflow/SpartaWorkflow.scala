/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.workflow

import java.io.Serializable

import akka.event.Logging
import akka.util.Timeout
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.constants.SdkConstants
import com.stratio.sparta.core.constants.SdkConstants._
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.SdkSchemaHelper.discardExtension
import com.stratio.sparta.core.helpers.{AggregationTimeHelper, SdkSchemaHelper}
import com.stratio.sparta.core.models.qualityrule.SparkQualityRuleResults
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, SpartaQualityRule, TransformationStepManagement}
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step._
import com.stratio.sparta.core.{ContextBuilder, DistributedMonad, WorkflowContext}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant.UserNameEnv
import com.stratio.sparta.serving.core.error.NotificationManager
import com.stratio.sparta.serving.core.exception.DriverException
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.helpers.GraphHelper._
import com.stratio.sparta.serving.core.helpers.WorkflowHelper.getConfigurationsFromObjects
import com.stratio.sparta.serving.core.helpers.{JarsHelper, WorkflowHelper}
import com.stratio.sparta.serving.core.models.enumerators.DataType
import com.stratio.sparta.serving.core.models.enumerators.DataType.DataType
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow, WorkflowRelationSettings}
import com.stratio.sparta.serving.core.services.SparkSubmitService
import com.stratio.sparta.serving.core.utils.CheckpointUtils
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.{Duration, StreamingContext}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.util.{Properties, Try}
import scalax.collection.Graph
import scalax.collection.GraphTraversal.{Parameters, Predecessors}
import scalax.collection.edge.LDiEdge

case class SpartaWorkflow[Underlying[Row] : ContextBuilder](
                                                             workflow: Workflow,
                                                             errorManager: NotificationManager,
                                                             files: Seq[String] = Seq.empty,
                                                             userId: Option[String] = Properties.envOrNone(UserNameEnv)
                                                           )
  extends CheckpointUtils with DistributedMonadImplicits {

  private val apiTimeout = Try(SpartaConfig.getDetailConfig().get.getInt("timeout")).getOrElse(DefaultApiTimeout) - 1

  implicit val timeout: Timeout = Timeout(apiTimeout.seconds)

  private val classpathUtils = WorkflowHelper.classpathUtils
  private var steps = Seq.empty[GraphStep]
  private var order = 0L

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
  def cleanUp(options: Map[String, String]): Unit = {
    val phaseEnum = PhaseEnum.Cleanup
    val errorMessage = s"An error was encountered while executing the cleanup steps with options: $options."
    val okMessage = s"Cleanup steps executed successfully with options: $options"

    errorManager.traceFunction(phaseEnum, okMessage, errorMessage) {
      steps.foreach(step => step.cleanUp(options))
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

  /**
    * *
    * @param inOutNodes sequence of input and output names cointained in the workflow
    * @return a Map(stepName -> Map(xdLineageKey -> xdLineageValue)) for every input and output with a non-empty
    *         lineage property map. The values contained in this map will be used to built the step metadataPath.
    */
  def lineageProperties(inOutNodes: Seq[String]): Map[String, Map[String, String]] = {
    val phaseEnum = PhaseEnum.Lineage
    val errorMessage = s"An error was encountered while extracting the lineage properties."
    val okMessage = s"Lineage properties successfully extracted."
    errorManager.traceFunction(phaseEnum, okMessage, errorMessage) {
      steps.filter(step => inOutNodes.contains(step.name)).map { step =>
        step.name -> step.lineageProperties()
      }.toMap.filter(_._2.nonEmpty)
    }
  }

  /**
    *
    * @param xdOutNodesWithWriter a Seq(outputName, tableName, Option(transformationName))
    * @return a Seq(stepName, Map(xdLineageKey -> xdLineageValue) resulting from all the Crossdata inputs, outputs and also
    *         all the triggers using tables from the catalog.
    *
    *         `xDStepsWithFilteredMetadataPaths` returns a Map(stepName -> Map(xdLineageKey -> xdLineageValue)) in which
    *         all the metadataPaths generated from a stepName are filtered out leaving only the ones generated
    *         by catalog tableNames. First, all the outputs and its lineage maps are filtered out. Then,
    *         for the remaining steps the second filtering is applied.
    *
    *         `xDStepsWithFilteredMetadataPaths` returns a Seq(stepName, Map(xdLineageKey -> xdLineageValue)).
    *         Every outputName will appear as many times as predecessor it has. The XD metadataPath builder
    *         "getLineageTable" is called for each outputName occurrence
    *         using the tablename parameter and is stored in a Seq(metadataPath).
    */
  def lineageXDProperties(xdOutNodesWithWriter: Seq[(String, String, Option[String])]): Seq[(String, Map[String, Seq[String]])] = {
    val phaseEnum = PhaseEnum.Lineage
    val errorMessage = s"An error was encountered while extracting the lineage catalog properties."
    val okMessage = s"Lineage catalog properties successfully extracted."

    errorManager.traceFunction(phaseEnum, okMessage, errorMessage) {
      val xdLineageOutProps = getXDOutStepsLineageProps(xdOutNodesWithWriter)
      val  xDOutputPropertiesWithMetadataPath = xdOutNodesWithWriter.map { case (stepName, tableName, _) =>
        val newXDProps = xdLineageOutProps.getOrElse(stepName, Map.empty[String,Seq[String]])

        newXDProps.map{case prop@(k, v) =>
          if (k.equals(ProvidedMetadatapathKey) && v.isEmpty)
            ProvidedMetadatapathKey -> Seq(getXDSession().fold(EmptyMetadataPath)(_.getLineageTable(tableName)))
          else
            prop
        }
        stepName -> newXDProps
      }

      val xDStepsWithFilteredMetadataPaths = getStepsNonEmptyLineageProps.filterNot(
        stepWithProps => xdLineageOutProps.contains(stepWithProps._1)).map { case (step, xdProps) =>
        val newXDProps = xdProps.map { case prop@(k, v) =>
          if (k.equals(ProvidedMetadatapathKey))
            ProvidedMetadatapathKey ->
              v.filterNot{ path => steps.map(step => path.endsWith(step.name + ":")).fold(false)(_|_)}
          else
            prop
        }
        step -> newXDProps
      }

      xDOutputPropertiesWithMetadataPath ++ xDStepsWithFilteredMetadataPaths
    }
  }

  /**
    *
    * @param xdOutNodesWithWriter a Seq(outputName, tableName, Option(transformationName))
    * @return a Map(outputStep -> Map(xdLineageKey -> xdLineageValue)) resulting from all the outputs with non-empty
    *         lineageCatalogProperties maps.
    */
  def getXDOutStepsLineageProps(xdOutNodesWithWriter: Seq[(String, String, Option[String])])
  : Map[String, Map[String, Seq[String]]] =
    getStepsNonEmptyLineageProps.filter { stepWithProps =>
      xdOutNodesWithWriter.exists(_._1 == stepWithProps._1)
    }


  /**
    *
    * @param xdOutNodesWithWriter a Seq(outputName, tableName, Option(transformationName))
    * @return a Seq(Map(predecessorName -> (outputName, metadataPath))) built by retrieving the step's Crossdata lineage
    *         property map. If the map returns an empty value the XD metadataPath builder "getLineageTable"
    *         is called for each outputName occurrence and is stored in a Seq(metadataPath).
    *
    *
    * spartaWorkflow.getStepsWithXDOutputNodesAndProperties(
        xdOutNodesWithWriter,
        spartaWorkflow.getXDOutStepsLineageProps(xdOutNodesWithWriter))
    */
  def getStepsWithXDOutputNodesAndProperties(xdOutNodesWithWriter: Seq[(String, String, Option[String])])
  :Seq[Map[String, (String, String)]] = {
    xdOutNodesWithWriter.map { case (outputName, tableName, predecessorName) =>
      val xdOutputWithProps = getXDOutStepsLineageProps(xdOutNodesWithWriter).getOrElse(outputName, Map.empty[String,Seq[String]])

      xdOutputWithProps.map { case (k, v) =>
          val metadataPath =
            if (k.equals(ProvidedMetadatapathKey) && v.isEmpty)
              getXDSession().fold("")(_.getLineageTable(tableName))
            else
              ""
          predecessorName.getOrElse(tableName) -> (outputName, metadataPath)
      }
    }
  }

  def inputOutputGraphNodesWithLineageProperties(workflow: Workflow): Seq[(NodeGraph, Map[String, String])] = {
    val phaseEnum = PhaseEnum.Lineage
    val errorMessage = s"An error was encountered while extracting the lineage properties."
    val okMessage = s"Lineage properties successfully extracted."
    errorManager.traceFunction(phaseEnum, okMessage, errorMessage) {
      workflow.pipelineGraph.nodes
        .filter(node => node.stepType.toLowerCase == OutputStep.StepType || node.stepType.toLowerCase == InputStep.StepType)
        .map(node => (node, steps.find(_.name == node.name).fold(Map.empty[String,String])(_.lineageProperties())))
    }
  }

  //scalastyle:off
  /**
    * Initialize the Spark contexts, create the steps for setup and cleanup functions and execute the workflow.
    *
    * @return The streaming context created, is used by the desing pattern in the Spark Streaming Context creation
    */
  def stages(execute: Boolean = true,
             qualityRules: Seq[SpartaQualityRule] = Seq.empty[SpartaQualityRule]): Seq[SparkQualityRuleResults] = {

    log.debug("Creating workflow stages")

    if (execute)
      errorManager.clearError()

    val phaseEnum = PhaseEnum.Context
    val errorMessage = s"An error was encountered while initializing Spark Session"
    val okMessage = s"Spark Session initialized successfully"
    val xDSession = errorManager.traceFunction(phaseEnum, okMessage, errorMessage, Logging.DebugLevel) {
      val isLocal = !execute || workflow.settings.global.executionMode == local // || workflow.debugMode.forall(mode => mode)
      val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
      val sparkLocalConfig = if (isLocal) {
        val sparkSubmitService = new SparkSubmitService(workflow)
        sparkSubmitService.getSparkLocalWorkflowConfig
      } else Map.empty[String, String]
      val xDSession = getOrCreateXDSession(
        isLocal,
        userId,
        forceStop = false,
        extraConfiguration = stepsSparkConfig ++ sparkLocalConfig
      )

      Thread.currentThread().setContextClassLoader(JarsHelper.getClassLoader)
      JarsHelper.addJarsToClassPath(files)

      xDSession
    }

    if (workflow.debugMode.isDefined && workflow.debugMode.get) {
      val errorMessage = s"An error was encountered while clearing cached tables"
      val okMessage = s"Cached tables cleared successfully"
      errorManager.traceFunction(phaseEnum, okMessage, errorMessage) {
        xDSession.sql("REFRESH DATABASES")
        xDSession.sql("REFRESH TABLES")
        xDSession.catalog.clearCache()
        xDSession.sessionState.catalog.clearTempTables()
      }
    }

    if (execute) {
      val errorUdfMessage = s"An error was encountered while creating UDFs"
      val okUdfMessage = s"UDFs created successfully"
      errorManager.traceFunction(phaseEnum, okUdfMessage, errorUdfMessage) {
        val udfsToRegister = workflow.settings.global.udfsToRegister
        registerUdfs(udfsToRegister.map(_.name), userId)
      }

      val errorUdafMessage = s"An error was encountered while creating UDAFs"
      val okUdafMessage = s"UDAFs created successfully"
      errorManager.traceFunction(phaseEnum, okUdafMessage, errorUdafMessage) {
        val udafsToRegister = workflow.settings.global.udafsToRegister
        registerUdafs(udafsToRegister.map(_.name), userId)
      }

      val errorMessage = s"An error was encountered while executing initial sql sentences"
      val okMessage = s"Initial Sql sentences executed successfully"
      errorManager.traceFunction(phaseEnum, okMessage, errorMessage) {
        val initSqlSentences = workflow.settings.global.preExecutionSqlSentences.map(modelSentence => modelSentence.sentence.toString)
        executeSentences(initSqlSentences, userId)
      }
    }

    implicit val workflowContext = implicitly[ContextBuilder[Underlying]].buildContext(classpathUtils, xDSession) {
      /*
      Prepare Workflow Context variables with the Spark Contexts used in steps.

      NOTE that his block will only run when the context builder for the concrete Underlying entity requires it,
      thus, DStreams won't cause the execution of this block.
      */
      val errorMessage = s"An error was encountered while creating workflow context"
      val okMessage = s"Workflow context created successfully"
      errorManager.traceFunction(phaseEnum, okMessage, errorMessage) {
        val workflowCheckpointPath = Option(checkpointPathFromWorkflow(workflow))
          .filter(_ => workflow.settings.streamingSettings.checkpointSettings.enableCheckpointing)
        val window = AggregationTimeHelper.parseValueToMilliSeconds(workflow.settings.streamingSettings.window.toString)

        workflow.debugMode.filter(isDebug => isDebug).foreach(_ => stopStreamingContext())

        getOrCreateStreamingContext(Duration(window),
          workflowCheckpointPath.notBlank,
          workflow.settings.streamingSettings.remember.notBlank
        )
      }
    }

    implicit val customClasspathClasses = workflow.pipelineGraph.nodes.filter { node =>
      node.className.matches("Custom[\\w]*Step") && !node.className.matches("CustomLite[\\w]*Step")
    } match {
      case Nil => Map[String, String]()
      case x :: xs =>
        (x :: xs).map { node =>
          val customClassType = node.configuration.getString("customClassType")
          if (customClassType.contains(".")) {
            (customClassType.substring(customClassType.lastIndexOf(".")), customClassType)
          } else (customClassType, s"com.stratio.sparta.$customClassType")
        }.toMap
    }

    steps = workflow.pipelineGraph.nodes.map { node =>
      val nodeToCreate = {
        if (workflow.debugMode.getOrElse(false))
          node
        else
          node.copy(configuration = node.configuration.filterKeys(key => key != "debugOptions"))
      }

      nodeToCreate.stepType.toLowerCase match {
        case InputStep.StepType =>
          createInputStep(nodeToCreate)
        case TransformStep.StepType =>
          createTransformStep(nodeToCreate)
        case OutputStep.StepType =>
          createOutputStep(nodeToCreate)
        case _ =>
          throw new DriverException(s"Incorrect node step ${nodeToCreate.stepType}. Review the nodes in pipelineGraph")
      }
    }

    val results: Seq[SparkQualityRuleResults] = if (execute) executeWorkflow(qualityRules) else Seq.empty[SparkQualityRuleResults]
    if (workflow.debugMode.getOrElse(false)) Seq.empty[SparkQualityRuleResults] else results
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
  def executeWorkflow(qualityRules: Seq[SpartaQualityRule] = Seq.empty[SpartaQualityRule])(implicit workflowContext: WorkflowContext,
                      customClasspathClasses: Map[String, String] ) : Seq[SparkQualityRuleResults] = {

    import com.stratio.sparta.serving.core.helpers.GraphHelperImplicits._

    log.debug(s"Executing workflow with quality rules: ${qualityRules.mkString(",")}")

    order = 0L

    val nodesModel = workflow.pipelineGraph.nodes
    val graph: Graph[NodeGraph, LDiEdge] = createGraph(workflow)
    val seqSparkResults = new ListBuffer[SparkQualityRuleResults]()


    implicit val outputStepOrdering = new Ordering[OutputStep[Underlying]] {
      override def compare(x: OutputStep[Underlying], y: OutputStep[Underlying]): Int = {
        if (x.priority != y.priority)
          y.priority.compare(x.priority)
        else x.name.compare(y.name)
      }
    }
    implicit val nodeGraphTypedOrdering = new Ordering[graph.NodeT] {
      override def compare(x: graph.NodeT, y: graph.NodeT): Int = {
        if (x.priority != y.priority)
          y.priority.compare(x.priority)
        else x.name.compare(y.name)
      }
    }
    //-1 indicates that the nodeY has more priority than the nodeX, and 1 indicates that nodeX has more priority
    val nodeOrdering = graph.NodeOrdering((nodeX, nodeY) => (nodeX.stepType.toLowerCase, nodeY.stepType.toLowerCase) match {
      case (x, y) if x == InputStep.StepType && y != InputStep.StepType => 1
      case (x, y) if x != InputStep.StepType && y == InputStep.StepType => -1
      case (x, y) if x == InputStep.StepType && y == InputStep.StepType => nodeGraphTypedOrdering.compare(nodeY, nodeX)
      case (x, y) if x == TransformStep.StepType && y == TransformStep.StepType =>
        if (nodeX.priority != nodeY.priority)
          nodeX.priority.compare(nodeY.priority)
        else nodeY.name.compare(nodeX.name)
      case _ => 0
    })

    val parameters = Parameters(direction = Predecessors)
    val transformations = scala.collection.mutable.HashMap.empty[String, TransformStepData[Underlying]]
    val inputs = scala.collection.mutable.HashMap.empty[String, InputStepData[Underlying]]
    val errorOutputs: Seq[OutputStep[Underlying]] = nodesModel
      .filter { node =>
        val isSinkOutput = Try(node.configuration(WorkflowHelper.OutputStepErrorProperty).toString.toBoolean)
          .getOrElse(false)
        node.stepType.toLowerCase == OutputStep.StepType && isSinkOutput
      }
      .map(errorOutputNode => createOutputStep(errorOutputNode))
      .sorted

    implicit val graphContext = GraphContext(graph, inputs, transformations)

    @tailrec
    def reOrderNodes(nodesList: List[NodeGraph], nextNodeIndex: Int): List[NodeGraph] = {
      if (nextNodeIndex < nodesList.size) {
        val nodeToAnalyze = nodesList(nextNodeIndex)
        val nodeGraph = graph.get(nodeToAnalyze)
        val fullNodePredecessors = nodeGraph.diPredecessors.toList.flatMap { predecessor =>
          predecessor.outerNodeTraverser(parameters).withOrdering(nodeOrdering).toList
        }.distinct
        val nextNodes = nodesList.slice(nextNodeIndex, nodesList.size)
        val nodesToReorder = nextNodes.filter { node => fullNodePredecessors.contains(node) }
        val nodeToReorder = nodesToReorder.lastOption
        val nextNodesWithoutReordered = nodeToReorder match {
          case Some(reOrderNode) => nextNodes.filter(node => node != reOrderNode)
          case None => nextNodes
        }
        val newPreviousNodes = nodesList.slice(0, nextNodeIndex) ++ nodesToReorder.takeRight(1)
        val nextNodeToAnalyze = if (nodesToReorder.isEmpty) nextNodeIndex + 1 else nextNodeIndex

        reOrderNodes(newPreviousNodes ++ nextNodesWithoutReordered, nextNodeToAnalyze)
      } else nodesList
    }

    nodesModel.filter(_.stepType.toLowerCase == OutputStep.StepType).sorted.foreach { outputNode =>
      val newOutput = createOutputStep(outputNode)
      val outNodeGraph = graph.get(outputNode)
      val outputPredecessors = outNodeGraph.diPredecessors.toList

      outputPredecessors.sorted.foreach { predecessor =>
        val nodesToReOrder = predecessor.outerNodeTraverser(parameters).withOrdering(nodeOrdering).toList.reverse
        log.debug(s"List of steps to order: ${nodesToReOrder.map(_.name).mkString(",")}")
        val nodesOrdered = reOrderNodes(nodesToReOrder, 0)
        log.debug(s"List of steps ordered: ${nodesOrdered.map(_.name).mkString(",")}")

        nodesOrdered.foreach(node => createStep(node))
      }

      val outputPredecessorsOrdered = outputPredecessors.sortBy { node =>
        node.stepType.toLowerCase match {
          case value if value == InputStep.StepType && graphContext.inputs.contains(node.name) =>
            graphContext.inputs(node.name).order
          case value if value == TransformStep.StepType && graphContext.transformations.contains(node.name) =>
            graphContext.transformations(node.name).order
          case _ => Long.MaxValue
        }
      }

      outputPredecessorsOrdered.foreach { predecessor =>
        if (predecessor.stepType.toLowerCase == InputStep.StepType) {
          val phaseEnum = PhaseEnum.Write
          val errorMessage = s"An error was encountered while writing input step ${predecessor.name}"
          val okMessage = s"Input step ${predecessor.name} written successfully"

          errorManager.traceFunction(phaseEnum, okMessage, errorMessage, Logging.DebugLevel, Option(predecessor.name)) {
            inputs.find(_._1 == predecessor.name).foreach {
              case (_, InputStepData(step, data, _, _)) =>
                seqSparkResults ++= newOutput.writeTransform(
                  data,
                  step.outputOptions,
                  workflow.settings.errorsManagement,
                  errorOutputs,
                  Seq.empty[String],
                  qualityRules.filter{ qr => qr.enable &&
                    qr.outputName == outputNode.name &&
                      (qr.stepName == predecessor.name || qr.stepName == step.outputOptions.tableName)
                  }
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
              val newOutputOptions = transform.step.outputOptions.copy(
                stepName = stepName,
                tableName = nodeName(transform.step.outputOptions.tableName, relationSettings.dataType, transform.step.outputOptions.discardTableName)
              )
              seqSparkResults ++= newOutput.writeTransform(
                transform.data,
                newOutputOptions,
                workflow.settings.errorsManagement,
                errorOutputs,
                transform.predecessors,
                qualityRules.filter { qr => qr.enable &&
                  qr.outputName == outputNode.name &&
                    (qr.stepName == stepName || qr.stepName == newOutputOptions.tableName)
                }
              )
            }
          }
        }
      }
    }

    inputs.foreach{ case (_, input) =>
      input.step match {
        case inputStep : OneTransactionOffsetManager =>
          if(inputStep.executeOffsetCommit)
            inputStep.commitOffsets()
        case _ =>
      }
    }
    seqSparkResults
  }

  /**
   * Execute steps once Spark execution has ended successfully. It applies only to batch workflows.
   */
  def postExecutionStep(): Unit = {
    val errorMessage = s"An error was encountered while executing final sql sentences"
    val okMessage = s"Final Sql sentences executed successfully"
    errorManager.traceFunction(PhaseEnum.Execution, okMessage, errorMessage) {
      val outputSqlSentences = workflow.settings.global.postExecutionSqlSentences.map(_.sentence.toString)
      executeSentences(outputSqlSentences, userId)
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
            val inputStepData = InputStepData(input, data, schema, order)
            order += 1

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
              transform, validData, iPredecessorsNames ++ tPredecessorsNames, validSchema, order))
            order += 1
            discardedData.foreach(data => graphContext.transformations += (discardedDataName -> TransformStepData(
              transform, data, iPredecessorsNames ++ tPredecessorsNames, discardedSchema, order)))
            order += 1
          }
        }
      case _ =>
        log.warn(s"Invalid node step type, the predecessor nodes must be input or transformation. Node: ${node.name} " +
          s"\tWrong type: ${node.stepType}")
    }

  private[core] def getStepsNonEmptyLineageProps: Map[String, Map[String,Seq[String]]] =
    steps.map { step =>
      step.name -> step.lineageCatalogProperties()
    }.toMap.filter(_._2.nonEmpty)


  private[core] def relationDataTypeFromName(nodeName: String): DataType =
    if (nodeName.contains(SdkSchemaHelper.discardExtension)) DataType.DiscardedData
    else DataType.ValidData

  private[core] def nodeName(
                              name: String,
                              relationDataType: DataType,
                              discardTableName: Option[String] = None
                            ): String =
    if (relationDataType == DataType.ValidData) name
    else if(!name.contains(discardExtension) && discardTableName.isDefined) discardTableName.get
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
      val configuration = {
        if(node.className.equalsIgnoreCase("MlModelTransformStep"))
          node.configuration ++ getIntelligenceConfiguration
        else node.configuration
      }  ++
        workflow.executionId.fold(Map.empty[String, JsoneyString]) { executionId =>
          Map(ExecutionIdKey -> JsoneyString(executionId))
        }
      val outputOptions = OutputOptions(
        node.writer.saveMode,
        node.name,
        tableName,
        node.writer.partitionBy.notBlank,
        node.writer.constraintType.notBlank,
        node.writer.primaryKey.notBlank,
        node.writer.uniqueConstraintName.notBlank,
        node.writer.uniqueConstraintFields.notBlank,
        node.writer.updateFields.notBlank,
        node.writer.errorTableName.notBlank.orElse(Option(tableName)),
        node.writer.discardTableName.notBlank.orElse(Option(tableName))
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
          workflowContext.ssc, workflowContext.xDSession, configuration)
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
      val configuration = node.configuration ++
        workflow.executionId.fold(Map.empty[String, JsoneyString]) { executionId =>
          Map(ExecutionIdKey -> JsoneyString(executionId))
        }
      val outputOptions = OutputOptions(
        node.writer.saveMode,
        node.name,
        tableName,
        node.writer.partitionBy.notBlank,
        node.writer.constraintType.notBlank,
        node.writer.primaryKey.notBlank,
        node.writer.uniqueConstraintName.notBlank,
        node.writer.uniqueConstraintFields.notBlank,
        node.writer.updateFields.notBlank,
        node.writer.errorTableName.notBlank.orElse(Option(tableName)),
        node.writer.discardTableName.notBlank.orElse(Option(tableName))
      )
      workflowContext.classUtils.tryToInstantiate[InputStep[Underlying]](classType, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[OutputOptions],
          classOf[Option[StreamingContext]],
          classOf[XDSession],
          classOf[Map[String, Serializable]]
        ).newInstance(node.name, outputOptions, workflowContext.ssc, workflowContext.xDSession, configuration)
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
      val configuration = {
        if (node.className.equalsIgnoreCase("DebugOutputStep")) {
          val extraConfig = Map(WorkflowIdKey -> JsoneyString(workflow.id.get))
          node.configuration ++ extraConfig
        } else if(node.className.equalsIgnoreCase("MlPipelineOutputStep"))
          node.configuration ++ getIntelligenceConfiguration
        else node.configuration
      } ++
        workflow.executionId.fold(Map.empty[String, JsoneyString]) { executionId =>
          Map(ExecutionIdKey -> JsoneyString(executionId))
        }
      workflowContext.classUtils.tryToInstantiate[OutputStep[Underlying]](classType, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[XDSession],
          classOf[Map[String, Serializable]]
        ).newInstance(node.name, workflowContext.xDSession, configuration).asInstanceOf[OutputStep[Underlying]],
        customClasspathClasses
      )
    }
  }

  private def getIntelligenceConfiguration: Map[String, JsoneyString] = {
    val url = SpartaConfig.getIntelligenceConfig() match {
      case Some(config) => Try(config.getString(ModelRepositoryUrlKey)).getOrElse(DefaultModelRepositoryUrl)
      case None => DefaultModelRepositoryUrl
    }

    Map(SdkConstants.ModelRepositoryUrl -> url)
  }
}

case class TransformStepData[Underlying[Row]](
                                               step: TransformStep[Underlying],
                                               data: DistributedMonad[Underlying],
                                               predecessors: Seq[String],
                                               schema: Option[StructType],
                                               order: Long
                                             )

case class InputStepData[Underlying[Row]](
                                           step: InputStep[Underlying],
                                           data: DistributedMonad[Underlying],
                                           schema: Option[StructType],
                                           order: Long
                                         )

case class GraphContext[Underlying[Row]](graph: Graph[NodeGraph, LDiEdge],
                                         inputs: scala.collection.mutable.HashMap[String, InputStepData[Underlying]],
                                         transformations: scala.collection.mutable.HashMap[String, TransformStepData[Underlying]])