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

package com.stratio.sparkta.serving.api.helpers


import com.stratio.sparkta.serving.api.Sparkta._
import com.stratio.sparkta.serving.core.models.{SparktaSerializer, PolicyStatusModel}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum
import com.stratio.sparkta.serving.core.{SparktaConfig, AppConstant, CuratorFactoryHolder}
import org.apache.zookeeper.KeeperException.NoNodeException
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

object ResetStatusHelper extends SparktaSerializer {

  def resetStatuses {
    Try {
      if (SparktaConfig.getClusterConfig.isEmpty) {
        val curator = CuratorFactoryHolder.getInstance()
        val contextPath = s"${AppConstant.ContextPath}"
        val children = curator.getChildren.forPath(contextPath)
        val statuses = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[PolicyStatusModel](new String(curator.getData.forPath(
            s"${AppConstant.ContextPath}/$element")))).toSeq
        statuses.foreach(p => update(PolicyStatusModel(p.id, PolicyStatusEnum.NotStarted)))
      }
      def update(policyStatus: PolicyStatusModel): Unit = {
        val curator = CuratorFactoryHolder.getInstance()
        val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"
        val ips =
          read[PolicyStatusModel](new String(curator.getData.forPath(statusPath)))
        log.info(s">> Updating context ${policyStatus.id} : <${ips.status}> to <${policyStatus.status}>")
        curator.setData().forPath(statusPath, write(policyStatus).getBytes)
      }
    } match {
      case Failure(ex: NoNodeException) => log.error("No Zookeeper node for /stratio/sparkta/contexts yet")
      case Failure(ex: Exception) => throw ex
      case Success(())=> {}
    }
  }

}
