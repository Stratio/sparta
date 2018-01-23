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
package com.stratio.sparta.sdk.properties

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