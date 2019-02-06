/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.helpers

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.{MarathonConstant, SparkConstant}
import com.stratio.sparta.serving.core.utils.NginxUtils
import com.stratio.sparta.core.properties.ValidatingPropertyMap._

import scala.util._

object LinkHelper extends SLF4JLogging {

  def getClusterLocalLink: Option[String] =
    SpartaConfig.getSparkConfig().flatMap { sparkConfig =>
      val sparkMaster = Try(sparkConfig.getString("master")).getOrElse("local[*]")
      val sparkUiEnabled = Try(sparkConfig.getString("spark.ui.enabled").toBoolean).getOrElse(true)
      val sparkUiPort = Try(sparkConfig.getInt("spark.ui.port")).getOrElse(SparkConstant.DefaultUIPort)

      if (sparkUiEnabled) {
        if (
          Properties.envOrNone(MarathonConstant.NginxMarathonLBHostEnv).notBlank.isDefined &&
            Properties.envOrNone(MarathonConstant.NginxMarathonLBPathEnv).notBlank.isDefined
        ) NginxUtils.buildSparkUI("crossdata-sparkUI")
        else if(sparkMaster.contains("local")) Option(s"http://localhost:$sparkUiPort") else None
      } else None
    }

}