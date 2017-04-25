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
import com.stratio.sparta.driver.writer.TransformationsWriterHelper
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.utils.AggregationTime
import com.stratio.sparta.serving.core.helpers.PolicyHelper
import com.stratio.sparta.serving.core.models.policy._
import com.stratio.sparta.serving.core.utils.CheckpointUtils
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.streaming.{Duration, StreamingContext}

class SpartaWorkflow(val policy: PolicyModel, val curatorFramework: CuratorFramework) extends CheckpointUtils
  with InputStage with OutputStage with ParserStage with CubeStage with RawDataStage with TriggerStage
  with ZooKeeperError {

  private val ReflectionUtils = PolicyHelper.ReflectionUtils

  def run(): StreamingContext = {
    clearError()
    val outputs = outputStage(ReflectionUtils)

    outputs.foreach(output => output.setup())

    val checkpointPolicyPath = checkpointPath(policy)
    val window = AggregationTime.parseValueToMilliSeconds(policy.sparkStreamingWindow)
    val ssc = sparkStreamingInstance(Duration(window), checkpointPolicyPath, policy.remember)
    val inputDStream = inputStreamStage(ssc.get, ReflectionUtils)

    saveRawData(policy.rawData, inputDStream, outputs)

    policy.transformations.foreach { transformationsModel =>
      val parserSchemas = SchemaHelper.getSchemasFromTransformations(
        transformationsModel.transformationsPipe, Input.InitSchema)
      val (parsers, writerOptions) = parserStage(ReflectionUtils, parserSchemas)
      val parsedData = ParserStage.applyParsers(
        inputDStream, parsers, parserSchemas.values.last, outputs, writerOptions)

      triggersStreamStage(parserSchemas.values.last, parsedData, outputs, window)
      cubesStreamStage(ReflectionUtils, parserSchemas.values.last, parsedData, outputs)
    }

    ssc.get
  }

}

object SpartaWorkflow {

  def apply(policy: PolicyModel, curatorFramework: CuratorFramework): SpartaWorkflow =
    new SpartaWorkflow(policy, curatorFramework)
}
