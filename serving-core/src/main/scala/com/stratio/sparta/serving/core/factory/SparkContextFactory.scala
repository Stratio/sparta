/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.factory

import java.io.File
import java.nio.file.{Files, Paths}
import javax.xml.bind.DatatypeConverter

import org.apache.spark.sql.functions.udf
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.AggregationTimeHelper
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.utils.ClasspathUtils
import com.stratio.sparta.sdk.lite.common.{SpartaUDAF, SpartaUDF}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.MarathonConstant
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.services.SparkSubmitService.spartaTenant
import com.stratio.sparta.serving.core.services.{HdfsService, SparkSubmitService}
import org.apache.spark.scheduler.KerberosUser
import org.apache.spark.security.ConfigSecurity
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.streaming.{Duration, StreamingContext, StreamingContextState}
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.{Failure, Properties, Success, Try}

object SparkContextFactory extends SLF4JLogging {

  /* MUTABLE VARIABLES */

  private var sc: Option[SparkContext] = None
  private var ssc: Option[StreamingContext] = None
  private val xdSession = scala.collection.mutable.Map[String, XDSession]()


  /* LAZY VARIABLES */

  private lazy val xDConfPath = SpartaConfig.getDetailConfig().get.getString("crossdata.reference")
  private lazy val hdfsWithUgiService = Try(HdfsService()).toOption
    .flatMap(utils => utils.ugiOption.flatMap(_ => Option(utils)))
  private lazy val jdbcDriverVariables: Seq[(String, String)] =
    SparkSubmitService.getJarsSparkConfigurations(JarsHelper.getJdbcDriverPaths, true).toSeq
  private lazy val kerberosYarnDefaultVariables: Seq[(String, String)] = {
    val hdfsConfig = SpartaConfig.getHdfsConfig()
    (HdfsService.getPrincipalName(hdfsConfig).notBlank, HdfsService.getKeyTabPath(hdfsConfig).notBlank) match {
      case (Some(principal), Some(keyTabPath)) =>
        KerberosUser.securize(principal, keyTabPath)
        Seq(
          ("spark.yarn.principal", principal),
          ("spark.hadoop.yarn.resourcemanager.principal", principal)
        )
      case _ =>
        Seq.empty[(String, String)]
    }
  }
  private lazy val securityVariables: Seq[(String, String)] = {
    if (Properties.envOrNone("SPARK_SECURITY_DATASTORE_ENABLE").isDefined) {
      val environment = ConfigSecurity.prepareEnvironment
      log.debug(s"XDSession secured environment prepared with variables: $environment")
      environment.toSeq
    } else Seq.empty[(String, String)]
  }
  private lazy val proxyVariables: Seq[(String, String)] = {
    if (Properties.envOrNone(MarathonConstant.NginxMarathonLBHostEnv).isDefined) {
      val proxyPath = s"/workflows-$spartaTenant/crossdata-sparkUI"
      log.debug(s"XDSession with proxy base: $proxyPath")
      Seq(("spark.ui.proxyBase", proxyPath))
    } else Seq.empty[(String, String)]
  }
  private lazy val classpathUtils = new ClasspathUtils


  /* PUBLIC METHODS */

  def maybeWithHdfsUgiService(f: => Unit): Unit = hdfsWithUgiService.map(_.runFunction(f)).getOrElse(f)

  //scalastyle:off

  def getXDSession(userId: Option[String] = None): Option[XDSession] = {
    val sessionId = getSessionIdFromUserId(userId)

    xdSession.get(sessionId)
  }

  def getSparkContext: Option[SparkContext] = sc

  def getOrCreateStandAloneXDSession(userId: Option[String]): XDSession =
    getOrCreateXDSession(withStandAloneExtraConf = true, userId, forceStop = false)

