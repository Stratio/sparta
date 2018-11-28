/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.models

case class WorkflowValidationMessage(message: String, step: Option[String] = None, subStep: Option[String] = None) {

  override def toString: String = if(step.isDefined){
    s"$message in step ${step.get}"
  } else message

}

object WorkflowValidationMessage {

  def apply(message: String, step: String): WorkflowValidationMessage =
    WorkflowValidationMessage(message, Option(step))

  def apply(message: String, step: String, subStep: String): WorkflowValidationMessage =
    WorkflowValidationMessage(message, Option(step), Option(subStep))

}
