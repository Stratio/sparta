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
import akka.event.slf4j.SLF4JLogging
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparta.driver.factory.SparkContextFactory
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.driver.util.PolicyUtils
import com.stratio.sparta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparta.serving.core.dao.ErrorDAO
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel, SpartaSerializer}
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparta.serving.core.policy.status.PolicyStatusEnum
import org.apache.spark.streaming.StreamingContext

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class LocalSparkStreamingContextActor(policy: AggregationPoliciesModel,
                                      streamingContextService: StreamingContextService,
                                      policyStatusActor: ActorRef) extends Actor
  with SLF4JLogging
  with SpartaSerializer {

  private var ssc: Option[StreamingContext] = None

  override def receive: PartialFunction[Any, Unit] = {
    case Start => doInitSpartaContext
  }

  private def doInitSpartaContext: Unit = {

    implicit val timeout: Timeout = Timeout(3.seconds)
    val jars = PolicyUtils.jarsFromPolicy(policy)
    jars.foreach(file => JarsHelper.addToClasspath(file))

    Try({
      policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Starting))
      Try(ErrorDAO().dao.delete(policy.id.get))
      ssc = streamingContextService.standAloneStreamingContext(policy, jars)
      ssc.get.start()
    }) match {
      case Success(_) =>
        policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Started))
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Failed))
        SparkContextFactory.destroySparkStreamingContext()
        SparkContextFactory.destroySparkContext()
    }
  }

  override def postStop(): Unit = {
    ssc match {
      case Some(sc: StreamingContext) =>
        SparkContextFactory.destroySparkStreamingContext()
      case x => log.warn("Unrecognized Standalone StreamingContext to stop!", x)
    }
    super.postStop()
  }
}
