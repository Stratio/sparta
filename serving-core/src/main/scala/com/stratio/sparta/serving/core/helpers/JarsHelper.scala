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
   * With the aim of having a pluggable system of plugins and given  a list of relative paths that contain jars (our
   * plugins). It tries to instance jars located in this paths and to load them in the classpath.
   * @param path base path when it starts to scan in order to find plugins.
   * @param endsWith to specify the end of the file.
   * @param contains to specify that the file has to contain whatever is in this parameter.
   * @param notContains to specify that the file hasn't to contain whatever is in this parameter.
   * @param excludedDirectories path to exclude and not look for plugins.
   * @param doAddToClassPath if it's true it will add the jars to the class path
   * @return a list of loaded jars.
   */

  def findJarsByPath(path : File,
                     endsWith : Option[String] = None,
                     contains : Option[String] = None,
                     notContains : Option[String] = None,
                     excludedDirectories : Option[Seq[String]] = None,
                     doAddToClassPath : Boolean = true) : Seq[File] = {
    if (isFileNotExcluded(path, excludedDirectories)) {
      val these = path.listFiles()
      val good = these.filter(f => {
        val filter = endsWith.forall(ends => f.getName.endsWith(ends)) &&
          contains.forall(cont => f.getName.contains(cont)) &&
          notContains.forall(ncont => !f.getName.contains(ncont))

        if (doAddToClassPath && filter) {
          addToClasspath(f)
          log.debug("File " + f.getName + " added")
        }
        filter
      })
      good ++ these.filter(file => isDirectoryNotExluded(file, excludedDirectories))
        .flatMap(path => findJarsByPath(path, endsWith, contains, notContains, excludedDirectories, doAddToClassPath))
    } else {
      log.warn(s"The file ${path.getName} not exists or is excluded")
      Seq()
    }
  }

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
          f.getName.toLowerCase.contains("plugin") &&
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
      method.invoke(ClassLoader.getSystemClassLoader, file.toURI.toURL)
    } else {
      log.warn(s"The file ${file.getName} not exists.")
    }
  }

  /**
   *
   * @param file file to search
   * @param excludedDirectories list of directories names excluded from the search
   * @return
   */
  private def isFileNotExcluded(file : File, excludedDirectories : Option[Seq[String]]) : Boolean =
    file.exists && (!file.isDirectory || isExcludedDirectory(file, excludedDirectories))

  /**
   *
   * @param directory file to search
   * @param excludedDirectories list of directories names excluded from the search
   * @return
   */
  private def isDirectoryNotExluded(directory : File, excludedDirectories : Option[Seq[String]]) : Boolean =
    directory.exists && (directory.isDirectory || isExcludedDirectory(directory, excludedDirectories))

  /**
   *
   * @param path to check if is excluded
   * @param excludedDirectories list of directories names excluded from the search
   * @return
   */
  private def isExcludedDirectory(path : File, excludedDirectories : Option[Seq[String]]) : Boolean =
    path.isDirectory && excludedDirectories.forall(folder => !folder.contains(path.getName))
}
