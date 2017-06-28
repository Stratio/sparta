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

import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.workflow.PhaseEnum
import com.stratio.sparta.serving.core.utils.ReflectionUtils
import org.apache.spark.sql.Row
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

trait InputStage extends BaseStage {
  this: ErrorPersistor =>

  def inputStreamStage(ssc: StreamingContext, input: Input): DStream[Row] = {
    val errorMessage = s"Something gone wrong creating the input stream for: ${workflow.input.get.name}."
    val okMessage = s"Stream for Input: ${workflow.input.get.name} created correctly."

    generalTransformation(PhaseEnum.InputStream, okMessage, errorMessage) {
      input.initStream(ssc)
    }
  }

  def createInput(ssc: StreamingContext, refUtils: ReflectionUtils): Input = {
    val errorMessage = s"Something gone wrong creating the input: ${workflow.input.get.name}." +
      s" Please re-check the policy."
    val okMessage = s"Input: ${workflow.input.get.name} created correctly."

    generalTransformation(PhaseEnum.Input, okMessage, errorMessage) {
      require(workflow.input.isDefined, "You need at least one input in your policy")
      val classType =
        workflow.input.get.configuration.getOrElse(AppConstant.CustomTypeKey, workflow.input.get.`type`).toString
      refUtils.tryToInstantiate[Input](classType + Input.ClassSuffix, (c) =>
        refUtils.instantiateParameterizable[Input](c, workflow.input.get.configuration))
    }
  }


}
