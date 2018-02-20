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

import com.stratio.governance.commons.agent.model.metadata.MetadataPath
import com.stratio.sparta.dg.agent.model.SpartaWorkflowStatusMetadata
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.governance.commons.agent.model.metadata.lineage.EventType
import com.stratio.governance.commons.agent.model.metadata.lineage.EventType.EventType
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatusStream
import org.joda.time.DateTime

object WorkflowStatusUtils {

  def processStatus(workflowStatusStream: WorkflowStatusStream): Option[List[SpartaWorkflowStatusMetadata]] =
    if (checkIfProcessableStatus(workflowStatusStream)) {
      val metadataSerialized = new SpartaWorkflowStatusMetadata(
        name = workflowStatusStream.workflow.get.name,
        status = mapSparta2GovernanceStatuses(workflowStatusStream.workflowStatus.status),
        error = if (workflowStatusStream.workflowStatus.status == Failed)
          Some(workflowStatusStream.workflowStatus.lastError.get.message) else None,
        key = workflowStatusStream.workflowStatus.id,
        metadataPath = extractMetadataPath(workflowStatusStream),
        tags = workflowStatusStream.workflow.get.tag.fold(List.empty[String]){tag => tag.split(",").toList},
        modificationTime = fromDatetimeToLongWithDefault(workflowStatusStream.workflow.get.lastUpdateDate),
        accessTime =  fromDatetimeToLongWithDefault(workflowStatusStream.workflowStatus.lastUpdateDate)
      )
      Some(List(metadataSerialized))
    }
    else None

  private def fromDatetimeToLongWithDefault(dateTime: Option[DateTime]) : Option[Long] =
    dateTime.fold(Some(System.currentTimeMillis())){dt => Some(dt.getMillis)}

  private def checkIfProcessableStatus(workflowStatusStream: WorkflowStatusStream): Boolean = {
    val eventStatus = workflowStatusStream.workflowStatus.status
    (eventStatus == Started || eventStatus == Finished || eventStatus == Failed) &&
      workflowStatusStream.workflow.isDefined
  }

  private def extractMetadataPath(workflowStatusStream: WorkflowStatusStream) : MetadataPath =
    MetadataPath(Seq(
      tenantName,
      workflowStatusStream.workflow.get.group.name.replaceAll("/", "_"),
      workflowStatusStream.workflow.get.name,
      workflowStatusStream.workflow.get.version,
      workflowStatusStream.workflow.get.lastUpdateDate.getOrElse(DateTime.now()).getMillis
    ).map(_.toString))

  private def mapSparta2GovernanceStatuses(spartaStatus: WorkflowStatusEnum.Value) : EventType =
    spartaStatus match {
      case Started => EventType.Running
      case Finished => EventType.Success
      case Failed => EventType.Failed
    }
}
