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

package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.serving.core.constants.AppConstant.DefaultGroup
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import org.joda.time.DateTime

import com.stratio.sparta.serving.core.models.dto.Dto

case class Workflow(
                     id: Option[String] = None,
                     name: String,
                     description: String = "Default description",
                     settings: Settings,
                     pipelineGraph: PipelineGraph,
                     executionEngine : ExecutionEngine = WorkflowExecutionEngine.Streaming,
                     uiSettings: Option[UiSettings] = None,
                     creationDate: Option[DateTime] = None,
                     lastUpdateDate: Option[DateTime] = None,
                     version: Long = 0L,
                     group: Group = DefaultGroup,
                     tag: Option[String] = None,
                     status: Option[WorkflowStatus] = None
                   )

/**
  * Wrapper class used by the api consumers
  */
case class WorkflowDto(id: Option[String],
                       name: String,
                       description: String,
                       settings: GlobalSettings,
                       nodes: Seq[NodeGraphDto],
                       executionEngine: ExecutionEngine,
                       lastUpdateDate: Option[DateTime],
                       version: Long,
                       group: String,
                       status: Option[WorkflowStatus]) extends Dto