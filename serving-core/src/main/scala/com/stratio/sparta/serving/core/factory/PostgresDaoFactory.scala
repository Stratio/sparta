/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.factory

import java.util.concurrent.ForkJoinPool

import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.services.dao._

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * Utility class for manage dao object creation
  */
object PostgresDaoFactory {

  implicit val pgExecutionContext = ExecutionContext.fromExecutorService(
    new ForkJoinPool(Try(SpartaConfig.getPostgresConfig().get.getInt("executionContext.parallelism"))
      .getOrElse(Runtime.getRuntime.availableProcessors() * 4))
  )

  lazy val workflowPgService = new WorkflowPostgresDao()
  lazy val groupPgService = new GroupPostgresDao()
  lazy val debugWorkflowPgService = new DebugWorkflowPostgresDao()
  lazy val executionPgService = new WorkflowExecutionPostgresDao()
  lazy val globalParametersService = new GlobalParametersPostgresDao()
  lazy val parameterListPostgresDao = new ParameterListPostgresDao()
  lazy val templatePgService = new TemplatePostgresDao()
  lazy val qualityRuleResultPgService = new QualityRuleResultPostgresDao()
  lazy val scheduledWorkflowTaskPgService = new ScheduledWorkflowTaskPostgresDao()
  lazy val plannedQualityRulePgService = new PlannedQualityRulePostgresDao()
}
