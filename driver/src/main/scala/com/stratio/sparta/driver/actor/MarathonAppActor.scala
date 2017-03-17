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
import com.stratio.sparta.serving.core.actor.LauncherActor.StartWithRequest
import com.stratio.sparta.serving.core.actor.{ClusterLauncherActor, FragmentActor}
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.helpers.FragmentsHelper
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PhaseEnum, PolicyErrorModel, PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.{ExecutionUtils, PolicyStatusUtils, PolicyUtils, SchedulerUtils}
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class MarathonAppActor(val curatorFramework: CuratorFramework) extends Actor
  with SchedulerUtils with PolicyStatusUtils {

  def receive: PartialFunction[Any, Unit] = {
    case StartApp(policyId) => doStartApp(policyId)
    case StopApp => doStopApp()
    case _ => log.info("Unrecognized message in Cluster Launcher Actor")
  }

  def doStopApp(exitCode: Int = 0): Unit = {
    log.info("Shutting down Sparta Marathon App system")
    //Await.ready(context.system.terminate(), 1 minute)
    context.system.shutdown()
    System.exit(exitCode)
  }

  def doStartApp(policyId: String): Unit = {
    Try {
      val policyUtils = new PolicyUtils {
        override val curatorFramework: CuratorFramework = curatorFramework
      }
      val executionUtils = new ExecutionUtils {
        override val curatorFramework: CuratorFramework = curatorFramework
      }
      val fragmentActor = context.actorOf(Props(new FragmentActor(curatorFramework)), FragmentActorName)
      val policy = FragmentsHelper.getPolicyWithFragments(policyUtils.getPolicyById(policyId), fragmentActor)
      val clusterLauncherActor =
        context.actorOf(Props(new ClusterLauncherActor(curatorFramework)), ClusterLauncherActorName)

      executionUtils.findRequestById(policyId) match {
        case Success(submitRequest) =>
          clusterLauncherActor ! StartWithRequest(policy, submitRequest)
        case Failure(exception) => throw exception
      }
      scheduleTask(KillMarathonDelay,
        DefaultKillMarathonDelay,
        KillMarathonInterval,
        DefaultKillMarathonInterval
      )(applicationKiller(policy))
    } match {
      case Success(_) =>
        log.info(s"Submitted job correctly in Marathon App")
      case Failure(exception) =>
        val information = s"Error submitting job in Marathon App"
        log.error(information)
        updateStatus(PolicyStatusModel(id = policyId, status = Failed, statusInfo = Option(information),
          lastError = Option(PolicyErrorModel(information, PhaseEnum.Execution, exception.toString))))
        doStopApp(-1)
    }
  }

  def applicationKiller(policy: PolicyModel): Unit = {
    findStatusById(policy.id.get) match {
      case Success(policyStatus) =>
        if (policyStatus.status == Stopped || policyStatus.status == Failed) {
          val information = s"Killing Sparta Marathon App"
          log.error(information)
          updateStatus(PolicyStatusModel(id = policy.id.get, status = NotDefined, statusInfo = Some(information)))
          doStopApp()
        }
      case Failure(exception) =>
        log.error(s"Error when extract policy status in scheduler task.", exception)
    }
  }
}

object MarathonAppActor {

  case class StartApp(policyId: String)

  case object StopApp

}