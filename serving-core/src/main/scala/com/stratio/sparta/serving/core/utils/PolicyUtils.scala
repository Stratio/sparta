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
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.helpers.FragmentsHelper._
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.fragment.FragmentType
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util._

trait PolicyUtils extends PolicyStatusUtils with CheckpointUtils with FragmentUtils {


  /** METHODS TO MANAGE POLICIES IN ZOOKEEPER **/

  def getPolicyById(id: String): PolicyModel = {
    read[PolicyModel](
      new Predef.String(curatorFramework.getData.forPath(s"${AppConstant.PoliciesBasePath}/$id")))
  }

  def findAllPolicies(withFragments: Boolean): List[PolicyModel] = {
    val children = curatorFramework.getChildren.forPath(AppConstant.PoliciesBasePath)

    JavaConversions.asScalaBuffer(children).toList.map(id => getPolicyById(id))
  }

  def deleteAllPolicies(): List[PolicyModel] = {
    val policiesModels = findAllPolicies(withFragments = false)
    policiesModels.foreach(policyModel => {
      if (autoDeleteCheckpointPath(policyModel)) deleteCheckpointPath(policyModel)
      doDeletePolicy(policyModel)
    })
    policiesModels
  }

  def findPoliciesByFragmentType(fragmentType: String): List[PolicyModel] =
    findAllPolicies(withFragments = true).filter(apm => apm.fragments.exists(f => f.fragmentType == fragmentType))

  def findPoliciesByFragmentId(fragmentType: String, id: String): List[PolicyModel] =
    findAllPolicies(withFragments = true).filter(apm => apm.fragments.exists(f =>
      if (f.id.isDefined)
        f.id.get == id && f.fragmentType == fragmentType
      else false
    ))

  def findPoliciesByFragmentName(fragmentType: String, name: String): List[PolicyModel] =
    findAllPolicies(withFragments = true)
      .filter(apm => apm.fragments.exists(f => f.name == name && f.fragmentType == fragmentType))

  def findPolicy(id: String): PolicyModel = policyWithFragments(getPolicyById(id), withFragmentCreation = false)

  def findPolicyByName(name: String): PolicyModel =
    existsPolicyByNameId(name, None).map(policy => policyWithFragments(policy, withFragmentCreation = false))
      .getOrElse(throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsPolicyWithName, s"No policy with name $name"))))

  def createPolicy(policy: PolicyModel): PolicyModel = {
    val searchPolicy = existsPolicyByNameId(policy.name, policy.id)
    if (searchPolicy.isDefined) {
      throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeExistsPolicyWithName,
          s"Policy with name ${policy.name} exists. The actual policy name is: ${searchPolicy.get.name}")
      ))
    }
    val policySaved = writePolicy(policyWithId(policy))
    updateStatus(PolicyStatusModel(
      id = policySaved.id.get,
      status = PolicyStatusEnum.NotStarted,
      name = Option(policy.name),
      description = Option(policy.description)
    ))
    policySaved
  }

  def updatePolicy(policy: PolicyModel): PolicyModel = {
    val searchPolicy = existsPolicyByNameId(policy.name, policy.id)
    if (searchPolicy.isEmpty) {
      throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeExistsPolicyWithName,
          s"Policy with name ${policy.name} not exists.")
      ))
    } else {
      val policySaved = doUpdatePolicy(policyWithId(policy))
      updateStatus(PolicyStatusModel(
        id = policySaved.id.get,
        status = PolicyStatusEnum.NotDefined,
        name = Option(policy.name),
        description = Option(policy.description)
      ))
      policySaved
    }
  }

  def deletePolicy(id: String): Unit = {
    val policyModel = getPolicyById(id)
    if (autoDeleteCheckpointPath(policyModel)) deleteCheckpointPath(policyModel)
    doDeletePolicy(policyModel)
  }

  /** PRIVATE METHODS **/

  private[sparta] def existsPath: Boolean = CuratorFactoryHolder.existsPath(AppConstant.PoliciesBasePath)

  private[sparta] def existsPolicyByNameId(name: String, id: Option[String] = None): Option[PolicyModel] =
    Try {
      if (existsPath) {
        findAllPolicies(withFragments = false).find(policy =>
          if (id.isDefined && policy.id.isDefined) policy.id.get == id.get else policy.name == name.toLowerCase
        )
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  private[sparta] def doDeletePolicy(policy: PolicyModel): Unit = {
    curatorFramework.delete().forPath(s"${AppConstant.PoliciesBasePath}/${policy.id.get}")
    deleteStatus(policy.id.get)
  }

  private[sparta] def writePolicy(policy: PolicyModel): PolicyModel = {
    val policyParsed = policyWithFragments(policy)

    curatorFramework.create().creatingParentsIfNeeded().forPath(
      s"${AppConstant.PoliciesBasePath}/${policyParsed.id.get}", write(policyParsed).getBytes)
    policyParsed
  }

  private[sparta] def doUpdatePolicy(policy: PolicyModel): PolicyModel = {
    val policyParsed = policyWithFragments(policy)

    curatorFramework.setData().forPath(
      s"${AppConstant.PoliciesBasePath}/${policyParsed.id.get}", write(policyParsed).getBytes)
    policyParsed
  }

  private[sparta] def policyWithFragments(policy: PolicyModel, withFragmentCreation: Boolean = true): PolicyModel = {
    if (withFragmentCreation)
      (populateFragmentFromPolicy(policy, FragmentType.input) ++
        populateFragmentFromPolicy(policy, FragmentType.output)
        ).foreach(fragment => createFragment(fragment))
      getPolicyWithFragments(policy)

    }

  private[sparta] def policyWithId(policy: PolicyModel): PolicyModel = {
    (policy.id match {
      case None => populatePolicyWithRandomUUID(policy)
      case Some(_) => policy
    }).copy(name = policy.name.toLowerCase)
  }

  private[sparta] def populatePolicyWithRandomUUID(policy: PolicyModel): PolicyModel =
    policy.copy(id = Some(UUID.randomUUID.toString))
}
