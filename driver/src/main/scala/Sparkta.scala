/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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

import java.io.File

import akka.actor.{ActorSystem, Props}
import com.stratio.sparkta.driver.actor.{Print, SupervisorActor}
import com.stratio.sparkta.driver.configuration.GeneralConfiguration
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.exception.DriverException
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationDouble
import scala.io.Source

/**
 * Created by ajnavarro on 2/10/14.
 */
object Sparkta extends App {

  sys addShutdownHook (shutdown)

  implicit val formats = DefaultFormats

  if (args.size != 1) {
    throw new DriverException("Usage: \n - param 1: Path with configuration files.")
  }

  val basePath = new File(args(0))
  val policiesFiles = new File(basePath, "policies").listFiles.filter(_.getName.endsWith(".json"))
  val aggregationPolicies: Seq[AggregationPoliciesDto] = policiesFiles.map(f =>
    parse(Source.fromFile(f).getLines().mkString)
      .extract[AggregationPoliciesDto]
  )

  val configurationFile = new File(basePath, "configuration.json")
  val generalConfiguration = parse(Source.fromFile(configurationFile).getLines().mkString).extract[GeneralConfiguration]

  val system = ActorSystem("sparkta")

  val supervisor = system.actorOf(Props(new SupervisorActor), "supervisor")
  aggregationPolicies.foreach(policy => supervisor !(policy, generalConfiguration))

  system.scheduler.schedule(1 seconds, 5 seconds, supervisor, Print)

  private def shutdown {
    if (system != null)
      system.shutdown()
  }
}
