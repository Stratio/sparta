/**
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

package com.stratio.sparta.serving.api.utils

import java.io.File
import java.util.UUID

import com.stratio.sparta.driver.util.HdfsUtils
import com.stratio.sparta.serving.api.constants.ActorsConstant
import com.stratio.sparta.serving.api.helpers.SpartaHelper._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.AggregationPoliciesModel
import com.stratio.sparta.serving.core.{CuratorFactoryHolder, SpartaConfig}
import org.apache.commons.io.FileUtils
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util._

trait PolicyUtils {

  def existsByName(name: String,
                   id: Option[String] = None,
                   curatorFramework: CuratorFramework): Boolean = {
    val nameToCompare = name.toLowerCase
    Try({
      if (existsPath) {
        getPolicies(curatorFramework)
          .filter(byName(id, nameToCompare)).toSeq.nonEmpty
      } else {
        log.warn(s"Zookeeper path for policies doesn't exists. It will be created.")
        false
      }
    }) match {
      case Success(result) => result
      case Failure(exception) => {
        log.error(exception.getLocalizedMessage, exception)
        false
      }
    }
  }

  def existsPath: Boolean = {
    CuratorFactoryHolder.existsPath(AppConstant.PoliciesBasePath)
  }

  def byName(id: Option[String], nameToCompare: String): (AggregationPoliciesModel) => Boolean = {
    policy =>
      if (id.isDefined)
        policy.name == nameToCompare && policy.id.get != id.get
      else policy.name == nameToCompare
  }


  def savePolicyInZk(policy: AggregationPoliciesModel, curatorFramework: CuratorFramework): Unit = {

    Try({
      populatePolicy(policy, curatorFramework)
    }) match {
      case Success(_) => log.info(s"Policy ${policy.id.get} already in zookeeper. Updating it...")
        updatePolicy(policy, curatorFramework)
      case Failure(e) => writePolicy(policy, curatorFramework)
    }
  }

  def writePolicy(policy: AggregationPoliciesModel, curatorFramework: CuratorFramework): Unit = {
    curatorFramework.create().creatingParentsIfNeeded().forPath(
      s"${AppConstant.PoliciesBasePath}/${policy.id.get}", write(policy).getBytes)
  }

  def updatePolicy(policy: AggregationPoliciesModel, curatorFramework: CuratorFramework): Unit = {
    curatorFramework.setData.forPath(s"${AppConstant.PoliciesBasePath}/${policy.id.get}", write(policy).getBytes)
  }

  def populatePolicy(policy: AggregationPoliciesModel, curatorFramework: CuratorFramework): AggregationPoliciesModel = {
    read[AggregationPoliciesModel](new Predef.String(curatorFramework.getData.forPath(
      s"${AppConstant.PoliciesBasePath}/${policy.id.get}")))
  }

  def policyWithId(policy: AggregationPoliciesModel): AggregationPoliciesModel =
    (policy.id match {
      case None => populatePolicyWithRandomUUID(policy)
      case Some(_) => policy
    }).copy(name = policy.name.toLowerCase, version = Some(ActorsConstant.UnitVersion))

  def populatePolicyWithRandomUUID(policy: AggregationPoliciesModel): AggregationPoliciesModel = {
    policy.copy(id = Some(UUID.randomUUID.toString))
  }

  def deleteCheckpointPath(policy: AggregationPoliciesModel): Unit = {
    Try {
      if (!isLocalMode || checkpointGoesToHDFS(policy)) {
        deleteFromHDFS(policy)
      } else {
        deleteFromLocal(policy)
      }
    } match {
      case Success(_) => log.info(s"Checkpoint deleted in folder: ${AggregationPoliciesModel.checkpointPath(policy)}")
      case Failure(ex) => log.error("Cannot delete checkpoint folder", ex)
    }
  }

  def deleteFromLocal(policy: AggregationPoliciesModel): Unit = {
    FileUtils.deleteDirectory(new File(AggregationPoliciesModel.checkpointPath(policy)))
  }

  def deleteFromHDFS(policy: AggregationPoliciesModel): Unit = {
    HdfsUtils(SpartaConfig.getHdfsConfig).delete(AggregationPoliciesModel.checkpointPath(policy))
  }

  def checkpointGoesToHDFS(policy: AggregationPoliciesModel): Boolean = {
    policy.checkpointPath.startsWith("hdfs://")
  }

  def isLocalMode: Boolean = {
    SpartaConfig.getDetailConfig.get.getString(AppConstant.ExecutionMode).equalsIgnoreCase("local")
  }

  def existsByNameId(name: String, id: Option[String] = None, curatorFramework: CuratorFramework):
  Option[AggregationPoliciesModel] = {
    val nameToCompare = name.toLowerCase
    Try({
      if (existsPath) {
        getPolicies(curatorFramework)
          .find(policy => if (id.isDefined) policy.id.get == id.get else policy.name == nameToCompare)
      } else None
    }) match {
      case Success(result) => result
      case Failure(exception) => {
        log.error(exception.getLocalizedMessage, exception)
        None
      }
    }
  }


  def setVersion(lastPolicy: AggregationPoliciesModel, newPolicy: AggregationPoliciesModel): Option[Int] = {
    if (lastPolicy.cubes != newPolicy.cubes) {
      lastPolicy.version match {
        case Some(version) => Some(version + ActorsConstant.UnitVersion)
        case None => Some(ActorsConstant.UnitVersion)
      }
    } else lastPolicy.version
  }
  def getPolicies(curatorFramework: CuratorFramework): List[AggregationPoliciesModel] = {
    val children = curatorFramework.getChildren.forPath(AppConstant.PoliciesBasePath)
    JavaConversions.asScalaBuffer(children).toList.map(element =>
      read[AggregationPoliciesModel](new Predef.String(curatorFramework.getData.
        forPath(s"${AppConstant.PoliciesBasePath}/$element"))))
  }

  def byId(id: String, curatorFramework: CuratorFramework): AggregationPoliciesModel =
    read[AggregationPoliciesModel](
      new Predef.String(curatorFramework.getData.forPath(s"${AppConstant.PoliciesBasePath}/$id")))

  def deleteRelatedPolicies(policies: Seq[AggregationPoliciesModel]): Unit = {
    policies.foreach(deleteCheckpointPath)
  }
}
