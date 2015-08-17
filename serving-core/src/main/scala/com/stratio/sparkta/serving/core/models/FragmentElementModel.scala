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

package com.stratio.sparkta.serving.core.models

/**
 * A fragmentElementDto represents a piece of policy that will be composed with other fragments before.
 * @param fragmentType that could be inputs/outputs/parsers
 * @param name that will be used as an identifier of the fragment.
 * @param element with all config parameters of the fragment.
 * @author anistal
 */
case class FragmentElementModel(fragmentType: String,
                                name: String,
                                description: String,
                                shortDescription: String,
                                icon: String,
                                element:PolicyElementModel)

object FragmentType extends Enumeration {
  type `type` = Value
  val input = Value("input")
  val output = Value("output")
  val AllowedTypes = Seq(input, output)
}
