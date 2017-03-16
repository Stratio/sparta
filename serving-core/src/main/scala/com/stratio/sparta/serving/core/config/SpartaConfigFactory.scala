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
package com.stratio.sparta.serving.core.config

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

case class SpartaConfigFactory(fromConfig: Option[Config]) extends SLF4JLogging {

  def getConfig(node: String, currentConfig: Option[Config] = None): Option[Config] =
    fromConfig match {
      case Some(fromCfg) => currentConfig match {
        case Some(config: Config) => Try(config.getConfig(node)).toOption
        case None => Some(fromCfg.getConfig(node))
      }
      case None => currentConfig match {
        case Some(config: Config) => Try(config.getConfig(node)).toOption
        case None => Try(ConfigFactory.load().getConfig(node)).toOption
      }
    }
}

object SpartaConfigFactory {

  def apply(configOption: Config): SpartaConfigFactory = new SpartaConfigFactory(Option(configOption))

  def apply(): SpartaConfigFactory = new SpartaConfigFactory(None)
}


