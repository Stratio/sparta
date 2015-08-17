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

import scala.concurrent.duration._
import scala.sys.process._

import akka.util.Timeout
import com.typesafe.config.Config
import org.json4s._
import org.json4s.native.Serialization

import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor.StreamingActor._
import com.stratio.sparkta.serving.core.models.AggregationPoliciesModel

class ClusterContextActor(policy: AggregationPoliciesModel,
                          streamingContextService: StreamingContextService,
                          cfg: Config) extends InstrumentedActor {

  implicit val timeout: Timeout = Timeout(90.seconds)
  implicit val json4sJacksonFormats = DefaultFormats
  implicit val formats = Serialization.formats(NoTypeHints)

  override def receive: PartialFunction[Any, Unit] = {
    case InitSparktaContext => doInitSparktaContext
  }

  def doInitSparktaContext: Unit = {
    log.debug("Init new cluster streamingContext with name " + policy.name)

    val cmd = s"spark-submit " +
      "--class com.stratio.sparkta.driver.SparktaJob " +
      s"--master ${cfg.getString("spark.master")} " +
      s"${cfg.getString("spark.extra")}} driver/target/driver-plugin.jar ${policy.name}"
    cmd.!!
  }
}
