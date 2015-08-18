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

package com.stratio.sparkta.serving.api.helpers

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.serving.api.actor._
import com.stratio.sparkta.serving.core.models.FragmentType._
import com.stratio.sparkta.serving.core.models._

import scala.concurrent.Await
import scala.util.{Failure, Success}

/**
 * Helper with operations over policies and policy fragments.
 * @author anistal
 */
object PolicyHelper {

  /**
   * If the policy has fragments, it tries to parse them and depending of its type it composes input/outputs/etc.
   * @param apConfig with the policy.
   * @return a parsed policy with fragments included in input/outputs.
   */
  def parseFragments(apConfig: AggregationPoliciesModel): AggregationPoliciesModel = {
    val mapInputsOutputs: Map[`type`, Seq[PolicyElementModel]] = (apConfig.fragments.map(fragment =>
      FragmentType.withName(fragment.fragmentType) match {
        case FragmentType.input => FragmentType.input -> fragment.element
        case FragmentType.output => FragmentType.output -> fragment.element
      })
      ++ apConfig.outputs.map(output => FragmentType.output -> output)
      ).groupBy(_._1).mapValues(_.map(_._2))

    if(mapInputsOutputs.get(FragmentType.output).isEmpty) {
      throw new IllegalStateException("It is mandatory to define at least one output in the policy.")
    }

    apConfig.copy(
      input = Some(getCurrentInput(mapInputsOutputs, apConfig)),
      outputs = mapInputsOutputs.getOrElse(FragmentType.output, Seq()))
  }

  /**
   * The policy only has fragments with its name and type. When this method is called it finds the full fragment in
   * ZK and fills the rest of the fragment.
   * @param apConfig with the policy.
   * @return a fragment with all fields filled.
   */
  def fillFragments(apConfig: AggregationPoliciesModel,
                    actor: ActorRef,
                    currentTimeout: Timeout): AggregationPoliciesModel = {
    implicit val timeout = currentTimeout

    val currentFragments: Seq[FragmentElementModel] = apConfig.fragments.map(fragment => {
      val future = actor ? new FragmentSupervisorActor_findByTypeAndName(fragment.fragmentType, fragment.name)
      Await.result(future, timeout.duration) match {
        case FragmentSupervisorActor_response_fragment(Failure(exception)) => {
          throw exception
        }
        case FragmentSupervisorActor_response_fragment(Success(fragment)) => fragment
      }
    })
    apConfig.copy(fragments = currentFragments)
  }

  //////////////////////////////////////////// PRIVATE METHODS /////////////////////////////////////////////////////////

  /**
   * Depending of where is the input it tries to get a input. If not an exceptions is thrown.
   * @param mapInputsOutputs with inputs/outputs extracted from the fragments.
   * @param apConfig with the current configuration.
   * @return A policyElementModel with the input.
   */
  private def getCurrentInput(mapInputsOutputs: Map[`type`, Seq[PolicyElementModel]],
                              apConfig: AggregationPoliciesModel): PolicyElementModel = {
    val currentInputs = mapInputsOutputs.filter(x => if(x._1 == FragmentType.input) true else false)

    if((currentInputs.nonEmpty && currentInputs.get(FragmentType.input).get.size > 1)
      ||(currentInputs.nonEmpty && currentInputs.get(FragmentType.input).get.size == 1 && apConfig.input.isDefined)) {
      throw new IllegalStateException("Only one input is allowed in the policy.")
    }

    if(currentInputs.isEmpty && apConfig.input.isDefined == false) {
      throw new IllegalStateException("It is mandatory to define one input in the policy.")
    }

    apConfig.input.getOrElse(mapInputsOutputs.get(FragmentType.input).get.head)
  }

}
