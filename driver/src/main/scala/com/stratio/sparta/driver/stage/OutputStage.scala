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

import com.stratio.sparta.driver.utils.ReflectionUtils
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.pipeline.schema.SpartaSchema
import com.stratio.sparta.serving.core.models.policy.{PhaseEnum, PolicyElementModel}

trait OutputStage extends BaseStage {
  this: ErrorPersistor =>

  def outputStage(schemas: Seq[SpartaSchema],
                  refUtils: ReflectionUtils): Seq[Output] = policy.outputs.map(o => {
    val schemasAssociated = schemas.filter(tableSchema => tableSchema.outputs.contains(o.name))
    createOutput(o, schemasAssociated, refUtils, policy.version)
  })

  def createOutput(model: PolicyElementModel, schemasAssociated: Seq[SpartaSchema],
                   refUtils: ReflectionUtils, version: Option[Int]): Output = {
    val errorMessage = s"Something gone wrong creating the output: ${model.`type`}. Please re-check the policy."
    val okMessage = s"Output: ${model.`type`} created correctly."
    generalTransformation(PhaseEnum.Output, okMessage, errorMessage) {
      refUtils.tryToInstantiate[Output](model.`type` + Output.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[Option[Int]],
          classOf[Map[String, Serializable]],
          classOf[Seq[SpartaSchema]])
          .newInstance(model.name, version, model.configuration, schemasAssociated)
          .asInstanceOf[Output])
    }
  }
}
