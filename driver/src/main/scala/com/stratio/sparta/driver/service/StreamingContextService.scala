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
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.helpers.PolicyHelper
import com.stratio.sparta.serving.core.models.policy.PolicyModel
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, SchedulerUtils}
import com.typesafe.config.Config
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.streaming.StreamingContext

import scala.util.Try

case class StreamingContextService(curatorFramework: CuratorFramework, generalConfig: Option[Config] = None)
  extends SchedulerUtils with CheckpointUtils with LocalListenerUtils {

  final val OutputsSparkConfiguration = "getSparkConfiguration"

  def localStreamingContext(policy: PolicyModel, files: Seq[File]): StreamingContext = {
    killLocalContextListener(policy, policy.name)

    if (autoDeleteCheckpointPath(policy)) deleteCheckpointPath(policy)

    createLocalCheckpointPath(policy)

    val outputsSparkConfig = PolicyHelper.getSparkConfigs(policy, OutputsSparkConfiguration, Output.ClassSuffix)
    val policySparkConfig = PolicyHelper.getSparkConfigFromPolicy(policy)
    val propsConfig = Try(PolicyHelper.getSparkConfFromProps(generalConfig.get.getConfig(ConfigLocal)))
      .getOrElse(Map.empty[String, String])

    sparkStandAloneContextInstance(propsConfig ++ policySparkConfig ++ outputsSparkConfig, files)

    val ssc = SpartaWorkflow(policy, curatorFramework).run()

    setSparkContext(ssc.sparkContext)
    setSparkStreamingContext(ssc)
    setInitialSentences(policy.initSqlSentences.map(modelSentence => modelSentence.sentence))

    ssc
  }

  def clusterStreamingContext(policy: PolicyModel, files: Seq[String])
  : StreamingContext = {
    if (autoDeleteCheckpointPath(policy)) deleteCheckpointPath(policy)

    val ssc = StreamingContext.getOrCreate(checkpointPath(policy), () => {
      log.info(s"Nothing in checkpoint path: ${checkpointPath(policy)}")
      val outputsSparkConfig = PolicyHelper.getSparkConfigs(policy, OutputsSparkConfiguration, Output.ClassSuffix)
      sparkClusterContextInstance(outputsSparkConfig, files)
      SpartaWorkflow(policy, curatorFramework).run()
    })

    setSparkContext(ssc.sparkContext)
    setSparkStreamingContext(ssc)
    setInitialSentences(policy.initSqlSentences.map(modelSentence => modelSentence.sentence))

    ssc
  }
}