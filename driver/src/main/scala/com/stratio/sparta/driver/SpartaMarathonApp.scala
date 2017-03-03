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

package com.stratio.sparta.driver

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import com.stratio.sparta.driver.exception.DriverException
import com.stratio.sparta.serving.core.actor.ExecutionActor.FindById
import com.stratio.sparta.serving.core.actor.LauncherActor.StartWithRequest
import com.stratio.sparta.serving.core.actor.StatusActor.Update
import com.stratio.sparta.serving.core.actor.{ClusterLauncherActor, ExecutionActor, FragmentActor, StatusActor}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.helpers.FragmentsHelper
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PhaseEnum, PolicyErrorModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.models.submit.SubmitRequest
import com.stratio.sparta.serving.core.utils.{PluginsFilesUtils, PolicyUtils}
import com.typesafe.config.ConfigFactory
import org.apache.curator.framework.CuratorFramework
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success, Try}

object SpartaMarathonApp extends PluginsFilesUtils {

  val NumberOfArguments = 2
  val PolicyIdIndex = 0
  val ZookeeperConfigurationIndex = 1

  def main(args: Array[String]): Unit = {
    assert(args.length == NumberOfArguments,
      s"Invalid number of arguments: ${args.length}, args: $args, expected: $NumberOfArguments")
    Try {
      val policyId = args(PolicyIdIndex)
      val zookeeperConf = new String(BaseEncoding.base64().decode(args(ZookeeperConfigurationIndex)))
      initSpartaConfig(zookeeperConf)
      val curatorInstance = CuratorFactoryHolder.getInstance()
      implicit val system = ActorSystem(policyId, SpartaConfig.daemonicAkkaConfig)
      val statusActor = system.actorOf(Props(new StatusActor(curatorInstance)), AkkaConstant.statusActor)
      Try {
        implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)
        val policyUtils = new PolicyUtils {
          override val curatorFramework: CuratorFramework = curatorInstance
        }
        val fragmentActor = system.actorOf(Props(new FragmentActor(curatorInstance)), AkkaConstant.FragmentActor)
        val policy = FragmentsHelper.getPolicyWithFragments(policyUtils.getPolicyById(policyId), fragmentActor)
        val executionActor = system.actorOf(Props(new ExecutionActor(curatorInstance)), AkkaConstant.ExecutionActor)

        for {
          response <- (executionActor ? FindById(policyId)).mapTo[Try[SubmitRequest]]
        } yield response match {
          case Success(submitRequest) =>
            val clusterLauncherActor = system.actorOf(Props(new ClusterLauncherActor(statusActor, executionActor)),
              AkkaConstant.ClusterLauncherActor)
            clusterLauncherActor ! StartWithRequest(policy, submitRequest)
          case Failure(exception) => throw exception
        }
      } match {
        case Success(_) =>
          val information = s"Submitted job correctly in Marathon App"
          log.info(information)
          statusActor ! Update(PolicyStatusModel(id = policyId, status = NotDefined, statusInfo = Some(information)))
        case Failure(exception) =>
          val information = s"Error submitting job in Marathon App"
          log.error(information)
          statusActor ! Update(PolicyStatusModel(id = policyId, status = Failed, statusInfo = Option(information),
            lastError = Option(PolicyErrorModel(information, PhaseEnum.Execution, exception.toString))))
          throw DriverException(information, exception)
      }
    } match {
      case Success(_) =>
        log.info("Finished correctly Marathon App")
      case Failure(driverException: DriverException) =>
        log.error(driverException.msg, driverException.getCause)
        throw driverException
      case Failure(exception) =>
        log.error(s"Error initiating Marathon App environment: ${exception.getLocalizedMessage}", exception)
        throw exception
    }
  }

  def initSpartaConfig(zKConfig: String): Unit = {
    val configStr = s"\n${zKConfig.stripPrefix("{").stripSuffix("}")}"
    log.info(s"Parsed config: sparta { $configStr }")
    SpartaConfig.initMainWithFallbackConfig(ConfigFactory.parseString(s"sparta{$configStr}"))
  }
}
