/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.enumerators


//scalastyle:off
object WorkflowStatusEnum extends Enumeration {

  type status = Value

  type WorkflowStatusEnum = Value

  val Started        = Value(1, "Started")
  val Stopped        = Value(2, "Stopped")
  val StoppedByUser  = Value(3, "StoppedByUser")
  val Finished       = Value(4, "Finished")
  val NotDefined     = Value(5, "NotDefined")
  val NotStarted     = Value(6, "NotStarted")
  val Created        = Value(7, "Created")
  val Launched       = Value(8, "Launched")
  val Uploaded       = Value(9, "Uploaded")
  val Starting       = Value(10, "Starting")
  val StoppingByUser = Value(11, "StoppingByUser")
  val Stopping       = Value(12, "Stopping")
  val Killed         = Value(13, "Killed")
  val Failed         = Value(14, "Failed")
}