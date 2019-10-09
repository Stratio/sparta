/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.dg.agent.commons

import com.stratio.sparta.core.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.core.constants.SdkConstants._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.{InputStep, OutputStep, TransformStep}
import com.stratio.sparta.dg.agent.models.{ActorMetadata, LineageWorkflow, MetadataPath}
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant.defaultWorkflowRelationSettings
import com.stratio.sparta.serving.core.error.PostgresNotificationManagerImpl
import com.stratio.sparta.serving.core.helpers.GraphHelper.createGraph
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.enumerators.{DataType, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.workflow.SpartaWorkflow
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

import scala.util.Try
import scalax.collection._
import scalax.collection.edge.LDiEdge

//scalastyle:off
object LineageUtils extends ContextBuilderImplicits {

  case class OutputNodeLineageEntity(outputName: String,
                                     nodeTableName: String,
                                     outputClassPrettyName: String,
                                     outputStepType: String,
                                     transformationName: Option[String] = None)

  lazy val StartKey = "startedAt"
  lazy val FinishedKey = "finishedAt"
  lazy val TypeFinishedKey = "detailedStatus"
  lazy val ErrorKey = "error"
  lazy val UrlKey = "link"
  lazy val PublicSchema = "public"
  lazy val ActorTypeKey = "SPARTA"
  lazy val noTenant = Some("NONE")
  lazy val current_tenant = AppConstant.EosTenant.orElse(noTenant)
  lazy val spartaVHost = AppConstant.virtualHost.getOrElse("localhost")

  def checkIfProcessableWorkflow(executionStatusChange: WorkflowExecutionStatusChange): Boolean = {
    val eventStatus = executionStatusChange.newExecution.lastStatus.state
    val exEngine = executionStatusChange.newExecution.executionEngine.get

    (exEngine == Batch && eventStatus == WorkflowStatusEnum.Finished || eventStatus == WorkflowStatusEnum.Failed
      || eventStatus == WorkflowStatusEnum.StoppedByUser) ||
      (exEngine == Streaming && eventStatus == WorkflowStatusEnum.Started || eventStatus == WorkflowStatusEnum.StoppedByUser
        || eventStatus == WorkflowStatusEnum.Finished || eventStatus == WorkflowStatusEnum.Failed)
  }

  def getOutputNodeLineageEntities(workflow: Workflow): Seq[OutputNodeLineageEntity] = {
    import com.stratio.sparta.serving.core.helpers.GraphHelperImplicits._

    val graph: Graph[NodeGraph, LDiEdge] = createGraph(workflow)

    workflow.pipelineGraph.nodes.filter(_.stepType.toLowerCase == OutputStep.StepType)
      .sorted
      .flatMap { outputNode =>
        val outNodeGraph = graph.get(outputNode)
        val predecessors = outNodeGraph.diPredecessors.toList
        predecessors.map { node =>
          val tableName = {
            val relationSettings = Try {
              node.findOutgoingTo(outNodeGraph).get.value.edge.label.asInstanceOf[WorkflowRelationSettings]
            }.getOrElse(defaultWorkflowRelationSettings)

            if (relationSettings.dataType == DataType.ValidData)
              node.outputTableName(outputNode.name)
            else if (relationSettings.dataType == DataType.DiscardedData)
              node.outputDiscardTableName(outputNode.name).getOrElse(SdkSchemaHelper.discardTableName(node.name))
            else node.name
          }

          OutputNodeLineageEntity(outNodeGraph.name, tableName, outNodeGraph.classPrettyName, outputNode.stepType, Some(node.name))
        }
      }
  }

  def getAllStepsProperties(workflow: Workflow, loggedUser: Option[String]): Map[String, Map[String, String]] = {
    val errorManager = PostgresNotificationManagerImpl(workflow)
    val inOutNodes = workflow.pipelineGraph.nodes.filter(node =>
      node.stepType.toLowerCase == OutputStep.StepType || node.stepType.toLowerCase == InputStep.StepType).map(_.name)

    if (workflow.executionEngine == Streaming) {
      val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager, userId = loggedUser)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.lineageProperties(inOutNodes)
    } else if (workflow.executionEngine == Batch) {
      val spartaWorkflow = SpartaWorkflow[RDD](workflow, errorManager, userId = loggedUser)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.lineageProperties(inOutNodes)
    } else Map.empty
  }

  /**
    *
    * @param workflow             Entity workflow built by the user
    * @param xdOutNodesWithWriter Seq(outputName, tableName, Option(transformationName))
    * @return A collection Seq(stepName, Map(metadataKey -> Seq(metadataPaths))) resulting from all the Crossdata input
    *         and outputs and also the transformation steps in which a table from the catalog is being used.
    *         A Seq[String] is used to store the metadataPaths given the situation in which a XD table could be built from
    *         multiple tables, resulting in multiple metadataPaths.
    */
  def getAllXDStepsProperties(workflow: Workflow,
                              xdOutNodesWithWriter: Seq[(String, String, Option[String])]): Seq[(String, Map[String, Seq[String]])] = {
    val errorManager = PostgresNotificationManagerImpl(workflow)

    if (workflow.executionEngine == Streaming) {
      val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.lineageXDProperties(xdOutNodesWithWriter)
    } else if (workflow.executionEngine == Batch) {
      val spartaWorkflow = SpartaWorkflow[RDD](workflow, errorManager)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.lineageXDProperties(xdOutNodesWithWriter)
    } else Seq.empty
  }

  def getXDOutputNodesWithWriter(workflow: Workflow, nodesOutGraph: Seq[OutputNodeLineageEntity]): Seq[(String, String, Option[String])] = {
    nodesOutGraph.filter(n => isCrossdataStepType(n.outputClassPrettyName)).map { node =>
      (node.outputName, node.nodeTableName, node.transformationName)
    }
  }

  def generateLineageEventFromWfExecution(executionStatusChange: WorkflowExecutionStatusChange): Option[LineageWorkflow] = {
    val workflow = executionStatusChange.newExecution.getWorkflowToExecute
    val executionId = executionStatusChange.newExecution.getExecutionId
    val loggedUser = executionStatusChange.newExecution.genericDataExecution.userId

    if (checkIfProcessableWorkflow(executionStatusChange)) {
      val executionProperties = setExecutionProperties(executionStatusChange.newExecution)
      val lineageProperties = getAllStepsProperties(workflow, loggedUser)
      val nodesOutGraph = getOutputNodeLineageEntities(workflow)
      val inputNodes = workflow.pipelineGraph.nodes.filter(_.stepType.toLowerCase == InputStep.StepType).map(_.name).toSet
      val inputNodesProperties = lineageProperties.filterKeys(inputNodes).toSeq
      val parsedLineageProperties = addTableNameFromWriterToOutput(nodesOutGraph, lineageProperties) ++ inputNodesProperties

      val listStepsMetadata: Seq[ActorMetadata] = parsedLineageProperties.flatMap { case (pluginName, props) =>
        props.get(ServiceKey).flatMap { serviceName =>
          val stepType = workflow.pipelineGraph.nodes.find(_.name == pluginName).map(_.stepType).getOrElse("").toLowerCase
          val dataStoreType = workflow.pipelineGraph.nodes.find(_.name == pluginName).map(_.classPrettyName).getOrElse("")
          val extraPath = props.get(PathKey).map(_ ++ LineageUtils.extraPathFromFilesystemOutput(stepType, dataStoreType, props.get(PathKey), props.get(ResourceKey)))
          val metaDataPath = MetadataPath(serviceName, extraPath, props.get(ResourceKey)).toString
          val govMetadataType = mapSparta2GovernanceStepType(stepType)
          val govDataStoreType = mapSparta2GovernanceDataStoreType(dataStoreType)

          for {
            govMetaType <- govMetadataType
            govDataStType <- govDataStoreType
          } yield {
            ActorMetadata(
              `type` = govMetaType,
              metaDataPath = metaDataPath,
              dataStoreType = govDataStType,
              tenant = current_tenant,
              properties = Map.empty
            )
          }
        }
      }

      mapSparta2GovernanceStatuses(executionStatusChange.newExecution.lastStatus.state).map { govStatus =>
        LineageWorkflow(
          id = -1,
          name = workflow.name,
          description = workflow.description,
          tenant = current_tenant,
          properties = executionProperties,
          transactionId = executionId,
          actorType = ActorTypeKey,
          jobType = mapSparta2GovernanceJobType(workflow.executionEngine),
          statusCode = govStatus,
          version = AppConstant.version,
          listActorMetaData = listStepsMetadata.toList ++ getAllDataAssetsFromXDSteps(workflow)
        )
      }

    } else None
  }

  private def getAllDataAssetsFromXDSteps(workflow: Workflow): List[ActorMetadata] = {
    val xdOutNodesWithWriter = getXDOutputNodesWithWriter(workflow, getOutputNodeLineageEntities(workflow))
    val xdStepsLineageProperties = getAllXDStepsProperties(workflow, xdOutNodesWithWriter)

    xdStepsLineageProperties.flatMap { case (step, xdProps) =>
      val metadataPaths = xdProps.getOrElse(ProvidedMetadatapathKey, Seq.empty[String])

      metadataPaths.flatMap { xdMetaDataPath =>
        val stepType = workflow.pipelineGraph.nodes.find(_.name == step).map(_.stepType).getOrElse("").toLowerCase
        val dataStoreType = workflow.pipelineGraph.nodes.find(_.name == step).map(_.classPrettyName).getOrElse("")
        val metaDataPath = xdMetaDataPath
        val govMetadataType = mapSparta2GovernanceStepType(stepType)
        val govDataStoreType = mapSparta2GovernanceDataStoreType(dataStoreType)

        for {
          govMetaType <- govMetadataType
          govDataStType <- govDataStoreType
        } yield {
          ActorMetadata(
            `type` = govMetaType,
            metaDataPath = metaDataPath,
            dataStoreType = govDataStType,
            tenant = AppConstant.EosTenant,
            properties = Map.empty
          )
        }
      }
    }.toList
  }

  def setExecutionUrl(executionId: String): String = {
    "https://" + spartaVHost + "/" + AppConstant.tenantIdInstanceNameWithDefault + "/#/executions/" + executionId
  }

  def setExecutionProperties(newExecution: WorkflowExecution): Map[String, String] = {
    Map(
      StartKey -> newExecution.genericDataExecution.startDate.getOrElse(None).toString,
      FinishedKey -> newExecution.resumedDate.getOrElse(None).toString,
      TypeFinishedKey -> Try(newExecution.statuses.head.state.toString).getOrElse("Finished"),
      ErrorKey -> newExecution.genericDataExecution.lastError.toString,
      UrlKey -> setExecutionUrl(newExecution.getExecutionId))
  }

  def updateLineageWorkflow(responseWorkflow: LineageWorkflow, newWorkflow: LineageWorkflow): LineageWorkflow = {
    LineageWorkflow(
      id = responseWorkflow.id,
      name = responseWorkflow.name,
      description = responseWorkflow.description,
      tenant = responseWorkflow.tenant,
      properties = newWorkflow.properties,
      transactionId = responseWorkflow.transactionId,
      actorType = responseWorkflow.actorType,
      jobType = responseWorkflow.jobType,
      statusCode = newWorkflow.statusCode,
      version = responseWorkflow.version,
      listActorMetaData = responseWorkflow.listActorMetaData
    )
  }

  def addTableNameFromWriterToOutput(nodesOutGraph: Seq[OutputNodeLineageEntity],
                                     lineageProperties: Map[String, Map[String, String]]): Seq[(String, Map[String, String])] = {
    nodesOutGraph.map { outputNodeLineageRelation =>
      import outputNodeLineageRelation._
      val newProperties = {
        val outputProperties = lineageProperties.getOrElse(outputName, Map.empty)
        val sourceProperty = outputProperties.get(SourceKey)
        val schema = outputProperties.getOrElse(DefaultSchemaKey, PublicSchema)

        outputProperties.map { case property@(key, value) =>
          if (key.equals(ResourceKey) && isFileSystemStepType(outputClassPrettyName))
            (ResourceKey, nodeTableName)
          else if (key.equals(ResourceKey) && isJdbcStepType(outputClassPrettyName))
            if (!nodeTableName.contains(".") && sourceProperty.isDefined && sourceProperty.get.toLowerCase.contains("postgres")) {
              (ResourceKey, s"$schema.$nodeTableName")
            } else (ResourceKey, nodeTableName)
          else property
        }
      }

      outputName -> newProperties
    }
  }

  def extraPathFromFilesystemOutput(stepType: String, stepClass: String, path: Option[String],
                                    resource: Option[String]): String =
    if (stepType.equals(OutputStep.StepType) && isFileSystemStepType(stepClass) && resource.nonEmpty) {
      "/" + resource.getOrElse("")
    }
    else ""

  def mapSparta2GovernanceJobType(executionEngine: ExecutionEngine): String =
    executionEngine match {
      case Streaming => "STREAM"
      case Batch => "BATCH"
    }

  def mapSparta2GovernanceStepType(stepType: String): Option[String] =
    stepType match {
      case InputStep.StepType | TransformStep.StepType => Some("IN")
      case OutputStep.StepType => Some("OUT")
      case _ => None
    }

  def mapSparta2GovernanceStatuses(spartaStatus: WorkflowStatusEnum.Value): Option[String] =
    spartaStatus match {
      case Started => Some("RUNNING")
      case Finished => Some("FINISHED")
      case Failed => Some("ERROR")
      case StoppedByUser => Some("FINISHED")
      case _ => None
    }

  def isFileSystemStepType(stepClassType: String): Boolean =
    stepClassType match {
      case "Avro" | "Csv" | "FileSystem" | "Parquet" | "Xml" | "Json" | "Text" => true
      case _ => false
    }

  def isJdbcStepType(stepClassType: String): Boolean =
    stepClassType match {
      case "Jdbc" | "Postgres" => true
      case _ => false
    }

  def isCrossdataStepType(stepClassType: String): Boolean =
    stepClassType match {
      case "Crossdata" | "Trigger" | "SQL" => true
      case _ => false
    }

  def mapSparta2GovernanceDataStoreType(stepClassType: String): Option[String] =
    stepClassType match {
      case "Avro" | "Csv" | "FileSystem" | "Parquet" | "Xml" | "Json" | "Text" => Some("HDFS")
      case "Jdbc" | "Postgres" => Some("SQL")
      case "Crossdata" | "Trigger" | "SQL" => Some("XD")
      case _ => None
    }
}