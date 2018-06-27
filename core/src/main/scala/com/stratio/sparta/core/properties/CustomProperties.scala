/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.properties

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.properties.ValidatingPropertyMap._

trait CustomProperties {

  val customKey: String
  val customPropertyKey: String
  val customPropertyValue: String
  val properties: Map[String, JSerializable]

  def getCustomProperties: Map[String, String] =
    properties.getOptionsList(customKey, customPropertyKey, customPropertyValue).filter(_._1.nonEmpty)
}
