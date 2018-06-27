/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.properties

import java.io.StringReader

import akka.event.slf4j.SLF4JLogging
import com.github.mustachejava.DefaultMustacheFactory
import com.twitter.mustache.ScalaObjectHandler

case class JsoneyString(
                         private val string: String,
                         private val environmentContext: Option[EnvironmentContext] = None
                       ) extends SLF4JLogging {

  override def toString: String = parseStringWithEnvContext(string)

  def toSeq: Seq[String] = {
    parseStringWithEnvContext(string.drop(1).dropRight(1).replaceAll("\"", "")).split(",").toSeq
  }

  private def parseStringWithEnvContext(valueToParse: String): String = {
    environmentContext match {
      case Some(context) =>
        val writer = new java.io.StringWriter()
        val moustacheFactory = new DefaultMustacheFactory
        moustacheFactory.setObjectHandler(new ScalaObjectHandler)
        val mustache = moustacheFactory.compile(new StringReader(string), "MoustacheEnv")
        mustache.execute(writer, context.environmentVariables)
        val parsedStr = writer.toString
        writer.flush()
        parsedStr
      case None =>
        string
    }
  }
}