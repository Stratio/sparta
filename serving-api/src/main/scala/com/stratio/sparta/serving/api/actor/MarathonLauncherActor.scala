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

import akka.actor.Actor
import com.stratio.sparta.serving.api.marathon.MarathonApp
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.actor.StatusActor.ResponseStatus
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PhaseEnum, PolicyErrorModel, PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.models.submit.SubmitRequest
import com.stratio.sparta.serving.core.utils._
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class MarathonLauncherActor(val curatorFramework: CuratorFramework) extends Actor
  with LauncherUtils with SchedulerUtils with SparkSubmitUtils with ClusterListenerUtils with ArgumentsUtils
  with ClusterCheckerUtils with PolicyStatusUtils with ExecutionUtils {

  override def receive: PartialFunction[Any, Unit] = {
    case Start(policy: PolicyModel) => initializeSubmitRequest(policy)
    case ResponseStatus(status) => loggingResponsePolicyStatus(status)
    case _ => log.info("Unrecognized message in Marathon Launcher Actor")
  }

  def initializeSubmitRequest(policy: PolicyModel): Unit = {
    Try {
      log.info(s"Initializing options for submit Marathon application associated to policy: ${policy.name}")
      val zookeeperConfig = getZookeeperConfig
      val clusterConfig = SpartaConfig.getClusterConfig(Option(ConfigMesos)).get
      val master = clusterConfig.getString(Master).trim
      val driverFile = extractMarathonDriverSubmit(policy, DetailConfig, SpartaConfig.getHdfsConfig)
      val pluginsFiles = pluginsJars(policy)
      val driverArguments =
        extractDriverArguments(policy, driverFile, clusterConfig, zookeeperConfig, ConfigMesos, pluginsFiles)
      val (sparkSubmitArguments, sparkConfigurations) =
        extractSubmitArgumentsAndSparkConf(policy, clusterConfig, pluginsFiles)
      val submitRequest = SubmitRequest(policy.id.get, SpartaDriverClass, driverFile, master, sparkSubmitArguments,
        sparkConfigurations, driverArguments, ConfigMesos, killUrl(clusterConfig))
      val detailExecMode = getDetailExecutionMode(policy, clusterConfig)

      createRequest(submitRequest).getOrElse(throw new Exception("Impossible to create submit request in persistence"))

      (new MarathonApp(context, policy, submitRequest, curatorFramework), detailExecMode)
    } match {
      case Failure(exception) =>
        val information = s"Error when initializing Sparta Marathon App options"
        log.error(information, exception)
        updateStatus(PolicyStatusModel(id = policy.id.get, status = Failed, statusInfo = Option(information),
          lastError = Option(PolicyErrorModel(information, PhaseEnum.Execution, exception.toString))
        ))
      case Success((marathonApp, detailExecMode)) =>
        val information = "Sparta Marathon App configurations initialized correctly"
        log.info(information)
        updateStatus(PolicyStatusModel(id = policy.id.get, status = NotStarted,
          statusInfo = Option(information), lastExecutionMode = Option(detailExecMode)))

        marathonApp.launch(detailExecMode)
        scheduleOneTask(AwaitPolicyChangeStatus, DefaultAwaitPolicyChangeStatus)(checkPolicyStatus(policy))
    }
  }
}