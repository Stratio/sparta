/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.driver

import java.io.File
import java.net.URI
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
import org.apache.curator.framework.CuratorFramework

import com.stratio.sparkta.driver.SparktaJob._
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.driver.util.HdfsUtils
import com.stratio.sparkta.driver.util.PolicyUtils
import com.stratio.sparkta.serving.core.CuratorFactoryHolder
import com.stratio.sparkta.serving.core.SparktaConfig
import com.stratio.sparkta.serving.core.actor.FragmentActor
import com.stratio.sparkta.serving.core.constants.AkkaConstant
import com.stratio.sparkta.serving.core.constants.AppConstant
import com.stratio.sparkta.serving.core.dao.ConfigDAO
import com.stratio.sparkta.serving.core.dao.ErrorDAO
import com.stratio.sparkta.serving.core.helpers.JarsHelper
import com.stratio.sparkta.serving.core.helpers.PolicyHelper
import com.stratio.sparkta.serving.core.models.AggregationPoliciesModel
import com.stratio.sparkta.serving.core.models.PolicyStatusModel
import com.stratio.sparkta.serving.core.models.SparktaSerializer
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum

object SparktaClusterJob extends SparktaSerializer {

  implicit val timeout: Timeout = Timeout(3.seconds)
  final val PolicyIdIndex = 0
  final val PluginsPathIndex = 1
  final val ClassPathIndex = 2
  final val ZookeperConfigurationIndex = 3
  final val DetailConfigurationIndex = 4

  def main(args: Array[String]): Unit = {
    assert(args.length == 5, s"Invalid number of params: ${args.length}, args: $args")
    Try {
      val detailConfiguration = new String(BaseEncoding.base64().decode(args(DetailConfigurationIndex)))
      val zookeperConfiguration = new String(BaseEncoding.base64().decode(args(ZookeperConfigurationIndex)))
      initSparktaConfig(detailConfiguration, zookeperConfiguration)

      val policyId = args(PolicyIdIndex)
      val curatorFramework = CuratorFactoryHolder.getInstance()
      val policyZk = getPolicyFromZookeeper(policyId, curatorFramework)
      implicit val system = ActorSystem(policyId)
      val fragmentActor = system.actorOf(Props(new FragmentActor(curatorFramework)), AkkaConstant.FragmentActor)
      val policy = PolicyHelper.parseFragments(
        PolicyHelper.fillFragments(policyZk, fragmentActor, timeout))
      val pluginsClasspathFiles = Try(addPluginsAndClasspath(args(PluginsPathIndex), args(ClassPathIndex))) match {
        case Success(files) => files
        case Failure(ex) => throw new RuntimeException("Someting goes wrong when trying to retrieve jars from HDFS", ex)
      }

      val policyStatusActor = system.actorOf(Props(new PolicyStatusActor(curatorFramework)),
        AkkaConstant.PolicyStatusActor)

      Try {
        policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Starting))
        Try(ErrorDAO().dao.delete(policy.id.get))

        val streamingContextService = new StreamingContextService(Some(policyStatusActor))
        val ssc = streamingContextService.clusterStreamingContext(
          policy, pluginsClasspathFiles, Map("spark.app.name" -> s"${policy.name}")).get

        ssc.start
        policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Started))
        log.info(s"Starting Streaming Context for policy $policyId")
        ssc.awaitTermination()
      } match {
        case Success(_) => {
          policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Stopped))
          log.info(s"Stopped Streaming Context for policy $policyId")
        }
        case Failure(exception) => {
          log.error(exception.getLocalizedMessage, exception)
          policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Failed))
        }
      }
    } match {
      case Success(_) => log.info("Streaming context is running")
      case Failure(exception) => log.error(exception.getLocalizedMessage, exception)
    }
  }

  def initSparktaConfig(detailConfig: String, zKConfig: String): Unit = {
    val configStr = s"${detailConfig.stripPrefix("{").stripSuffix("}")}\n${zKConfig.stripPrefix("{").stripSuffix("}")}"
    log.info(s"Parsed config: sparkta { $configStr }")
    SparktaConfig.initMainConfig(Option(ConfigFactory.parseString(s"sparkta{$configStr}")))
    SparktaConfig.initDAOs
  }

  def addPluginsAndClasspath(pluginsPath: String, classPath: String): Seq[URI] = {
    val hdfsJsonConfig = ConfigDAO().dao.get(AppConstant.HdfsID).get
    val config = ConfigFactory.parseString(hdfsJsonConfig).getConfig(AppConstant.HdfsID)
    val hdfsUtils = HdfsUtils(config)
    val pluginFiles = addHdfsFiles(hdfsUtils, pluginsPath)
    val classPathFiles = addHdfsFiles(hdfsUtils, classPath)

    pluginFiles ++ classPathFiles
  }

  def addHdfsFiles(hdfsUtils: HdfsUtils, hdfsPath: String): Array[URI] = {
    hdfsUtils.getFiles(hdfsPath).map(file => {
      val fileName = s"$hdfsPath${file.getPath.getName}"
      val tempFile = File.createTempFile(file.getPath.getName, "")

      FileUtils.copyInputStreamToFile(hdfsUtils.getFile(fileName), tempFile)
      JarsHelper.addToClasspath(tempFile)
      file.getPath.toUri
    })
  }

  def getPolicyFromZookeeper(policyId: String, curatorFramework: CuratorFramework): AggregationPoliciesModel = {
    Try {
      PolicyUtils.parseJson(new Predef.String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/${policyId}")))
    } match {
      case Success(policy) => policy
      case Failure(e) => log.error(s"Cannot load policy $policyId", e); throw e
    }
  }
}
