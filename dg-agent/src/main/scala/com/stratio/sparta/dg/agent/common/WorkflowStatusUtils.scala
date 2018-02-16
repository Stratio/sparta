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
package com.stratio.sparta.dg.agent.common

import akka.actor.ActorRef
import com.stratio.governance.commons.agent.actors.KafkaSender.KafkaEvent
import com.stratio.sparta.dg.agent.model.SpartaWorkflowStatusMetadata
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus

object WorkflowStatusUtils {

  def extractMetadataPath(implicit workflowStatus: WorkflowStatus)

  def processStatus(workflowStatus: WorkflowStatus, topicKafka: String): KafkaEvent = {

    val metadataSerialized =new SpartaWorkflowStatusMetadata(

    )
    KafkaEvent(List(metadataSerialized),topicKafka)
  }
}
