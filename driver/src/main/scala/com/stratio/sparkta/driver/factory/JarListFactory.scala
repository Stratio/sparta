/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.driver.factory

import java.io.File
import java.lang.reflect.Method
import java.net.{URL, URLClassLoader}

import akka.event.slf4j.SLF4JLogging

/**
 * Get all jars into a specified directory recursively
 */
object JarListFactory extends SLF4JLogging {

  def findJarsByPath(f: File): Array[File] = {
    val these = f.listFiles()
    val good = these.filter(f => {
      if (f.getName.endsWith("-plugin.jar")) {
        addToClasspath(f)
        log.debug("File " + f.getName + " added")
        true
      } else {
        false
      }
    })

    good ++ these.filter(_.isDirectory).flatMap(findJarsByPath)
  }

  private def addToClasspath(file: File): Unit = {
    val method: Method = classOf[URLClassLoader].getDeclaredMethod("addURL", classOf[URL])
    method.setAccessible(true)
    method.invoke(ClassLoader.getSystemClassLoader(), file.toURI().toURL());
  }

}


