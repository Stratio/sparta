/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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


