/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.utils

import java.net.URLClassLoader

import scala.collection.JavaConversions._
import scala.util.Try
import akka.event.slf4j.SLF4JLogging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Dataset
import org.apache.spark.streaming.dstream.DStream
import org.reflections.Reflections
import com.stratio.sparta.core.workflow.step.{InputStep, OutputStep, TransformStep}
import com.stratio.sparta.sdk.lite.batch.{LiteCustomBatchInput, LiteCustomBatchTransform}
import com.stratio.sparta.sdk.lite.xd.batch.{LiteCustomXDBatchInput, LiteCustomXDBatchTransform}
import com.stratio.sparta.sdk.lite.common.LiteCustomOutput
import com.stratio.sparta.sdk.lite.xd.common.LiteCustomXDOutput
import com.stratio.sparta.sdk.lite.streaming.{LiteCustomStreamingInput, LiteCustomStreamingTransform}
import com.stratio.sparta.sdk.lite.xd.streaming.{LiteCustomXDStreamingInput, LiteCustomXDStreamingTransform}

class ClasspathUtils extends SLF4JLogging {

  lazy val defaultStepsInClasspath: Map[String, String] = {
    classesInClasspath(
      classes = Seq(
        classOf[InputStep[DStream]], classOf[OutputStep[DStream]], classOf[TransformStep[DStream]],
        classOf[LiteCustomBatchInput], classOf[LiteCustomBatchTransform], classOf[LiteCustomOutput],
        classOf[LiteCustomXDBatchInput], classOf[LiteCustomXDBatchTransform], classOf[LiteCustomXDOutput],
        classOf[LiteCustomStreamingInput], classOf[LiteCustomStreamingTransform],
        classOf[LiteCustomXDStreamingInput], classOf[LiteCustomXDStreamingTransform],
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

  //scalastyle:off
  def tryToInstantiate[C](
                           classAndPackage: String,
                           block: Class[_] => C,
                           inputClazzMap: Map[String, String] = Map.empty[String, String]
                         ): C = {
    //If non empty. the custom classes are added to Sparta internal classes
    val clazMap: Map[String, String] = if(inputClazzMap.isEmpty) defaultStepsInClasspath else inputClazzMap ++ defaultStepsInClasspath
    val finalClazzToInstance = clazMap.getOrElse(classAndPackage, classAndPackage)
    try {
      val clazz = Class.forName(finalClazzToInstance)
      block(clazz)
    } catch {
      case e: ClassNotFoundException =>
        throw new Exception(s"Class $classAndPackage cannot be found in the classpath. ${e.toString}", e)
      case e: InstantiationException =>
        throw new Exception(s"Class $classAndPackage cannot be instantiated. ${e.toString}", e)
      case e: Exception =>
        throw new Exception(s"Generic error trying to instantiate $classAndPackage. ${e.toString}", e)
    }
  }

  private def printClassPath(cl: ClassLoader): Unit = {
    val urls = cl.asInstanceOf[URLClassLoader].getURLs
    urls.foreach(url => log.debug(url.getFile))
  }
}