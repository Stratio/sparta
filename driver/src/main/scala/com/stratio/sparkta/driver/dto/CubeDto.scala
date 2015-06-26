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

package com.stratio.sparkta.driver.dto

import com.stratio.sparkta.sdk.DimensionType

case class CubeDto(name: String,
                   dimensions: Seq[DimensionDto],
                   operators: Seq[OperatorDto],
                   multiplexer: String = CubeDto.Multiplexer)

case object CubeDto {

  val Multiplexer = "false"
}

case class DimensionDto(name: String,
                        field: String,
                        precision: String = DimensionType.IdentityName,
                       `type`: String = DimensionType.DefaultDimensionClass,
                        configuration: Option[Map[String, String]])
