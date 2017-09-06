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
import com.stratio.sparta.driver.utils.LocalListenerUtils
import com.stratio.sparta.sdk.workflow.step.GraphStep
import com.stratio.sparta.serving.core.helpers.WorkflowHelper._
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, SchedulerUtils, SparkSubmitService}
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.streaming.StreamingContext

case class StreamingContextService(curatorFramework: CuratorFramework)
  extends SchedulerUtils with CheckpointUtils with LocalListenerUtils {

  def localStreamingContext(workflow: Workflow, files: Seq[File]): (SpartaWorkflow, StreamingContext) = {
    killLocalContextListener(workflow, workflow.name)

    if (workflow.settings.checkpointSettings.enableCheckpointing) {
      if (workflow.settings.checkpointSettings.autoDeleteCheckpoint)
        deleteCheckpointPath(workflow)
      createLocalCheckpointPath(workflow)
    }

    val stepsSparkConfig = getSparkConfsReflec(workflow.pipelineGraph.nodes, GraphStep.SparkConfMethod)
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
      if (workflow.settings.checkpointSettings.enableCheckpointing) {
        if (workflow.settings.checkpointSettings.autoDeleteCheckpoint) deleteCheckpointPath(workflow)
        StreamingContext.getOrCreate(checkpointPath(workflow), () => {
          log.info(s"Nothing in checkpoint path: ${checkpointPath(workflow)}")
          val stepsSparkConfig = getSparkConfsReflec(workflow.pipelineGraph.nodes, GraphStep.SparkConfMethod)
          sparkClusterContextInstance(stepsSparkConfig, files)
          spartaWorkflow.streamingStages()
        })
      } else {
        val stepsSparkConfig = getSparkConfsReflec(workflow.pipelineGraph.nodes, GraphStep.SparkConfMethod)
        sparkClusterContextInstance(stepsSparkConfig, files)
        spartaWorkflow.streamingStages()
      }
    }

    setSparkContext(ssc.sparkContext)
    setSparkStreamingContext(ssc)
    setInitialSentences(workflow.settings.global.initSqlSentences.map(modelSentence => modelSentence.sentence))

    (spartaWorkflow, ssc)
  }
}