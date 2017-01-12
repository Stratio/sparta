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

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import akka.actor.ActorRef
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Success
import scala.util.Try
import akka.util.Timeout

import com.stratio.sparta.serving.core.actor.PolicyStatusActor
import com.stratio.sparta.serving.core.constants.AkkaConstant
import PolicyStatusActor._
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.CheckpointUtils

trait PolicyStatusUtils extends CheckpointUtils {

  implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)

  def isAnyPolicyStarted(policyStatusActor: ActorRef): Future[Boolean] = {
    for {
      response <- findAllPolicies(policyStatusActor)
    } yield response.policyStatus match {
      case Success(policiesStatus) =>
        policiesStatus.policiesStatus.exists(_.status == PolicyStatusEnum.Started) ||
          policiesStatus.policiesStatus.exists(_.status == PolicyStatusEnum.Starting) ||
          policiesStatus.policiesStatus.exists(_.status == PolicyStatusEnum.Launched)
      case _ => false
    }
  }

  def updateAll(policyStatusActor: ActorRef, status: PolicyStatusEnum.Value): Unit = {
    for {
      response <- findAllPolicies(policyStatusActor)
    } yield response.policyStatus match {
      case Success(policiesStatuses) =>
        policiesStatuses.policiesStatus.foreach(policyStatus =>
          policyStatusActor ! Update(PolicyStatusModel(policyStatus.id, status)))
      case _ =>
        log.error(s"The policies are not updated to status: $status")
    }
  }

  def isContextAvailable(policyModel: PolicyModel, policyStatusActor: ActorRef): Future[Boolean] =
    for {
      maybeStarted <- isAnyPolicyStarted(policyStatusActor)
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

  def findAllPolicies(policyStatusActor: ActorRef): Future[Response] = {
    (policyStatusActor ? FindAll).mapTo[Response]
  }

  def updatePolicy(policy: PolicyModel, status: PolicyStatusEnum.Value,
                   policyStatusActor: ActorRef): Unit = {
    policyStatusActor ! Update(PolicyStatusModel(policy.id.get, status))
  }

  def createPolicy(policyStatusActor: ActorRef,
                   policyWithIdModel: PolicyModel): Future[Option[PolicyStatusModel]] = {
    (policyStatusActor ? PolicyStatusActor.Create(PolicyStatusModel(
      id = policyWithIdModel.id.get,
      status = PolicyStatusEnum.NotStarted
    ))).mapTo[Option[PolicyStatusModel]]
  }

  def killActorByName(policyStatusActor: ActorRef, actorName: String): Unit = {
    Try(Await.result(policyStatusActor ? PolicyStatusActor.Kill(actorName), timeout.duration) match {
      case false => log.warn(s"The actor with name: $actorName has been stopped previously")
    }).getOrElse(log.warn(s"The actor with name: $actorName could not be stopped correctly"))
  }
}
