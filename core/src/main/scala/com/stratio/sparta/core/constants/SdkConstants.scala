/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.core.constants

object SdkConstants {

  //Zookeeper
  val ZKConnection = "connectionString"
  val DefaultZKConnection = "127.0.0.1:2181"
  val ZKConnectionTimeout = "connectionTimeout"
  val DefaultZKConnectionTimeout = 19000
  val ZKSessionTimeout = "sessionTimeout"
  val DefaultZKSessionTimeout = 60000
  val ZKRetryAttemps = "retryAttempts"
  val DefaultZKRetryAttemps = 5
  val ZKRetryInterval = "retryInterval"
  val DefaultZKRetryInterval = 10000
  val StepErrorDataKey = "errorPath"
  val StepDataKey = "dataPath"
  val WorkflowIdKey = "workflowId"
}
