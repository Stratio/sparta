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

import com.stratio.sparta.driver.SpartaWorkflow
import com.stratio.sparta.driver.factory.SparkContextFactory._
import com.stratio.sparta.sdk.workflow.step.GraphStep
import com.stratio.sparta.serving.core.helpers.WorkflowHelper._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowStatus}
import com.stratio.sparta.serving.core.services.{ListenerService, SparkSubmitService, WorkflowStatusService}
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, SchedulerUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

case class StreamingContextService(curatorFramework: CuratorFramework)
  extends SchedulerUtils with CheckpointUtils {

  private val statusService = new WorkflowStatusService(curatorFramework)
  private val listenerService = new ListenerService(curatorFramework)

  def localStreamingContext(workflow: Workflow, files: Seq[File]): (SpartaWorkflow, StreamingContext) = {
    killLocalContextListener(workflow, workflow.name)

    import workflow.settings.streamingSettings.checkpointSettings._

    if (enableCheckpointing) {
      if (autoDeleteCheckpoint) deleteCheckpointPath(workflow)
      createLocalCheckpointPath(workflow)
    }

    val stepsSparkConfig = getConfigurationsFromObjects(workflow.pipelineGraph.nodes, GraphStep.SparkConfMethod)
    val sparkSubmitService = new SparkSubmitService(workflow)
    val sparkConfig = sparkSubmitService.getSparkLocalConfig

    sparkStandAloneContextInstance(sparkConfig ++ stepsSparkConfig, files)

    val spartaWorkflow = SpartaWorkflow(workflow, curatorFramework)
    val ssc = spartaWorkflow.streamingStages()

    setSparkContext(ssc.sparkContext)
    setSparkStreamingContext(ssc)
    setInitialSentences(workflow.settings.global.initSqlSentences.map(modelSentence => modelSentence.sentence))

    (spartaWorkflow, ssc)
  }

  def clusterStreamingContext(workflow: Workflow, files: Seq[String]): (SpartaWorkflow, StreamingContext) = {

    val spartaWorkflow = SpartaWorkflow(workflow, curatorFramework)
    val ssc = {
      import workflow.settings.streamingSettings.checkpointSettings._

      if (enableCheckpointing) {
        if (autoDeleteCheckpoint) deleteCheckpointPath(workflow)
        StreamingContext.getOrCreate(checkpointPathFromWorkflow(workflow), () => {
          log.info(s"Nothing in checkpoint path: ${checkpointPathFromWorkflow(workflow)}")
          val stepsSparkConfig = getConfigurationsFromObjects(workflow.pipelineGraph.nodes, GraphStep.SparkConfMethod)
          sparkClusterContextInstance(stepsSparkConfig, files)
          spartaWorkflow.streamingStages()
        })
      } else {
        val stepsSparkConfig = getConfigurationsFromObjects(workflow.pipelineGraph.nodes, GraphStep.SparkConfMethod)
        sparkClusterContextInstance(stepsSparkConfig, files)
        spartaWorkflow.streamingStages()
      }
    }

    setSparkContext(ssc.sparkContext)
    setSparkStreamingContext(ssc)
    setInitialSentences(workflow.settings.global.initSqlSentences.map(modelSentence => modelSentence.sentence))

    (spartaWorkflow, ssc)
  }

  private[driver] def killLocalContextListener(workflow: Workflow, name: String): Unit = {
    log.info(s"Listener added for workflow ${workflow.name}")
    listenerService.addWorkflowStatusListener(
      workflow.id.get,
      (workflowStatus: WorkflowStatus, nodeCache: NodeCache) =>
        synchronized {
          if (workflowStatus.status == Stopping)
            try {
              log.info("Stopping message received from Zookeeper")
              closeContexts(workflow.id.get)
            } finally {
              Try(nodeCache.close()) match {
                case Success(_) =>
                  log.info("Node cache correctly closed")
                case Failure(e) =>
                  log.error(s"The node cache in Zookeeper was noy  correctly closed", e)
              }
            }
        }
    )
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