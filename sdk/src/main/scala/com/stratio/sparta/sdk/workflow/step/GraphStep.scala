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

package com.stratio.sparta.sdk.workflow.step

import java.io.Serializable

import com.stratio.sparta.sdk.properties.CustomProperties
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.WhenError
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import org.apache.spark.sql.types.{IntegerType, _}

import scala.util.Try


trait GraphStep extends CustomProperties {

  val customKey = "transformationOptions"
  val customPropertyKey = "transformationOptionsKey"
  val customPropertyValue = "transformationOptionsValue"
  val propertiesWithCustom: Map[String, Serializable] = properties ++ getCustomProperties
  val whenErrorDo : WhenError = Try(WhenError.withName(propertiesWithCustom.getString("whenError")))
    .getOrElse(WhenError.Error)
  val sparkTypes = Map(
    "long" -> LongType,
    "double" -> DoubleType,
    "int" -> IntegerType,
    "integer" -> IntegerType,
    "bool" -> BooleanType,
    "boolean" -> BooleanType,
    "date" -> DateType,
    "datetime" -> TimestampType,
    "timestamp" -> TimestampType,
    "string" -> StringType,
    "arraydouble" -> ArrayType(DoubleType),
    "arraystring" -> ArrayType(StringType),
    "arraymapstringstring" -> ArrayType(MapType(StringType, StringType)),
    "mapstringlong" -> MapType(StringType, LongType),
    "mapstringdouble" -> MapType(StringType, DoubleType),
    "mapstringint" -> MapType(StringType, IntegerType),
    "mapstringstring" -> MapType(StringType, StringType),
    "text" -> StringType
  )

  def getOutputSchema: StructType

  def setUp(options: Map[String, String] = Map.empty[String, String]): Unit = {}

  def cleanUp(options: Map[String, String] = Map.empty[String, String]): Unit = {}

}

object GraphStep {

  val SparkSubmitConfMethod = "getSparkSubmitConfiguration"
  val SparkConfMethod = "getSparkConfiguration"

}
