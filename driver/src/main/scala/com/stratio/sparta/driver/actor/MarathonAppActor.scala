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

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparta.driver.actor.MarathonAppActor.{StartApp, StopApp}
import com.stratio.sparta.serving.core.actor.LauncherActor.StartWithRequest
import com.stratio.sparta.serving.core.actor.StatusActor.{ResponseStatus, Update}
import com.stratio.sparta.serving.core.actor.{ClusterLauncherActor, ExecutionActor, FragmentActor, StatusActor}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.helpers.FragmentsHelper
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PhaseEnum, PolicyErrorModel, PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.models.submit.SubmitRequest
import com.stratio.sparta.serving.core.utils.{PolicyUtils, SchedulerUtils}
import org.apache.curator.framework.CuratorFramework

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class MarathonAppActor(curatorInstance: CuratorFramework) extends Actor with SchedulerUtils {

  implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)
  val statusActor: ActorRef = context.actorOf(Props(new StatusActor(curatorInstance)), AkkaConstant.statusActor)

  def receive: PartialFunction[Any, Unit] = {
    case StartApp(policyId) => doStartApp(policyId)
    case StopApp => doStopApp()
    case _ => log.info("Unrecognized message in Cluster Launcher Actor")
  }

  def doStopApp(exitCode : Int = 0) : Unit = {
    log.info("Shutting down Sparta Marathon App system")
    Await.ready(context.system.terminate(), 1 minute)
    System.exit(exitCode)
  }

  def doStartApp(policyId: String): Unit = {
    Try {
      val policyUtils = new PolicyUtils {
        override val curatorFramework: CuratorFramework = curatorInstance
      }
      val fragmentActor = context.actorOf(Props(new FragmentActor(curatorInstance)), AkkaConstant.FragmentActor)
      val policy = FragmentsHelper.getPolicyWithFragments(policyUtils.getPolicyById(policyId), fragmentActor)
      val executionActor = context.actorOf(Props(new ExecutionActor(curatorInstance)), AkkaConstant.ExecutionActor)
      val clusterLauncherActor = context.actorOf(Props(new ClusterLauncherActor(statusActor, executionActor)),
        AkkaConstant.ClusterLauncherActor)

      for {
        response <- (executionActor ? ExecutionActor.FindById(policyId)).mapTo[Try[SubmitRequest]]
      } yield response match {
        case Success(submitRequest) => clusterLauncherActor ! StartWithRequest(policy, submitRequest)
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
        statusActor ! Update(PolicyStatusModel(id = policyId, status = Failed, statusInfo = Option(information),
          lastError = Option(PolicyErrorModel(information, PhaseEnum.Execution, exception.toString))))
        doStopApp(-1)
    }
  }

  def applicationKiller(policy: PolicyModel): Unit = {
    for {
      statusResponse <- (statusActor ? StatusActor.FindById(policy.id.get)).mapTo[ResponseStatus]
    } yield statusResponse match {
      case StatusActor.ResponseStatus(Success(policyStatus)) =>
        if (policyStatus.status == Stopped || policyStatus.status == Failed) {
          val information = s"Killing Sparta Marathon App"
          log.error(information)
          statusActor ! Update(PolicyStatusModel(id = policy.id.get,
            status = NotDefined,
            statusInfo = Some(information)
          ))
          doStopApp()
        }
      case StatusActor.ResponseStatus(Failure(exception)) =>
        log.error(s"Error when extract policy status in scheduler task.", exception)
    }
  }
}

object MarathonAppActor {

  case class StartApp(policyId: String)

  case object StopApp

}