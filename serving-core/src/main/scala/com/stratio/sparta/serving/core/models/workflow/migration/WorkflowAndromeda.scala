/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow.migration

import java.util.Date

import org.joda.time.DateTime

import com.stratio.sparta.serving.core.constants.AppConstant.DefaultGroup
import com.stratio.sparta.serving.core.models.EntityAuthorization
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine.ExecutionEngine
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode.WorkflowExecutionMode
import com.stratio.sparta.serving.core.models.enumerators.{WorkflowExecutionEngine, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow._

case class WorkflowAndromeda(
                              id: Option[String] = None,
                              name: String,
                              description: String = "Default description",
                              settings: SettingsAndromeda,
                              pipelineGraph: PipelineGraph,
                              executionEngine: ExecutionEngine = WorkflowExecutionEngine.Streaming,
                              uiSettings: Option[UiSettings] = None,
                              creationDate: Option[DateTime] = None,
                              lastUpdateDate: Option[DateTime] = None,
                              version: Long = 0L,
                              group: Group = DefaultGroup,
                              tags: Option[Seq[String]] = None,
                              status: Option[WorkflowStatusAndromeda] = None,
                              execution: Option[WorkflowExecutionAndromeda] = None,
                              debugMode: Option[Boolean] = Option(false),
                              versionSparta: Option[String] = None
                            ) extends EntityAuthorization {

  def authorizationId: String = s"${group.name}/$name"

}

case class WorkflowStatusAndromeda(
                                    id: String,
                                    status: WorkflowStatusEnum.Value,
                                    statusId: Option[String] = None,
                                    statusInfo: Option[String] = None,
                                    creationDate: Option[DateTime] = None,
                                    lastUpdateDate: Option[DateTime] = None,
                                    lastUpdateDateWorkflow: Option[DateTime] = None
                                  )

case class WorkflowExecutionAndromeda(
                                       id: String,
                                       sparkSubmitExecution: Option[SparkSubmitExecutionAndromeda] = None,
                                       sparkExecution: Option[SparkExecutionAndromeda] = None,
                                       sparkDispatcherExecution: Option[SparkDispatcherExecution] = None,
                                       marathonExecution: Option[MarathonExecutionAndromeda] = None,
                                       genericDataExecution: Option[GenericDataExecutionAndromeda] = None,
                                       localExecution: Option[LocalExecutionAndromeda] = None
                                     ) extends EntityAuthorization {

  def authorizationId: String = genericDataExecution.map { executionInfo =>
    executionInfo.workflow.authorizationId
  }.getOrElse("N/A")

}

case class GenericDataExecutionAndromeda(
                                          workflow: WorkflowAndromeda,
                                          executionMode: WorkflowExecutionMode,
                                          executionId: String,
                                          launchDate: Option[DateTime] = None,
                                          startDate: Option[DateTime] = None,
                                          endDate: Option[DateTime] = None,
                                          userId: Option[String] = None,
                                          lastError: Option[WorkflowErrorAndromeda] = None
                                        )

case class SparkSubmitExecutionAndromeda(
                                          driverClass: String,
                                          driverFile: String,
                                          pluginFiles: Seq[String],
                                          master: String,
                                          submitArguments: Map[String, String],
                                          sparkConfigurations: Map[String, String],
                                          driverArguments: Map[String, String],
                                          sparkHome: String
                                        )

case class MarathonExecutionAndromeda(
                                       marathonId: String,
                                       sparkURI: Option[String] = None,
                                       historyServerURI: Option[String] = None
                                     )

case class SparkExecutionAndromeda(applicationId: String)

case class LocalExecutionAndromeda(sparkURI: Option[String] = None)

case class WorkflowErrorAndromeda(
                                   message: String,
                                   phase: PhaseEnumAndromeda.Value,
                                   exceptionMsg: String,
                                   localizedMsg: String,
                                   date: Date = new Date,
                                   step: Option[String] = None
                                 )

object PhaseEnumAndromeda extends Enumeration {

  val Checkpoint = Value("Checkpoint")
  val Context = Value("Context")
  val Setup = Value("Setup")
  val Cleanup = Value("Cleanup")
  val Input = Value("Input")
  val Transform = Value("Transform")
  val Output = Value("Output")
  val Write = Value("Write")
  val Execution = Value("Execution")
  val Launch = Value("Launch")
  val Stop = Value("Stop")
  val Validate = Value("Validate")

}