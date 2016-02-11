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

package com.stratio.sparkta.serving.core.helpers

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.serving.core.actor.FragmentActor.{FindByTypeAndId, ResponseFragment}
import com.stratio.sparkta.serving.core.models.FragmentType._
import com.stratio.sparkta.serving.core.models._

import scala.concurrent.Await
import scala.util.{Failure, Success}

/**
 * Helper with operations over policies and policy fragments.
 */
object PolicyHelper {

  /**
   * If the policy has fragments, it tries to parse them and depending of its type it composes input/outputs/etc.
   * @param apConfig with the policy.
   * @return a parsed policy with fragments included in input/outputs.
   */
  def parseFragments(apConfig: CommonPoliciesModel): CommonPoliciesModel = {

    val fragmentInputs = getFragmentFromType(apConfig.fragments, FragmentType.input)
    val fragmentOutputs = getFragmentFromType(apConfig.fragments, FragmentType.output)

    apConfig.copy(
      input = Some(getCurrentInput(fragmentInputs, apConfig.input)),
      outputs = getCurrentOutputs(fragmentOutputs, apConfig.outputs))
  }

  /**
   * The policy only has fragments with its name and type. When this method is called it finds the full fragment in
   * ZK and fills the rest of the fragment.
   * @param apConfig with the policy.
   * @return a fragment with all fields filled.
   */
  def fillFragments(apConfig: CommonPoliciesModel,
                    actor: ActorRef,
                    currentTimeout: Timeout): CommonPoliciesModel = {
    implicit val timeout = currentTimeout

    val currentFragments: Seq[FragmentElementModel] = apConfig.fragments.map(fragment => {
      val future = actor ? new FindByTypeAndId(fragment.fragmentType, fragment.id.get)
      Await.result(future, timeout.duration) match {
        case ResponseFragment(Failure(exception)) => throw exception
        case ResponseFragment(Success(fragment)) => fragment
      }
    })
    apConfig.copy(fragments = currentFragments)
  }

  //////////////////////////////////////////// PRIVATE METHODS /////////////////////////////////////////////////////////

  private def getFragmentFromType(fragments: Seq[FragmentElementModel], fragmentType: `type`)
  : Seq[PolicyElementModel] = {
    fragments.flatMap(fragment =>
      if (FragmentType.withName(fragment.fragmentType) == fragmentType) Some(fragment.element) else None)
  }

  /**
   * Depending of where is the input it tries to get a input. If not an exceptions is thrown.
   * @param fragmentsInputs with inputs extracted from the fragments.
   * @param inputs with the current configuration.
   * @return A policyElementModel with the input.
   */
  private def getCurrentInput(fragmentsInputs: Seq[PolicyElementModel],
                              inputs: Option[PolicyElementModel]): PolicyElementModel = {

    if (fragmentsInputs.isEmpty && inputs.isEmpty) {
      throw new IllegalStateException("It is mandatory to define one input in the policy.")
    }

    if ((fragmentsInputs.size > 1) ||
      (fragmentsInputs.size == 1 && inputs.isDefined &&
        ((fragmentsInputs.head.name != inputs.get.name) || (fragmentsInputs.head.`type` != inputs.get.`type`)))) {
      throw new IllegalStateException("Only one input is allowed in the policy.")
    }

    if (fragmentsInputs.isEmpty) inputs.get else fragmentsInputs.head
  }

  private def getCurrentOutputs(fragmentsOutputs: Seq[PolicyElementModel],
                                outputs: Seq[PolicyElementModel]): Seq[PolicyElementModel] = {

    if (fragmentsOutputs.isEmpty && outputs.isEmpty) {
      throw new IllegalStateException("It is mandatory to define at least one output in the policy.")
    }

    val outputsTypesNames = fragmentsOutputs.map(element => (element.`type`, element.name))

    val outputsNotIncluded = for {
      output <- outputs
      ouputTypeName = (output.`type`, output.name)
    } yield if (outputsTypesNames.contains(ouputTypeName)) None else Some(output)

    fragmentsOutputs ++ outputsNotIncluded.flatten
  }
}
