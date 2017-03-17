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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.helpers.FragmentsHelper
import com.stratio.sparta.serving.core.models.policy.fragment.FragmentType._
import com.stratio.sparta.serving.core.models.policy.fragment.{FragmentElementModel, FragmentType}
import com.stratio.sparta.serving.core.models.policy.{PolicyElementModel, PolicyModel}
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.Try

trait FragmentUtils extends SLF4JLogging with SpartaSerializer {

  val curatorFramework: CuratorFramework

  def findAllFragments: List[FragmentElementModel] = {
    if (CuratorFactoryHolder.existsPath(FragmentsPath)) {
      val children = curatorFramework.getChildren.forPath(FragmentsPath)
      JavaConversions.asScalaBuffer(children).toList.flatMap(fragmentType => findFragmentsByType(fragmentType))
    } else List.empty[FragmentElementModel]
  }

  def findFragmentsByType(fragmentType: String): List[FragmentElementModel] = {
    val fragmentLocation = fragmentPathType(fragmentType)
    if (CuratorFactoryHolder.existsPath(fragmentLocation)) {
      val children = curatorFramework.getChildren.forPath(fragmentLocation)
      JavaConversions.asScalaBuffer(children).toList.map(id => findFragmentByTypeAndId(fragmentType, id))
    } else List.empty[FragmentElementModel]
  }

