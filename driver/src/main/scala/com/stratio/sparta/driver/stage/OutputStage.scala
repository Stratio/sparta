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
package com.stratio.sparta.driver.stage

import java.io.Serializable

import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.policy.{PhaseEnum, PolicyElementModel}
import com.stratio.sparta.serving.core.utils.ReflectionUtils

trait OutputStage extends BaseStage {
  this: ErrorPersistor =>

  def outputStage(refUtils: ReflectionUtils): Seq[Output] =
    policy.outputs.map(o => createOutput(o, refUtils))

  def createOutput(model: PolicyElementModel, refUtils: ReflectionUtils)
  : Output = {
    val errorMessage = s"Something gone wrong creating the output: ${model.name}. Please re-check the policy."
    val okMessage = s"Output: ${model.name} created correctly."
    generalTransformation(PhaseEnum.Output, okMessage, errorMessage) {
      val classType = model.configuration.getOrElse(AppConstant.CustomTypeKey, model.`type`).toString
      refUtils.tryToInstantiate[Output](classType + Output.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Map[String, Serializable]])
          .newInstance(model.name, model.configuration)
          .asInstanceOf[Output])
    }
  }
}
