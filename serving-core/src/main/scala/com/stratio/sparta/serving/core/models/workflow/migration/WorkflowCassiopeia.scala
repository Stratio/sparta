/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.models.workflow.migration

import java.util.Date

import org.joda.time.DateTime

import com.stratio.sparta.serving.core.constants.AppConstant.DefaultGroup
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine.ExecutionEngine
import com.stratio.sparta.serving.core.models.enumerators.{WorkflowExecutionEngine, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow._

case class WorkflowCassiopeia(
                        id: Option[String] = None,
                        name: String,
                        description: String = "Default description",
                        settings: SettingsCassiopea,
                        pipelineGraph: PipelineGraph,
                        executionEngine: ExecutionEngine = WorkflowExecutionEngine.Streaming,
                        uiSettings: Option[UiSettings] = None,
                        creationDate: Option[DateTime] = None,
                        lastUpdateDate: Option[DateTime] = None,
                        version: Long = 0L,
                        group: Group = DefaultGroup,
                        tags: Option[Seq[String]] = None,
                        status: Option[WorkflowStatusCassiopeia] = None
                      )

case class WorkflowStatusCassiopeia(
                                     id: String,
                                     status: WorkflowStatusEnum.Value,
                                     statusInfo: Option[String] = None,
                                     lastExecutionMode: Option[String] = None,
                                     lastError: Option[WorkflowErrorCassiopeia] = None,
                                     creationDate: Option[DateTime] = None,
                                     lastUpdateDate: Option[DateTime] = None,
                                     sparkURI: Option[String] = None,
                                     lastUpdateDateWorkflow: Option[DateTime] = None
                            )

object PhaseEnumCassiopeia extends Enumeration {

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

case class WorkflowErrorCassiopeia(
                             message: String,
                             phase: PhaseEnumCassiopeia.Value,
                             originalMsg: String,
                             date: Date = new Date
                           )

case class WorkflowExecutionCassiopeia(
                                        id: String,
                                        sparkSubmitExecution: SparkSubmitExecutionCassiopeia,
                                        sparkExecution: Option[SparkExecutionCassiopeia] = None,
                                        sparkDispatcherExecution: Option[SparkDispatcherExecution] = None,
                                        marathonExecution: Option[MarathonExecutionCassiopeia] = None
                               )

case class SparkSubmitExecutionCassiopeia(
                                    driverClass: String,
                                    driverFile: String,
                                    pluginFiles: Seq[String],
                                    master: String,
                                    submitArguments: Map[String, String],
                                    sparkConfigurations: Map[String, String],
                                    driverArguments: Map[String, String],
                                    sparkHome: String,
                                    userId: Option[String]
                                  )

case class SparkExecutionCassiopeia(applicationId: String)

case class MarathonExecutionCassiopeia(marathonId: String)
