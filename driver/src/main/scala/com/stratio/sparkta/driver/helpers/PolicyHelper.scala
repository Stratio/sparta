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
 * See the License for the specific language governing permis;sions and
 * limitations under the License.
 */

package com.stratio.sparkta.driver.helpers

import com.stratio.sparkta.driver.dto.FragmentType._
import com.stratio.sparkta.driver.dto.{AggregationPoliciesDto, FragmentType, PolicyElementDto}

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
  def parseFragments(apConfig: AggregationPoliciesDto): AggregationPoliciesDto = {
    val mapInputsOutputs: Map[`type`, Seq[PolicyElementDto]] = (apConfig.fragments.map(fragment =>
      FragmentType.withName(fragment.fragmentType) match {
        case FragmentType.input => (FragmentType.input -> fragment.element)
        case FragmentType.output => (FragmentType.output -> fragment.element)
      })
      ++ apConfig.inputs.map(input => (FragmentType.input -> input))
      ++ apConfig.outputs.map(output => (FragmentType.output -> output))
      ).groupBy(_._1).mapValues(_.map(_._2))

    apConfig.copy(
      inputs = mapInputsOutputs.get(FragmentType.input).getOrElse(Seq()),
      outputs = mapInputsOutputs.get(FragmentType.output).getOrElse(Seq()))
  }
}
