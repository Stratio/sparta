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

package com.stratio.sparta.serving.core.utils

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparta.serving.core.actor.StatusActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.PolicyModel

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

trait PolicyStatusUtils extends CheckpointUtils {

  implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)

  def isAnyPolicyStarted(statusActor: ActorRef): Future[Boolean] = {
    for {
      response <- findAllStatuses(statusActor)
    } yield response.policyStatus match {
      case Success(policiesStatus) =>
        policiesStatus.policiesStatus.exists(_.status == PolicyStatusEnum.Started) ||
          policiesStatus.policiesStatus.exists(_.status == PolicyStatusEnum.Starting) ||
          policiesStatus.policiesStatus.exists(_.status == PolicyStatusEnum.Launched)
      case _ => false
    }
  }

  def isAvailableToRun(policyModel: PolicyModel, statusActor: ActorRef): Future[Boolean] =
    for {
      maybeStarted <- isAnyPolicyStarted(statusActor)
      localMode = isLocalMode(policyModel)
    } yield (localMode, maybeStarted) match {
      case (false, _) =>
        true
      case (true, false) =>
        true
      case (true, true) =>
        log.warn(s"One policy is already launched")
        false
    }

  def findAllStatuses(statusActor: ActorRef): Future[Response] = {
    (statusActor ? FindAll).mapTo[Response]
  }

}
