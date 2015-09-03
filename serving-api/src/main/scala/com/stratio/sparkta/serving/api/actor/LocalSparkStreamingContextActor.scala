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

package com.stratio.sparkta.serving.api.actor

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparkta.serving.core.{SparktaConfig, AppConstant}
import com.stratio.sparkta.serving.core.helpers.JarsHelper
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum
import org.apache.spark.streaming.StreamingContext
import java.io.File

import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

class LocalSparkStreamingContextActor(policy: AggregationPoliciesModel,
                                      streamingContextService: StreamingContextService,
                                      policyStatusActor: ActorRef) extends InstrumentedActor {

  private var ssc: Option[StreamingContext] = None

  override def receive: PartialFunction[Any, Unit] = {
    case Start => doInitSparktaContext
  }

  private def doInitSparktaContext: Unit = {

    implicit val timeout: Timeout = Timeout(3.seconds)
    val jars = JarsHelper.findJarsByPath(new File(SparktaConfig.sparktaHome, AppConstant.JarPluginsFolder), true)

    Try({
      policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Starting))
      ssc = streamingContextService.standAloneStreamingContext(policy, jars)
      ssc.get.start
    }) match {
      case Success(_) => {
        policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Started))
      }
      case Failure(exception) => {
        log.error(exception.getLocalizedMessage, exception)
        policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Failed))
      }
    }
  }

  override def postStop(): Unit = {
    ssc match {
      case Some(sc: StreamingContext) =>
        SparkContextFactory.destroySparkStreamingContext
      case x => log.warn("Unrecognized standalone StreamingContext to stop!", x)
    }
    super.postStop()
  }
}
