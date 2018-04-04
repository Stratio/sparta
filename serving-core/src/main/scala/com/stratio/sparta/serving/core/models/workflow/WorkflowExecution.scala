/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

case class WorkflowExecution(
                              id: String,
                              sparkSubmitExecution: SparkSubmitExecution,
                              sparkExecution: Option[SparkExecution] = None,
                              sparkDispatcherExecution: Option[SparkDispatcherExecution] = None,
                              marathonExecution: Option[MarathonExecution] = None
                            )

case class SparkSubmitExecution(
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

case class SparkDispatcherExecution(killUrl: String)

case class MarathonExecution(marathonId: String)

case class SparkExecution(applicationId: String)
