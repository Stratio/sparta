/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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