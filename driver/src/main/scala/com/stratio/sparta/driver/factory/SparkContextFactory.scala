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
import com.stratio.sparta.sdk.utils.AggregationTimeUtils
import com.stratio.sparta.serving.core.config.SpartaConfig
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.{Failure, Properties, Success, Try}

object SparkContextFactory extends SLF4JLogging {

  private var sc: Option[SparkContext] = None
  private var xdSession: Option[XDSession] = None
  private var ssc: Option[StreamingContext] = None
  private var sqlInitialSentences: Seq[String] = Seq.empty[String]

  def xdSessionInstance: XDSession = {
    synchronized {
      xdSession.getOrElse {
        val referenceFile = Try {
          val confPath = SpartaConfig.getDetailConfig.get.getString("crossdata.reference")
          new File(confPath)
        } match {
          case Success(file) =>
            log.info(s"Loading Crossdata configuration from file: ${file.getAbsolutePath}")
            file
          case Failure(e) =>
            val refFile = "/reference.conf"
            log.debug(s"Error loading Crossdata configuration file.", e)
            log.info(s"Loading Crossdata configuration from resource file $refFile ...")
            new File(getClass.getResource("/reference.conf").getPath)
        }
        if (sc.isDefined) xdSession = Option(XDSession.builder()
          .config(referenceFile)
          .config(sc.get.getConf)
          .create(Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME", "dummyUser")))
        sqlInitialSentences.filter(_.nonEmpty).foreach { sentence =>
          if (sentence.startsWith("CREATE") || sentence.startsWith("IMPORT"))
            xdSession.get.sql(sentence)
          else log.warn(s"Initial query ($sentence) not supported. Available operations: CREATE and IMPORT")
        }
        xdSession.get
      }
    }
  }


  def sparkStreamingInstance(batchDuration: Duration, checkpointDir: Option[String], remember: Option[String]):
  Option[StreamingContext] = {
    synchronized {
      ssc match {
        case Some(_) => ssc
        case None => ssc = Some(getNewStreamingContext(batchDuration, checkpointDir, remember))
      }
    }
    ssc
  }

  def sparkStandAloneContextInstance(specificConfig: Map[String, String], jars: Seq[File]): SparkContext =
    synchronized {
      sc.getOrElse(instantiateSparkContext(specificConfig, jars))
    }

  def destroySparkContext(destroyStreamingContext: Boolean = true): Unit = {
    if (destroyStreamingContext) destroySparkStreamingContext()

    sc.fold(log.warn("Spark Context is empty")) { sparkContext =>
      synchronized {
        try {
          log.debug("Stopping SparkContext named: " + sparkContext.appName)
          sparkContext.stop()
          log.info("SparkContext named: " + sparkContext.appName + "stopped correctly")
        } finally {
          xdSession = None
          sqlInitialSentences = Seq.empty[String]
          ssc = None
          sc = None
        }
      }
    }
  }

  private[driver] def setSparkContext(createdContext: SparkContext): Unit = sc = Option(createdContext)

  private[driver] def setSparkStreamingContext(createdContext: StreamingContext): Unit = ssc = Option(createdContext)

  private[driver] def getNewStreamingContext(
                                              batchDuration: Duration,
                                              checkpointDir: Option[String],
                                              remember: Option[String]
                                            ): StreamingContext = {
    val ssc = new StreamingContext(sc.get, batchDuration)
    checkpointDir.foreach(ssc.checkpoint)
    remember.foreach(value => ssc.remember(Duration(AggregationTimeUtils.parseValueToMilliSeconds(value))))
    ssc
  }

  private[driver] def sparkClusterContextInstance(specificConfig: Map[String, String],
                                                  files: Seq[String]): SparkContext =
    sc.getOrElse(instantiateClusterContext(specificConfig, files))

  private[driver] def instantiateSparkContext(specificConfig: Map[String, String], jars: Seq[File]): SparkContext = {
    sc = Some(SparkContext.getOrCreate(configToSparkConf(specificConfig)))
    jars.foreach(f => {
      log.info(s"Adding jar ${f.getAbsolutePath} to Spark context")
      sc.get.addJar(f.getAbsolutePath)
    })
    sc.get
  }

  private[driver] def instantiateClusterContext(specificConfig: Map[String, String],
                                                files: Seq[String]): SparkContext = {
    sc = Some(SparkContext.getOrCreate(configToSparkConf(specificConfig)))
    files.foreach(f => {
      log.info(s"Adding jar $f to cluster Spark context")
      sc.get.addJar(f)
    })
    sc.get
  }

  private[driver] def configToSparkConf(specificConfig: Map[String, String]): SparkConf = {
    val conf = new SparkConf()
    specificConfig.foreach { case (key, value) => conf.set(key, value) }
    conf
  }

  private[driver] def setInitialSentences(sentences: Seq[String]): Unit = sqlInitialSentences = sentences

  private[driver] def destroySparkStreamingContext(): Unit = {
    ssc.fold(log.warn("Spark Streaming Context is empty")) { streamingContext =>
      try {
        synchronized {
          log.debug(s"Stopping Streaming Context named: ${streamingContext.sparkContext.appName}")
          Try(streamingContext.stop(stopSparkContext = false, stopGracefully = false)) match {
            case Success(_) =>
              log.info("Streaming Context has been stopped")
            case Failure(error) =>
              log.error("Streaming Context not properly stopped", error)
          }
        }
      } finally {
        ssc = None
      }
    }
  }
}