/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import java.util.UUID

import com.stratio.sparta.serving.core.models.dto.Dto
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode.WorkflowExecutionMode
import org.joda.time.DateTime

case class WorkflowExecution(
                              id: String,
                              uniqueId : String = UUID.randomUUID().toString,
                              sparkSubmitExecution: Option[SparkSubmitExecution] = None,
                              sparkExecution: Option[SparkExecution] = None,
                              sparkDispatcherExecution: Option[SparkDispatcherExecution] = None,
                              marathonExecution: Option[MarathonExecution] = None,
                              genericDataExecution: Option[GenericDataExecution] = None,
                              localExecution: Option[LocalExecution] = None
                            )

case class GenericDataExecution(
                                 workflow: Workflow,
                                 executionMode: WorkflowExecutionMode,
                                 executionId: String,
                                 launchDate: Option[DateTime] = None,
                                 startDate: Option[DateTime] = None,
                                 endDate: Option[DateTime] = None,
                                 userId: Option[String] = None,
                                 lastError: Option[WorkflowError] = None
                               )

case class SparkSubmitExecution(
                                 driverClass: String,
                                 driverFile: String,
                                 pluginFiles: Seq[String],
                                 master: String,
                                 submitArguments: Map[String, String],
                                 sparkConfigurations: Map[String, String],
                                 driverArguments: Map[String, String],
                                 sparkHome: String
                               )

case class SparkDispatcherExecution(killUrl: String)

case class MarathonExecution(
                              marathonId: String,
                              sparkURI: Option[String] = None,
                              historyServerURI: Option[String] = None
                            )

case class SparkExecution(applicationId: String)

case class LocalExecution(sparkURI: Option[String] = None)

/**
  * Wrapper class used by the api consumers
  */
case class WorkflowExecutionDto(
                                 id: String,
                                 marathonExecution: Option[MarathonExecution] = None,
                                 genericDataExecution: Option[GenericDataExecutionDto] = None,
                                 localExecution: Option[LocalExecution] = None
                               ) extends Dto

case class GenericDataExecutionDto(
                                    executionMode: WorkflowExecutionMode,
                                    executionId: String,
                                    launchDate: Option[DateTime] = None,
                                    startDate: Option[DateTime] = None,
                                    endDate: Option[DateTime] = None,
                                    userId: Option[String] = None,
                                    lastError: Option[WorkflowError] = None
                                  ) extends Dto


