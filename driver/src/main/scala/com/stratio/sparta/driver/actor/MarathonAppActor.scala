/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
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

package com.stratio.sparta.driver.actor

import akka.actor.{Actor, Props}
import com.stratio.sparta.driver.actor.MarathonAppActor.{StartApp, StopApp}
import com.stratio.sparta.serving.core.actor.ClusterLauncherActor
import com.stratio.sparta.serving.core.actor.LauncherActor.StartWithRequest
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, WorkflowErrorModel, WorkflowStatusModel}
import com.stratio.sparta.serving.core.utils.{FragmentUtils, PolicyStatusUtils, PolicyUtils, RequestUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.NodeCache

import scala.util.{Failure, Success, Try}

class MarathonAppActor(val curatorFramework: CuratorFramework) extends Actor
  with PolicyStatusUtils with FragmentUtils with RequestUtils with PolicyUtils {

  def receive: PartialFunction[Any, Unit] = {
    case StartApp(policyId) => doStartApp(policyId)
    case StopApp => preStopActions()
    case _ => log.info("Unrecognized message in Marathon App Actor")
  }

  def preStopActions(): Unit = {
    log.info("Shutting down Sparta Marathon Actor system")
    //Await.ready(context.system.terminate(), 1 minute)
    context.system.shutdown()
  }

  //scalastyle:off
  def doStartApp(policyId: String): Unit = {
    Try {
      log.debug(s"Obtaining status by id: $policyId")
      findStatusById(policyId) match {
        case Success(status) =>
          log.debug(s"Obtained status: ${status.status}")
          if (status.status != Stopped && status.status != Stopping && status.status != Failed &&
            status.status != Finished) {
            log.debug(s"Obtaining policy with fragments by id: $policyId")
            val policy = getPolicyWithFragments(getPolicyById(policyId))
            log.debug(s"Obtained policy: ${policy.toString}")
            log.debug(s"Closing checker by id: $policyId and name: ${policy.name}")
            closeChecker(policy.id.get, policy.name)
            log.debug(s"Obtaining request by id: $policyId")
            findRequestById(policyId) match {
              case Success(submitRequest) =>
                log.debug(s"Starting request: ${submitRequest.toString}")
                val clusterLauncherActor =
                  context.actorOf(Props(new ClusterLauncherActor(curatorFramework)), ClusterLauncherActorName)
                clusterLauncherActor ! StartWithRequest(policy, submitRequest)
              case Failure(exception) => throw exception
            }
          } else {
            log.info(s"Marathon App launched by marathon with incorrect state, the job is not executed, finish them")
            preStopActions()
          }
        case Failure(e) => throw e
      }
    } match {
      case Success(_) =>
        log.info(s"StartApp in Marathon App finished without errors")
      case Failure(exception) =>
        val information = s"Error submitting job with Marathon App"
        log.error(information, exception)
        updateStatus(WorkflowStatusModel(id = policyId, status = Failed, statusInfo = Option(information),
          lastError = Option(WorkflowErrorModel(information, PhaseEnum.Execution, exception.toString))))
        preStopActions()
    }
  }

  //scalastyle:on

  def closeChecker(policyId: String, policyName: String): Unit = {
    log.info(s"Listener added to $policyName with id: $policyId")
    addListener(policyId, (policyStatus: WorkflowStatusModel, nodeCache: NodeCache) => {
      synchronized {
        if (policyStatus.status == Stopped || policyStatus.status == Failed) {
          try {
            val information = s"Executing pre-close actions in Marathon App ..."
            log.info(information)
            updateStatus(WorkflowStatusModel(id = policyId, status = NotDefined, statusInfo = Some(information)))
            preStopActions()
          } finally {
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Node cache to Marathon App Listener closed correctly")
              case Failure(e) =>
                log.error(s"Node Cache to Marathon App is not closed correctly", e)
            }
          }
        }
      }
    })
  }
}

object MarathonAppActor {

  case class StartApp(policyId: String)

  case object StopApp

}