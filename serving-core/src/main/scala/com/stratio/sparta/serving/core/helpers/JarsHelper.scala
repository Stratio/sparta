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

package com.stratio.sparta.serving.core.helpers

import java.io.File
import java.lang.reflect.Method
import java.net.{URL, URLClassLoader}

import akka.event.slf4j.SLF4JLogging

object JarsHelper extends SLF4JLogging {

  /**
   * Finds files that are the driver application.
   * @param path base path when it starts to scan in order to find plugins.
   * @return a list of jars.
   */
  def findDriverByPath(path : File) : Seq[File] = {
    if (path.exists) {
      val these = path.listFiles()
      val good =
        these.filter(f => f.getName.toLowerCase.contains("driver") &&
          f.getName.toLowerCase.contains("sparta") &&
          f.getName.endsWith(".jar"))

      good ++ these.filter(_.isDirectory).flatMap(path => findDriverByPath(path))
    } else {
      log.warn(s"The file ${path.getName} not exists.")
      Seq()
    }
  }

  /**
   * Adds a file to the classpath of the application.
   * @param file to add in the classpath.
   */
  def addToClasspath(file : File) : Unit = {
    if (file.exists) {
      val method : Method = classOf[URLClassLoader].getDeclaredMethod("addURL", classOf[URL])

      method.setAccessible(true)
      method.invoke(getClass.getClassLoader, file.toURI.toURL)
    } else {
      log.warn(s"The file ${file.getName} not exists.")
    }
  }

}
