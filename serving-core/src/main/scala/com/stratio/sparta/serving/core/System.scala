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

import scala.util.{Failure, Success, Try}

/**
 * This wrapper around Java's system is necessary because we need to mock it.
 * @author anistal
 */
trait System extends SLF4JLogging {

  def getenv(env: String): Option[String] = {
    Try(System.getenv(env)) match {
      case Success(environment) => Option(environment)
      case Failure(exception) => {
        log.debug(exception.getLocalizedMessage, exception)
        None
      }
    }
  }

  def getProperty(key: String, defaultValue: String): Option[String] = {
    Try(System.getProperty(key, defaultValue)) match {
      case Success(property) => Option(property)
      case Failure(exception) => {
        log.debug(exception.getLocalizedMessage, exception)
        None
      }
    }
  }
}

/**
 * Sparta's system wrapper used in SpartaHelper.
 */
class SpartaSystem extends System {}

/**
 * Sparta's system wrapper used as a mock in SpartaHelperSpec.
 * @param env that contains mocked environment variables.
 * @param properties that contains mocked properties.
 */
case class MockSystem(env: Map[String, String], properties: Map[String, String]) extends System {

  override def getenv(env: String): Option[String] = {
    this.env.get(env)
  }

  override def getProperty(key: String, defaultValue: String): Option[String] = {
    this.properties.get(key).orElse(Some(defaultValue))
  }
}
