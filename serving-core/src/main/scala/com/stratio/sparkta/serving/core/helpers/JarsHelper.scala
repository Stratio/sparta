/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.serving.core.helpers

import java.io.File
import java.lang.reflect.Method
import java.net.{URL, URLClassLoader}

import akka.event.slf4j.SLF4JLogging

object JarsHelper extends SLF4JLogging {

  /**
   * With the aim of having a pluggable system of plugins and given  a list of relative paths that contain jars (our
   * plugins). It tries to instance jars located in this paths and to load them in the classpath.
   * @param relativeJarPaths that contains jar plugins.
   * @param sparktaHome with Sparkta's base path.
   * @return a list of loaded jars.
   */
  def initStandAloneJars(relativeJarPaths: Seq[String], sparktaHome: String): Seq[File] =
    relativeJarPaths.flatMap(path => {
      log.info(s"> Loading jars from $sparktaHome/$path")
      findJarsByPath(new File(sparktaHome, path), true)
    })

  /**
   * Finds files that end with the sufix *-plugin.jar and load them in the classpath of the application.
   * @param path base path when it starts to scan in order to find plugins.
   * @param doAddToClassPath Add the jar founded to classPath.
   * @return a list of jars.
   */
  def findJarsByPath(path: File, doAddToClassPath: Boolean): Seq[File] = {
    val these = path.listFiles()
    val good = these.filter(f => {
      if (f.getName.endsWith("-plugin.jar")) {
        if (doAddToClassPath) {
          addToClasspath(f)
          log.debug("File " + f.getName + " added")
        }
        true
      } else {
        false
      }
    })
    good ++ these.filter(_.isDirectory).flatMap(path => findJarsByPath(path, doAddToClassPath))
  }

  /**
   * Adds a file to the classpath of the application.
   * @param file to add in the classpath.
   */
  def addToClasspath(file: File): Unit = {
    val method: Method = classOf[URLClassLoader].getDeclaredMethod("addURL", classOf[URL])
    method.setAccessible(true)
    method.invoke(ClassLoader.getSystemClassLoader, file.toURI.toURL)
  }
}
