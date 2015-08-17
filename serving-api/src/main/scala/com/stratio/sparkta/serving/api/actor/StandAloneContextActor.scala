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

import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor.StreamingActor._
import com.stratio.sparkta.serving.core.models.AggregationPoliciesModel
import com.stratio.sparkta.serving.core.models.StreamingContextStatusEnum._
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

class StandAloneContextActor(policy: AggregationPoliciesModel,
                             streamingContextService: StreamingContextService) extends InstrumentedActor {

  private var ssc: Option[StreamingContext] = None
  private final val StartErrorException = "Problem starting up standalone SparkStreamingContext"
  private final val CreatingErrorException = "Problem creating standalone SparkStreamingContext"
  private final val InstantiatingErrorException = "Problem instantiating standalone SparkStreamingContext"

  override def receive: PartialFunction[Any, Unit] = {
    case InitSparktaContext => doInitSparktaContext
  }

  private def doInitSparktaContext: Unit = {
    log.debug("Init new standalone streamingContext with name " + policy.name)

    //TODO validate policy

    ssc = Try(streamingContextService.standAloneStreamingContext(policy)) match {
      case Success(_ssc) =>
        _ssc match {
          case Some(ssc) => Try(ssc.start()) match {
            case Failure(e: Exception) =>
              sender ! new ResponseCreateContext(new StatusContextActor(context.self,
                policy.name,
                Error,
                Some(StartErrorException)))
              None
            case _ =>
              sender ! new ResponseCreateContext(new StatusContextActor(context.self,
                policy.name,
                Initialized,
                None))
              Some(ssc)
          }
          case None =>
            sender ! new ResponseCreateContext(new StatusContextActor(context.self,
              policy.name,
              Error,
              Some(CreatingErrorException)))
            None
        }
      case Failure(e: Exception) =>
        log.error("Error in streamingContextService.standAloneStreamingContext", e)
        sender ! new ResponseCreateContext(new StatusContextActor(context.self,
          policy.name,
          ConfigurationError,
          Some(InstantiatingErrorException)))
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
