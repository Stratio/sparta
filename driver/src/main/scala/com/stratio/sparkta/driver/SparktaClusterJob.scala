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

package com.stratio.sparkta.driver

import java.io.File
import java.net.URI

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.SparktaJob._
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.driver.util.{HdfsUtils, PolicyUtils}
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.stratio.sparkta.serving.core.actor.FragmentActor
import com.stratio.sparkta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparkta.serving.core.helpers.{JarsHelper, PolicyHelper}
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel, StreamingContextStatusEnum}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparkta.serving.core.policy.status.{PolicyStatusActor, PolicyStatusEnum}
import com.stratio.sparkta.serving.core.{CuratorFactoryHolder, SparktaConfig}
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringEscapeUtils
import org.apache.curator.framework.CuratorFramework
import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object SparktaClusterJob {

  implicit val timeout: Timeout = Timeout(3.seconds)
  final val PolicyIdIndex = 0
  final val PluginsPathIndex = 1
  final val ClassPathIndex = 2
  final val ZookeperConfigurationIndex = 3
  final val DetailConfigurationIndex = 4

  def main(args: Array[String]): Unit = {
    assert(args.length == 5, s"Invalid number of params: ${args.length}, args: $args")
    Try {
      initSparktaConfig(StringEscapeUtils.unescapeJavaScript(args(DetailConfigurationIndex)),
        StringEscapeUtils.unescapeJavaScript(args(ZookeperConfigurationIndex)))

      val policyId = args(PolicyIdIndex)
      val curatorFramework = CuratorFactoryHolder.getInstance()
      val policyZk = getPolicyFromZookeeper(policyId, curatorFramework)
      implicit val system = ActorSystem(policyId)
      val fragmentActor = system.actorOf(Props(new FragmentActor(curatorFramework)), AkkaConstant.FragmentActor)
      val policy = PolicyHelper.parseFragments(
        PolicyHelper.fillFragments(policyZk, fragmentActor, timeout))
      val pluginsClasspathFiles = addPluginsAndClasspath(args(PluginsPathIndex), args(ClassPathIndex))

      implicit val json4sJacksonFormats: Formats =
        DefaultFormats +
          new EnumNameSerializer(StreamingContextStatusEnum) +
          new JsoneyStringSerializer() +
          new EnumNameSerializer(PolicyStatusEnum)

      val policyStatusActor = system.actorOf(Props(new PolicyStatusActor(curatorFramework)),
        AkkaConstant.PolicyStatusActor)

      Try {
        policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Starting))

        val streamingContextService = new StreamingContextService(Some(policyStatusActor))
        val ssc = streamingContextService.clusterStreamingContext(
          policy, pluginsClasspathFiles, Map("spark.app.name" -> s"${policy.name}")).get

        ssc.start
      } match {
        case Success(_) => {
          policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Started))
          log.info(s"Starting Streaming Context for policy $policyId")
        }
        case Failure(exception) => {
          log.error(exception.getLocalizedMessage, exception)
          policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Failed))
        }
      }
    } match {
      case Success(_) => log.info("Policy started")
      case Failure(exception) => log.error(exception.getLocalizedMessage, exception)
    }
  }

  def initSparktaConfig(detailConfig: String, zKConfig: String): Unit = {
    val configStr = s"${detailConfig.stripPrefix("{").stripSuffix("}")}\n${zKConfig.stripPrefix("{").stripSuffix("}")}"
    log.info(s"Parsed config: sparkta { $configStr }")
    SparktaConfig.initMainConfig(Option(ConfigFactory.parseString(s"sparkta{$configStr}")))
  }

  def addPluginsAndClasspath(pluginsPath: String, classPath: String): Seq[URI] = {
    val hadoopUserName = scala.util.Properties.envOrElse("HADOOP_USER_NAME", AppConstant.DefaultHadoopUserName)
    val hdfsUgi = HdfsUtils.ugi(hadoopUserName)
    val hadoopConfDir = scala.util.Properties.envOrNone("HADOOP_CONF_DIR")
    val hdfsConf = HdfsUtils.hdfsConfiguration(hadoopConfDir)
    val hdfsUtils = new HdfsUtils(hdfsUgi, hdfsConf)
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