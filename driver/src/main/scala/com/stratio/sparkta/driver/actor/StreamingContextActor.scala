/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.driver.actor

import com.stratio.sparkta.driver.actor.StreamingContextStatusEnum._
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.service.StreamingContextService
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

/**
 * Created by ajnavarro on 3/10/14.
 */
case object Init

case class InitError(e: Exception)

case object Stop

class StreamingContextActor
(policy: AggregationPoliciesDto, streamingContextService: StreamingContextService) extends InstrumentedActor {

  private var ssc: Option[StreamingContext] = None

  override def receive = {
    case Init =>
      log.debug("Init new streamingContext with name " + policy.name)
      ssc = Try(streamingContextService.createStreamingContext(policy)) match {
        case Success(_ssc) =>
          Try(_ssc.start()) match {
            case Failure(e: Exception) =>
              sender ! InitError(e)
            case x => log.debug("StreamingContext started successfully.")
          }
          sender ! Initialized
          log.debug("StreamingContext initialized with name " + policy.name)
          Some(_ssc)
        case Failure(e: Exception) =>
          sender ! InitError(e)
          None
      }
  }

  override def postStop(): Unit = {
    ssc match {
      case Some(sc: StreamingContext) =>
        log.debug("Stopping streamingContext with name: " + sc.sparkContext.appName)
        sc.stop()
        log.debug("Stopped streamingContext with name: " + sc.sparkContext.appName)
      case x => log.warn("Unrecognized StreamingContext to stop!", x)
    }
    super.postStop()
  }

}
