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

import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.driver.util.{HdfsUtils, PolicyUtils}
import com.stratio.sparkta.serving.api.actor.StreamingActor._
import com.stratio.sparkta.serving.api.helpers.SparktaHelper
import com.stratio.sparkta.serving.core.AppConstant
import com.stratio.sparkta.serving.core.helpers.JarsHelper
import com.stratio.sparkta.serving.core.models.AggregationPoliciesModel
import com.typesafe.config.{ConfigFactory, Config, ConfigRenderOptions}
import org.apache.commons.lang.StringEscapeUtils
import org.json4s._
import org.json4s.native.Serialization

import scala.collection.JavaConversions._
import scala.sys.process._

class ClusterContextActor(policy: AggregationPoliciesModel,
                          streamingContextService: StreamingContextService,
                          executionMode: String,
                          clusterConfig: Config,
                          hdfsConfig: Config,
                          zookeeperConfig: Config,
                          sparktaHome: String) extends InstrumentedActor {

  implicit val json4sJacksonFormats = DefaultFormats
  implicit val formats = Serialization.formats(NoTypeHints)

  override def receive: PartialFunction[Any, Unit] = {
    case InitSparktaContext => doInitSparktaContext
  }

  def doInitSparktaContext: Unit = {
    log.debug("Init new cluster streamingContext with name " + policy.name)
    val jarsPlugins = JarsHelper.findJarsByPath(new File(sparktaHome, AppConstant.JarPluginsFolder), false)
    val activeJars = PolicyUtils.activeJars(policy, jarsPlugins)
    if (checkPolicyJars(activeJars)) {
      val main = getMainCommand
      if (main.isEmpty)
        log.warn("You must set the sparkSubmit path in configuration")
      else {
        val hadoopUserName =
          scala.util.Properties.envOrElse("HADOOP_USER_NAME", hdfsConfig.getString(AppConstant.HadoopUserName))
        val hadoopConfDir =
          Some(scala.util.Properties.envOrElse("HADOOP_CONF_DIR", hdfsConfig.getString(AppConstant.HadoopConfDir)))
        val hdfsUtils = new HdfsUtils(hadoopUserName, hadoopConfDir)
        val pluginsJarsFiles = PolicyUtils.activeJarFiles(activeJars.right.get, jarsPlugins)
        val pluginsJarsPath = s"/user/$hadoopUserName/${hdfsConfig.getString(AppConstant.PluginsPath)}/${policy.name}/"
        pluginsJarsFiles.foreach(file => hdfsUtils.write(file.getAbsolutePath, pluginsJarsPath, true))
        log.info("Jars plugins uploaded to HDFS")
        JarsHelper.findJarsByPath(
          new File(sparktaHome, AppConstant.ClusterExecutionJarFolder), false).headOption match {
          case Some(driverJar) => {
            val driverJarPath =
              s"/user/$hadoopUserName/${hdfsConfig.getString(AppConstant.ExecutionJarPath)}/${policy.name}/"
            val hdfsDriverFile =
              s" hdfs://${hdfsConfig.getString(AppConstant.HdfsMaster)}$driverJarPath${driverJar.getName}"
            hdfsUtils.write(driverJar.getAbsolutePath, driverJarPath, true)
            log.info("Jar driver uploaded to HDFS")
            if (executionMode == "mesos") {
              val cmd = s"${main} ${getGenericCommand} ${getMesosCommandString} " +
                s"${getSparkConfig} ${hdfsDriverFile} ${policy.name} $pluginsJarsPath ${getZookeeperCommand}"
              cmd.!!
              log.info("Spark submit to Mesos cluster sentence executed")
            }
            if (executionMode == "yarn") {
              val cmd = s"${main} ${getGenericCommand} ${getYarnCommandString} " +
                s"${getSparkConfig} ${hdfsDriverFile} ${policy.name} $pluginsJarsPath ${getZookeeperCommand}"
              cmd.!!
              log.info("Spark submit sentence to Yarn cluster executed")
            }
            if (executionMode == "standAlone") {
              val cmd = s"${main} ${getGenericCommand} ${getStandAloneCommandString} " +
                s"${getSparkConfig} ${hdfsDriverFile} ${policy.name} $pluginsJarsPath ${getZookeeperCommand}"
              cmd.!!
              log.info("Spark submit to StandAlone cluster sentence executed")
            }
          }
          case None => log.warn(s"The driver jar cannot be found in classpath")
        }
      }
    }
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
    var submit = scala.util.Properties.envOrElse("SPARK_HOME",
      SparktaHelper.getOptionStringConfig(AppConstant.SparkHome, clusterConfig) match {
        case Some(submitPath) => s"$submitPath"
        case None => ""
      })
    submit = if (submit.endsWith("/")) submit else submit + "/"
    s"${submit}bin/spark-submit --class com.stratio.sparkta.driver.SparktaClusterJob"
  }

  //scalastyle:off
  private def getGenericCommand: String = {
    val deploy = SparktaHelper.getOptionStringConfig(AppConstant.DeployMode, clusterConfig)
      .map(deployMode => s"--deploy-mode $deployMode")
    val executors = SparktaHelper.getOptionStringConfig(AppConstant.NumExecutors, clusterConfig)
      .map(numExecutors => s"--num-executors $numExecutors")
    val executorCores = SparktaHelper.getOptionStringConfig(AppConstant.ExecutorCores, clusterConfig)
      .map(cores => s"--executor-cores $cores")
    val totalExecutorCores = SparktaHelper.getOptionStringConfig(AppConstant.TotalExecutorCores, clusterConfig)
      .map(totalCores => s"--total-executor-cores $totalCores")
    val executorMemory = SparktaHelper.getOptionStringConfig(AppConstant.ExecutorMemory, clusterConfig)
      .map(executorsMemory => s"--executor-memory $executorsMemory")

    Seq(deploy, executors, executorCores, totalExecutorCores, executorMemory).flatten.mkString(" ")
  }

  //scalastyle:on

  private def getMesosCommandString: String = {
    val master = SparktaHelper.getOptionStringConfig(AppConstant.MesosMasterDispatchers, clusterConfig) match {
      case Some(masterDispatcher) => s"--master mesos://$masterDispatcher:7077"
      case None => ""
    }
    master
  }

  private def getYarnCommandString: String = {
    val master = SparktaHelper.getOptionStringConfig(AppConstant.YarnMaster, clusterConfig) match {
      case Some(master) => s"--master $master"
      case None => ""
    }
    val queue = SparktaHelper.getOptionStringConfig(AppConstant.YarnQueue, clusterConfig) match {
      case Some(queue) => s"--queue $queue"
      case None => ""
    }
    master + queue
  }

  private def getStandAloneCommandString: String = {
    val master = SparktaHelper.getOptionStringConfig(AppConstant.StandAloneMasterNode, clusterConfig) match {
      case Some(master) => s"--master spark://$master:7077"
      case None => ""
    }
    val supervise = SparktaHelper.getOptionStringConfig(AppConstant.StandAloneSupervise, clusterConfig) match {
      case Some(supervise) => if (supervise == "true") s"--supervise" else ""
      case None => ""
    }
    master + supervise
  }

  private def getZookeeperCommand: String = {
    val config = zookeeperConfig.atKey("zk").root.render(ConfigRenderOptions.concise)
    log.info("ZOOOOOOO:   " + config)
      "\"" + StringEscapeUtils.escapeJavaScript(config) + "\""
  }

  private def getSparkConfig: String = {
    val properties = clusterConfig.entrySet()
    properties.flatMap(prop => {
      if (prop.getKey.startsWith("spark.")) Some(s"--conf ${prop.getKey}=${clusterConfig.getString(prop.getKey)}")
      else None
    }).mkString(" ")
  }
}
