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

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.factory._
import com.stratio.sparkta.driver.models._
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.serving.core.messages.ActorsMessages._
import com.typesafe.config.Config
import org.apache.spark.streaming.StreamingContext
import scala.concurrent.duration._

class StreamingContextService(generalConfig: Config, jars: Seq[File]) extends SLF4JLogging {

  implicit val timeout: Timeout = Timeout(15.seconds)

  def createStreamingContext(apConfig: AggregationPoliciesModel, jobServerRef: Option[ActorRef]): StreamingContext = {
    val OutputsSparkConfiguration = "getSparkConfiguration"

    jobServerRef.foreach(actorRef => {
      val activeJars = SparktaJob.activeJars(apConfig, jars)
      if (activeJars.isLeft) {
        log.warn(s"The policy have jars witch cannot be found in classpath:")
        activeJars.left.get.foreach(log.warn)
      } else {
        val activeJarsFilesToSend = SparktaJob.activeJarFiles(activeJars.right.get, jars)
        actorRef ? new JobServerSupervisorActor_uploadJars(activeJarsFilesToSend)
      }
    })

    val specifictSparkConfig = SparktaJob.getSparkConfigs(apConfig, OutputsSparkConfiguration, Output.ClassSuffix)
    val sc = SparkContextFactory.sparkContextInstance(generalConfig, specifictSparkConfig, jars)
    SparktaJob.runSparktaJob(sc, apConfig)
    SparkContextFactory.sparkStreamingInstance.get
  }
}
