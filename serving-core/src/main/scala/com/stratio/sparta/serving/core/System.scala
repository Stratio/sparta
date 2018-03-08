/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
