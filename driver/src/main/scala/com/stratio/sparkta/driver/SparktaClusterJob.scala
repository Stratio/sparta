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
import com.stratio.sparkta.serving.core.actor.FragmentActor
import com.stratio.sparkta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparkta.serving.core.helpers.{JarsHelper, PolicyHelper}
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel, SparktaSerializer}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparkta.serving.core.policy.status.{PolicyStatusActor, PolicyStatusEnum}
import com.stratio.sparkta.serving.core.{CuratorFactoryHolder, SparktaConfig}
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
import org.apache.curator.framework.CuratorFramework

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object SparktaClusterJob extends SparktaSerializer {

  implicit val timeout: Timeout = Timeout(3.seconds)

  def main(args: Array[String]): Unit = {
    if (checkArgs(args)) {
      Try {
        initSparktaConfig(args(4), args(3))
        val policyId = args(0)
        val curatorFramework = CuratorFactoryHolder.getInstance()
        val policyZk = getPolicyFromZookeeper(policyId, curatorFramework)
        implicit val system = ActorSystem(policyId)
        val fragmentActor = system.actorOf(Props(new FragmentActor(curatorFramework)), AkkaConstant.FragmentActor)
        val policy = PolicyHelper.parseFragments(
          PolicyHelper.fillFragments(policyZk, fragmentActor, timeout))
        val hadoopUserName = scala.util.Properties.envOrElse("HADOOP_USER_NAME", AppConstant.DefaultHadoopUserName)
        val hdfsUgi = HdfsUtils.ugi(hadoopUserName)
        val hadoopConfDir = scala.util.Properties.envOrNone("HADOOP_CONF_DIR")
        val hdfsConf = HdfsUtils.hdfsConfiguration(hadoopConfDir)
        val hdfsUtils = new HdfsUtils(hdfsUgi, hdfsConf)
        val pluginFiles = addHdfsFiles(hdfsUtils, args(1))
        val classPathFiles = addHdfsFiles(hdfsUtils, args(2))
        val policyStatusActor = system.actorOf(Props[PolicyStatusActor], AkkaConstant.PolicyStatusActor)
        Try {
          policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Starting))
          val streamingContextService = new StreamingContextService(Some(policyStatusActor))
          val ssc = streamingContextService.clusterStreamingContext(
            policy, pluginFiles ++ classPathFiles, Map("spark.app.name" -> s"${policy.name}")).get
          ssc.start
        } match {
          case Success(_) => {
            policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Started))
            log.info(s"Starting Streaming Context for policy ${policyId}")
          }
          case Failure(exception) => {
            log.error(exception.getLocalizedMessage, exception)
            policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Failed))
          }
        }
      } match {
        case Success(_) => {
          log.info("Policy started")
        }
        case Failure(exception) => {
          log.error("Creating classpath and StatusActor")
          log.error(exception.getLocalizedMessage, exception)
        }
      }
    } else {
      log.error("Invalid arguments")
      System.exit(-1)
    }
  }

  def initSparktaConfig(detailConfig: String, zKConfig: String): Unit = {
    val configStr = s"$detailConfig,$zKConfig".stripPrefix(",").stripSuffix(",")
    SparktaConfig.initMainConfig(Some(ConfigFactory.parseString(s"{$configStr}").atKey("sparkta")))
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

  def checkArgs(args: Array[String]): Boolean = args.length == 5
}
