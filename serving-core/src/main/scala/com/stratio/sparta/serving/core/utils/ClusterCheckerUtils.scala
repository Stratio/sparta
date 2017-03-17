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

import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success}

trait ClusterCheckerUtils extends PolicyStatusUtils {

  val curatorFramework: CuratorFramework

  def checkPolicyStatus(policy: PolicyModel): Unit = {
    findStatusById(policy.id.get) match {
      case Success(policyStatus) =>
        if (policyStatus.status == Launched || policyStatus.status == Starting || policyStatus.status == Stopping) {
          val information = s"The checker detects that the policy not start/stop correctly"
          log.error(information)
          updateStatus(PolicyStatusModel(id = policy.id.get, status = Failed, statusInfo = Some(information)))
        } else {
          val information = s"The checker detects that the policy run/stop correctly"
          log.info(information)
          updateStatus(PolicyStatusModel(id = policy.id.get, status = NotDefined, statusInfo = Some(information)))
        }
      case Failure(exception) =>
        log.error(s"Error when extract policy status in scheduler task.", exception)
    }
  }
}
