/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.workflow.step

import java.io.Serializable

import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, OutputWriterOptions}
import com.stratio.sparta.core.properties.CustomProperties
import org.apache.spark.sql.catalyst.parser.LegacyTypeStringParser
import org.apache.spark.sql.types.{IntegerType, _}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._

import scala.util.Try

trait GraphStep extends CustomProperties {

  /* GLOBAL VARIABLES */
  val name: String
  val outputOptions: OutputOptions

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
  lazy val priority: Int = Try {
    propertiesWithCustom.asInstanceOf[Map[String, String]].getOrElse("priority", "0").toInt
  }.getOrElse(0)

  /* METHODS TO IMPLEMENT */

  def setUp(options: Map[String, String] = Map.empty[String, String]): Unit = {}

  def cleanUp(options: Map[String, String] = Map.empty[String, String]): Unit = {}

  def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations =
    ErrorValidations(valid = true, messages = Seq.empty)


  /* METHODS IMPLEMENTED */

  def schemaFromString(raw: String): DataType =
    Try(DataType.fromJson(raw)).getOrElse(LegacyTypeStringParser.parse(raw))


  def lineageProperties(): Map[String, String] = Map.empty

  def lineageCatalogProperties(): Map[String, Seq[String]] = Map.empty

  def getOutputWriterOptions(outputStepName: String): Option[OutputWriterOptions] =
    outputOptions.outputWriterOptions.find(_.outputStepName == outputStepName)
      .orElse(outputOptions.outputWriterOptions.find(_.outputStepName == OutputWriterOptions.OutputStepNameNA))

  def outputTableName(outputStepName : String): String =
    getOutputWriterOptions(outputStepName).map(_.tableName).notBlank.getOrElse(name)

  def outputDiscardTableName(outputStepName : String): Option[String] =
    getOutputWriterOptions(outputStepName).flatMap(_.discardTableName).notBlank

  def outputErrorTableName: String =
    outputOptions.outputWriterOptions.headOption.map(_.errorTableName).notBlank.getOrElse(name)

}

object GraphStep {
  val SparkSubmitConfMethod = "getSparkSubmitConfiguration"
  val SparkConfMethod = "getSparkConfiguration"
  val FailedKey = "Failed"
}
