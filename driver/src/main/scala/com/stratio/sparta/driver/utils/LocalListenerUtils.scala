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

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.actor.StatusActor.{AddListener, Update}
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import org.apache.curator.framework.recipes.cache.NodeCache

import scala.util.{Failure, Success, Try}


trait LocalListenerUtils extends SLF4JLogging {

  val statusActor: ActorRef

  def killLocalContextListener(policy: PolicyModel, name: String): Unit = {
    log.info(s"Listener added to ${policy.name} with id: ${policy.id.get}")
    statusActor ! AddListener(policy.id.get, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
      synchronized {
        if (policyStatus.status == Stopping) {
          try {
            log.info("Stopping message received from Zookeeper")
            closeContexts(policy.id.get, statusActor)
          } finally {
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Node cache closed correctly")
              case Failure(e) =>
                log.error(s"The nodeCache in Zookeeper is not closed correctly", e)
            }
          }
        }
      }
    })
  }

  private def closeContexts(policyId: String, statusActor: ActorRef): Unit = {
    val information = "The Context have been stopped correctly in the local listener"
    log.info(information)
    statusActor ! Update(PolicyStatusModel(id = policyId, status = Stopped, statusInfo = Some(information)))
    destroySparkContext(destroyStreamingContext = true)
  }

}
