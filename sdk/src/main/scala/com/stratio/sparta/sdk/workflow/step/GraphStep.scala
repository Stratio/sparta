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

import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import com.stratio.sparta.sdk.properties.CustomProperties
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.WhenError
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, _}

import scala.util.{Failure, Success, Try}

trait GraphStep extends CustomProperties {

  /* GLOBAL VARIABLES */

  lazy val customKey = "transformationOptions"
  lazy val customPropertyKey = "transformationOptionsKey"
  lazy val customPropertyValue = "transformationOptionsValue"
  lazy val propertiesWithCustom: Map[String, Serializable] = properties ++ getCustomProperties
  lazy val whenErrorDo: WhenError = Try(WhenError.withName(propertiesWithCustom.getString("whenError")))
    .getOrElse(WhenError.Error)
  lazy val sparkTypes = Map(
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
  lazy val outputSchema: StructType = getOutputSchema
  lazy val outputSchemaMap: Map[String, DataType] = outputSchema.fields
    .map(field => field.name -> field.dataType)
    .toMap

  /* METHODS TO IMPLEMENT */

  def getOutputSchema: StructType

  def setUp(options: Map[String, String] = Map.empty[String, String]): Unit = {}

  def cleanUp(options: Map[String, String] = Map.empty[String, String]): Unit = {}

  /* METHODS IMPLEMENTED */

  /**
   * Default parsing function to apply inside the transform function.
   *
   * By default make one casting of the input fields based on the output fields. It's mandatory that the input
   * schema fields and the output fields have the same name.
   *
   * @param row    The data to parse
   * @param schema The schema of the data
   * @return One or more rows that the parsing function generates
   */
  def parseWithSchema(row: Row, schema: StructType): Seq[Row] = {
    returnData(Try {
      outputSchema.map { outputField =>
        Try {
          schema.find(_.name == outputField.name)
            .getOrElse(throw new IllegalStateException(
              s"Output field: ${outputField.name} not found in the schema: $schema"))
            .dataType
        } match {
          case Success(inputSchemaType) =>
            Try(row.get(schema.fieldIndex(outputField.name))) match {
              case Success(dataRow) =>
                if (inputSchemaType == outputField.dataType)
                  dataRow
                else castingToOutputSchema(outputField, dataRow)
              case Failure(e) =>
                returnWhenError(new IllegalStateException(
                  s"Impossible to find outputField: $outputField in the schema $schema", e))
            }
          case Failure(e: Exception) =>
            returnWhenError(e)
        }
      }
    })
  }

  /**
   * Compare schema fields: InputSchema with outputSchema.
   *
   * @param inputSchema The input schema to compare
   * @return If the schemas are equals
   */
  def compareToOutputSchema(inputSchema: StructType): Boolean = {
    if (inputSchema == outputSchema)
      true
    else {
      inputSchema.fields.forall(inputField =>
        outputSchemaMap.get(inputField.name) match {
          case Some(dataType) => dataType == inputField.dataType
          case None => false
        }
      )
    }
  }

  //scalastyle:off
  def returnWhenError(exception: Exception): Null =
    whenErrorDo match {
      case WhenError.Null => null
      case _ => throw exception
    }

  //scalastyle:on

  def castingToOutputSchema(outSchema: StructField, inputValue: Any): Any =
    Try {
      TypeOp.castingToSchemaType(outSchema.dataType, inputValue.asInstanceOf[Any])
    } match {
      case Success(result) => result
      case Failure(e) => returnWhenError(new IllegalStateException(
        s"Error casting to output type the value: ${inputValue.toString}", e))
    }

  def returnData(newData: Try[_]): Seq[Row] =
    newData match {
      case Success(data: Seq[_]) => Seq(Row.fromSeq(data))
      case Success(data: Row) => Seq(data)
      case Success(_) => whenErrorDo match {
        case WhenError.Discard => Seq.empty[Row]
        case _ => throw new IllegalStateException("Invalid new data in step")
      }
      case Failure(e) => whenErrorDo match {
        case WhenError.Discard => Seq.empty[Row]
        case _ => throw e
      }
    }
}

object GraphStep {

  val SparkSubmitConfMethod = "getSparkSubmitConfiguration"
  val SparkConfMethod = "getSparkConfiguration"
}
