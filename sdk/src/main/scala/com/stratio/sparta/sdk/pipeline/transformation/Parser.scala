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

package com.stratio.sparta.sdk.pipeline.transformation

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import com.stratio.sparta.sdk.properties.{CustomProperties, Parameterizable}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, StructType}

import scala.util.{Failure, Success, Try}

abstract class Parser(order: Integer,
                      inputField: Option[String],
                      outputFields: Seq[String],
                      schema: StructType,
                      properties: Map[String, JSerializable])
  extends Parameterizable(properties) with Ordered[Parser] with CustomProperties {

  val customKey = "transformationOptions"
  val customPropertyKey = "transformationOptionsKey"
  val customPropertyValue = "transformationOptionsValue"

  val outputFieldsSchema = schema.fields.filter(field => outputFields.contains(field.name))

  val inputFieldRemoved = Try(properties.getBoolean("removeInputField")).getOrElse(false)

  val inputFieldIndex = inputField match {
    case Some(field) => Try(schema.fieldIndex(field)).getOrElse(0)
    case None => 0
  }

  val whenErrorDo = Try(WhenError.withName(properties.getString("whenError")))
    .getOrElse(WhenError.Error)

  def parse(data: Row): Seq[Row]

  def getOrder: Integer = order

  def checkFields(keyMap: Map[String, JSerializable]): Map[String, JSerializable] =
    keyMap.flatMap(key => if (outputFields.contains(key._1)) Some(key) else None)

  def compare(that: Parser): Int = this.getOrder.compareTo(that.getOrder)

  //scalastyle:off
  def returnWhenError(exception: Exception): Null =
  whenErrorDo match {
    case WhenError.Null => null
    case _ => throw exception
  }

  //scalastyle:on

  def parseToOutputType(outSchema: StructField, inputValue: Any): Any =
    TypeOp.transformValueByTypeOp(outSchema.dataType, inputValue.asInstanceOf[Any])

  def returnData(newData: Try[Seq[_]], prevData: Seq[_]): Seq[Row] =
    newData match {
      case Success(data) => Seq(Row.fromSeq(prevData ++ data))
      case Failure(e) => whenErrorDo match {
        case WhenError.Discard => Seq.empty[Row]
        case _ => throw e
      }
    }

  def returnData(newData: Try[Row], prevData: Row): Seq[Row] =
    newData match {
      case Success(data) => Seq(Row.merge(prevData, data))
      case Failure(e) => whenErrorDo match {
        case WhenError.Discard => Seq.empty[Row]
        case _ => throw e
      }
    }

  def removeIndex(row: Seq[_], inputFieldIndex: Int): Seq[_] = if (row.size < inputFieldIndex) row
    else row.take(inputFieldIndex) ++ row.drop(inputFieldIndex + 1)

  def removeInputField(row: Row): Seq[_] = {
    if (inputFieldRemoved && inputField.isDefined)
      removeIndex(row.toSeq, inputFieldIndex)
    else
      row.toSeq
  }


}

object Parser {

  final val ClassSuffix = "Parser"
  final val DefaultOutputType = "string"
  final val TypesFromParserClass = Map("datetime" -> "timestamp")
}
