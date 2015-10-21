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

package com.stratio.sparkta.serving.api.helpers

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import akka.routing.RoundRobinPool
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.Sparkta._
import com.stratio.sparkta.serving.api.actor._
import com.stratio.sparkta.serving.api.constants.AkkaConstant
import com.stratio.sparkta.serving.core._
import com.stratio.sparkta.serving.core.models.{SparktaSerializer, PolicyStatusModel}
import com.stratio.sparkta.serving.core.policy.status.{PolicyStatusEnum, PolicyStatusActor}
import org.apache.zookeeper.KeeperException.NoNodeException
import org.json4s.jackson.Serialization._
import spray.can.Http

import scala.collection.JavaConversions
import scala.util.{Success, Failure, Try}

/**
 * Helper with common operations used to create a Sparkta context used to run the application.
 */
object SparktaHelper extends SLF4JLogging
with SparktaSerializer {

  implicit var system: ActorSystem = _

  /**
   * Initializes Sparkta's akka system running an embedded http server with the REST API.
   * @param appName with the name of the application.
   */

  def initAkkaSystem(appName: String): Unit = {
    if (SparktaConfig.mainConfig.isDefined &&
      SparktaConfig.apiConfig.isDefined &&
      SparktaConfig.swaggerConfig.isDefined) {
      val curatorFramework = CuratorFactoryHolder.getInstance()
      log.info("> Initializing akka actors")
      system = ActorSystem(appName)
      val akkaConfig = SparktaConfig.mainConfig.get.getConfig(AppConstant.ConfigAkka)
      val controllerInstances = if (!akkaConfig.isEmpty) akkaConfig.getInt(AkkaConstant.ControllerActorInstances)
      else AkkaConstant.DefaultControllerActorInstances
      val streamingActorInstances = if (!akkaConfig.isEmpty) akkaConfig.getInt(AkkaConstant.ControllerActorInstances)
      else AkkaConstant.DefaultStreamingActorInstances
      val policyStatusActor = system.actorOf(Props(new PolicyStatusActor()), AkkaConstant.PolicyStatusActor)
      val streamingContextService = new StreamingContextService(Some(policyStatusActor), SparktaConfig.mainConfig)
      implicit val actors = Map(
        AkkaConstant.PolicyStatusActor -> policyStatusActor,
        AkkaConstant.FragmentActor ->
          system.actorOf(Props(new FragmentActor(curatorFramework)), AkkaConstant.FragmentActor),
        AkkaConstant.TemplateActor ->
          system.actorOf(Props(new TemplateActor()), AkkaConstant.TemplateActor),
        AkkaConstant.PolicyActor ->
          system.actorOf(Props(new PolicyActor(curatorFramework, policyStatusActor)), AkkaConstant.PolicyActor),
        AkkaConstant.SparkStreamingContextActor -> system.actorOf(RoundRobinPool(streamingActorInstances).props(Props(
          new SparkStreamingContextActor(
            streamingContextService, policyStatusActor))),
          AkkaConstant.SparkStreamingContextActor)
      )
      val swaggerActor = system.actorOf(
        Props(new SwaggerActor(actors)), AkkaConstant.SwaggerActor)
      val controllerActor = system.actorOf(RoundRobinPool(controllerInstances)
        .props(Props(new ControllerActor(actors))), AkkaConstant.ControllerActor)

      IO(Http) ! Http.Bind(controllerActor, interface = SparktaConfig.apiConfig.get.getString("host"),
        port = SparktaConfig.apiConfig.get.getInt("port"))
      IO(Http) ! Http.Bind(swaggerActor, interface = SparktaConfig.swaggerConfig.get.getString("host"),
        port = SparktaConfig.swaggerConfig.get.getInt("port"))

      log.info("> Actors System UP!")
    } else log.info("Config for Sparkta is not defined")
  }

  // TODO (anistal) this should be refactored in an actor
  def initPolicyContextStatus {
    Try {
      if (SparktaConfig.getClusterConfig.isEmpty) {
        val curator = CuratorFactoryHolder.getInstance()
        val contextPath = s"${AppConstant.ContextPath}"
        val children = curator.getChildren.forPath(contextPath)
        val statuses = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[PolicyStatusModel](new String(curator.getData.forPath(
            s"${AppConstant.ContextPath}/$element")))).toSeq
        statuses.foreach(p => update(PolicyStatusModel(p.id, PolicyStatusEnum.NotStarted)))
      }
      def update(policyStatus: PolicyStatusModel): Unit = {
        val curator = CuratorFactoryHolder.getInstance()
        val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"
        val ips =
          read[PolicyStatusModel](new String(curator.getData.forPath(statusPath)))
        log.info(s">> Updating context ${policyStatus.id} : <${ips.status}> to <${policyStatus.status}>")
        curator.setData().forPath(statusPath, write(policyStatus).getBytes)
      }
    } match {
      case Failure(ex: NoNodeException) => log.error("No Zookeeper node for /stratio/sparkta/contexts yet")
      case Failure(ex: Exception) => throw ex
      case Success(())=> {}
    }
  }

  def shutdown: Unit = {
    synchronized {
      SparkContextFactory.destroySparkContext
      CuratorFactoryHolder.resetInstance()
      system.shutdown
    }
  }
}
