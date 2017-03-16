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

import java.util.UUID

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.helpers.FragmentsHelper._
import com.stratio.sparta.serving.core.models.policy.fragment.{FragmentElementModel, FragmentType}
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util._

trait PolicyUtils extends PolicyStatusUtils with FragmentUtils {

  /** METHODS TO MANAGE POLICIES IN ZOOKEEPER **/

  def existsPath: Boolean = CuratorFactoryHolder.existsPath(AppConstant.PoliciesBasePath)

  def deletePolicy(policy: PolicyModel): Unit = {
    curatorFramework.delete().forPath(s"${AppConstant.PoliciesBasePath}/${policy.id.get}")
    deleteStatus(policy.id.get)
  }

  def writePolicy(policy: PolicyModel): PolicyModel = {
    val policyParsed = policyWithFragments(policy)

    curatorFramework.create().creatingParentsIfNeeded().forPath(
      s"${AppConstant.PoliciesBasePath}/${policyParsed.id.get}", write(policyParsed).getBytes)
    policyParsed
  }

  def updatePolicy(policy: PolicyModel): PolicyModel = {
    val policyParsed = policyWithFragments(policy)

    curatorFramework.setData().forPath(
      s"${AppConstant.PoliciesBasePath}/${policyParsed.id.get}", write(policyParsed).getBytes)
    policyParsed
  }

  def existsPolicyByNameId(name: String, id: Option[String] = None): Option[PolicyModel] =
    Try {
      if (existsPath) {
        getPolicies(withFragments = false).find(policy =>
          if (id.isDefined && policy.id.isDefined) policy.id.get == id.get else policy.name == name.toLowerCase
        )
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  def getPolicies(withFragments: Boolean): List[PolicyModel] = {
    val children = curatorFramework.getChildren.forPath(AppConstant.PoliciesBasePath)

    JavaConversions.asScalaBuffer(children).toList.map(id => getPolicyById(id))
  }

  def getPolicyById(id: String): PolicyModel = {
    read[PolicyModel](
      new Predef.String(curatorFramework.getData.forPath(s"${AppConstant.PoliciesBasePath}/$id")))
  }

  /** METHODS TO CALCULATE THE CORRECT ID IN POLICIES **/

  def policyWithId(policy: PolicyModel): PolicyModel = {
    (policy.id match {
      case None => populatePolicyWithRandomUUID(policy)
      case Some(_) => policy
    }).copy(name = policy.name.toLowerCase)
  }

  def populatePolicyWithRandomUUID(policy: PolicyModel): PolicyModel =
    policy.copy(id = Some(UUID.randomUUID.toString))

  def policyWithFragments(policy: PolicyModel, withFragmentCreation: Boolean = true): PolicyModel = {
    if (withFragmentCreation)
      (populateFragmentFromPolicy(policy, FragmentType.input) ++
        populateFragmentFromPolicy(policy, FragmentType.output)
        ).foreach(fragment => createFragment(fragment))

    getPolicyWithFragments(policy)
  }

  def loggingResponseFragment(response: Try[FragmentElementModel]): Unit =
    response match {
      case Success(fragment) =>
        log.info(s"Fragment created correctly: \n\tId: ${fragment.id}\n\tName: ${fragment.name}")
      case Failure(e) =>
        log.error(s"Fragment creation failure. Error: ${e.getLocalizedMessage}", e)
    }

  def loggingResponsePolicyStatus(response: Try[PolicyStatusModel]): Unit =
    response match {
      case Success(statusModel) =>
        log.info(s"Policy status model created or updated correctly: " +
          s"\n\tId: ${statusModel.id}\n\tStatus: ${statusModel.status}")
      case Failure(e) =>
        log.error(s"Policy status model creation failure. Error: ${e.getLocalizedMessage}", e)
    }
}
