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

package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.sdk.properties.JsoneyString
import org.joda.time.DateTime

case class TemplateElement(
                                 id: Option[String] = None,
                                 templateType: String,
                                 name: String,
                                 description: Option[String],
                                 className: String,
                                 classPrettyName: String,
                                 configuration: Map[String, JsoneyString] = Map(),
                                 creationDate: Option[DateTime] = None,
                                 lastUpdateDate: Option[DateTime] = None
                               )

object TemplateType extends Enumeration {

  type TemplateType = Value

  val InputValue = "input"
  val OutputValue = "output"
  val TransformationValue = "transformation"
  val input = Value(InputValue)
  val output = Value(OutputValue)
  val transformation = Value(TransformationValue)

  val AllowedTypes = Seq(input, output, transformation)
}