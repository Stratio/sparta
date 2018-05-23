/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.models

import java.util.Date

import com.stratio.sparta.sdk.workflow.enumerators.PhaseEnum

case class WorkflowError(
                               message: String,
                               phase: PhaseEnum.Value,
                               originalMsg: String,
                               date: Date = new Date,
                               step: Option[String] = None
                             )
