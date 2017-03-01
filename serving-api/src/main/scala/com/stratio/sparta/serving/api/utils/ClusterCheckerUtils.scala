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

package com.stratio.sparta.serving.api.utils

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import akka.util.Timeout
import com.stratio.sparta.serving.core.actor.StatusActor
import com.stratio.sparta.serving.core.actor.StatusActor.{FindById, ResponseStatus, Update}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import scala.concurrent.duration._
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success, Try}

trait ClusterCheckerUtils extends SLF4JLogging{

  implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)

  val statusActor: ActorRef

  def checkPolicyStatus(policy: PolicyModel): Unit = {
    for {
      statusResponse <- (statusActor ? FindById(policy.id.get)).mapTo[ResponseStatus]
    } yield statusResponse match {
      case StatusActor.ResponseStatus(Success(policyStatus)) =>
        if (policyStatus.status == Launched || policyStatus.status == Starting || policyStatus.status == Stopping) {
          val information = s"The checker detects that the policy not start/stop correctly"
          log.error(information)
          statusActor ! Update(PolicyStatusModel(
            id = policy.id.get,
            status = Failed,
            statusInfo = Some(information)
          ))
        } else {
          val information = s"The checker detects that the policy run/stop correctly"
          log.info(information)
          statusActor ! Update(PolicyStatusModel(
            id = policy.id.get, status = NotDefined, statusInfo = Some(information)))
        }
      case StatusActor.ResponseStatus(Failure(exception)) =>
        log.error(s"Error when extract policy status in scheduler task.", exception)
    }
  }

  def loggingResponsePolicyStatus(response: Try[PolicyStatusModel]): Unit =
    response match {
      case Success(statusModel) =>
        log.info(s"Policy status model created or updated correctly: " +
          s"\n\tId: ${statusModel.id}\n\tStatus: ${statusModel.status}")
      case Failure(e) =>
        log.error(s"Policy status model creation failure. Error: ${e.getLocalizedMessage}", e)
    }
}
