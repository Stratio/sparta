/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.driver.services


import com.stratio.sparta.core.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.core.models.qualityrule.SparkQualityRuleResults
import com.stratio.sparta.core.models.qualityrule.SparkQualityRuleResults
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.GraphStep
import com.stratio.sparta.serving.core.config.SpartaConfig.getCrossdataConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant.UserNameEnv
import com.stratio.sparta.serving.core.error._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.governance.QualityRuleResult
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, SchedulerUtils}
import com.stratio.sparta.serving.core.workflow.SpartaWorkflow
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Properties, Success, Try}
import com.stratio.sparta.core.workflow.step.GraphStep


case class ContextsService()

  extends SchedulerUtils with CheckpointUtils with DistributedMonadImplicits with ContextBuilderImplicits {

  private val executionService = PostgresDaoFactory.executionPgService
  private val qualityRuleService = PostgresDaoFactory.qualityRuleResultPgService
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

    Try {
      val qualityRulesWithExecutionId = execution.qualityRules.map( qr => qr.copy(executionId = execution.id))
      spartaWorkflow.stages(qualityRules = qualityRulesWithExecutionId)
      val ssc = getStreamingContext
      spartaWorkflow.setup()
      ssc.start()
      notifyWorkflowExecutionStarted(execution)
      ssc.awaitTermination()
    } match {
      case Success(_) =>
        finishLocalStreamingContext(spartaWorkflow, Map.empty)
      case Failure(e) =>
        finishLocalStreamingContext(spartaWorkflow, Map(GraphStep.FailedKey -> e.getLocalizedMessage))
        throw e
    }
  }

  def localContext(execution: WorkflowExecution, files: Seq[String]): Unit = {
    val workflow = execution.getWorkflowToExecute
    val errorManager = getErrorManager(workflow)
    val spartaWorkflow = SpartaWorkflow[RDD](workflow, errorManager, files, execution.genericDataExecution.userId)

    Try {
      spartaWorkflow.setup()
      notifyWorkflowExecutionStarted(execution)
      val qualityRulesWithExecutionId = execution.qualityRules.map( qr => qr.copy(executionId = execution.id))
      val qualityRulesResults = spartaWorkflow.stages(qualityRules = qualityRulesWithExecutionId)
      execution.id.map( id => saveQualityRuleResultsToPostgres(qualityRulesResults, id))
      spartaWorkflow.postExecutionStep()
    } match {
      case Success(_) =>
        finishLocalBatchContext(spartaWorkflow, Map.empty)
      case Failure(e) =>
        finishLocalBatchContext(spartaWorkflow, Map(GraphStep.FailedKey -> e.getLocalizedMessage))
        throw e
    }
  }

  def clusterStreamingContext(execution: WorkflowExecution, files: Seq[String]): Unit = {
    val workflow = execution.getWorkflowToExecute

    JarsHelper.addJarsToClassPath(files)

    if(Try(getCrossdataConfig().get.getBoolean("security.enable-manager")).getOrElse(false))
      JarsHelper.addDyplonCrossdataPluginsToClassPath()


    val errorManager = getErrorManager(workflow)
    val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager)

    Try {
      val ssc = {
        import workflow.settings.streamingSettings.checkpointSettings._

        val qualityRulesWithExecutionId = execution.qualityRules.map( qr => qr.copy(executionId = execution.id))

        if (enableCheckpointing) {
          if (autoDeleteCheckpoint) {
            errorManager.traceFunction(phase, okMessage, errorMessage) {
              deleteCheckpointPath(workflow)
            }
          }
          StreamingContext.getOrCreate(checkpointPathFromWorkflow(workflow), () => {
            log.info(s"Creating streaming context from empty checkpoint: ${checkpointPathFromWorkflow(workflow)}")
            spartaWorkflow.stages(qualityRules = qualityRulesWithExecutionId)
            getStreamingContext
          })
        } else {
          spartaWorkflow.stages(qualityRules = qualityRulesWithExecutionId)
          getStreamingContext
        }
      }

      getXDSession(Properties.envOrNone(UserNameEnv)).foreach(session => setDispatcherSettings(execution, session.sparkContext))
      spartaWorkflow.setup()
      ssc.start
      notifyWorkflowExecutionStarted(execution)
      getSparkContext.foreach(sparkContext => setSparkHistoryServerURI(execution, sparkContext))
      ssc.awaitTermination()
    } match {
      case Success(_) =>
        finishClusterStreamingContext(spartaWorkflow, Map.empty)
      case Failure(e) =>
        finishClusterStreamingContext(spartaWorkflow, Map(GraphStep.FailedKey -> e.getLocalizedMessage))
        throw e
    }
  }

  def clusterContext(execution: WorkflowExecution, files: Seq[String]): Unit = {
    val workflow = execution.getWorkflowToExecute

    JarsHelper.addJarsToClassPath(files)

    val qualityRulesExecution = execution.qualityRules

    log.info(s"Quality rules: $qualityRulesExecution")

    if(Try(getCrossdataConfig().get.getBoolean("security.enable-manager")).getOrElse(false))
      JarsHelper.addDyplonCrossdataPluginsToClassPath()

    val spartaWorkflow = SpartaWorkflow[RDD](workflow, getErrorManager(workflow))

    Try {
      spartaWorkflow.setup()
      notifyWorkflowExecutionStarted(execution)
      val qualityRulesWithExecutionId = execution.qualityRules.map( qr => qr.copy(executionId = execution.id))
      val qualityRulesResults = spartaWorkflow.stages(qualityRules = qualityRulesWithExecutionId)
      execution.id.map( id => saveQualityRuleResultsToPostgres(qualityRulesResults, id))
      getSparkContext.foreach(sparkContext => setSparkHistoryServerURI(execution, sparkContext))
      getXDSession(Properties.envOrNone(UserNameEnv)).foreach(session => setDispatcherSettings(execution, session.sparkContext))
      spartaWorkflow.postExecutionStep()
    } match {
      case Success(_) =>
        finishClusterBatchContext(spartaWorkflow, Map.empty)
      case Failure(e) =>
        finishClusterBatchContext(spartaWorkflow, Map(GraphStep.FailedKey -> e.getLocalizedMessage))
        throw e
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

  private[driver] def setSparkHistoryServerURI(execution: WorkflowExecution, sparkContext: SparkContext): Unit = {
    import execution.genericDataExecution.workflow.settings.sparkSettings.sparkConf.sparkHistoryServerConf._
    if(execution.marathonExecution.isDefined && enableHistoryServerMonitoring && sparkHistoryServerMonitoringURL.isDefined) {
      val applicationId = sparkContext.applicationId
      val historyServerURI =
        if (enableHistoryServerMonitoring && sparkHistoryServerMonitoringURL.isDefined)
          Option(s"${sparkHistoryServerMonitoringURL.get.toString}/history/$applicationId/jobs")
        else None

      log.debug(s"Setting sparkHistoryServerMonitoringURL to ${historyServerURI.getOrElse("None")} for execution ${execution.id.getOrElse("No id")} with spark.app.id = $applicationId")

      executionService.updateExecutionSparkURI(execution.copy(
        marathonExecution =
          Option(execution.marathonExecution.get.copy(historyServerURI = historyServerURI)
          )))
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

  private[driver] def saveQualityRuleResultsToPostgres(qualityRulesResults: Seq[SparkQualityRuleResults], executionId: String): Unit =
    qualityRulesResults.foreach { sparkResult =>
      qualityRuleService.createQualityRuleResult(
        QualityRuleResult(
          executionId = executionId,
          dataQualityRuleId = sparkResult.dataQualityRuleId,
          numTotalEvents = sparkResult.numTotalEvents,
          numPassedEvents = sparkResult.numPassedEvents,
          numDiscardedEvents = sparkResult.numDiscardedEvents,
          metadataPath = sparkResult.metadataPath,
          transformationStepName = sparkResult.transformationStepName,
          outputStepName = sparkResult.outputStepName,
          satisfied = sparkResult.satisfied,
          successfulWriting = sparkResult.successfulWriting,
          conditionThreshold = sparkResult.condition,
          qualityRuleName = sparkResult.qualityRuleName,
          conditionsString = sparkResult.conditionsString,
          globalAction = sparkResult.globalAction
        )
      )
    }


  private[driver] def finishClusterBatchContext(
                                                 spartaWorkflow: SpartaWorkflow[RDD],
                                                 cleanUpProperties: Map[String, String]
                                               ): Unit = {
    finishLocalBatchContext(spartaWorkflow, cleanUpProperties)
    stopContexts()
  }

  private[driver] def finishLocalBatchContext(
                                               spartaWorkflow: SpartaWorkflow[RDD],
                                               cleanUpProperties: Map[String, String]
                                             ): Unit = {
    log.debug("Executing cleanUp in workflow steps ...")
    spartaWorkflow.cleanUp(cleanUpProperties)
    log.debug("CleanUp in workflow steps executed")
  }

  private[driver] def finishClusterStreamingContext(
                                                     spartaWorkflow: SpartaWorkflow[DStream],
                                                     cleanUpProperties: Map[String, String]
                                                   ): Unit = {
    finishLocalStreamingContext(spartaWorkflow, cleanUpProperties)
    stopContexts()
  }

  private[driver] def finishLocalStreamingContext(
                                                   spartaWorkflow: SpartaWorkflow[DStream],
                                                   cleanUpProperties: Map[String, String]
                                                 ): Unit = {
    log.debug("Executing cleanUp in workflow steps ...")
    spartaWorkflow.cleanUp(cleanUpProperties)
    log.debug("CleanUp in workflow steps executed")
  }
  
}