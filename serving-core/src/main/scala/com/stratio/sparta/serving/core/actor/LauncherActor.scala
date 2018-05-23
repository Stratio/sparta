/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.{WorkflowExecution, Workflow}

object LauncherActor {

  case class Launch(workflowId: String, user: Option[LoggedUser])

  case class Debug(workflowId: String, user: Option[LoggedUser])

  case class Start(workflow: Workflow, userId: Option[String])

  case class StartWithRequest(workflow: Workflow, request: WorkflowExecution)
}
