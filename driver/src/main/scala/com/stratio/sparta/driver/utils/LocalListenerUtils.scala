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

package com.stratio.sparta.driver.utils

import com.stratio.sparta.driver.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.{WorkflowModel, WorkflowStatusModel}
import com.stratio.sparta.serving.core.utils.PolicyStatusUtils
import org.apache.curator.framework.recipes.cache.NodeCache

import scala.util.{Failure, Success, Try}

trait LocalListenerUtils extends PolicyStatusUtils {

  def killLocalContextListener(policy: WorkflowModel, name: String): Unit = {
    log.info(s"Listener added to ${policy.name} with id: ${policy.id.get}")
    addListener(policy.id.get, (policyStatus: WorkflowStatusModel, nodeCache: NodeCache) => {
      synchronized {
        if (policyStatus.status == Stopping) {
          try {
            log.info("Stopping message received from Zookeeper")
            closeContexts(policy.id.get)
          } finally {
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Node cache correctly closed")
              case Failure(e) =>
                log.error(s"The node cache in Zookeeper was noy  correctly closed", e)
            }
          }
        }
      }
    })
  }

  private[driver] def closeContexts(policyId: String): Unit = {
    val information = "The Context was successfully closed in the local listener"
    log.info(information)
    updateStatus(WorkflowStatusModel(id = policyId, status = Stopped, statusInfo = Some(information)))
    destroySparkContext()
  }
}
