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

package com.stratio.sparkta.serving.core.policy.status

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.serving.core.CuratorFactoryHolder
import com.stratio.sparkta.serving.core.constants.AppConstant
import com.stratio.sparkta.serving.core.models.{PolicyStatusModel, SparktaSerializer}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor._
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.{NodeCache, NodeCacheListener}
import org.json4s.jackson.Serialization.{read, write}

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

class PolicyStatusActor(curatorFramework: CuratorFramework)
  extends Actor with SLF4JLogging with SparktaSerializer {

  override def receive: Receive = {
    case Create(policyStatus) => sender ! create(policyStatus)
    case Update(policyStatus) => sender ! update(policyStatus)
    case FindAll => findAll
    case AddListener(name, callback) => addListener(name, callback)
  }

  def update(policyStatus: PolicyStatusModel): Option[PolicyStatusModel] = {

    val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"

    //TODO check the correct statuses
    if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
      val ips =
        read[PolicyStatusModel](new String(curatorFramework.getData.forPath(statusPath)))
      log.info(s">> Updating context ${policyStatus.id} : <${ips.status}> to <${policyStatus.status}>")
      //validate(Some(ips.status), policyStatus.status)
      curatorFramework.setData().forPath(statusPath, write(policyStatus).getBytes)
      Some(policyStatus)
    } else None
  }

  def create(policyStatus: PolicyStatusModel): Option[PolicyStatusModel] = {
    val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"

    if (CuratorFactoryHolder.existsPath(statusPath)) {
      val ips =
        read[PolicyStatusModel](new String(curatorFramework.getData.forPath(statusPath)))
      log.info(s">> Updating context ${policyStatus.id} : <${ips.status}> to <${policyStatus.status}>")
      //validate(Some(ips.status), policyStatus.status)
      curatorFramework.setData().forPath(statusPath, write(policyStatus).getBytes)
      Some(policyStatus)
    } else {
      log.info(s">> Creating policy context |${policyStatus.id}| to <${policyStatus.status}>")
      validate(None, policyStatus.status)
      curatorFramework.create.creatingParentsIfNeeded.forPath(statusPath, write(policyStatus).getBytes)
      Some(policyStatus)
    }
  }

  def setNotStartedStatus(policyStatus: PolicyStatusModel): Option[PolicyStatusModel] = {
    val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"

    log.info(s">> Creating policy context |${policyStatus.id}| to <${policyStatus.status}>")
    validate(None, policyStatus.status)
    curatorFramework.create.creatingParentsIfNeeded.forPath(statusPath, write(policyStatus).getBytes)
    Some(policyStatus)
  }

  def findAll(): Unit = {
    sender ! Response(Try({
      val contextPath = s"${AppConstant.ContextPath}"
      if (CuratorFactoryHolder.existsPath(contextPath)) {
        val children = curatorFramework.getChildren.forPath(contextPath)
        JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[PolicyStatusModel](new String(curatorFramework.getData.forPath(
            s"${AppConstant.ContextPath}/$element")))).toSeq
      } else Seq()
    }))
  }

  /**
   * Adds a listener to one policy and executes the callback when it changed.
   * @param id of the policy.
   * @param callback with a function that will be executed.
   */
  def addListener(id: String, callback: (PolicyStatusModel, NodeCache) => Unit): Unit = {
    val contextPath = s"${AppConstant.ContextPath}/$id"
    val nodeCache: NodeCache = new NodeCache(curatorFramework, contextPath)

    nodeCache.getListenable.addListener(new NodeCacheListener {
      override def nodeChanged(): Unit = {
        Try(new String(nodeCache.getCurrentData.getData)) match {
          case Success(value) => {
            callback(read[PolicyStatusModel](value), nodeCache)
          }
          case Failure(e) => log.error(s"NodeCache value: ${nodeCache.getCurrentData}", e)
        }
      }
    })
    nodeCache.start()
  }
}

object PolicyStatusActor {

  case class Update(policyStatus: PolicyStatusModel)

  case class Create(policyStatus: PolicyStatusModel)

  case class AddListener(name: String, callback: (PolicyStatusModel, NodeCache) => Unit)

  case object FindAll

  case class Response(policyStatus: Try[Seq[PolicyStatusModel]])

  /**
   * This map represents the state machine of one context.
   */
  val StateMachine: Map[Option[PolicyStatusEnum.Value], Seq[PolicyStatusEnum.Value]] = Map(
    None -> Seq(PolicyStatusEnum.NotStarted),
    Some(PolicyStatusEnum.NotStarted) -> Seq(PolicyStatusEnum.Launched, PolicyStatusEnum.Failed),
    Some(PolicyStatusEnum.Launched) -> Seq(PolicyStatusEnum.Starting, PolicyStatusEnum.Failed),
    Some(PolicyStatusEnum.Starting) -> Seq(PolicyStatusEnum.Started, PolicyStatusEnum.Failed),
    Some(PolicyStatusEnum.Started) -> Seq(PolicyStatusEnum.Stopping, PolicyStatusEnum.Failed),
    Some(PolicyStatusEnum.Stopping) -> Seq(PolicyStatusEnum.Stopped, PolicyStatusEnum.Failed),
    Some(PolicyStatusEnum.Stopped) -> Seq(PolicyStatusEnum.Launched, PolicyStatusEnum.Failed),
    Some(PolicyStatusEnum.Failed) -> Seq(PolicyStatusEnum.Launched)

  )

  /**
   * Validates with the StateMachine if one status could be changed to another.
   * @param initialStatus that contains the currently status.
   * @param finalStatus to change. If not one exception will be thrown.
   */
  def validate(initialStatus: Option[PolicyStatusEnum.Value], finalStatus: PolicyStatusEnum.Value): Unit = {
    if (!StateMachine.exists(_._1 == initialStatus)) {
      throw new IllegalStateException(s"The status ${initialStatus.get} is not in the StateMachine")
    }
    if (!StateMachine.get(initialStatus).get.contains(finalStatus)) {
      throw new IllegalStateException(s"Imposible change status from $initialStatus to $finalStatus")
    }
  }
}

/**
 * Possible states that a policy could be when it was run.
 *
 * Launched: Sparkta performs a spark-submit to the cluster.
 * Starting: SparktaJob tries to start the job.
 * Started: if the job was successfully started and the receiver is running.
 * Failed: if the lifecycle fails.
 * Stopping: Sparkta sends a stop signal to the job to stop it gracefully.
 * Stopped: the job is stopped.
 */
object PolicyStatusEnum extends Enumeration {

  type status = Value

  val Launched = Value("Launched")
  val Starting = Value("Starting")
  val Started = Value("Started")
  val Failed = Value("Failed")
  val Stopping = Value("Stopping")
  val Stopped = Value("Stopped")
  val NotStarted = Value("NotStarted")
}