  def findFragmentByTypeAndId(fragmentType: String, id: String): FragmentElementModel = {
    val fragmentLocation = s"${fragmentPathType(fragmentType)}/$id"
    if (CuratorFactoryHolder.existsPath(fragmentLocation)) {
      read[FragmentElementModel](new String(curatorFramework.getData.forPath(fragmentLocation)))
    } else throw new ServingCoreException(ErrorModel.toString(
      new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId, s"Fragment type: $fragmentType and id: $id not exists")))
  }

  def findFragmentByTypeAndName(fragmentType: String, name: String): Option[FragmentElementModel] =
    findFragmentsByType(fragmentType).find(fragment => fragment.name == name)

  def createFragment(fragment: FragmentElementModel): FragmentElementModel =
    findFragmentByTypeAndName(fragment.fragmentType, fragment.name.toLowerCase)
      .getOrElse(createNewFragment(fragment))

  def  updateFragment(fragment: FragmentElementModel): FragmentElementModel = {
    val newFragment = fragment.copy(name = fragment.name.toLowerCase)
    curatorFramework.setData().forPath(
      s"${fragmentPathType(newFragment.fragmentType)}/${fragment.id.get}", write(newFragment).getBytes)
    newFragment
  }

  def deleteAllFragments(): List[FragmentElementModel] = {
    val fragmentsFound = findAllFragments
    fragmentsFound.foreach(fragment => {
      val id = fragment.id.getOrElse {
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeErrorDeletingAllFragments, s"Fragment without id: ${fragment.name}.")))
      }
      deleteFragmentByTypeAndId(fragment.fragmentType, id)
    })
    fragmentsFound
  }

  def deleteFragmentsByType(fragmentType: String): Unit = {
    val children = curatorFramework.getChildren.forPath(fragmentPathType(fragmentType))
    val fragmentsFound = JavaConversions.asScalaBuffer(children).toList.map(element =>
      read[FragmentElementModel](new String(curatorFramework.getData.forPath(
        s"${fragmentPathType(fragmentType)}/$element"))))
    fragmentsFound.foreach(fragment => {
      val id = fragment.id.getOrElse {
        throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsFragmentWithId, s"Fragment without id: ${fragment.name}.")))
      }
      deleteFragmentByTypeAndId(fragmentType, id)
    })
  }

  def deleteFragmentByTypeAndId(fragmentType: String, id: String): Unit = {
    val fragmentLocation = s"${fragmentPathType(fragmentType)}/$id"
    if (CuratorFactoryHolder.existsPath(fragmentLocation)) curatorFramework.delete().forPath(fragmentLocation)
  }

  def deleteFragmentByTypeAndName(fragmentType: String, name: String): Unit = {
    val fragmentFound = findFragmentByTypeAndName(fragmentType, name)
    if (fragmentFound.isDefined && fragmentFound.get.id.isDefined) {
      val id = fragmentFound.get.id.get
      val fragmentLocation = s"${fragmentPathType(fragmentType)}/$id"
      if (CuratorFactoryHolder.existsPath(fragmentLocation))
        curatorFramework.delete().forPath(fragmentLocation)
      else throw new ServingCoreException(ErrorModel.toString(new ErrorModel(
        ErrorModel.CodeNotExistsFragmentWithId, s"Fragment type: $fragmentType and id: $id not exists")))
    } else {
      throw new ServingCoreException(ErrorModel.toString(new ErrorModel(
        ErrorModel.CodeExistsFragmentWithName, s"Fragment without id: $name.")))
    }
  }

  /* PRIVATE METHODS */

  private def createNewFragment(fragment: FragmentElementModel): FragmentElementModel = {
    val newFragment = fragment.copy(
      id = Option(UUID.randomUUID.toString),
      name = fragment.name.toLowerCase
    )
    curatorFramework.create().creatingParentsIfNeeded().forPath(
      s"${fragmentPathType(newFragment.fragmentType)}/${newFragment.id.get}", write(newFragment).getBytes())

    newFragment
  }

  private def fragmentPathType(fragmentType: String): String = {
    fragmentType match {
      case "input" => s"$FragmentsPath/input"
      case "output" => s"$FragmentsPath/output"
      case _ => throw new IllegalArgumentException("The fragment type must be input|output")
    }
  }

  /* POLICY METHODS */

  def getPolicyWithFragments(policy: PolicyModel): PolicyModel = {
    val policyWithFragments = parseFragments(fillFragments(policy))
    if (policyWithFragments.fragments.isEmpty) {
      val input = FragmentsHelper.populateFragmentFromPolicy(policy, FragmentType.input)
      val outputs = FragmentsHelper.populateFragmentFromPolicy(policy, FragmentType.output)
      policyWithFragments.copy(fragments = input ++ outputs)
    } else policyWithFragments
  }

  private def parseFragments(apConfig: PolicyModel): PolicyModel = {
    val fragmentInputs = getFragmentFromType(apConfig.fragments, FragmentType.input)
    val fragmentOutputs = getFragmentFromType(apConfig.fragments, FragmentType.output)

    apConfig.copy(
      input = Some(getCurrentInput(fragmentInputs, apConfig.input)),
      outputs = getCurrentOutputs(fragmentOutputs, apConfig.outputs))
  }

  private def fillFragments(apConfig: PolicyModel): PolicyModel = {
    val currentFragments = apConfig.fragments.flatMap(fragment => {
      fragment.id match {
        case Some(id) =>
          Try(findFragmentByTypeAndId(fragment.fragmentType, id)).toOption
        case None => findFragmentByTypeAndName(fragment.fragmentType, fragment.name)
      }
    })
    apConfig.copy(fragments = currentFragments)
  }

  private def getFragmentFromType(fragments: Seq[FragmentElementModel], fragmentType: `type`)
  : Seq[FragmentElementModel] = {
    fragments.flatMap(fragment =>
      if (FragmentType.withName(fragment.fragmentType) == fragmentType) Some(fragment) else None)
  }

  private def getCurrentInput(fragmentsInputs: Seq[FragmentElementModel],
                              inputs: Option[PolicyElementModel]): PolicyElementModel = {

    if (fragmentsInputs.isEmpty && inputs.isEmpty) {
      throw new IllegalStateException("It is mandatory to define one input in the policy.")
    }

    if ((fragmentsInputs.size > 1) ||
      (fragmentsInputs.size == 1 && inputs.isDefined &&
        ((fragmentsInputs.head.name != inputs.get.name) ||
          (fragmentsInputs.head.element.configuration.getOrElse(
            AppConstant.CustomTypeKey, fragmentsInputs.head.element.`type`) !=
            inputs.get.configuration.getOrElse(AppConstant.CustomTypeKey, inputs.get.`type`))))) {
      throw new IllegalStateException("Only one input is allowed in the policy.")
    }

    if (fragmentsInputs.isEmpty) inputs.get else fragmentsInputs.head.element.copy(name = fragmentsInputs.head.name)
  }

  private def getCurrentOutputs(fragmentsOutputs: Seq[FragmentElementModel],
                                outputs: Seq[PolicyElementModel]): Seq[PolicyElementModel] = {

    val outputsTypesNames = fragmentsOutputs.map(fragment =>
      (fragment.element.configuration.getOrElse(AppConstant.CustomTypeKey, fragment.element.`type`), fragment.name))

    val outputsNotIncluded = for {
      output <- outputs
      outputType = output.configuration.getOrElse(AppConstant.CustomTypeKey, output.`type`)
      outputTypeName = (outputType, output.name)
    } yield if (outputsTypesNames.contains(outputTypeName)) None else Some(output)

    fragmentsOutputs.map(fragment => fragment.element.copy(name = fragment.name)) ++ outputsNotIncluded.flatten
  }
}