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

package com.stratio.sparta.driver

import com.stratio.sparta.driver.factory.SparkContextFactory._
import com.stratio.sparta.driver.schema.SchemaHelper
import com.stratio.sparta.driver.stage._
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.utils.AggregationTime
import com.stratio.sparta.serving.core.helpers.WorkflowHelper
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.utils.CheckpointUtils
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.streaming.{Duration, StreamingContext}

class SpartaWorkflow(val workflow: WorkflowModel, val curatorFramework: CuratorFramework) extends CheckpointUtils
  with InputStage with OutputStage with ParserStage with CubeStage with RawDataStage with TriggerStage
  with ZooKeeperError {

  clearError()

  private val ReflectionUtils = WorkflowHelper.ReflectionUtils
  private var outputs : Option[Seq[Output]] = None
  private var input: Option[Input] = None

  def setup(): Unit = {
    input.foreach(input => input.setUp())
    outputs.foreach(outputs => outputs.foreach(_.setUp()))
  }

  def cleanUp(): Unit = {
    input.foreach(input => input.cleanUp())
    outputs.foreach(outputs => outputs.foreach(_.cleanUp()))
  }

  def streamingStages(): StreamingContext = {
    clearError()

    val checkpointPolicyPath = checkpointPath(workflow)
    val window = AggregationTime.parseValueToMilliSeconds(workflow.settings.streamingSettings.window)
    val ssc = sparkStreamingInstance(Duration(window),
      checkpointPolicyPath,
      workflow.settings.streamingSettings.remember
    )
    val sparkSession = xdSessionInstance
    if(input.isEmpty)
      input = Option(createInput(ssc.get, sparkSession, ReflectionUtils))
    if(outputs.isEmpty)
      outputs = Option(outputStage(ReflectionUtils, sparkSession))
    val inputDStream = inputStreamStage(input.get)

    saveRawData(workflow.rawData, inputDStream, outputs.get)

    workflow.transformations.foreach { transformationsModel =>
      val parserSchemas = SchemaHelper.getSchemasFromTransformations(
        transformationsModel.transformationsPipe, Input.InitSchema)
      val (parsers, writerOptions) = parserStage(ReflectionUtils, parserSchemas)
      val parsedData = ParserStage.applyParsers(
        inputDStream, parsers, parserSchemas.values.last, outputs.get, writerOptions)

      triggersStreamStage(parserSchemas.values.last, parsedData, outputs.get, window)
      cubesStreamStage(ReflectionUtils, parserSchemas.values.last, parsedData, outputs.get)
    }

    ssc.get
  }
}

object SpartaWorkflow {

  def apply(policy: WorkflowModel, curatorFramework: CuratorFramework): SpartaWorkflow =
    new SpartaWorkflow(policy, curatorFramework)
}
