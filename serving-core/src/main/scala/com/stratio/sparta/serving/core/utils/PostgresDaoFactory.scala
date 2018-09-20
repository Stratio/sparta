/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.utils

import java.util.concurrent.ForkJoinPool
import scala.concurrent.ExecutionContext
import scala.util.Try

import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.services.dao._

/**
  * Utiliy class for manage dao object creation
  */
object PostgresDaoFactory {

  implicit val pgExecutionContext = ExecutionContext.fromExecutorService(
    new ForkJoinPool(Try(SpartaConfig.getPostgresConfig().get.getInt("numThreads")).getOrElse(Runtime.getRuntime.availableProcessors() * 2)))

  lazy val workflowPgService = new WorkflowPostgresDao()
  lazy val groupPgService = new GroupPostgresDao()
  lazy val debugWorkflowPgService = new DebugWorkflowPostgresDao()
  lazy val executionPgService = new WorkflowExecutionPostgresDao()
  lazy val globalParametersService = new GlobalParametersPostgresDao()
  lazy val parameterListPostgresDao = new ParameterListPostgresDao()
  lazy val templatePgService = new TemplatePostgresDao()
}
