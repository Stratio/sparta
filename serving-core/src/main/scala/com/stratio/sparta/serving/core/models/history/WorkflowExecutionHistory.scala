/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.history

import org.joda.time.DateTime

import com.stratio.sparta.serving.core.models.dto.Dto
import com.stratio.sparta.serving.core.models.workflow.GenericDataExecutionDto

case class WorkflowExecutionHistory(executionId: String,
                                    workflowId: String,
                                    executionMode: String,
                                    launchDate: Option[Long] = None,
                                    startDate: Option[Long] = None,
                                    endDate: Option[Long] = None,
                                    userId: Option[String] = None,
                                    lastError: Option[String] = None,
                                    genericExecution: String)

case class WorkflowExecutionHistoryDto(executionId: String,
                                    workflowId: String,
                                    executionMode: String,
                                    launchDate: Option[DateTime] = None,
                                    startDate: Option[DateTime] = None,
                                    endDate: Option[DateTime] = None,
                                    userId: Option[String] = None,
                                    lastError: Option[String] = None,
                                    genericExecution: GenericDataExecutionDto) extends Dto