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

import java.util.UUID

import akka.actor.SupervisorStrategy.Escalate
import akka.actor._
import akka.event.slf4j.SLF4JLogging
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparkta.serving.api.constants.ActorsConstant
import com.stratio.sparkta.serving.core.SparktaConfig
import com.stratio.sparkta.serving.core.constants.AppConstant
import com.stratio.sparkta.serving.core.exception.ServingCoreException
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel, SparktaSerializer}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.{FindAll, Response, Update}
import com.stratio.sparkta.serving.core.policy.status.{PolicyStatusActor, PolicyStatusEnum}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization.{read, write}

import scala.collection.JavaConversions
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class SparkStreamingContextActor(streamingContextService: StreamingContextService,
                                 policyStatusActor: ActorRef, curatorFramework: CuratorFramework) extends Actor
with SLF4JLogging
with SparktaSerializer {

  val SparkStreamingContextActorPrefix: String = "sparkStreamingContextActor"

  implicit val timeout: Timeout = Timeout(10.seconds)

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _: ServingCoreException => Escalate
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

  override def receive: PartialFunction[Any, Unit] = {
    case Create(policy) => sender ! create(policy)
  }

  def isNotRunning(policy: AggregationPoliciesModel): Boolean = {
    val future = policyStatusActor ? FindAll
    val models = Await.result(future, timeout.duration) match {
      case Response(Success(s)) => s.filter(s => s.id == policy.id.get)
      case Response(Failure(ex)) => throw ex
    }
    models.asInstanceOf[Seq[PolicyStatusModel]].exists(p => p.status match {
      case PolicyStatusEnum.Launched => false
      case PolicyStatusEnum.Starting => false
      case PolicyStatusEnum.Started => false
      case _ => true
    })
  }

  def launch(policy: AggregationPoliciesModel): AggregationPoliciesModel = {
    if (isNotRunning(policy)) {
      policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Launched))
      getStreamingContextActor(policy) match {
        case Some(streamingContextActor) => streamingContextActor ? Start
        case None =>
          policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Failed))
      }
    }
    else
      throw new Exception(s"policy ${policy.name} is launched")

    policy
  }

  /**
    * Tries to create a spark streaming context with a given configuration.
    * @param policy that contains the configuration to run.
    */
  private def create(policy: AggregationPoliciesModel): Try[AggregationPoliciesModel] = Try {
    if (policy.id.isDefined)
      launch(policy)
    else {
      if (existsByName(policy.name)) throw new Exception(s"${policy.name} already exists")
      launchNewPolicy(policy)
    }
  }

  def existsByName(name: String, id: Option[String] = None): Boolean = {
    val nameToCompare = name.toLowerCase
    Try({
      val children = curatorFramework.getChildren.forPath(s"${AppConstant.PoliciesBasePath}")
      JavaConversions.asScalaBuffer(children).toList.map(element =>
        read[AggregationPoliciesModel](new String(curatorFramework.getData.forPath(
          s"${AppConstant.PoliciesBasePath}/$element"))))
        .filter(policy => if (id.isDefined) policy.name == nameToCompare && policy.id.get != id.get
        else policy.name == nameToCompare).toSeq.nonEmpty
    }) match {
      case Success(result) => result
      case Failure(exception) => {
        log.error(exception.getLocalizedMessage, exception)
        false
      }
    }
  }

  def launchNewPolicy(policy: AggregationPoliciesModel): AggregationPoliciesModel = {
    val policyWithIdModel = policyWithId(policy)

    for {
      response <- policyStatusActor ? PolicyStatusActor.Create(PolicyStatusModel(
        id = policyWithIdModel.id.get,
        status = PolicyStatusEnum.NotStarted
      ))
    } yield policyStatusActor ! Update(PolicyStatusModel(policyWithIdModel.id.get, PolicyStatusEnum.Launched))

    getStreamingContextActor(policyWithIdModel) match {
      case Some(streamingContextActor) =>
        // TODO (anistal) change and use PolicyActor.
        savePolicyInZk(policyWithIdModel)
        streamingContextActor ? Start
      case None =>
        policyStatusActor ? Update(PolicyStatusModel(policyWithIdModel.id.get, PolicyStatusEnum.Failed))
    }
    policyWithIdModel
  }

  private def policyWithId(policy: AggregationPoliciesModel) =
    (
      policy.id match {
        case None => policy.copy(id = Some(UUID.randomUUID.toString))
        case Some(_) => policy
      }
      ).copy(name = policy.name.toLowerCase, version = Some(ActorsConstant.UnitVersion))

  // XXX Private Methods.
  private def savePolicyInZk(policy: AggregationPoliciesModel): Unit = {

    Try({
      read[AggregationPoliciesModel](new Predef.String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/${policy.id.get}")))
    }) match {
      case Success(_) => log.info(s"Policy ${policy.id.get} already in zookeeper. Updating it...")
        curatorFramework.setData.forPath(s"${AppConstant.PoliciesBasePath}/${policy.id.get}", write(policy).getBytes)
      case Failure(e) => curatorFramework.create().creatingParentsIfNeeded().forPath(
        s"${AppConstant.PoliciesBasePath}/${policy.id.get}", write(policy).getBytes)
    }
  }

  private def getStreamingContextActor(policy: AggregationPoliciesModel): Option[ActorRef] = {
    val actorName = policy.name.replace(" ", "_")
    SparktaConfig.getClusterConfig match {
      case Some(clusterConfig) => {
        val zookeeperConfig = SparktaConfig.getZookeeperConfig
        val hdfsConfig = SparktaConfig.getHdfsConfig
        val detailConfig = SparktaConfig.getDetailConfig

        if (zookeeperConfig.isDefined && hdfsConfig.isDefined) {
          log.info(s"launched -> $SparkStreamingContextActorPrefix-${actorName}")
          Some(context.actorOf(Props(new ClusterSparkStreamingContextActor(
            policy, streamingContextService, clusterConfig, hdfsConfig.get, zookeeperConfig.get, detailConfig,
            policyStatusActor)),
            s"$SparkStreamingContextActorPrefix-${actorName}"))
        } else None
      }
      case None => Some(context.actorOf(
        Props(new LocalSparkStreamingContextActor(
          policy, streamingContextService, policyStatusActor)),
        s"$SparkStreamingContextActorPrefix-${actorName}"))
    }
  }
}

object SparkStreamingContextActor {

  case class Create(policy: AggregationPoliciesModel)

  case object Start

}