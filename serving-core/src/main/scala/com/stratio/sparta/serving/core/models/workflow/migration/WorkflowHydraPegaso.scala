/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.workflow.migration

import com.stratio.sparta.serving.core.constants.AppConstant.DefaultGroup
import com.stratio.sparta.serving.core.models.EntityAuthorization
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import org.joda.time.DateTime
import com.stratio.sparta.serving.core.models.workflow.{Group, Settings, UiSettings}

case class WorkflowHydraPegaso(
                                id: Option[String] = None,
                                name: String,
                                description: String = "Default description",
                                settings: Settings,
                                pipelineGraph: PipelineGraphHydraPegaso,
                                executionEngine: ExecutionEngine = WorkflowExecutionEngine.Streaming,
                                uiSettings: Option[UiSettings] = None,
                                creationDate: Option[DateTime] = None,
                                lastUpdateDate: Option[DateTime] = None,
                                version: Long = 0L,
                                group: Group = DefaultGroup,
                                tags: Option[Seq[String]] = None,
                                debugMode: Option[Boolean] = Option(false),
                                versionSparta: Option[String] = None,
                                parametersUsedInExecution: Option[Map[String, String]] = None,
                                executionId: Option[String] = None,
                                groupId: Option[String] = None
                   )