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
import org.apache.spark.sql.catalyst.parser.LegacyTypeStringParser
import org.apache.spark.sql.types.{IntegerType, _}

import scala.util.Try

trait GraphStep extends CustomProperties {

  /* GLOBAL VARIABLES */

  lazy val customKey = "customOptions"
  lazy val customPropertyKey = "customOptionsKey"
  lazy val customPropertyValue = "customOptionsValue"
  lazy val propertiesWithCustom: Map[String, Serializable] = properties ++ getCustomProperties
  lazy val SparkTypes = Map(
    "long" -> LongType,
    "float" -> FloatType,
    "double" -> DoubleType,
    "integer" -> IntegerType,
    "boolean" -> BooleanType,
    "binary" -> BinaryType,
    "date" -> DateType,
    "timestamp" -> TimestampType,
    "string" -> StringType,
    "arraydouble" -> ArrayType(DoubleType),
    "arraystring" -> ArrayType(StringType),
    "arraylong" -> ArrayType(LongType),
    "arrayinteger" -> ArrayType(IntegerType),
    "arraymapstringstring" -> ArrayType(MapType(StringType, StringType)),
    "mapstringlong" -> MapType(StringType, LongType),
    "mapstringdouble" -> MapType(StringType, DoubleType),
    "mapstringinteger" -> MapType(StringType, IntegerType),
    "mapstringstring" -> MapType(StringType, StringType)
  )

  /* METHODS TO IMPLEMENT */

  def setUp(options: Map[String, String] = Map.empty[String, String]): Unit = {}

  def cleanUp(options: Map[String, String] = Map.empty[String, String]): Unit = {}

  def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations =
    ErrorValidations(valid = true, messages = Seq.empty)


  /* METHODS IMPLEMENTED */

  def schemaFromString(raw: String): DataType =
    Try(DataType.fromJson(raw)).getOrElse(LegacyTypeStringParser.parse(raw))


  def lineageProperties(): Map[String, String] = properties.asInstanceOf[Map[String,String]]

}

object GraphStep {

  val SparkSubmitConfMethod = "getSparkSubmitConfiguration"
  val SparkConfMethod = "getSparkConfiguration"
}
