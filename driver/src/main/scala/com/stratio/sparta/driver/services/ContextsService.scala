/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.driver.services

import java.io.File

import akka.actor.ActorRef
import com.stratio.sparta.sdk.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.GraphStep
import com.stratio.sparta.serving.core.actor.StatusListenerActor.{ForgetWorkflowStatusActions, OnWorkflowStatusChangeDo}
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
import org.apache.spark.sql.Dataset
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

case class ContextsService(curatorFramework: CuratorFramework, listenerActor: ActorRef)

  extends SchedulerUtils with CheckpointUtils with DistributedMonadImplicits with ContextBuilderImplicits {

  private val statusService = new WorkflowStatusService(curatorFramework)
  private val executionService = new ExecutionService(curatorFramework)

  def localStreamingContext(workflow: Workflow, files: Seq[File]): (SpartaWorkflow[DStream], StreamingContext) = {
    killLocalContextListener(workflow, workflow.name)

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

    (spartaWorkflow, ssc)
  }

  def localContext(workflow: Workflow, files: Seq[File]): SpartaWorkflow[RDD] = {
    killLocalContextListener(workflow, workflow.name)

    val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
    val sparkSubmitService = new SparkSubmitService(workflow)
    val sparkConfig = sparkSubmitService.getSparkLocalWorkflowConfig

    getOrCreateSparkContext(sparkConfig ++ stepsSparkConfig, files)

    val spartaWorkflow = SpartaWorkflow[RDD](workflow, curatorFramework)

    spartaWorkflow.setup()
    spartaWorkflow.stages()
    spartaWorkflow
  }

  def clusterStreamingContext(workflow: Workflow, files: Seq[String]): SpartaWorkflow[DStream] = {
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
    spartaWorkflow
  }

  def clusterContext(workflow: Workflow, files: Seq[String]): SpartaWorkflow[RDD] = {
    val stepsSparkConfig = getConfigurationsFromObjects(workflow, GraphStep.SparkConfMethod)
    val sparkContext = getOrCreateClusterSparkContext(stepsSparkConfig, files)
    val spartaWorkflow = SpartaWorkflow[RDD](workflow, curatorFramework)

    setDispatcherSettings(workflow, sparkContext)
    spartaWorkflow.setup()
    notifyWorkflowStarted(workflow)
    spartaWorkflow.stages()
    spartaWorkflow
  }

  private[driver] def killLocalContextListener(workflow: Workflow, name: String): Unit = {
    log.info(s"Listener added for workflow ${workflow.name}")

    listenerActor ! OnWorkflowStatusChangeDo(workflow.id.get) { workflowStatusStream =>
      if (workflowStatusStream.workflowStatus.status == Stopping ||
        workflowStatusStream.workflowStatus.status == Stopped)
        try {
          log.info("Stopping message received from Zookeeper")
          closeContexts(workflow.id.get)
        } finally {
          listenerActor ! ForgetWorkflowStatusActions(workflow.id.get)
        }
    }

  }

  private[driver] def closeContexts(workflowId: String): Unit = {
    val information = "The Context was successfully closed in the local listener"
    log.info(information)
    statusService.update(WorkflowStatus(
      id = workflowId,
      status = Finished,
      statusInfo = Some(information)
    ))
    stopSparkContext()
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
    val startedInfo = s"Workflow in Spark driver was properly launched"
    log.info(startedInfo)
    statusService.update(WorkflowStatus(
      id = workflow.id.get,
      status = Started,
      statusInfo = Some(startedInfo)
    ))
  }
}