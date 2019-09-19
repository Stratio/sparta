/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import java.util.UUID

import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.enumerators._
import com.stratio.sparta.serving.core.models.orchestrator.ScheduledWorkflowTask
import com.stratio.sparta.serving.core.models.parameters.ParameterVariable
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.dao.{ScheduledWorkflowTaskPostgresDao, WorkflowPostgresDao}

import scala.concurrent.Future

class PlannedQualityRulesUtils {

  lazy val workflowPostgresService: WorkflowPostgresDao = PostgresDaoFactory.workflowPgService
  lazy val scheduledTaskPostgresService: ScheduledWorkflowTaskPostgresDao = PostgresDaoFactory.scheduledWorkflowTaskPgService

  import PlannedQualityRulesUtils._

  def createOrUpdateTaskPlanning(spartaQualityRule: SpartaQualityRule): Future[(ScheduledWorkflowTask, SpartaQualityRule)] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    implicit val sp_QR = spartaQualityRule

    val parametrizedWorkflow: Future[Workflow] =
      (for {
        pqrWf <- workflowPostgresService.findWorkflowById(DefaultPlannedQRWorkflowId)
      } yield pqrWf).fallbackTo(workflowPostgresService.createWorkflow(createWorkflowFromScratch))

    lazy val oldTaskFromDb: Future[Option[ScheduledWorkflowTask]] =
      if (spartaQualityRule.taskId.isEmpty) Future(None)
      else
        for {oldTask <- scheduledTaskPostgresService.findByID(spartaQualityRule.taskId.get)} yield oldTask

    for {
      _ <- parametrizedWorkflow
      oldTask <- oldTaskFromDb
      task <- {
        if (oldTask.isDefined)
          upsertWorkflowTask(updateQualityRuleToWorkflowTask(oldTask.get))
        else
          upsertWorkflowTask(createQualityRuleToWorkflowTask)
      }
    } yield (task, spartaQualityRule.copy(taskId = Some(task.id)))
  }

  def upsertWorkflowTask(task: ScheduledWorkflowTask): Future[ScheduledWorkflowTask] =
    scheduledTaskPostgresService.updateScheduledWorkflowTask(task)

  /**
    * Add specific env variables from the Quality Rule
    **/
  def createQualityRuleToWorkflowTask(implicit spartaQualityRule: SpartaQualityRule): ScheduledWorkflowTask =
    ScheduledWorkflowTask(
      id = UUID.randomUUID().toString,
      taskType = ScheduledTaskType.UNIQUE_PERIODICAL,
      actionType = ScheduledActionType.RUN,
      entityId = DefaultPlannedQRWorkflowId,
      executionContext = plannedQRExecutionContext,
      active = true,
      state = ScheduledTaskState.NOT_EXECUTED,
      duration = Some(s"${spartaQualityRule.period.get}s"),
      initDate = spartaQualityRule.initDate.get,
      loggedUser = None
    )

  def updateQualityRuleToWorkflowTask(oldTask: ScheduledWorkflowTask)
                                     (implicit spartaQualityRule: SpartaQualityRule): ScheduledWorkflowTask =
    ScheduledWorkflowTask(
      id = oldTask.id,
      taskType = ScheduledTaskType.UNIQUE_PERIODICAL,
      actionType = ScheduledActionType.RUN,
      entityId = DefaultPlannedQRWorkflowId,
      executionContext = plannedQRExecutionContext,
      active = true,
      state = ScheduledTaskState.NOT_EXECUTED,
      duration = Some(s"${spartaQualityRule.period.get}s"),
      initDate = spartaQualityRule.initDate.get,
      loggedUser = None
    )

  def plannedQRExecutionContext(implicit spartaQualityRule: SpartaQualityRule): Option[ExecutionContext] = Some(
    ExecutionContext(
      extraParams = Seq(
        ParameterVariable(name = inputTableNameEnvVariable,
          value = Some(spartaQualityRule.inputStepNameForPlannedQR)),
        ParameterVariable(name = queryReferenceEnvVariable,
          value = spartaQualityRule.plannedQuery.map(_.queryReference))
      ),
      paramsLists = Seq(spartaQualityRule.sparkResourcesSize.getOrElse(DefaultPlannedQRParameterList)))
  )
}

object PlannedQualityRulesUtils{

