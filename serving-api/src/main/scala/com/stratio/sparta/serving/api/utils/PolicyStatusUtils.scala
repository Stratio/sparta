/**
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
import scala.concurrent.duration._
import scala.util.Success

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import com.stratio.sparta.serving.api.helpers.SpartaHelper._
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor._
import com.stratio.sparta.serving.core.policy.status.{PolicyStatusActor, PolicyStatusEnum}

trait PolicyStatusUtils {

  implicit val timeout: Timeout = Timeout(15.seconds)

  def isAnyPolicyStarted(policyStatusActor: ActorRef): Future[Boolean] = {
    for {
      response <- findAllPolicies(policyStatusActor)
    } yield response.policyStatus match {
      case Success(policiesStatus) =>
        policiesStatus.policiesStatus.exists(_.status == PolicyStatusEnum.Started) ||
          policiesStatus.policiesStatus.exists(_.status == PolicyStatusEnum.Starting)
      case _ => false
    }
  }

  def isContextAvailable(policyStatusActor: ActorRef): Future[Boolean] =
    for {
      maybeStarted <- isAnyPolicyStarted(policyStatusActor)
      clusterMode = isClusterMode
    } yield (clusterMode, maybeStarted) match {
      case (true, _) => true
      case (false, false) => true
      case (false, true) => throw new RuntimeException(s"One policy is already launched")
    }

  def findAllPolicies(policyStatusActor: ActorRef): Future[Response] = {
    (policyStatusActor ? FindAll).mapTo[Response]
  }

  def updatePolicy(policy: AggregationPoliciesModel, status: PolicyStatusEnum.Value,
                   policyStatusActor: ActorRef): Unit = {
    policyStatusActor ! Update(PolicyStatusModel(policy.id.get, status))
  }

  def createPolicy(policyStatusActor: ActorRef,
                   policyWithIdModel: AggregationPoliciesModel): Future[Option[PolicyStatusModel]] = {
    (policyStatusActor ? PolicyStatusActor.Create(PolicyStatusModel(
      id = policyWithIdModel.id.get,
      status = PolicyStatusEnum.NotStarted
    ))).mapTo[Option[PolicyStatusModel]]
  }

  def killPolicy(policyStatusActor: ActorRef, actorName: String): Unit = {
    policyStatusActor ! PolicyStatusActor.Kill(actorName)
  }
}
