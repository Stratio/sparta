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
import com.stratio.sparkta.serving.api.actor.FragmentActor.{ResponseFragment, FindByTypeAndId}
import com.stratio.sparkta.serving.core.models.FragmentType._
import com.stratio.sparkta.serving.core.models._
import spray.http.{IllegalRequestException, StatusCodes}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Helper with operations over policies and policy fragments.
 * @author anistal
 */
object PolicyHelper {

  /**
   * If the policy has fragments, it tries to parse them and depending of its type it composes input/outputs/etc.
   * @param apConfigF with the policy.
   * @return a parsed policy with fragments included in input/outputs.
   */
  def parseFragments(apConfigF: Future[AggregationPoliciesModel]): Future[AggregationPoliciesModel] = {
    for {
      apConfig <- apConfigF
    } yield {
      val mapInputsOutputs: Map[`type`, Seq[PolicyElementModel]] = (apConfig.fragments.map(fragment =>
        FragmentType.withName(fragment.fragmentType) match {
          case FragmentType.input => FragmentType.input -> fragment.element
          case FragmentType.output => FragmentType.output -> fragment.element
        })
        ++ apConfig.outputs.map(output => FragmentType.output -> output)
        ).groupBy(_._1).mapValues(_.map(_._2))

      if (mapInputsOutputs.get(FragmentType.output).isEmpty) {
        throw new IllegalStateException("It is mandatory to define at least one output in the policy.")
      }

      apConfig.copy(
        input = Some(getCurrentInput(mapInputsOutputs, apConfig)),
        outputs = mapInputsOutputs.getOrElse(FragmentType.output, Seq())
      )
    }
  }

  /**
   * The policy only has fragments with its name and type. When this method is called it finds the full fragment in
   * ZK and fills the rest of the fragment.
   * @param apConfig with the policy.
   * @return a fragment with all fields filled.
   */
  def fillFragments(apConfig: AggregationPoliciesModel,
                    actor: ActorRef,
                    currentTimeout: Timeout): Future[AggregationPoliciesModel] = {
    implicit val timeout = currentTimeout

    val currentFragments: Seq[Future[FragmentElementModel]] = apConfig.fragments.map(fragment =>
      for {
        response <- (actor ? new FindByTypeAndId(fragment.fragmentType, fragment.id.get)).mapTo[ResponseFragment]
      } yield response.fragment.get
    )

    Future.sequence(currentFragments).map(
      current => apConfig.copy(fragments = current)
    )
  }

  /**
   * If the aggregationPoliciesModel is invalid, throw an IllegalRequestException
   * @param aggregationPoliciesModel to evaluate
   * @return if the aggregationPoliciesModel is valid or invalid
   */
  def validateAggregationPolicy(aggregationPoliciesModel: AggregationPoliciesModel): Boolean =
    AggregationPoliciesValidator.validateDto(aggregationPoliciesModel) match {
      case (false, msg) => throw new IllegalRequestException(StatusCodes.BadRequest, msg)
      case (validationOk, _) => validationOk
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
    val currentInputs = mapInputsOutputs.filter(x => if (x._1 == FragmentType.input) true else false)

    if ((currentInputs.nonEmpty && currentInputs.get(FragmentType.input).get.size > 1)
      || (currentInputs.nonEmpty && currentInputs.get(FragmentType.input).get.size == 1 && apConfig.input.isDefined)) {
      throw new IllegalStateException("Only one input is allowed in the policy.")
    }

    if (currentInputs.isEmpty && apConfig.input.isDefined == false) {
      throw new IllegalStateException("It is mandatory to define one input in the policy.")
    }

    apConfig.input.getOrElse(mapInputsOutputs.get(FragmentType.input).get.head)
  }

}