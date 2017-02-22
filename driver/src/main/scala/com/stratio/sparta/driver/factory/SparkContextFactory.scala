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

package com.stratio.sparta.driver.factory

import java.io.File

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.utils.AggregationTime
import com.typesafe.config.Config
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

object SparkContextFactory extends SLF4JLogging {

  private var sc: Option[SparkContext] = None
  private var sqlContext: Option[SQLContext] = None
  private var ssc: Option[StreamingContext] = None
  private var sqlInitialSentences: Seq[String] = Seq.empty[String]

  def sparkSqlContextInstance: SQLContext = {
    synchronized {
      sqlContext match {
        case Some(context) =>
          context
        case None =>
          if (sc.isDefined) sqlContext = Option(SQLContext.getOrCreate(sc.get))
          sqlInitialSentences.foreach(sentence => if (sentence.nonEmpty) sqlContext.get.sql(sentence))
          sqlContext.get
      }
    }
  }

  def setInitialSentences(sentences: Seq[String]): Unit = sqlInitialSentences = sentences

  def sparkStreamingInstance(batchDuration: Duration, checkpointDir: String, remember: Option[String]):
  Option[StreamingContext] = {
    synchronized {
      ssc match {
        case Some(_) => ssc
        case None => ssc = Some(getNewStreamingContext(batchDuration, checkpointDir, remember))
      }
    }
    ssc
  }

  def setSparkContext(createdContext: SparkContext): Unit = sc = Option(createdContext)

  def setSparkStreamingContext(createdContext: StreamingContext): Unit = ssc = Option(createdContext)

  private def getNewStreamingContext(batchDuration: Duration, checkpointDir: String, remember: Option[String]):
  StreamingContext = {
    val ssc = new StreamingContext(sc.get, batchDuration)
    ssc.checkpoint(checkpointDir)
    remember.foreach(value => ssc.remember(Duration(AggregationTime.parseValueToMilliSeconds(value))))
    ssc
  }

  def sparkStandAloneContextInstance(specificConfig: Map[String, String], jars: Seq[File]): SparkContext =
    synchronized {
      sc.getOrElse(instantiateSparkContext(specificConfig, jars))
    }

  def sparkClusterContextInstance(specificConfig: Map[String, String], files: Seq[String]): SparkContext =
    sc.getOrElse(instantiateClusterContext(specificConfig, files))

  private def instantiateSparkContext(specificConfig: Map[String, String], jars: Seq[File]): SparkContext = {
    sc = Some(SparkContext.getOrCreate(configToSparkConf(specificConfig)))
    jars.foreach(f => {
      log.info(s"Adding jar ${f.getAbsolutePath} to Spark context")
      sc.get.addJar(f.getAbsolutePath)
    })
    sc.get
  }

  private def instantiateClusterContext(specificConfig: Map[String, String], jars: Seq[String]): SparkContext = {
    sc = Some(SparkContext.getOrCreate(configToSparkConf(specificConfig)))
    jars.foreach(f => {
      log.info(s"Adding jar $f to cluster Spark context")
      sc.get.addJar(f)
    })
    sc.get
  }

  private def configToSparkConf(specificConfig: Map[String, String]): SparkConf = {
    val conf = new SparkConf()
    specificConfig.foreach { case (key, value) => conf.set(key, value) }
    conf
  }

  def destroySparkStreamingContext(): Unit = {
    ssc.fold(log.warn("Spark Streaming Context is empty")) { streamingContext =>
      try {
        synchronized {
          log.info(s"Stopping Streaming Context with name: ${streamingContext.sparkContext.appName}")
          Try(streamingContext.stop(stopSparkContext = false, stopGracefully = false)) match {
            case Success(_) =>
              log.info("Streaming Context have been stopped")
            case Failure(error) =>
              log.error("Streaming Context is not been stopped correctly", error)
          }
        }
      } finally {
        ssc = None
      }
    }
  }

  def destroySparkContext(destroyStreamingContext: Boolean = true): Unit = {
    if (destroyStreamingContext)
      destroySparkStreamingContext()

    sc.fold(log.warn("Spark Context is empty")) { sparkContext =>
      synchronized {
        try {
          log.info("Stopping SparkContext with name: " + sparkContext.appName)
          sparkContext.stop()
          log.info("Stopped SparkContext with name: " + sparkContext.appName)
        } finally {
          sqlContext = None
          sqlInitialSentences = Seq.empty[String]
          ssc = None
          sc = None
        }
      }
    }
  }
}