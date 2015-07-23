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
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.driver.models.AggregationPoliciesModel
import com.stratio.sparkta.driver.models.StreamingContextStatusEnum._
import com.stratio.sparkta.driver.service.StreamingContextService
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

case object Init

case class InitError(e: Exception)

case object Stop

class ClusterContextActor(policy: AggregationPoliciesModel,
                          streamingContextService: StreamingContextService,
                          jobServerRef: ActorRef) extends InstrumentedActor {

  private var ssc: Option[StreamingContext] = None

  override def receive: PartialFunction[Any, Unit] = {
    case Init =>
      log.debug("Init new standalone streamingContext with name " + policy.name)
      ssc = Try(streamingContextService.clusterStreamingContext(policy, jobServerRef)) match {
        case Success(_ssc) =>
          Try(_ssc.start()) match {
            case Failure(e: Exception) =>
              log.error(s"Exception starting up standalone SparkStreamingContext for policy ${policy.name}", e)
              sender ! InitError(e)
            case x =>
              log.debug("Standalone StreamingContext started successfully.")
          }
          sender ! Initialized
          log.debug("Standalone StreamingContext initialized with name " + policy.name)
          Some(_ssc)
        case Failure(e: Exception) =>
          log.error(s"Exception instantiating policy ${policy.name}", e)
          sender ! InitError(e)
          None
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
