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

package com.stratio.sparta.sdk.utils

import java.net.URLClassLoader

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputStep, TransformStep}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.streaming.dstream.DStream
import org.reflections.Reflections

import scala.collection.JavaConversions._
import scala.util.Try

class ClasspathUtils extends SLF4JLogging {

  lazy val defaultStepsInClasspath: Map[String, String] = {
    classesInClasspath(
      classes = Seq(
        classOf[InputStep[DStream]], classOf[OutputStep[DStream]], classOf[TransformStep[DStream]],
        classOf[InputStep[RDD]], classOf[OutputStep[RDD]], classOf[TransformStep[RDD]],
        classOf[InputStep[Dataset]], classOf[OutputStep[Dataset]], classOf[TransformStep[Dataset]]
      ),
      packagePath = "com.stratio.sparta",
      printClasspath = true
    )
  }

  def classesInClasspath(classes: Seq[Class[_]], packagePath: String, printClasspath: Boolean) : Map[String, String] = {
    val reflections = new Reflections(packagePath)

    if(printClasspath){
      Try {
        log.debug("SPARK MUTABLE_URL_CLASS_LOADER:")
        log.debug(getClass.getClassLoader.toString)
        printClassPath(getClass.getClassLoader)
        log.debug("APP_CLASS_LOADER / SYSTEM CLASSLOADER:")
        log.debug(ClassLoader.getSystemClassLoader.toString)
        printClassPath(ClassLoader.getSystemClassLoader)
        log.debug("EXTRA_CLASS_LOADER:")
        log.debug(getClass.getClassLoader.getParent.getParent.toString)
        printClassPath(getClass.getClassLoader.getParent.getParent)
      }
    }

    val result = classes.flatMap { clazz =>
      reflections.getSubTypesOf(clazz).map(t => t.getSimpleName -> t.getCanonicalName)
    }.toMap

    log.debug("PLUGINS LOADED:")
    result.foreach {
      case (_, canonicalName) => log.debug(s"$canonicalName")
    }

    result
  }

  def tryToInstantiate[C](
                           classAndPackage: String,
                           block: Class[_] => C,
                           inputClazzMap: Map[String, String] = Map.empty[String, String]
                         ): C = {
    val clazMap: Map[String, String] = if(inputClazzMap.isEmpty) defaultStepsInClasspath else inputClazzMap
    val finalClazzToInstance = clazMap.getOrElse(classAndPackage, classAndPackage)
    try {
      val clazz = Class.forName(finalClazzToInstance)
      block(clazz)
    } catch {
      case e: ClassNotFoundException =>
        throw new Exception("Class with name " + classAndPackage + " Cannot be found in the classpath.", e)
      case e: InstantiationException =>
        throw new Exception("Class with name " + classAndPackage + " cannot be instantiated", e)
      case e: Exception =>
        throw new Exception("Generic error trying to instantiate " + classAndPackage, e)
    }
  }

  private def printClassPath(cl: ClassLoader): Unit = {
    val urls = cl.asInstanceOf[URLClassLoader].getURLs
    urls.foreach(url => log.debug(url.getFile))
  }
}