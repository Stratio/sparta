/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import java.util.UUID

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.utils.RegexUtils._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.enumerators._
import com.stratio.sparta.serving.core.models.orchestrator.ScheduledWorkflowTask
import com.stratio.sparta.serving.core.models.parameters.ParameterVariable
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.dao.{ScheduledWorkflowTaskPostgresDao, WorkflowPostgresDao}

import scala.concurrent.Future

object PlannedQualityRulesUtils extends SLF4JLogging{

  lazy val workflowPostgresService: WorkflowPostgresDao = PostgresDaoFactory.workflowPgService
  lazy val scheduledTaskPostgresService: ScheduledWorkflowTaskPostgresDao = PostgresDaoFactory.scheduledWorkflowTaskPgService
  lazy val plannedQualityRulePgService = PostgresDaoFactory.plannedQualityRulePgService

  def createOrUpdateTaskPlanning(spartaQualityRule: SpartaQualityRule,
                                 extraProperties: Seq[(String,String)] = Seq.empty[(String, String)]
                                ): Future[(ScheduledWorkflowTask, SpartaQualityRule)] = {
    import scala.concurrent.ExecutionContext.Implicits.global


    val parametrizedWorkflow: Future[Workflow] = {
      val idAccordingToType = DefaultXDPlannedQRWorkflowId
      (for {
        pqrWf <- workflowPostgresService.findWorkflowById(idAccordingToType)
      } yield pqrWf).fallbackTo(workflowPostgresService.createWorkflow(createWorkflowFromScratch))
    }

    def oldTaskFromDb(qr: Option[SpartaQualityRule]): Future[Option[ScheduledWorkflowTask]] =
      qr.fold(Future(None): Future[Option[ScheduledWorkflowTask]]) { qr =>
        for {oldTask <- scheduledTaskPostgresService.findByID(qr.taskId.get)} yield oldTask
      }

    for {
      _ <- parametrizedWorkflow
      oldQR <- plannedQualityRulePgService.findById(spartaQualityRule.id)
      oldTask <- oldTaskFromDb(oldQR)
      task <- updatedWorkflowTask(oldTask, spartaQualityRule)
    } yield
      (task, spartaQualityRule.copy(taskId = Some(task.id)))
  }

  def updatedWorkflowTask(oldTask: Option[ScheduledWorkflowTask], spartaQualityRule: SpartaQualityRule): Future[ScheduledWorkflowTask] =
    oldTask match {
      case Some(oldTaskItem) =>
        val updatedTask = updateQualityRuleToWorkflowTask(oldTaskItem, spartaQualityRule)
        upsertWorkflowTask(updatedTask)
      case None =>
        val newTask = createQualityRuleToWorkflowTask(spartaQualityRule)
        upsertWorkflowTask(newTask)
    }

  def upsertWorkflowTask(task: ScheduledWorkflowTask): Future[ScheduledWorkflowTask] = {
    scheduledTaskPostgresService.updateScheduledWorkflowTask(task)
  }

  /**
    * Add specific env variables from the Quality Rule
    **/
  def createQualityRuleToWorkflowTask(spartaQualityRule: SpartaQualityRule): ScheduledWorkflowTask = {
    val newExecutionContext = plannedQRExecutionContext(spartaQualityRule)
    log.debug(s"Creating task execution context with the following variables: [${newExecutionContext.map(_.extraParams.seq.map(x => s"${x.name} -> ${x.value}").mkString(","))}]")
    val periodAsDuration: Option[Long] = spartaQualityRule.schedulingDetails.flatMap(_.period)
    val activeTask = spartaQualityRule.enable
    ScheduledWorkflowTask(
      id = UUID.randomUUID().toString,
      taskType = periodAsDuration.fold(ScheduledTaskType.ONE_TIME){ _=> ScheduledTaskType.PERIODICAL},
      actionType = ScheduledActionType.RUN,
      entityId = DefaultXDPlannedQRWorkflowId,
      executionContext = newExecutionContext,
      active = activeTask,
      state = ScheduledTaskState.NOT_EXECUTED,
      duration = periodAsDuration.fold(None: Option[String]){ period => Some(s"${period}s")},
      initDate = spartaQualityRule.schedulingDetails.get.initDate.get,
      loggedUser = None
    )
  }

  def updateQualityRuleToWorkflowTask(oldTask: ScheduledWorkflowTask, spartaQualityRule: SpartaQualityRule): ScheduledWorkflowTask = {
    val newExecutionContext = plannedQRExecutionContext(spartaQualityRule)
    log.debug(s"Updating task execution context with the following variables: [${newExecutionContext.map(_.extraParams.seq.map(x => s"${x.name} -> ${x.value}").mkString(","))}]")
    val periodAsDuration: Option[Long] = spartaQualityRule.schedulingDetails.flatMap(_.period)
    val activeTask = spartaQualityRule.enable
    ScheduledWorkflowTask(
      id = oldTask.id,
      taskType = periodAsDuration.fold(ScheduledTaskType.ONE_TIME){ _=> ScheduledTaskType.PERIODICAL},
      actionType = ScheduledActionType.RUN,
      entityId = DefaultXDPlannedQRWorkflowId,
      executionContext = newExecutionContext,
      active = activeTask,
      state = ScheduledTaskState.NOT_EXECUTED,
      duration = periodAsDuration.fold(None: Option[String]){ period => Some(s"${period}s")},
      initDate = spartaQualityRule.schedulingDetails.get.initDate.get,
      loggedUser = None
    )
  }

