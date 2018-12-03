/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.driver.services


import scala.util.Try
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import com.stratio.sparta.core.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.config.SpartaConfig.getCrossdataConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.error._
import com.stratio.sparta.serving.core.factory.SparkContextFactory
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, PostgresDaoFactory, SchedulerUtils}
import com.stratio.sparta.serving.core.workflow.SpartaWorkflow


case class ContextsService()

  extends SchedulerUtils with CheckpointUtils with DistributedMonadImplicits with ContextBuilderImplicits {

  private val executionService = PostgresDaoFactory.executionPgService
  private val phase = PhaseEnum.Checkpoint
  private val errorMessage = s"An error was encountered while initializing Checkpoint"
  private val okMessage = s"Spark Checkpoint initialized successfully"

  def localStreamingContext(execution: WorkflowExecution, files: Seq[String]): Unit = {
    val workflow = execution.getWorkflowToExecute
    val errorManager = getErrorManager(workflow)
    import workflow.settings.streamingSettings.checkpointSettings._

    if (enableCheckpointing) {
      errorManager.traceFunction(phase, okMessage, errorMessage) {
        if (autoDeleteCheckpoint) deleteCheckpointPath(workflow)
        createLocalCheckpointPath(workflow)
      }
    }

    val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager, files, execution.genericDataExecution.userId)

    try {
      spartaWorkflow.stages()
      val ssc = getStreamingContext
      spartaWorkflow.setup()
      ssc.start()
      notifyWorkflowExecutionStarted(execution)
      ssc.awaitTermination()
    }
    finally {
      spartaWorkflow.cleanUp()
    }
  }

  def localContext(execution: WorkflowExecution, files: Seq[String]): Unit = {
    val workflow = execution.getWorkflowToExecute
    val errorManager = getErrorManager(workflow)
    val spartaWorkflow = SpartaWorkflow[RDD](workflow, errorManager, files, execution.genericDataExecution.userId)

    try {
      spartaWorkflow.setup()
      notifyWorkflowExecutionStarted(execution)
      spartaWorkflow.stages()
      spartaWorkflow.postExecutionStep()
    } finally {
      spartaWorkflow.cleanUp()
    }
  }

  def clusterStreamingContext(execution: WorkflowExecution, files: Seq[String]): Unit = {
    val workflow = execution.getWorkflowToExecute

    JarsHelper.addJarsToClassPath(files)

    if(Try(getCrossdataConfig().get.getBoolean("security.enable-manager")).getOrElse(false))
      JarsHelper.addDyplonCrossdataPluginsToClassPath()


    val errorManager = getErrorManager(workflow)
    val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager)
    try {
      val ssc = {
        import workflow.settings.streamingSettings.checkpointSettings._

        if (enableCheckpointing) {
          if (autoDeleteCheckpoint) {
            errorManager.traceFunction(phase, okMessage, errorMessage) {
              deleteCheckpointPath(workflow)
            }
          }
          StreamingContext.getOrCreate(checkpointPathFromWorkflow(workflow), () => {
            log.info(s"Creating streaming context from empty checkpoint: ${checkpointPathFromWorkflow(workflow)}")
            spartaWorkflow.stages()
            getStreamingContext
          })
        } else {
          spartaWorkflow.stages()
          getStreamingContext
        }
      }

      getXDSession().foreach(session => setDispatcherSettings(execution, session.sparkContext))
      spartaWorkflow.setup()
      ssc.start
      notifyWorkflowExecutionStarted(execution)
      ssc.awaitTermination()
    } finally {
      spartaWorkflow.cleanUp()
      SparkContextFactory.stopStreamingContext(stopGracefully = true, stopSparkContext = true)
    }
  }

  def clusterContext(execution: WorkflowExecution, files: Seq[String]): Unit = {
    val workflow = execution.getWorkflowToExecute

    JarsHelper.addJarsToClassPath(files)

    if(Try(getCrossdataConfig().get.getBoolean("security.enable-manager")).getOrElse(false))
      JarsHelper.addDyplonCrossdataPluginsToClassPath()

    val spartaWorkflow = SpartaWorkflow[RDD](workflow, getErrorManager(workflow))

    try {
      spartaWorkflow.setup()
      notifyWorkflowExecutionStarted(execution)
      spartaWorkflow.stages()
      getXDSession().foreach(session => setDispatcherSettings(execution, session.sparkContext))
      spartaWorkflow.postExecutionStep()
    } finally {
      spartaWorkflow.cleanUp()
    }
  }

  private[driver] def getErrorManager(workflow: Workflow): NotificationManager = {
    if (workflow.debugMode.forall(mode => mode))
      PostgresDebugNotificationManagerImpl(workflow)
    else PostgresNotificationManagerImpl(workflow)
  }

  private[driver] def setDispatcherSettings(execution: WorkflowExecution, sparkContext: SparkContext): Unit = {
    if (execution.genericDataExecution.executionMode == dispatcher)
      executionService.updateExecution {
        execution.copy(
          sparkExecution = Option(SparkExecution(
            applicationId = extractSparkApplicationId(sparkContext.applicationId))),
          sparkDispatcherExecution = Option(SparkDispatcherExecution(
            killUrl = execution.getWorkflowToExecute.settings.sparkSettings.killUrl.notBlankWithDefault(DefaultkillUrl)
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

  private[driver] def notifyWorkflowExecutionStarted(workflowExecution: WorkflowExecution): Unit = {
    if (workflowExecution.getWorkflowToExecute.debugMode.forall(mode => !mode) && workflowExecution.id.isDefined) {
      val startedInfo = s"Workflow started successfully"
      log.info(startedInfo)
      executionService.updateStatus(ExecutionStatusUpdate(
        workflowExecution.getExecutionId,
        ExecutionStatus(
          state = Started,
          statusInfo = Some(startedInfo)
        )))
    }
  }
}