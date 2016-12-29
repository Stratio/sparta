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

package com.stratio.sparta.serving.core.utils

import java.io.File
import java.util.UUID

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import akka.util.Timeout
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.{ActorsConstant, AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.helpers.FragmentsHelper._
import com.stratio.sparta.serving.core.models.policy.fragment.{FragmentElementModel, FragmentType}
import com.stratio.sparta.serving.core.models.policy.PolicyModel
import com.stratio.sparta.serving.core.models.SpartaSerializer
import org.apache.commons.io.FileUtils
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.concurrent.duration._
import scala.util._

trait PolicyUtils extends SpartaSerializer with SLF4JLogging {

  val fragmentActor: Option[ActorRef] = None

  implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)

  /** METHODS TO MANAGE POLICIES IN ZOOKEEPER **/

  def existsPath: Boolean = CuratorFactoryHolder.existsPath(AppConstant.PoliciesBasePath)

  def savePolicyInZk(policy: PolicyModel, curatorFramework: CuratorFramework): Unit =
    if (existsByNameId(policy.name, policy.id, curatorFramework).isDefined) {
      log.info(s"Policy ${policy.name} already in zookeeper. Updating it...")
      updatePolicy(policy, curatorFramework)
    } else writePolicy(policy, curatorFramework)

  def deletePolicy(policy: PolicyModel, curatorFramework: CuratorFramework): Unit =
    curatorFramework.delete().forPath(s"${AppConstant.PoliciesBasePath}/${policy.id.get}")

  def writePolicy(policy: PolicyModel, curatorFramework: CuratorFramework): Unit = {
    val policyParsed = policyWithFragments(policy)

    curatorFramework.create().creatingParentsIfNeeded().forPath(
      s"${AppConstant.PoliciesBasePath}/${policyParsed.id.get}", write(policyParsed).getBytes)
  }

  def updatePolicy(policy: PolicyModel, curatorFramework: CuratorFramework): Unit = {
    val policyParsed = policyWithFragments(policy)

    curatorFramework.setData().forPath(
      s"${AppConstant.PoliciesBasePath}/${policyParsed.id.get}", write(policyParsed).getBytes)
  }

  def populatePolicy(policy: PolicyModel, curatorFramework: CuratorFramework): PolicyModel = {
    val policyInZk = read[PolicyModel](new Predef.String(curatorFramework.getData.forPath(
      s"${AppConstant.PoliciesBasePath}/${policy.id.get}")))

    policyWithFragments(policyInZk)
  }

  def existsByNameId(name: String,
                     id: Option[String] = None,
                     curatorFramework: CuratorFramework
                    ): Option[PolicyModel] =
    Try {
      if (existsPath) {
        getPolicies(curatorFramework).find(policy =>
          if (id.isDefined && policy.id.isDefined) policy.id.get == id.get else policy.name == name.toLowerCase
        )
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  def getPolicies(curatorFramework: CuratorFramework): List[PolicyModel] = {
    val children = curatorFramework.getChildren.forPath(AppConstant.PoliciesBasePath)

    JavaConversions.asScalaBuffer(children).toList.map(id => byId(id, curatorFramework))
  }

  def byId(id: String, curatorFramework: CuratorFramework): PolicyModel = {
    val policy = read[PolicyModel](
      new Predef.String(curatorFramework.getData.forPath(s"${AppConstant.PoliciesBasePath}/$id")))

    policyWithFragments(policy)
  }

  /** METHODS TO CALCULATE THE CORRECT ID IN POLICIES **/

  def policyWithId(policy: PolicyModel): PolicyModel = {
    val policyF = policyWithFragments(policy)
    (policyF.id match {
      case None => populatePolicyWithRandomUUID(policyF)
      case Some(_) => policyF
    }).copy(name = policyF.name.toLowerCase, version = Some(ActorsConstant.UnitVersion))
  }

  def populatePolicyWithRandomUUID(policy: PolicyModel): PolicyModel =
    policy.copy(id = Some(UUID.randomUUID.toString))

  def setVersion(lastPolicy: PolicyModel, newPolicy: PolicyModel): Option[Int] =
    if (lastPolicy.cubes != newPolicy.cubes) {
      lastPolicy.version match {
        case Some(version) => Some(version + ActorsConstant.UnitVersion)
        case None => Some(ActorsConstant.UnitVersion)
      }
    } else lastPolicy.version

  def policyWithFragments(policy: PolicyModel)(implicit timeout: Timeout): PolicyModel =
    fragmentActor.fold(policy) { actorRef => {
      (populateFragmentFromPolicy(policy, FragmentType.input) ++
        populateFragmentFromPolicy(policy, FragmentType.output)
        ).foreach(fragment => actorRef ! FragmentActor.Create(fragment))
      getPolicyWithFragments(policy, actorRef)
    }
    }

  def jarsFromPolicy(apConfig: PolicyModel): Seq[File] =
    apConfig.userPluginsJars.filter(!_.jarPath.isEmpty).map(_.jarPath).distinct.map(filePath => new File(filePath))

  def loggingResponseFragment(response: Try[FragmentElementModel]): Unit =
    response match {
      case Success(fragment) =>
        log.info(s"Fragment created correctly: \n\tId: ${fragment.id}\n\tName: ${fragment.name}")
      case Failure(e) =>
        log.error(s"Fragment creation failure. Error: ${e.getLocalizedMessage}", e)
    }

  /** CHECKPOINT OPTIONS **/

  /* PUBLIC METHODS */

  def isLocalMode: Boolean =
    SpartaConfig.getDetailConfig match {
      case Some(detailConfig) => detailConfig.getString(AppConstant.ExecutionMode).equalsIgnoreCase("local")
      case None => true
    }

  def deleteFromLocal(policy: PolicyModel): Unit = {
    val checkpointDirectory = checkpointPath(policy)
    log.info(s"Deleting checkpoint directory: $checkpointDirectory")
    FileUtils.deleteDirectory(new File(checkpointDirectory))
  }

  def deleteFromHDFS(policy: PolicyModel): Unit = {
    val checkpointDirectory = checkpointPath(policy)
    log.info(s"Deleting checkpoint directory: $checkpointDirectory")
    HdfsUtils().delete(checkpointDirectory)
  }

  def isHadoopEnvironmentDefined: Boolean =
    Option(System.getenv(AppConstant.SystemHadoopConfDir)) match {
      case Some(_) => true
      case None => false
    }

  def deleteCheckpointPath(policy: PolicyModel): Unit =
    Try {
      if (isLocalMode) deleteFromLocal(policy)
      else deleteFromHDFS(policy)
    } match {
      case Success(_) => log.info(s"Checkpoint deleted in folder: ${checkpointPath(policy)}")
      case Failure(ex) => log.error("Cannot delete checkpoint folder", ex)
    }

  def createLocalCheckpointPath(policy: PolicyModel) : Unit = {
    if (isLocalMode)
      Try {
        createFromLocal(policy)
      } match {
        case Success(_) => log.info(s"Checkpoint created in folder: ${checkpointPath(policy)}")
        case Failure(ex) => log.error("Cannot create checkpoint folder", ex)
      }
  }

  def checkpointPath(policy: PolicyModel): String =
    policy.checkpointPath.map { path =>
      s"${cleanCheckpointPath(path)}/${policy.name}"
    } getOrElse checkpointPathFromProperties(policy.name)

  def autoDeleteCheckpointPath(policy: PolicyModel): Boolean =
    policy.autoDeleteCheckpoint.getOrElse(autoDeleteCheckpointPathFromProperties)

  /* PRIVATE METHODS */

  private def cleanCheckpointPath(path: String): String = {
    val hdfsPrefix = "hdfs://"

    if (path.startsWith(hdfsPrefix))
      log.info(s"The path starts with $hdfsPrefix and is not valid, it is replaced with empty value")
    path.replace(hdfsPrefix, "")
  }

  private def checkpointPathFromProperties(policyName: String): String =
    (for {
      config <- SpartaConfig.getDetailConfig
      checkpointPath <- Try(cleanCheckpointPath(config.getString(AppConstant.ConfigCheckpointPath))).toOption
    } yield s"$checkpointPath/$policyName").getOrElse(generateDefaultCheckpointPath)

  private def autoDeleteCheckpointPathFromProperties: Boolean =
    Try(SpartaConfig.getDetailConfig.get.getBoolean(AppConstant.ConfigAutoDeleteCheckpoint))
      .getOrElse(AppConstant.DefaultAutoDeleteCheckpoint)

  private def generateDefaultCheckpointPath: String =
    SpartaConfig.getDetailConfig.map(_.getString(AppConstant.ExecutionMode)) match {
      case Some(mode) if mode == AppConstant.ConfigMesos || mode == AppConstant.ConfigYarn =>
        AppConstant.DefaultCheckpointPathClusterMode +
          Try(SpartaConfig.getHdfsConfig.get.getString(AppConstant.HadoopUserName))
            .getOrElse(AppConstant.DefaultHdfsUser) +
          AppConstant.DefaultHdfsUser
      case Some(AppConstant.ConfigLocal) =>
        AppConstant.DefaultCheckpointPathLocalMode
      case _ =>
        throw new RuntimeException("Error getting execution mode")
    }

  private def createFromLocal(policy: PolicyModel): Unit = {
    val checkpointDirectory = checkpointPath(policy)
    log.info(s"Creating checkpoint directory: $checkpointDirectory")
    FileUtils.forceMkdir(new File(checkpointDirectory))
  }
}
