/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.driver.service

import java.io.File
import java.net.URI

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.driver.SparktaJob
import com.stratio.sparkta.driver.factory._
import com.stratio.sparkta.serving.core.AppConstant
import com.stratio.sparkta.serving.core.models._
import com.stratio.sparkta.sdk._
import com.typesafe.config.Config
import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext

import scala.util.{Success, Try}

case class StreamingContextService(generalConfig: Config) extends SLF4JLogging {

  final val OutputsSparkConfiguration = "getSparkConfiguration"

  def standAloneStreamingContext(apConfig: AggregationPoliciesModel, files: Seq[File]): Option[StreamingContext] = {
    SparktaJob.runSparktaJob(getStandAloneSparkContext(apConfig, files), apConfig)
    SparkContextFactory.sparkStreamingInstance
  }

  def clusterStreamingContext(apConfig: AggregationPoliciesModel, files : Seq[URI]): Option[StreamingContext] = {
    SparktaJob.runSparktaJob(getClusterSparkContext(apConfig, files), apConfig)
    SparkContextFactory.sparkStreamingInstance

  }

  private def getStandAloneSparkContext(apConfig: AggregationPoliciesModel, jars: Seq[File]): SparkContext = {
    val pluginsSparkConfig = SparktaJob.getSparkConfigs(apConfig, OutputsSparkConfiguration, Output.ClassSuffix)
    val standAloneConfig = Try(generalConfig.getConfig(AppConstant.ConfigLocal)) match {
      case Success(config) => Some(config)
      case _ => None
    }
    SparkContextFactory.sparkStandAloneContextInstance(standAloneConfig, pluginsSparkConfig, jars)
  }

  private def getClusterSparkContext(apConfig: AggregationPoliciesModel, classPath: Seq[URI]): SparkContext = {
    val pluginsSparkConfig = SparktaJob.getSparkConfigs(apConfig, OutputsSparkConfiguration, Output.ClassSuffix)
    SparkContextFactory.sparkClusterContextInstance(pluginsSparkConfig, classPath)
  }
}
