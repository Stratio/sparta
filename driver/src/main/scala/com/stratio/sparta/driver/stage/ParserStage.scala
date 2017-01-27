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
import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.serving.core.models.PhaseEnum
import com.stratio.sparta.serving.core.models.policy.TransformationsModel
import org.apache.spark.sql.types.StructType

trait ParserStage extends BaseStage {
  this: ErrorPersistor =>

  def parserStage(refUtils: ReflectionUtils,
                  schemas: Map[String, StructType]): Seq[Parser] =
    policy.transformations.map(parser => createParser(parser, refUtils, schemas))

  private def createParser(model: TransformationsModel,
                           refUtils: ReflectionUtils,
                           schemas: Map[String, StructType]): Parser = {
    val errorMessage = s"Something gone wrong creating the parser: ${model.`type`}. Please re-check the policy."
    val okMessage = s"Parser: ${model.`type`} created correctly."
    generalTransformation(PhaseEnum.Parser, okMessage, errorMessage) {
      val outputFieldsNames = model.outputFieldsTransformed.map(_.name)
      val schema = schemas.getOrElse(model.order.toString, throw new Exception("Can not find transformation schema"))
      refUtils.tryToInstantiate[Parser](model.`type` + Parser.ClassSuffix, (c) =>
        c.getDeclaredConstructor(
          classOf[Integer],
          classOf[Option[String]],
          classOf[Seq[String]],
          classOf[StructType],
          classOf[Map[String, Serializable]])
          .newInstance(model.order, model.inputField, outputFieldsNames, schema, model.configuration)
          .asInstanceOf[Parser])
    }
  }

}
