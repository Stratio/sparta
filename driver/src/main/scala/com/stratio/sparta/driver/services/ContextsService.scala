/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.driver.services

import java.io.File
import java.util.UUID

import com.stratio.sparta.sdk.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.enumerators.PhaseEnum
import com.stratio.sparta.sdk.workflow.step.GraphStep
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.error.{ErrorManager, ZookeeperDebugErrorImpl, ZookeeperErrorImpl}
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.helpers.WorkflowHelper._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.{SparkDispatcherExecution, SparkExecution, Workflow, WorkflowStatus}
import com.stratio.sparta.serving.core.services.{ExecutionService, SparkSubmitService, WorkflowStatusService}
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, SchedulerUtils}
import com.stratio.sparta.serving.core.workflow.SpartaWorkflow
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

case class ContextsService(curatorFramework: CuratorFramework)

  extends SchedulerUtils with CheckpointUtils with DistributedMonadImplicits with ContextBuilderImplicits {

  private val statusService = new WorkflowStatusService(curatorFramework)
  private val executionService = new ExecutionService(curatorFramework)
  private val phase = PhaseEnum.Context
  private val errorMessage = s"An error was encountered while initializing Spark Contexts"
  private val okMessage = s"Spark Contexts created successfully"

  def localStreamingContext(workflow: Workflow, files: Seq[File], timeout: Option[Long] = None): Unit = {
    val errorManager = getErrorManager(workflow)

    errorManager.traceFunction(phase, okMessage, errorMessage) {
      import workflow.settings.streamingSettings.checkpointSettings._

      if (enableCheckpointing) {
        if (autoDeleteCheckpoint) deleteCheckpointPath(workflow)
        createLocalCheckpointPath(workflow)
      }

      val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
      val sparkSubmitService = new SparkSubmitService(workflow)
      val sparkConfig = sparkSubmitService.getSparkLocalWorkflowConfig

      getOrCreateSparkContext(sparkConfig ++ stepsSparkConfig, files)
    }

    val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager)

    try {
      spartaWorkflow.stages()
      val ssc = getStreamingContext

      spartaWorkflow.setup()
      ssc.start()
      notifyWorkflowStarted(workflow)
      timeout match {
        case Some(time) => ssc.awaitTerminationOrTimeout(time)
        case None => ssc.awaitTermination()
      }
    } finally {
      spartaWorkflow.cleanUp()
    }
  }

  def localContext(workflow: Workflow, files: Seq[File]): Unit = {
    val errorManager = getErrorManager(workflow)

    errorManager.traceFunction(phase, okMessage, errorMessage) {
      val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
      val sparkSubmitService = new SparkSubmitService(workflow)
      val sparkConfig = sparkSubmitService.getSparkLocalWorkflowConfig

      getOrCreateSparkContext(sparkConfig ++ stepsSparkConfig, files)
    }

    val spartaWorkflow = SpartaWorkflow[RDD](workflow, errorManager)

    try {
      spartaWorkflow.setup()
      notifyWorkflowStarted(workflow)
      spartaWorkflow.stages()
    } finally {
      spartaWorkflow.cleanUp()
    }
  }

  def clusterStreamingContext(workflow: Workflow, files: Seq[String]): Unit = {
    val errorManager = getErrorManager(workflow)
    val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager)
    try {
      val ssc = {
        import workflow.settings.streamingSettings.checkpointSettings._

        if (enableCheckpointing) {
          if (autoDeleteCheckpoint) deleteCheckpointPath(workflow)
          StreamingContext.getOrCreate(checkpointPathFromWorkflow(workflow), () => {
            errorManager.traceFunction(phase, okMessage, errorMessage) {
              log.info(s"Creating streaming context from empty checkpoint: ${checkpointPathFromWorkflow(workflow)}")
              val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
              getOrCreateClusterSparkContext(stepsSparkConfig, files)
              spartaWorkflow.stages()
            }
            getStreamingContext
          })
        } else {
          errorManager.traceFunction(phase, okMessage, errorMessage) {
            val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
            getOrCreateClusterSparkContext(stepsSparkConfig, files)
            spartaWorkflow.stages()
          }
          getStreamingContext
        }
      }

      setDispatcherSettings(workflow, ssc.sparkContext)
      spartaWorkflow.setup()
      ssc.start
      notifyWorkflowStarted(workflow)
      ssc.awaitTermination()
    } finally {
      spartaWorkflow.cleanUp()
    }
  }

  def clusterContext(workflow: Workflow, files: Seq[String]): Unit = {
    val errorManager = getErrorManager(workflow)

    errorManager.traceFunction(phase, okMessage, errorMessage) {
      val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
      val sparkContext = getOrCreateClusterSparkContext(stepsSparkConfig, files)

      setDispatcherSettings(workflow, sparkContext)
    }

    val spartaWorkflow = SpartaWorkflow[RDD](workflow, errorManager)

    try {
      spartaWorkflow.setup()
      notifyWorkflowStarted(workflow)
      spartaWorkflow.stages()
    } finally {
      spartaWorkflow.cleanUp()
    }
  }

  private[driver] def getErrorManager(workflow: Workflow): ErrorManager = {
    if (workflow.debugMode.forall(mode => mode))
      ZookeeperDebugErrorImpl(workflow, curatorFramework)
    else ZookeeperErrorImpl(workflow, curatorFramework)
  }

  private[driver] def setDispatcherSettings(workflow: Workflow, sparkContext: SparkContext): Unit = {
    for {
      execution <- executionService.findById(workflow.id.get)
      exec <- execution.genericDataExecution
    } if (exec.executionMode == dispatcher)
      executionService.update {
        execution.copy(
          sparkExecution = Option(SparkExecution(
            applicationId = extractSparkApplicationId(sparkContext.applicationId))),
          sparkDispatcherExecution = Option(SparkDispatcherExecution(
            killUrl = workflow.settings.sparkSettings.killUrl.notBlankWithDefault(DefaultkillUrl)
          ))
        )
      }
  }

  private[driver] def extractSparkApplicationId(contextId: String): String = {
    if (contextId.contains("driver")) {
      val sparkApplicationId = contextId.substring(contextId.indexOf("driver"))
      log.info(s"The extracted Framework id is: ${contextId.substring(0, contextId.indexOf("driver") - 1)}")
      log.info(s"The extracted Spark application id is: $sparkApplicationId")
      sparkApplicationId
    } else contextId
  }

  private[driver] def notifyWorkflowStarted(workflow: Workflow): Unit = {
    if (workflow.debugMode.forall(mode => !mode)) {
      val startedInfo = s"Workflow started successfully"
      log.info(startedInfo)
      statusService.update(WorkflowStatus(
        id = workflow.id.get,
        statusId = UUID.randomUUID.toString,
        status = Started,
        statusInfo = Some(startedInfo)
      ))
    }
  }
}