  private val inputTableNameEnvVariable = "{{{PLANNED_QR_INPUT_TABLE}}}"
  private val queryReferenceEnvVariable = "{{{PLANNED_QR_QUERY}}}"
  private val sparkUserForPlannedQR = "root"

  def createWorkflowFromScratch: Workflow = {
    Workflow(
      id = Option(DefaultPlannedQRWorkflowId),
      name = "planned-quality-rule-workflow",
      description = "Workflow whose Input is a Crossdata Step and output is a Print that outputs statistics",
      settings =  defaultSettings,
      pipelineGraph = defaultPipeline,
      executionEngine = Batch,
      group = DefaultSystemGroup,
      tags = Option(Seq("plannedQR")),
      debugMode = Option(false),
      versionSparta = Option(DefaultVersion),
      parametersUsedInExecution = None,
      groupId = DefaultSystemGroup.id
    )
  }

  protected[core] def inputQRNode : NodeGraph = NodeGraph(
    name = "planned_qr_input",
    stepType = "Input",
    className = "CrossdataInputStep",
    classPrettyName = "Crossdata",
    arity = Seq(NodeArityEnum.NullaryToNary),
    writer =  Option(WriterGraph(tableName = Some(inputTableNameEnvVariable))),
    description = Some("Input step Crossdata"),
    configuration = Map("query" -> JsoneyString(queryReferenceEnvVariable),
      "tlsEnabled" -> JsoneyString("true")),
    nodeTemplate = None,
    supportedEngines = Seq(Batch),
    executionEngine = Option(Batch),
    supportedDataRelations = Some(Seq(DataType.ValidData))
  )

  protected[core] def outputQRNode : NodeGraph = NodeGraph(
    name = "metrics",
    stepType = "Output",
    className = "PrintOutputStep",
    classPrettyName = "Print",
    arity = Seq(NodeArityEnum.NaryToNullary),
    writer =  Option(WriterGraph()),
    description = Some("Output step metrics"),
    configuration = Map(
      "printSchema" -> JsoneyString("true"),
      "printMetadata" -> JsoneyString("true"),
      "logLevel" -> JsoneyString("error")),
    nodeTemplate = None,
    supportedEngines = Seq(Batch),
    executionEngine = Option(Batch),
    supportedDataRelations = Some(Seq(DataType.ValidData))
  )

  protected[core] def defaultPipeline : PipelineGraph =
    PipelineGraph(nodes = Seq(inputQRNode, outputQRNode), edges = defaultEdges)

  protected[core] def defaultEdges : Seq[EdgeGraph] = Seq(EdgeGraph(origin = "planned_qr_input",
    destination = "metrics"))

  protected[core] def defaultSettings : Settings = Settings(
    global = GlobalSettings().copy(parametersLists = Seq(DefaultPlannedQRParameterList)),
    sparkSettings = SparkSettings(
      sparkConf = SparkConf(
        coarse = Some(true),
        sparkResourcesConf = defaultSparkResourcesSettings,
        sparkUser = Option(JsoneyString(sparkUserForPlannedQR)),
        sparkSqlCaseSensitive = Some(true),
        hdfsTokenCache = Some(true)
      )
    )
  )

  protected[core] def defaultSparkResourcesSettings : SparkResourcesConf =
    SparkResourcesConf(
      coresMax = Option(JsoneyString(s"{{{$DefaultPlannedQRParameterList.SPARK_CORES_MAX}}}")),
      executorMemory = Option(JsoneyString(s"{{{$DefaultPlannedQRParameterList.SPARK_EXECUTOR_MEMORY}}}")),
      executorCores = Option(JsoneyString(s"{{{$DefaultPlannedQRParameterList.SPARK_EXECUTOR_CORES}}}")),
      driverCores = Option(JsoneyString(s"{{{$DefaultPlannedQRParameterList.SPARK_DRIVER_CORES}}}")),
      driverMemory = Option(JsoneyString(s"{{{$DefaultPlannedQRParameterList.SPARK_DRIVER_MEMORY}}}")),
      localityWait = Option(JsoneyString(s"{{{$DefaultPlannedQRParameterList.SPARK_LOCALITY_WAIT}}}")),
      taskMaxFailures = Option(JsoneyString(s"{{{$DefaultPlannedQRParameterList.SPARK_TASK_MAX_FAILURES}}}")),
      sparkMemoryFraction = Option(JsoneyString(s"{{{$DefaultPlannedQRParameterList.SPARK_MEMORY_FRACTION}}}")),
      sparkParallelism = None
    )
}
