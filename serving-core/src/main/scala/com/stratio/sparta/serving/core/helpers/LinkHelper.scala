/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.helpers

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.utils.NginxUtils

import scala.util._

object LinkHelper extends SLF4JLogging {

  private val defaultPort = 4041

  def getClusterLocalLink: Option[String] =
    SpartaConfig.sparkConfig.flatMap { sparkConfig =>
      val sparkMaster = Try(sparkConfig.getString("master")).getOrElse("local[*]")
      val sparkUiEnabled = Try(sparkConfig.getString("spark.ui.enabled").toBoolean).getOrElse(true)
      val sparkUiPort = Try(sparkConfig.getInt("spark.ui.port")).getOrElse(defaultPort)

      if (sparkUiEnabled) {
        if (
          Properties.envOrNone("MARATHON_APP_LABEL_HAPROXY_1_VHOST").isDefined &&
          Properties.envOrNone("MARATHON_APP_LABEL_DCOS_SERVICE_NAME").isDefined
        ) NginxUtils.buildSparkUI("crossdata-sparkUI")
        else if(sparkMaster.contains("local")) Option(s"http://localhost:$sparkUiPort") else None
      } else None
    }

}