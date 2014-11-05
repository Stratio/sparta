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
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import com.stratio.sparkta.driver.actor.{PolicyControllerActor, SupervisorActor}
import com.stratio.sparkta.driver.service.StreamingContextService
import com.typesafe.config.ConfigFactory
import spray.can.Http

/**
 * Created by ajnavarro on 2/10/14.
 */
object Sparkta extends App with SLF4JLogging {

  val sparktaConfig = ConfigFactory.load().getConfig("sparkta")

  implicit val system = ActorSystem("sparkta")

  val streamingContextService = new StreamingContextService(sparktaConfig)

  val supervisor: ActorRef = system.actorOf(Props(new SupervisorActor(streamingContextService)), "supervisor")

  val controller = system.actorOf(Props(
    new PolicyControllerActor(supervisor)), "workflowController")

  val apiConfig = sparktaConfig.getConfig("api")

  IO(Http) ! Http.Bind(controller, interface = apiConfig.getString("host"), port = apiConfig.getInt("port"))

}