  def getOrCreateXDSession(
                            withStandAloneExtraConf: Boolean,
                            userId: Option[String],
                            forceStop: Boolean,
                            extraConfiguration: Map[String, String] = Map.empty[String, String]
                          ): XDSession = {
    synchronized {
      if (forceStop) stopSparkContext()
      val sessionId = getSessionIdFromUserId(userId)
      maybeWithHdfsUgiService {
        if (xdSession.isEmpty) {
          val referenceFile = Try {
            new File(xDConfPath)
          } match {
            case Success(file) =>
              log.info(s"Loading Crossdata configuration from file: ${file.getAbsolutePath}")
              file
            case Failure(e) =>
              val refFile = "/reference.conf"
              log.debug(s"Error loading Crossdata configuration file with error: ${e.getLocalizedMessage}")
              log.debug(s"Loading Crossdata configuration from resource file $refFile")
              new File(getClass.getResource(refFile).getPath)
          }

          JarsHelper.addJdbcDriversToClassPath()

          sc match {
            case Some(sparkContext) =>
              val sparkConf = sparkContext.getConf

              if (withStandAloneExtraConf) {
                log.debug("Adding StandAlone configuration to Spark Session")
                addStandAloneExtraConf(sparkConf)
              }

              log.debug(s"Creating session($sessionId) from file $referenceFile with sparkConf: ${sparkConf.toDebugString}")

              val newSession = XDSession.builder()
                .config(referenceFile)
                .config(sparkConf)
                .create(sessionId)

              xdSession += (sessionId -> newSession)
            case None =>
              val sparkConf = new SparkConf()

              if (withStandAloneExtraConf) {
                log.debug("Adding StandAlone configuration to Spark Session")
                addStandAloneExtraConf(sparkConf)
              }

              log.debug(s"Creating session($sessionId) and sparkContext from file $referenceFile with sparkConf: ${sparkConf.toDebugString}")

              getOrCreateSparkContext(sparkConf.getAll.toMap ++ extraConfiguration)

              val newSession = XDSession.builder()
                .config(referenceFile)
                .config(sparkConf)
                .create(sessionId)

              xdSession += (sessionId -> newSession)
          }


        } else if (!xdSession.contains(sessionId)) {
          xdSession += (sessionId -> xdSession.head._2.newSession(sessionId))
        }
      }

      xdSession.getOrElse(sessionId, throw new Exception("Spark Session not initialized"))
    }
  }

  def executeSentences(sqlSentences: Seq[String], userId: Option[String] = None): Unit = {
    maybeWithHdfsUgiService {
      val sessionId = getSessionIdFromUserId(userId)
      xdSession.get(sessionId).foreach { session =>
        sqlSentences.filter(_.nonEmpty).foreach { sentence =>
          val trimSentence = sentence.trim
          session.sql(trimSentence)
        }
      }
    }
  }

  def registerUdfs(udfsToRegister: Seq[String], userId: Option[String] = None): Unit = {
    maybeWithHdfsUgiService {
      val sessionId = getSessionIdFromUserId(userId)
      xdSession.get(sessionId).foreach { session =>
        udfsToRegister.foreach { udfName =>
          val (customClass, customClassAndPackage) = classpathUtils.getCustomClassAndPackage(udfName)
          val udfToRegister = classpathUtils.tryToInstantiate[SpartaUDF](
            classAndPackage = customClass,
            block = (c) => c.newInstance().asInstanceOf[SpartaUDF],
            inputClazzMap = Map(customClass -> customClassAndPackage)
          )

          session.udf.register(udfToRegister.name, udfToRegister.userDefinedFunction)
        }
      }
    }
  }

  def registerUdafs(udafsToRegister: Seq[String], userId: Option[String] = None): Unit = {
    maybeWithHdfsUgiService {
      val sessionId = getSessionIdFromUserId(userId)
      xdSession.get(sessionId).foreach { session =>
        udafsToRegister.foreach { udafName =>
          val (customClass, customClassAndPackage) = classpathUtils.getCustomClassAndPackage(udafName)
          val udafToRegister = classpathUtils.tryToInstantiate[SpartaUDAF](
            classAndPackage = customClass,
            block = (c) => c.newInstance().asInstanceOf[SpartaUDAF],
            inputClazzMap = Map(customClass -> customClassAndPackage)
          )

          session.udf.register(udafToRegister.name, udafToRegister.userDefinedAggregateFunction)
        }
      }
    }
  }

  def addFilesToSparkContext(files: Seq[String]): Unit = {
    maybeWithHdfsUgiService {
      sc.foreach { sparkContext =>
        files.foreach { file =>
          log.info(s"Adding file $file to Spark context")
          sparkContext.addJar(file)
        }
      }
    }
  }

