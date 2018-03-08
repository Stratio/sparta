/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.enumerators


object WorkflowStatusEnum extends Enumeration {

  type status = Value

  val Launched = Value("Launched")
  val Starting = Value("Starting")
  val Started = Value("Started")
  val Failed = Value("Failed")
  val Stopping = Value("Stopping")
  val Stopped = Value("Stopped")
  val Finished = Value("Finished")
  val Killed = Value("Killed")
  val NotStarted = Value("NotStarted")
  val Uploaded = Value("Uploaded")
  val NotDefined = Value("NotDefined")
  val Created = Value("Created")
}
