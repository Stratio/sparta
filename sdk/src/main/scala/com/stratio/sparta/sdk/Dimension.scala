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
package com.stratio.sparta.sdk

case class Dimension(name: String, field: String, precisionKey: String, dimensionType: DimensionType)
  extends Ordered[Dimension] {

  val precision = dimensionType.precision(precisionKey)

  def getNamePrecision: String = precision.id match {
    case DimensionType.IdentityName => field
    case _ => precision.id
  }

  def compare(dimension: Dimension): Int = name compareTo dimension.name
}

object Dimension {

  final val FieldClassSuffix = "Field"
}