  def getOrCreateStreamingContext(
                                   batchDuration: Duration,
                                   checkpointDir: Option[String],
                                   remember: Option[String]
                                 ): StreamingContext =
    synchronized {
      ssc.getOrElse(createStreamingContext(batchDuration, checkpointDir, remember))
    }

  def getStreamingContext: StreamingContext =
    ssc.getOrElse(throw new Exception("Streaming Context not initialized"))

  def stopContexts(): Unit = synchronized {
    stopStreamingContext(stopGracefully = true)
    stopSparkContext()
  }

  def stopSparkContext(): Unit = {
    sc.fold(log.debug("Spark Context is empty")) { sparkContext =>
      try {
        log.debug(s"Stopping Spark Context named: ${sparkContext.appName}")
        Try(sparkContext.stop()) match {
          case Success(_) =>
            log.debug("Spark Context has been stopped")
          case Failure(error) =>
            log.debug("Spark Context not properly stopped", error)
        }
      } finally {
        sc = None
      }
    }
  }

  def stopStreamingContext(stopGracefully: Boolean = false, stopSparkContext: Boolean = false): Unit = synchronized {
    ssc.orElse(StreamingContext.getActive()).fold(log.debug("Spark Streaming Context is empty")) { streamingContext =>
      try {
          log.debug(s"Stopping Streaming Context named: ${streamingContext.sparkContext.appName}")
          Try(streamingContext.stop(stopSparkContext, stopGracefully)) match {
            case Success(_) =>
              log.debug("Streaming Context has been stopped")
            case Failure(error) =>
              log.debug("Streaming Context not properly stopped", error)
          }
      } finally {
        ssc = None
      }
    }
  }


  /* PRIVATE METHODS */

  private[core] def getSessionIdFromUserId(userId: Option[String] = None): String = userId.getOrElse(spartaTenant)

  private[core] def getOrCreateSparkContext(extraConfiguration: Map[String, String]): SparkContext =
    synchronized {
      sc = Option(SparkContext.getOrCreate(configurationToSparkConf(extraConfiguration)))
      sc.get
    }

  private[core] def addStandAloneExtraConf(sparkConf: SparkConf): SparkConf = {
    val configurationsToAdd = securityVariables ++ proxyVariables ++ jdbcDriverVariables ++
      kerberosYarnDefaultVariables ++ SparkSubmitService.getSparkStandAloneConfig

    sparkConf.setAll(configurationsToAdd)

    log.debug(s"Added variables to Spark Conf in XDSession: $configurationsToAdd")

    sparkConf.setAppName(SparkSubmitService.spartaLocalAppName)

    sparkConf
  }

  private[core] def createStreamingContext(
                                            batchDuration: Duration,
                                            checkpointDir: Option[String],
                                            remember: Option[String]
                                          ): StreamingContext = {
    val sparkContext = sc.getOrElse(throw new Exception("Spark Context not initialized," +
      " is mandatory initialize it before create a new Streaming Context"))
    ssc = Option(new StreamingContext(sparkContext, batchDuration))
    checkpointDir.foreach(ssc.get.checkpoint)
    remember.foreach(value => ssc.get.remember(Duration(AggregationTimeHelper.parseValueToMilliSeconds(value))))
    ssc.get
  }

  private[core] def configurationToSparkConf(configuration: Map[String, String]): SparkConf = {
    val conf = new SparkConf()
    configuration.foreach { case (key, value) => conf.set(key, value) }
    conf
  }

  private[core] def stopSparkContext(stopStreaming: Boolean = true): Unit = {
    if (stopStreaming) stopStreamingContext()

    sc.fold(log.debug("Spark Context is empty")) { sparkContext =>
      synchronized {
        try {
          log.debug("Stopping XDSessions")
          xdSession.foreach(_._2.stopAll())
          log.debug("Stopping SparkContext: " + sparkContext.appName)
          sparkContext.stop()
          log.debug("SparkContext: " + sparkContext.appName + " stopped correctly")
        } finally {
          xdSession.clear()
          ssc = None
          sc = None
        }
      }
    }
  }
}