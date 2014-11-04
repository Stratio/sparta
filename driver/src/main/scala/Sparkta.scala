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
import java.io.File

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import com.stratio.sparkta.driver.actor.{SupervisorActor, PolicyControllerActor}
import com.stratio.sparkta.driver.configuration.GeneralConfiguration
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.exception.DriverException
import com.stratio.sparkta.driver.service.StreamingContextService
import org.json4s._
import org.json4s.jackson.JsonMethods._
import spray.can.Http

import scala.io.Source

/**
 * Created by ajnavarro on 2/10/14.
 */
object Sparkta extends App with SLF4JLogging {

  implicit val formats = DefaultFormats

  if (args.size != 1) {
    throw new DriverException("Usage: \n - param 1: Path with configuration files.")
  }

  val basePath = new File(args(0))
  val configurationFile = new File(basePath, "configuration.json")
  val generalConfiguration = parse(Source.fromFile(configurationFile).getLines().mkString).extract[GeneralConfiguration]

  implicit val system = ActorSystem("sparkta")
  val streamingContextService = new StreamingContextService(generalConfiguration)

  val supervisor: ActorRef = system.actorOf(Props(new SupervisorActor(streamingContextService)), "supervisor")

  val controller = system.actorOf(Props(
    new PolicyControllerActor(supervisor)), "workflowController")

  IO(Http) ! Http.Bind(controller, interface = "localhost", port = 8080)

}
