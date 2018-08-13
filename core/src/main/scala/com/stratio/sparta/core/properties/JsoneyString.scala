/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.properties

import akka.event.slf4j.SLF4JLogging

case class JsoneyString(private val string: String) extends SLF4JLogging {

  override def toString: String = string

  def toSeq: Seq[String] = {
    val removeLeftBrackets = if(string.startsWith("["))
      string.drop(1)
    else string
    val removeRightBrackets = if(removeLeftBrackets.endsWith("]"))
      removeLeftBrackets.dropRight(1)
    else removeLeftBrackets

    removeRightBrackets.replaceAll("\"", "").split(",").toSeq
  }
}