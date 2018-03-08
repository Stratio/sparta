/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.helpers.SpartaHelper
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant

/**
 * Entry point of the application.
 */
object Sparta extends App with SLF4JLogging {

  SpartaConfig.initMainConfig()
  SpartaConfig.initApiConfig()
  SpartaConfig.initSparkConfig()
  SpartaHelper.initSpartaAPI(AppConstant.ConfigAppName)
}
