/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow.migration

import com.stratio.sparta.core.workflow.step.OutputStep
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.enumerators.DataType
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.core.properties.ValidatingPropertyMap._

object MigrationModelImplicits {

  implicit def orionWorkflowToHydra(workflowOrion: WorkflowOrion): WorkflowHydraPegaso =
    WorkflowHydraPegaso(
      id = workflowOrion.id,
      name = workflowOrion.name,
      description = workflowOrion.description,
      settings = workflowOrion.settings,
      pipelineGraph = workflowOrion.pipelineGraph,
      executionEngine = workflowOrion.executionEngine,
      uiSettings = workflowOrion.uiSettings,
      creationDate = workflowOrion.creationDate,
      lastUpdateDate = workflowOrion.lastUpdateDate,
      version = workflowOrion.version,
      group = workflowOrion.group,
      tags = workflowOrion.tags,
      debugMode = None,
      versionSparta = Option(AppConstant.version),
      parametersUsedInExecution = None,
      executionId = None,
      groupId = workflowOrion.group.id
    )

  implicit def hydraPipelineGraphToR9(pipelineGraph: PipelineGraphHydraPegaso): PipelineGraph = {
    val outputsWriters = pipelineGraph.edges.filter { edge =>
      pipelineGraph.nodes.exists(node => node.name == edge.destination && node.stepType == OutputStep.StepType)
    }.flatMap { edge =>
      pipelineGraph.nodes.find(node => node.name == edge.origin).map { node =>
        (node.name, OutputWriter(
          saveMode = node.writer.saveMode,
          outputStepName = edge.destination,
          tableName = node.writer.tableName.map(_.toString),
          discardTableName = node.writer.discardTableName.map(_.toString),
          extraOptions = Map(
            OutputStep.PrimaryKey -> node.writer.primaryKey.map(_.toString),
            OutputStep.PartitionByKey -> node.writer.partitionBy.map(_.toString),
            OutputStep.UniqueConstraintName -> node.writer.uniqueConstraintName.map(_.toString),
            OutputStep.UniqueConstraintFields -> node.writer.uniqueConstraintFields.map(_.toString),
            OutputStep.UpdateFields -> node.writer.updateFields.map(_.toString)
          ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> value)) }.flatten.toMap
        ))
      }
    }

    PipelineGraph(
      edges = pipelineGraph.edges,
      nodes = pipelineGraph.nodes.map { node =>
        NodeGraph(
          name = node.name,
          stepType = node.stepType,
          className = node.className,
          classPrettyName = node.classPrettyName,
          arity = node.arity,
          writer = None,
          description = node.description,
          uiConfiguration = node.uiConfiguration,
          configuration = node.configuration,
          nodeTemplate = node.nodeTemplate,
          supportedEngines = node.supportedEngines,
          executionEngine = node.executionEngine,
          supportedDataRelations = node.supportedDataRelations,
          lineageProperties = node.lineageProperties,
          outputsWriter = outputsWriters.filter { case (nodeName, _) => nodeName == node.name }.map(_._2),
          errorTableName = node.writer.errorTableName.map(_.toString)
        )
      }
    )
  }

  implicit def hydraWorkflowToR9(workflowPegason: WorkflowHydraPegaso): Workflow =
    Workflow(
      id = workflowPegason.id,
      name = workflowPegason.name,
      description = workflowPegason.description,
      settings = workflowPegason.settings,
      pipelineGraph = workflowPegason.pipelineGraph,
      executionEngine = workflowPegason.executionEngine,
      uiSettings = workflowPegason.uiSettings,
      creationDate = workflowPegason.creationDate,
      lastUpdateDate = workflowPegason.lastUpdateDate,
      version = workflowPegason.version,
      group = workflowPegason.group,
      tags = workflowPegason.tags,
      debugMode = None,
      versionSparta = Option(AppConstant.version),
      parametersUsedInExecution = None,
      executionId = None,
      groupId = workflowPegason.group.id
    )

  implicit def orionSettingsToHydra(orionSettings: SettingsOrion): Settings =
    Settings(
      global = orionSettings.global,
      streamingSettings = orionSettings.streamingSettings,
      sparkSettings = orionSettings.sparkSettings,
      errorsManagement = orionSettings.errorsManagement
    )

  implicit def orionSparkSettingsToHydra(orionSparkSettings: SparkSettingsOrion): SparkSettings =
    SparkSettings(
      master = orionSparkSettings.master,
      sparkKerberos = orionSparkSettings.sparkKerberos,
      sparkDataStoreTls = orionSparkSettings.sparkDataStoreTls,
      sparkMesosSecurity = orionSparkSettings.sparkMesosSecurity,
      killUrl = orionSparkSettings.killUrl,
      submitArguments = orionSparkSettings.submitArguments,
      sparkConf = orionSparkSettings.sparkConf
    )

  implicit def orionSparkConfToHydra(orionSparkConf: SparkConfOrion): SparkConf =
    SparkConf(
      sparkResourcesConf = orionSparkConf.sparkResourcesConf,
      sparkHistoryServerConf = SparkHistoryServerConf(),
      userSparkConf = orionSparkConf.userSparkConf,
      coarse = orionSparkConf.coarse,
      sparkUser = orionSparkConf.sparkUser,
      sparkLocalDir = orionSparkConf.sparkLocalDir,
      sparkKryoSerialization = orionSparkConf.sparkKryoSerialization,
      sparkSqlCaseSensitive = orionSparkConf.sparkSqlCaseSensitive,
      logStagesProgress = orionSparkConf.logStagesProgress,
      hdfsTokenCache = orionSparkConf.hdfsTokenCache,
      executorExtraJavaOptions = orionSparkConf.executorExtraJavaOptions
    )

  implicit def templateOrionToHydra(templateOrion: TemplateElementOrion): TemplateElement =
    TemplateElement(
      id = templateOrion.id,
      templateType = templateOrion.templateType,
      name = templateOrion.name,
      description = templateOrion.description,
      className = templateOrion.className,
      classPrettyName = templateOrion.classPrettyName,
      configuration = templateOrion.configuration,
      creationDate = templateOrion.creationDate,
      lastUpdateDate = templateOrion.lastUpdateDate,
      supportedEngines = templateOrion.supportedEngines,
      executionEngine = templateOrion.executionEngine,
      supportedDataRelations = Option(Seq(DataType.ValidData, DataType.ValidData)),
      versionSparta = Option(AppConstant.version)
    )

}
