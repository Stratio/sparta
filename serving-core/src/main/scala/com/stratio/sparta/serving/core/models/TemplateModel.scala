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
package com.stratio.sparta.serving.core.models

/**
 * Class that represents a template used by the frontend to render inputs/outputs.
 */
case class TemplateModel(name: String,
                        modelType: String,
                        description: Map[String,String],
                        icon: Map[String,String],
                        properties: Seq[PropertyElementModel] )

case class PropertyElementModel(propertyId: String,
                              propertyName: String,
                              propertyType: String,
                              values: Option[Seq[Map[String, String]]],
                              regexp: Option[String],
                              default: Option[String],
                              required: Boolean,
                              tooltip: String,
                              hidden: Option[Boolean],
                              visible: Option[Seq[Seq[VisibleItemModel]]],
                              fields: Option[Seq[Any]])

case class VisibleItemModel(propertyId: String,
                            value: String,
                            overrideProps: Option[Seq[Map[String, String]]])
