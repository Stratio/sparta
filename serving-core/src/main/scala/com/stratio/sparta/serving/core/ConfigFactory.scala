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
package com.stratio.sparta.serving.core

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.{Failure, Success, Try}

/**
 * This wrapper around Java's config factory is necessary because we need to mock it.
 * @author anistal
 */
trait ConfigFactory extends SLF4JLogging {

  def getConfig(node: String, currentConfig: Option[Config] = None): Option[Config] =
    currentConfig match {
      case Some(config) => Try(config.getConfig(node)) match {
        case Success(config) => Some(config)
        case Failure(exception) => {
          log.error(exception.getLocalizedMessage, exception)
          None
        }
      }
      case _ => Try(ConfigFactory.load().getConfig(node)) match {
        case Success(config) => Some(config)
        case Failure(exception) => {
          log.error(exception.getLocalizedMessage, exception)
          None
        }
      }
    }
}

/**
 * Sparta's ConfigFactory wrapper around Java's ConfigFactory used in SpartaHelper.
 */
class SpartaConfigFactory extends ConfigFactory {}

/**
 * Sparta's ConfigFactory wrapper used as a mock in SpartaHelperSpec.
 * @param config that contains a mocked config.
 */
case class MockConfigFactory(config: Config) extends ConfigFactory {

  override def getConfig(node: String, currentConfig: Option[Config] = None): Option[Config] =
    currentConfig match {
      case Some(config) => Try(config.getConfig(node)) match {
        case Success(config) => Some(config)
        case Failure(exception) => {
          log.error(exception.getLocalizedMessage, exception)
          None
        }
      }
      case _ => Some(config.getConfig(node))
    }
}


