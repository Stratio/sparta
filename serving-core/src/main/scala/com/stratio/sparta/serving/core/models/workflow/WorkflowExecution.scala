/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.serving.core.models.EntityAuthorization
import com.stratio.sparta.serving.core.models.dto.Dto
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine.ExecutionEngine
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode.WorkflowExecutionMode
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.WorkflowStatusEnum
import org.joda.time.DateTime

case class WorkflowExecution(
                              id: Option[String] = None,
                              statuses: Seq[ExecutionStatus] = Seq(ExecutionStatus()),
                              genericDataExecution: GenericDataExecution,
                              sparkSubmitExecution: Option[SparkSubmitExecution] = None,
                              sparkExecution: Option[SparkExecution] = None,
                              sparkDispatcherExecution: Option[SparkDispatcherExecution] = None,
                              marathonExecution: Option[MarathonExecution] = None,
                              localExecution: Option[LocalExecution] = None,
                              archived: Option[Boolean] = Option(false),
                              resumedDate: Option[DateTime] = None,
                              resumedStatus: Option[WorkflowStatusEnum] = None,
                              executionEngine: Option[ExecutionEngine] = None,
                              searchText: Option[String] = None,
                              executedFromScheduler: Option[Boolean] = Option(false),
                              executedFromExecution: Option[String] = None
                            ) extends EntityAuthorization {

  def authorizationId: String = genericDataExecution.workflow.authorizationId

  def getWorkflowToExecute: Workflow = genericDataExecution.workflow

  def getExecutionId: String =
    id.getOrElse(throw new Exception("The selected execution does not have id"))

  def lastStatus: ExecutionStatus = statuses.head
}

case class GenericDataExecution(
                                 workflow: Workflow,
                                 executionMode: WorkflowExecutionMode,
                                 executionContext: ExecutionContext,
                                 launchDate: Option[DateTime] = None,
                                 startDate: Option[DateTime] = None,
                                 endDate: Option[DateTime] = None,
                                 userId: Option[String] = None,
                                 lastError: Option[WorkflowError] = None,
                                 name: Option[String] = None,
                                 description: Option[String] = None
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


case class ExecutionStatus(
                            state: WorkflowStatusEnum.Value = WorkflowStatusEnum.Created,
                            statusInfo: Option[String] = None,
                            lastUpdateDate: Option[DateTime] = None
                          )

/**
  * Wrapper class used by the api consumers
  */
case class WorkflowExecutionDto(
                                 id: Option[String],
                                 statuses: Seq[ExecutionStatus],
                                 genericDataExecution: GenericDataExecutionDto,
                                 marathonExecution: Option[MarathonExecutionDto] = None,
                                 localExecution: Option[LocalExecution] = None,
                                 archived: Option[Boolean] = None,
                                 resumedDate: Option[DateTime] = None,
                                 resumedStatus: Option[WorkflowStatusEnum] = None,
                                 executionEngine: Option[ExecutionEngine] = None,
                                 searchText: Option[String] = None,
                                 executedFromScheduler: Option[Boolean] = Option(false),
                                 executedFromExecution: Option[String] = None,
                                 totalCount :Int = 0
                               ) extends Dto with EntityAuthorization {

  def authorizationId: String = genericDataExecution.workflow.authorizationId

}

case class MarathonExecutionDto(sparkURI: Option[String] = None, historyServerURI: Option[String] = None) extends Dto

case class GenericDataExecutionDto(
                                    executionMode: WorkflowExecutionMode,
                                    executionContext: ExecutionContext,
                                    workflow: WorkflowDto,
                                    launchDate: Option[DateTime] = None,
                                    startDate: Option[DateTime] = None,
                                    endDate: Option[DateTime] = None,
                                    lastError: Option[WorkflowError] = None,
                                    name: Option[String] = None,
                                    description: Option[String] = None
                                  ) extends Dto

