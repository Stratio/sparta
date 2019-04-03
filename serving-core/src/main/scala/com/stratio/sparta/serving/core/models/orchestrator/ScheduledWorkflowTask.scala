/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.orchestrator

import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.enumerators.ScheduledActionType.ScheduledActionType
import com.stratio.sparta.serving.core.models.enumerators.ScheduledTaskState.ScheduledTaskState
import com.stratio.sparta.serving.core.models.enumerators.ScheduledTaskType.ScheduledTaskType
import com.stratio.sparta.serving.core.models.workflow.ExecutionContext

case class ScheduledWorkflowTask(
                                  id: String,
                                  taskType: ScheduledTaskType,
                                  actionType: ScheduledActionType,
                                  entityId: String,
                                  executionContext: Option[ExecutionContext],
                                  active: Boolean,
                                  state: ScheduledTaskState,
                                  initDate: Long,
                                  duration: Option[String],
                                  loggedUser: Option[LoggedUser]
                                )

case class ScheduledWorkflowTaskInsert(
                                        taskType: ScheduledTaskType,
                                        actionType: ScheduledActionType,
                                        entityId: String,
                                        executionContext: Option[ExecutionContext],
                                        active: Boolean,
                                        initDate: Long,
                                        duration: Option[String]
                                      )
