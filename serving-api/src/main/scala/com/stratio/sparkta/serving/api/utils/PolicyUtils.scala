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

package com.stratio.sparkta.serving.api.utils

import java.util.UUID
import scala.collection.JavaConversions
import scala.util._

import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import com.stratio.sparkta.serving.api.constants.ActorsConstant
import com.stratio.sparkta.serving.api.helpers.SparktaHelper._
import com.stratio.sparkta.serving.core.CuratorFactoryHolder
import com.stratio.sparkta.serving.core.constants.AppConstant
import com.stratio.sparkta.serving.core.models.AggregationPoliciesModel

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

  def getPolicies(curatorFramework: CuratorFramework): List[AggregationPoliciesModel] = {
    val children = curatorFramework.getChildren.forPath(s"${AppConstant.PoliciesBasePath}")
    JavaConversions.asScalaBuffer(children).toList.map(element =>
      read[AggregationPoliciesModel](new String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/$element"))))
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
}
