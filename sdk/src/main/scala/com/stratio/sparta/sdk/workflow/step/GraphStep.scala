/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.workflow.step

import java.io.Serializable

import com.stratio.sparta.sdk.models.ErrorValidations
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