  def plannedQRExecutionContext(spartaQualityRule: SpartaQualityRule): Option[ExecutionContext] = Some(
    ExecutionContext(
      extraParams = Seq(
        ParameterVariable(name = hadoopConfigURIEnv,
          value = Some(retrieveHadoopConfigUriForQR(spartaQualityRule))),
        ParameterVariable(name = queryReferenceEnv,
          value = Some(spartaQualityRule.retrieveQueryForInputStepForPlannedQR.cleanOutControlChar.trimAndNormalizeString))),
      paramsLists = Seq(
        spartaQualityRule.schedulingDetails.flatMap(details => details.sparkResourcesSize.map(size => size)).getOrElse(DefaultPlannedQRParameterList))
    )
  )

  private val hadoopConfigURIEnv = "QR_HADOOP_CONFIG"
  private val hadoopConfigURIEnvVariable = s"{{{$hadoopConfigURIEnv}}}"
  private val queryReferenceEnv = "PLANNED_QR_QUERY"
  private val queryReferenceEnvVariable = s"{{{$queryReferenceEnv}}}"
  private val sparkUserForPlannedQR = "root"
  private val offsetPosition = 200
  private val inputPosition = Position(0.0, 0.0)
  private val outputPosition = Position(inputPosition.x + offsetPosition, inputPosition.y + offsetPosition)

  def createWorkflowFromScratch: Workflow = {

    Workflow(
      id = Option(DefaultXDPlannedQRWorkflowId),
      name = "planned-quality-rule-workflow",
      description = "Workflow whose Input is a SQL Input Step and output is a Print that outputs statistics",
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

  protected def retrieveHadoopConfigUriForQR(qr: SpartaQualityRule): String = {
    qr.hadoopConfigUri.fold(AppConstant.getInstanceHadoopURI){ uri => uri}
  }

  import SpartaQualityRule._

  protected[core] def inputXD_QRNode : NodeGraph = NodeGraph(
    name = inputStepNamePlannedQR,
    stepType = "Input",
    className = "SQLInputStepBatch",
    classPrettyName = "SQL",
    arity = Seq(NodeArityEnum.NullaryToNary),
    writer =  Option(WriterGraph(tableName = Some(inputStepNamePlannedQR))),
    description = Some("Input step SQL"),
    uiConfiguration = Some(NodeUiConfiguration(Some(inputPosition))),
    configuration = Map("query" -> JsoneyString(queryReferenceEnvVariable)),
    nodeTemplate = None,
    supportedEngines = Seq(Batch),
    executionEngine = Option(Batch),
    supportedDataRelations = Some(Seq(DataType.ValidData))
  )

  protected[core] def outputQRNode : NodeGraph = NodeGraph(
    name = outputStepNamePlannedQR,
    stepType = "Output",
    className = "PrintOutputStep",
    classPrettyName = "Print",
    arity = Seq(NodeArityEnum.NaryToNullary),
    writer =  Option(WriterGraph()),
    description = Some("Output step metrics"),
    uiConfiguration = Some(NodeUiConfiguration(Some(outputPosition))),
    configuration = Map(
      "printSchema" -> JsoneyString("true"),
      "printMetadata" -> JsoneyString("false"),
      "logLevel" -> JsoneyString("error")),
    nodeTemplate = None,
    supportedEngines = Seq(Batch),
    executionEngine = Option(Batch),
    supportedDataRelations = Some(Seq(DataType.ValidData))
  )

  protected[core] def defaultPipeline : PipelineGraph = {
    val inputQRNode = inputXD_QRNode
    PipelineGraph(nodes = Seq(inputQRNode, outputQRNode), edges = defaultEdges)
  }

  protected[core] def defaultEdges : Seq[EdgeGraph] =
    Seq(EdgeGraph(origin = inputStepNamePlannedQR, destination = outputStepNamePlannedQR))

  protected[core] def defaultSettings : Settings = Settings(
    global = GlobalSettings().copy(
      //executionMode = WorkflowExecutionMode.local, //Use to test locally
      enableQualityRules = Some(true),
      parametersLists = Seq(DefaultPlannedQRParameterList),
      marathonDeploymentSettings = Some(
        MarathonDeploymentSettings().copy(
          forcePullImage = Some(true),
          userEnvVariables = Seq(KeyValuePair(JsoneyString(SystemHadoopConfUri), JsoneyString(hadoopConfigURIEnvVariable)))
        )
      )
    ),
    sparkSettings = SparkSettings(
      sparkConf = SparkConf(
        coarse = Some(true),
        sparkResourcesConf = defaultSparkResourcesSettings,
        sparkUser = Option(JsoneyString(sparkUserForPlannedQR)),
        sparkSqlCaseSensitive = Some(true),
        hdfsTokenCache = Some(true),
        userSparkConf = Seq.empty[SparkProperty]
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