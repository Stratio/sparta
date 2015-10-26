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

package com.stratio.sparkta.serving.api.actor

import java.io.File

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.driver.util.{HdfsUtils, PolicyUtils}
import com.stratio.sparkta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparkta.serving.core.SparktaConfig
import com.stratio.sparkta.serving.core.constants.AppConstant
import com.stratio.sparkta.serving.core.helpers.JarsHelper
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel, SparktaSerializer}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum
import com.typesafe.config.{Config, ConfigRenderOptions}
import org.apache.commons.lang.StringEscapeUtils

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.sys.process._
import scala.util.{Failure, Success, Try}

class ClusterSparkStreamingContextActor(policy: AggregationPoliciesModel,
                                        streamingContextService: StreamingContextService,
                                        clusterConfig: Config,
                                        hdfsConfig: Config,
                                        zookeeperConfig: Config,
                                        detailConfig: Option[Config],
                                        policyStatusActor: ActorRef) extends InstrumentedActor with SparktaSerializer {

  implicit val timeout: Timeout = Timeout(3.seconds)

  override def receive: PartialFunction[Any, Unit] = {
    case Start => doInitSparktaContext
  }

  //scalastyle:off
  def doInitSparktaContext: Unit = {
    Try {
      log.info("Init new cluster streamingContext with name " + policy.name)
      val jarsPlugins = JarsHelper.findJarsByPath(
        new File(SparktaConfig.sparktaHome, AppConstant.JarPluginsFolder), Some("-plugin.jar"), None, None, None, false)
      val activeJars = PolicyUtils.activeJars(policy, jarsPlugins)
      if (checkPolicyJars(activeJars)) {
        val main = getMainCommand
        if (main.isEmpty) {
          log.warn("You must set the sparkSubmit path in configuration")
          setErrorStatus
        }
        else {
          val policyId = policy.id.get.trim
          val hadoopUserName =
            scala.util.Properties.envOrElse("HADOOP_USER_NAME", hdfsConfig.getString(AppConstant.HadoopUserName))
          val hdfsUgi = HdfsUtils.ugi(hadoopUserName)
          val hadoopConfDir =
            Some(scala.util.Properties.envOrElse("HADOOP_CONF_DIR", hdfsConfig.getString(AppConstant.HadoopConfDir)))
          val hdfsConf = HdfsUtils.hdfsConfiguration(hadoopConfDir)
          val hdfsUtils = new HdfsUtils(hdfsUgi, hdfsConf)
          val pluginsJarsFiles = PolicyUtils.activeJarFiles(activeJars.right.get, jarsPlugins)
          val pluginsJarsPath =
            s"/user/$hadoopUserName/$policyId/${hdfsConfig.getString(AppConstant.PluginsFolder)}/"
          val classpathJarsPath =
            s"/user/$hadoopUserName/$policyId/${hdfsConfig.getString(AppConstant.ClasspathFolder)}/"

          pluginsJarsFiles.foreach(file => hdfsUtils.write(file.getAbsolutePath, pluginsJarsPath, true))
          log.info("Jars plugins uploaded to HDFS")

          JarsHelper.findJarsByPath(new File(SparktaConfig.sparktaHome, AppConstant.ClasspathJarFolder),
            Some(".jar"), None, Some("driver"), Some(Seq("plugins", "spark", "driver", "web", "serving-api")), false)
            .foreach(file => hdfsUtils.write(file.getAbsolutePath, classpathJarsPath, true))
          log.info("Classpath uploaded to HDFS")

          JarsHelper.findDriverByPath(
            new File(SparktaConfig.sparktaHome, AppConstant.ClusterExecutionJarFolder)).headOption match {
            case Some(driverJar) => {
              val driverJarPath = s"/user/$hadoopUserName/$policyId/" +
                s"${hdfsConfig.getString(AppConstant.ExecutionJarFolder)}/"
              val hdfsDriverFile =
                s" hdfs://${hdfsConfig.getString(AppConstant.HdfsMaster)}$driverJarPath${driverJar.getName}"
              hdfsUtils.write(driverJar.getAbsolutePath, driverJarPath, true)
              log.info("Jar driver uploaded to HDFS")
              sparkSubmitExecution(main, hdfsDriverFile, pluginsJarsPath, classpathJarsPath)
            }
            case None => {
              log.warn(s"The driver jar cannot be found in classpath")
              setErrorStatus
            }
          }
        }
      }
    } match {
      case Failure(exception) => {
        log.error(exception.getLocalizedMessage, exception)
        setErrorStatus
      }
      case Success(_) => {
        //TODO add more statuses for the policies
      }
    }
  }

  //scalastyle:on

  private def setErrorStatus: Unit = {
    policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Failed))
  }

  private def sparkSubmitExecution(main: String,
                                   hdfsDriverFile: String,
                                   pluginsJarsPath: String,
                                   classpathJarsPath: String): Unit = {
    val commandString = getExecutionModel match {
      case AppConstant.ConfigMesos => getMesosCommandString
      case AppConstant.ConfigYarn => getYarnCommandString
      case AppConstant.ConfigStandAlone => getStandAloneCommandString
      case _ => throw new IllegalArgumentException(s"Execution model do not exist.")
    }

    sparkSubmitSentence(
      main, hdfsDriverFile, pluginsJarsPath, classpathJarsPath, getExecutionModel, commandString)
  }

  private def sparkSubmitSentence(main: String,
                                  hdfsDriverFile: String,
                                  pluginsJarsPath: String,
                                  classpathJarsPath: String,
                                  execMode: String,
                                  execModeSentence: String): Unit = {
    val cmd = s"$main $getGenericCommand $execModeSentence $getSparkConfig $hdfsDriverFile " +
      s"${policy.id.get} $pluginsJarsPath $classpathJarsPath $getZookeeperCommand $getDetailConfigCommand"

    log.info(s"$cmd")

    cmd.!!
  }

  private def checkPolicyJars(activeJars: Either[Seq[String], Seq[String]]): Boolean = {
    if (activeJars.isLeft || activeJars.right.get.isEmpty) {
      if (activeJars.isLeft) {
        log.warn(s"The policy have jars witch cannot be found in classpath:")
        activeJars.left.get.foreach(log.warn)
      } else {
        if (activeJars.right.get.isEmpty) {
          log.warn(s"The policy don't have jars in the plugins")
        }
      }
      false
    } else true
  }

  private def getMainCommand: String = {
    var sparkPath = scala.util.Properties.envOrElse("SPARK_HOME",
      SparktaConfig.getOptionStringConfig(AppConstant.SparkHome, clusterConfig) match {
        case Some(submitPath) => s"$submitPath"
        case None => ""
      })
    sparkPath = if (sparkPath.endsWith("/")) sparkPath else sparkPath + "/"
    s"${sparkPath}bin/spark-submit --class com.stratio.sparkta.driver.SparktaClusterJob"
  }

  private def getGenericCommand: String = {
    val deploy = SparktaConfig.getOptionStringConfig(AppConstant.DeployMode, clusterConfig)
      .map(deployMode => s"--deploy-mode $deployMode")
    val executors = SparktaConfig.getOptionStringConfig(AppConstant.NumExecutors, clusterConfig)
      .map(numExecutors => s"--num-executors $numExecutors")
    val executorCores = SparktaConfig.getOptionStringConfig(AppConstant.ExecutorCores, clusterConfig)
      .map(cores => s"--executor-cores $cores")
    val totalExecutorCores = SparktaConfig.getOptionStringConfig(AppConstant.TotalExecutorCores, clusterConfig)
      .map(totalCores => s"--total-executor-cores $totalCores")
    val executorMemory = SparktaConfig.getOptionStringConfig(AppConstant.ExecutorMemory, clusterConfig)
      .map(executorsMemory => s"--executor-memory $executorsMemory")

    Seq(deploy, executors, executorCores, totalExecutorCores, executorMemory).flatten.mkString(" ")
  }

  private def getMesosCommandString: String = {
    val master = SparktaConfig.getOptionStringConfig(AppConstant.MesosMasterDispatchers, clusterConfig) match {
      case Some(masterDispatcher) => s"--master mesos://$masterDispatcher:7077"
      case None => ""
    }
    master
  }

  private def getYarnCommandString: String = {
    val master = SparktaConfig.getOptionStringConfig(AppConstant.YarnMaster, clusterConfig) match {
      case Some(master) => s"--master $master"
      case None => ""
    }
    val queue = SparktaConfig.getOptionStringConfig(AppConstant.YarnQueue, clusterConfig) match {
      case Some(queue) => s"--queue $queue"
      case None => ""
    }
    master + queue
  }

  private def getStandAloneCommandString: String = {
    val master = SparktaConfig.getOptionStringConfig(AppConstant.StandAloneMasterNode, clusterConfig) match {
      case Some(master) => s"--master spark://$master:7077"
      case None => ""
    }
    val supervise = SparktaConfig.getOptionStringConfig(AppConstant.StandAloneSupervise, clusterConfig) match {
      case Some(supervise) => if (supervise == "true") s"--supervise" else ""
      case None => ""
    }
    master + supervise
  }

  private def getZookeeperCommand: String = {
    val config = zookeeperConfig.atKey("zk").root.render(ConfigRenderOptions.concise)
    StringEscapeUtils.escapeJavaScript(config)
  }

  private def getDetailConfigCommand: String = {
    if (detailConfig.isDefined) {
      val config = detailConfig.get.atKey("config").root.render(ConfigRenderOptions.concise)
      StringEscapeUtils.escapeJavaScript(config)
    } else ""
  }

  private def getSparkConfig: String = {
    val properties = clusterConfig.entrySet()
    properties.flatMap(prop => {
      if (prop.getKey.startsWith("spark.")) Some(s"--conf ${prop.getKey}=${clusterConfig.getString(prop.getKey)}")
      else None
    }).mkString(" ")
  }

  def getExecutionModel: String = detailConfig match {
    case Some(config) => config.getString(AppConstant.ExecutionMode)
    case None => AppConstant.DefaultExecutionMode
  }
}
