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
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, WorkflowElementModel}
import com.stratio.sparta.serving.core.utils.ReflectionUtils
import org.apache.spark.sql.crossdata.XDSession

trait OutputStage extends BaseStage {
  this: ErrorPersistor =>

  def outputStage(refUtils: ReflectionUtils, sparkSession: XDSession): Seq[Output] =
    workflow.outputs.map(o => createOutput(o, refUtils, sparkSession))

  private[driver] def createOutput(model: WorkflowElementModel,
                                   refUtils: ReflectionUtils,
                                   sparkSession: XDSession): Output = {
    val errorMessage = s"Something went wrong while creating the output: ${model.name}. Please re-check the policy"
    val okMessage = s"Output: ${model.name} created correctly."
    generalTransformation(PhaseEnum.Output, okMessage, errorMessage) {
      val classType = model.configuration.getOrElse(AppConstant.CustomTypeKey, model.`type`).toString
      refUtils.tryToInstantiate[Output](classType + Output.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[String],
          classOf[XDSession],
          classOf[Map[String, Serializable]])
          .newInstance(model.name, sparkSession, model.configuration)
          .asInstanceOf[Output])
    }
  }
}
