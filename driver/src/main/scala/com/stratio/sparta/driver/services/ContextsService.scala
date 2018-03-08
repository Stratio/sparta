/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.driver.services

import java.io.File

import akka.actor.ActorRef
import com.stratio.sparta.sdk.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.GraphStep
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.helpers.WorkflowHelper._
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

case class ContextsService(curatorFramework: CuratorFramework, listenerActor: ActorRef)

  extends SchedulerUtils with CheckpointUtils with DistributedMonadImplicits with ContextBuilderImplicits {

  private val statusService = new WorkflowStatusService(curatorFramework)
  private val executionService = new ExecutionService(curatorFramework)

  def localStreamingContext(workflow: Workflow, files: Seq[File]): Unit = {
    import workflow.settings.streamingSettings.checkpointSettings._

    if (enableCheckpointing) {
      if (autoDeleteCheckpoint) deleteCheckpointPath(workflow)
      createLocalCheckpointPath(workflow)
    }

    val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
    val sparkSubmitService = new SparkSubmitService(workflow)
    val sparkConfig = sparkSubmitService.getSparkLocalWorkflowConfig

    getOrCreateSparkContext(sparkConfig ++ stepsSparkConfig, files)

    val spartaWorkflow = SpartaWorkflow[DStream](workflow, curatorFramework)
    spartaWorkflow.stages()
    val ssc = getStreamingContext

    spartaWorkflow.setup()
    ssc.start()
    notifyWorkflowStarted(workflow)
    ssc.awaitTermination()
    spartaWorkflow.cleanUp()
  }

  def localContext(workflow: Workflow, files: Seq[File]): Unit = {
    val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
    val sparkSubmitService = new SparkSubmitService(workflow)
    val sparkConfig = sparkSubmitService.getSparkLocalWorkflowConfig

    getOrCreateSparkContext(sparkConfig ++ stepsSparkConfig, files)

    val spartaWorkflow = SpartaWorkflow[RDD](workflow, curatorFramework)

    spartaWorkflow.setup()
    notifyWorkflowStarted(workflow)
    spartaWorkflow.stages()
    spartaWorkflow.cleanUp()
  }

  def clusterStreamingContext(workflow: Workflow, files: Seq[String]): Unit = {
    val spartaWorkflow = SpartaWorkflow[DStream](workflow, curatorFramework)
    val ssc = {
      import workflow.settings.streamingSettings.checkpointSettings._

      if (enableCheckpointing) {
        if (autoDeleteCheckpoint) deleteCheckpointPath(workflow)
        StreamingContext.getOrCreate(checkpointPathFromWorkflow(workflow), () => {
          log.info(s"Nothing in checkpoint path: ${checkpointPathFromWorkflow(workflow)}")
          val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
          getOrCreateClusterSparkContext(stepsSparkConfig, files)
          spartaWorkflow.stages()
          getStreamingContext
        })
      } else {
        val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
        getOrCreateClusterSparkContext(stepsSparkConfig, files)
        spartaWorkflow.stages()
        getStreamingContext
      }
    }

    setDispatcherSettings(workflow, ssc.sparkContext)
    spartaWorkflow.setup()
    ssc.start
    notifyWorkflowStarted(workflow)
    ssc.awaitTermination()
    spartaWorkflow.cleanUp()
  }

  def clusterContext(workflow: Workflow, files: Seq[String]): Unit = {
    val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
    val sparkContext = getOrCreateClusterSparkContext(stepsSparkConfig, files)
    val spartaWorkflow = SpartaWorkflow[RDD](workflow, curatorFramework)

    setDispatcherSettings(workflow, sparkContext)
    spartaWorkflow.setup()
    notifyWorkflowStarted(workflow)
    spartaWorkflow.stages()
    spartaWorkflow.cleanUp()
  }

  private[driver] def setDispatcherSettings(workflow: Workflow, sparkContext: SparkContext): Unit = {
    for {
      status <- statusService.findById(workflow.id.get)
      execution <- executionService.findById(workflow.id.get)
      execMode <- status.lastExecutionMode
    } if (execMode.contains(ConfigMesos))
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
    val startedInfo = s"Workflow started correctly"
    log.info(startedInfo)
    statusService.update(WorkflowStatus(
      id = workflow.id.get,
      status = Started,
      statusInfo = Some(startedInfo)
    ))
  }
}