/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models

import com.stratio.sparta.serving.core.config.SpartaConfig


object RocketModes {

  sealed trait RocketMode
  case object Local extends RocketMode
  case object Remote extends RocketMode
  case object Debug extends RocketMode
  case object Validator extends RocketMode
  case object Catalog extends RocketMode
  case object Unknown extends RocketMode

  val bootstrapModes = Seq(Local, Remote, Debug, Validator, Catalog)

  protected[this] lazy val bootstrapMode: String = SpartaConfig.getBootstrapConfig()
    .map(_.getString("mode").toLowerCase)
    .getOrElse(throw new RuntimeException("bootstrap mode is a mandatory parameter in the configuration"))

  def retrieveRocketMode: RocketMode = bootstrapModes.find(_.toString.toLowerCase == bootstrapMode).getOrElse(Unknown)
}
