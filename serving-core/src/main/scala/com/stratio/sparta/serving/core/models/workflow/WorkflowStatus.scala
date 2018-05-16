/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import org.joda.time.DateTime

case class WorkflowStatus(
                           id: String,
                           status: WorkflowStatusEnum.Value,
                           statusInfo: Option[String] = None,
                           creationDate: Option[DateTime] = None,
                           lastUpdateDate: Option[DateTime] = None,
                           lastUpdateDateWorkflow: Option[DateTime] = None
                         )
