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

package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import com.stratio.sparta.driver.SpartaClusterJob
import com.stratio.sparta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparta.serving.api.models.SubmissionResponse
import com.stratio.sparta.serving.api.utils.SparkSubmitUtils
import com.stratio.sparta.serving.core.actor.PolicyStatusActor
import com.stratio.sparta.serving.core.actor.PolicyStatusActor.{AddListener, FindById, ResponseStatus, Update}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.{HdfsUtils, SchedulerUtils}
import com.typesafe.config.{Config, ConfigRenderOptions}
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.spark.launcher.{SparkAppHandle, SparkLauncher}
import org.json4s.jackson.Serialization._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class ClusterLauncherActor(statusActor: ActorRef) extends Actor
  with SpartaSerializer with SchedulerUtils with SparkSubmitUtils {

  val SpartaDriverClass = SpartaClusterJob.getClass.getCanonicalName.replace("$", "")
  val DetailConfig = SpartaConfig.getDetailConfig.getOrElse {
    val message = "Impossible to extract Detail Configuration"
    log.error(message)
    throw new RuntimeException(message)
  }
  val ZookeeperConfig = SpartaConfig.getZookeeperConfig.getOrElse {
    val message = "Impossible to extract Zookeeper Configuration"
    log.error(message)
    throw new RuntimeException(message)
  }

  implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)

  val submissionHandlers = scala.collection.mutable.Map.empty[String, SparkAppHandle]

  override def receive: PartialFunction[Any, Unit] = {
    case Start(policy: PolicyModel) => doInitSpartaContext(policy)
    case _ => log.info("Unrecognized message in Cluster Launcher Actor")
  }

  def doInitSpartaContext(policy: PolicyModel): Unit = {
    Try {
      val clusterConfig = SpartaConfig.getClusterConfig(policy.executionMode).get
      val driverLocation = Try(policy.driverLocation.getOrElse(DetailConfig.getString(DriverLocation)))
        .getOrElse(DefaultDriverLocation)
      val driverLocationConfig = SpartaConfig.initOptionalConfig(driverLocation, SpartaConfig.mainConfig)

      validateSparkHome(clusterConfig)

      log.info("Init new cluster streamingContext with name " + policy.name)

      val driverPath = driverSubmit(policy, DetailConfig, SpartaConfig.getHdfsConfig)
      val master = clusterConfig.getString(Master).trim
      val localPluginsFiles = pluginsSubmit(policy, DetailConfig, LocalPluginsLocation)
      val allPluginsFiles = pluginsSubmit(policy, DetailConfig, ProvidedPluginsLocation)
      val driverParams = Seq(policy.id.get.trim,
        zkConfigEncoded,
        detailConfigEncoded,
        pluginsEncoded(localPluginsFiles),
        driverLocationConfigEncoded(driverLocation, driverLocationConfig))
      val sparkArguments = submitArgsFromProps(clusterConfig) ++ submitArgsFromPolicy(policy.sparkSubmitArguments)

      log.info(s"Launching Sparta Job with options ... \n\t" +
        s"Policy name: ${policy.name}\n\t" +
        s"Main: $SpartaDriverClass\n\t" +
        s"Driver path: $driverPath\n\t" +
        s"Master: $master\n\t" +
        s"Spark arguments: ${sparkArguments.mkString(",")}\n\t" +
        s"Driver params: $driverParams\n\t" +
        s"Plugins files: ${allPluginsFiles.mkString(",")}")

      (launch(policy, SpartaDriverClass, driverPath, master, sparkArguments, driverParams, allPluginsFiles,
        clusterConfig), clusterConfig)
    } match {
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        setPolicyStatus(policy.id.get, Failed, None, None, Some(exception.getLocalizedMessage))
      case Success((sparkHandler, clusterConfig)) =>
        val information = "Sparta Cluster Job launched correctly"
        log.info(information)
        setPolicyStatus(policy.id.get,
          Launched,
          Option(sparkHandler.getAppId),
          Option(sparkHandler.getState.name()),
          Some(information))
        contextListener(policy, clusterConfig)
        scheduleOneTask(AwaitPolicyChangeStatus, DefaultAwaitPolicyChangeStatus)(checkPolicyStatus(policy))
        submissionHandlers.put(policy.id.get, sparkHandler)
    }
  }

  def launch(policy: PolicyModel,
             main: String,
             driverFile: String,
             master: String,
             sparkArguments: Map[String, String],
             driverArguments: Seq[String],
             pluginsFiles: Seq[String],
             clusterConfig: Config): SparkAppHandle = {
    val sparkLauncher = new SparkLauncher()
      .setSparkHome(sparkHome(clusterConfig))
      .setAppResource(driverFile)
      .setMainClass(main)
      .setMaster(master)

    // Plugins files
    pluginsFiles.foreach(file => sparkLauncher.addJar(file))

    //Spark arguments
    sparkArguments.filter(_._2.nonEmpty).foreach { case (k: String, v: String) => sparkLauncher.addSparkArg(k, v) }
    sparkArguments.filter(_._2.isEmpty).foreach { case (k: String, v: String) => sparkLauncher.addSparkArg(k) }
    if (!sparkArguments.contains(SubmitSupervise) && isSupervised(policy, DetailConfig, clusterConfig))
      sparkLauncher.addSparkArg(SubmitSupervise)

    // Kerberos Options
    val principalNameOption = HdfsUtils.getPrincipalName
    val keyTabPathOption = HdfsUtils.getKeyTabPath
    (principalNameOption, keyTabPathOption) match {
      case (Some(principalName), Some(keyTabPath)) =>
        log.info(s"Launching Spark Submit with Kerberos security, adding principal and keyTab arguments... \n\t" +
          s"Principal: $principalName \n\tKeyTab: $keyTabPath")
        sparkLauncher.addSparkArg(SubmitPrincipal, principalName)
        sparkLauncher.addSparkArg(SubmitKeyTab, keyTabPath)
      case _ =>
        log.info("Launching Spark Submit without Kerberos security")
    }

    // Spark properties
    log.info("Adding Spark options to Sparta Job ... ")
    sparkConf(clusterConfig).foreach { case (key: String, value: String) =>
      val valueParsed = if (key == "spark.app.name") s"$value-${policy.name}" else value
      log.info(s"\t$key = $valueParsed")
      sparkLauncher.setConf(key.trim, valueParsed.trim)
    }
    sparkLauncher.setConf(SparkGracefullyStopProperty, gracefulStop(policy, DetailConfig).toString)

    // Driver (Sparta) params
    driverArguments.foreach(sparkLauncher.addAppArgs(_))

    // Launch SparkApp
    sparkLauncher.startApplication(addSparkListener(policy))
  }

  /** Spark Launcher functions **/

  def addSparkListener(policy: PolicyModel): SparkAppHandle.Listener = {
    new SparkAppHandle.Listener() {
      override def stateChanged(handle: SparkAppHandle): Unit = {
        log.info(s"Submission state changed to ... ${handle.getState.name()}")
        setPolicyStatus(policy.id.get, NotDefined, None, Try(handle.getState.name()).toOption)
      }

      override def infoChanged(handle: SparkAppHandle): Unit = {
        log.info(s"Submission info changed with status ... ${handle.getState.name()}")
      }
    }
  }

  /** Arguments functions **/

  def render(config: Config, key: String): String = config.atKey(key).root.render(ConfigRenderOptions.concise)

  def encode(value: String): String = BaseEncoding.base64().encode(value.getBytes)

  def zkConfigEncoded: String = encode(render(ZookeeperConfig, "zookeeper"))

  def detailConfigEncoded: String = encode(render(DetailConfig, "config"))

  def pluginsEncoded(plugins: Seq[String]): String = encode((Seq(" ") ++ plugins).mkString(","))

  def driverLocationConfigEncoded(executionMode: String, clusterConfig: Option[Config]): String =
    clusterConfig match {
      case Some(config) => encode(render(config, executionMode))
      case None => encode(" ")
    }

  /** Policy Status Functions **/

  def setPolicyStatus(policyId: String,
                      status: PolicyStatusEnum.Value,
                      submissionId: Option[String] = None,
                      submissionStatus: Option[String] = None,
                      information: Option[String] = None): Unit =
    statusActor ! Update(PolicyStatusModel(policyId, status, submissionId, submissionStatus, information))

  def checkPolicyStatus(policy: PolicyModel): Unit = {
    for {
      statusResponse <- (statusActor ? FindById(policy.id.get)).mapTo[ResponseStatus]
    } yield statusResponse match {
      case PolicyStatusActor.ResponseStatus(Success(policyStatus)) =>
        if (policyStatus.status == Launched || policyStatus.status == Starting || policyStatus.status == Stopping) {
          val information = s"The policy-checker detects that the policy ${policy.name} was not started/stopped " +
            s"correctly, setting the failed status..."
          log.error(information)
          setPolicyStatus(policy.id.get, Failed, None, None, Some(information))
        } else {
          val information = s"The policy-checker detects that the policy ${policy.name} was started/stopped correctly"
          log.info(information)
          setPolicyStatus(policy.id.get, NotDefined, None, None, Some(information))
        }
      case PolicyStatusActor.ResponseStatus(Failure(exception)) =>
        log.error(s"Error when extract policy status in scheduler task.", exception)
    }
  }

  //scalastyle:off
  def contextListener(policy: PolicyModel, clusterConfig: Config): Unit = {
    log.info(s"Listener added to ${policy.name} with id: ${policy.id.get}")
    statusActor ! AddListener(policy.id.get, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
      synchronized {
        if (policyStatus.status != Launched && policyStatus.status != Starting && policyStatus.status != Started) {
          log.info("Stopping message received from Zookeeper")
          try {
            policyStatus.submissionId match {
              case Some(submissionId) =>
                try {
                  if (isCluster(policy, clusterConfig)) {
                    val url = killUrl(clusterConfig)
                    log.info(s"Killing submission ($submissionId) with Spark Submissions API in url: $url")
                    val post = if (submissionId.contains("driver")) {
                      val frameworkId = submissionId.substring(0, submissionId.indexOf("driver") - 1)
                      val sparkApplicationId = submissionId.substring(submissionId.indexOf("driver"))
                      log.info(s"The extracted Framework id is: $frameworkId")
                      log.info(s"The extracted Spark application id is: $sparkApplicationId")
                      new HttpPost(s"$url/$sparkApplicationId")
                    } else new HttpPost(s"$url/$submissionId")
                    val postResponse = new DefaultHttpClient().execute(post)
                    Try {
                      read[SubmissionResponse](Source.fromInputStream(postResponse.getEntity.getContent).mkString)
                    } match {
                      case Success(submissionResponse) =>
                        log.info(s"Kill submission response status: ${submissionResponse.success}")
                      case Failure(e) =>
                        log.error("Impossible to parse submission killing response", e)
                    }
                  } else {
                    log.info("Stopping submission policy with handler")
                    submissionHandlers.get(policy.id.get).get.stop()
                  }
                } finally {
                  val message = s"The policy ${policy.name} was stopped correctly"
                  log.info(message)
                  setPolicyStatus(policy.id.get, Stopped, None, None, Some(message))
                }
              case None =>
                log.info(s"The Sparta System don't have submission id associated to policy ${policy.name}")
            }
          } finally {
            submissionHandlers.get(policy.id.get).foreach(handle => {
              log.info("Killing submission policy with handler")
              handle.kill()
              submissionHandlers.remove(policy.id.get)
            })
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Node cache to contextListener closed correctly")
              case Failure(e) =>
                log.error(s"The node Cache to contextListener in Zookeeper is not closed correctly", e)
            }
          }
        }
      }
    })
  }

  //scalastyle:on
}