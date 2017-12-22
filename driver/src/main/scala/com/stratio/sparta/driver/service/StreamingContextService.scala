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

package com.stratio.sparta.driver.service

import java.io.File

import akka.actor.ActorRef
import com.stratio.sparta.driver.SpartaWorkflow
import com.stratio.sparta.driver.factory.SparkContextFactory._
import com.stratio.sparta.sdk.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.step.GraphStep
import com.stratio.sparta.serving.core.actor.ListenerActor.{ForgetWorkflowActions, OnWorkflowChangeDo}
import com.stratio.sparta.serving.core.helpers.WorkflowHelper._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowStatus}
import com.stratio.sparta.serving.core.services.{SparkSubmitService, WorkflowStatusService}
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, SchedulerUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.sql.Dataset

case class StreamingContextService(curatorFramework: CuratorFramework, listenerActor: ActorRef)

  extends SchedulerUtils with CheckpointUtils with DistributedMonadImplicits with ContextBuilderImplicits {


  private val statusService = new WorkflowStatusService(curatorFramework)

  def localStreamingContext(workflow: Workflow, files: Seq[File]): (SpartaWorkflow[DStream], StreamingContext) = {
    killLocalContextListener(workflow, workflow.name)

    import workflow.settings.streamingSettings.checkpointSettings._

    if (enableCheckpointing) {
      if (autoDeleteCheckpoint) deleteCheckpointPath(workflow)
      createLocalCheckpointPath(workflow)
    }

    setInitialSentences(workflow.settings.global.initSqlSentences.map(modelSentence => modelSentence.sentence))
    
    val stepsSparkConfig = getConfigurationsFromObjects(workflow.pipelineGraph.nodes, GraphStep.SparkConfMethod)

    val sparkSubmitService = new SparkSubmitService(workflow)
    val sparkConfig = sparkSubmitService.getSparkLocalConfig

    sparkStandAloneContextInstance(sparkConfig ++ stepsSparkConfig, files)

    val spartaWorkflow = SpartaWorkflow[DStream](workflow, curatorFramework)
    spartaWorkflow.stages()

    val ssc = currentSparkStreamingInstance.get

    setSparkContext(ssc.sparkContext)
    setSparkStreamingContext(ssc)

    (spartaWorkflow, ssc)
  }

  def localContext(workflow: Workflow, files: Seq[File]): SpartaWorkflow[Dataset] = {
    killLocalContextListener(workflow, workflow.name)

    setInitialSentences(workflow.settings.global.initSqlSentences.map(modelSentence => modelSentence.sentence))

    val stepsSparkConfig = getConfigurationsFromObjects(workflow.pipelineGraph.nodes, GraphStep.SparkConfMethod)
    val sparkSubmitService = new SparkSubmitService(workflow)
    val sparkConfig = sparkSubmitService.getSparkLocalConfig

    sparkStandAloneContextInstance(sparkConfig ++ stepsSparkConfig, files)

    val spartaWorkflow = SpartaWorkflow[Dataset](workflow, curatorFramework)

    spartaWorkflow.setup()

    spartaWorkflow.stages()

    spartaWorkflow
  }

  def clusterStreamingContext(workflow: Workflow, files: Seq[String]): (SpartaWorkflow[DStream], StreamingContext) = {
    setInitialSentences(workflow.settings.global.initSqlSentences.map(modelSentence => modelSentence.sentence))

    val spartaWorkflow = SpartaWorkflow[DStream](workflow, curatorFramework)
    val ssc = {
      import workflow.settings.streamingSettings.checkpointSettings._

      if (enableCheckpointing) {
        if (autoDeleteCheckpoint) deleteCheckpointPath(workflow)
        StreamingContext.getOrCreate(checkpointPathFromWorkflow(workflow), () => {
          log.info(s"Nothing in checkpoint path: ${checkpointPathFromWorkflow(workflow)}")
          val stepsSparkConfig = getConfigurationsFromObjects(workflow.pipelineGraph.nodes, GraphStep.SparkConfMethod)
          sparkClusterContextInstance(stepsSparkConfig, files)
          spartaWorkflow.stages()
          currentSparkStreamingInstance.get
        })
      } else {
        val stepsSparkConfig = getConfigurationsFromObjects(workflow.pipelineGraph.nodes, GraphStep.SparkConfMethod)
        sparkClusterContextInstance(stepsSparkConfig, files)
        spartaWorkflow.stages()
        currentSparkStreamingInstance.get
      }
    }

    setSparkContext(ssc.sparkContext)
    setSparkStreamingContext(ssc)

    (spartaWorkflow, ssc)
  }

  def clusterContext(workflow: Workflow, files: Seq[String]): SpartaWorkflow[Dataset] = {
    setInitialSentences(workflow.settings.global.initSqlSentences.map(modelSentence => modelSentence.sentence))

    val stepsSparkConfig = getConfigurationsFromObjects(workflow.pipelineGraph.nodes, GraphStep.SparkConfMethod)
    val sparkSubmitService = new SparkSubmitService(workflow)
    val sparkConfig = sparkSubmitService.getSparkLocalConfig

    sparkClusterContextInstance(sparkConfig ++ stepsSparkConfig, files)

    val spartaWorkflow = SpartaWorkflow[Dataset](workflow, curatorFramework)

    spartaWorkflow.setup()

    spartaWorkflow.stages()

    spartaWorkflow
  }

  private[driver] def killLocalContextListener(workflow: Workflow, name: String): Unit = {
    log.info(s"Listener added for workflow ${workflow.name}")

    listenerActor ! OnWorkflowChangeDo(workflow.id.get) { workflowStatus =>
      if (workflowStatus.status == Stopping)
        try {
          log.info("Stopping message received from Zookeeper")
          closeContexts(workflow.id.get)
        } finally {
          listenerActor ! ForgetWorkflowActions(workflow.id.get)
        }
    }

  }

  private[driver] def closeContexts(workflowId: String): Unit = {
    val information = "The Context was successfully closed in the local listener"
    log.info(information)
    statusService.update(WorkflowStatus(
      id = workflowId,
      status = Stopped,
      statusInfo = Some(information)
    ))
    destroySparkContext()
  }
}