/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.parameters

object ParameterVariable {

  def create(name: String, value: String): ParameterVariable = ParameterVariable(name, Option(value))

  def create(name: String, value: Int): ParameterVariable = ParameterVariable(name, Option(value.toString))

}

case class ParameterVariable(name: String, value: Option[String] = None) {

  def findAndEscapeQuotes(): ParameterVariable = {
    val regex = "(\"|\\\\\")".r

    val quotedValues = this.value.map { x =>
      regex.replaceAllIn(x, "\"")
    }
    this.copy(value = quotedValues)
  }
}



