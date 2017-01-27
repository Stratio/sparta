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

package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, ActorRef}
import com.stratio.sparta.driver.factory.SparkContextFactory
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.api.actor.LauncherActor._
import com.stratio.sparta.serving.core.actor.StatusActor.Update
import com.stratio.sparta.serving.core.dao.ErrorDAO
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.PolicyUtils
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

class LocalLauncherActor(streamingContextService: StreamingContextService, statusActor: ActorRef)
  extends Actor
    with PolicyUtils {

  private var ssc: Option[StreamingContext] = None

  override def receive: PartialFunction[Any, Unit] = {
    case Start(policy: PolicyModel) => doInitSpartaContext(policy)
  }

  private def doInitSpartaContext(policy: PolicyModel): Unit = {
    val jars = jarsFromPolicy(policy)

    jars.foreach(file => JarsHelper.addToClasspath(file))
    Try {
      val startingInformation = s"Starting Sparta Streaming Context Job for policy:  ${policy.name}"
      log.info(startingInformation)
      statusActor ! Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Starting, None, None,
        Some(startingInformation)))

      ssc = Option(streamingContextService.localStreamingContext(policy, jars))
      ssc.get.start()

      val startedInformation = s"The Sparta Streaming Context Job was started correctly"
      log.info(startedInformation)
      statusActor ! Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Started, None, None,
        Some(startedInformation)))

      ssc.get.awaitTermination()
    } match {
      case Success(_) =>
        val information = s"Stopped correctly Sparta Streaming Context Job for policy: ${policy.id.get}"
        log.info(information)
        statusActor ! Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Stopped, None, None,
          Some(information)))
      case Failure(exception) =>
        val information = s"Error initiating Sparta Streaming Context Job: ${exception.getLocalizedMessage}"
        log.error(information, exception)
        statusActor ! Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Failed, None, None,
          Some(information)))

        SparkContextFactory.destroySparkContext(destroyStreamingContext = true)
    }
  }
}
