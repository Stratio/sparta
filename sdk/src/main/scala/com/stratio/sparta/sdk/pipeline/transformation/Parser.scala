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
import com.stratio.sparta.sdk.properties.Parameterizable
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, StructType}

import scala.util.Try

abstract class Parser(order: Integer,
                      inputField: String,
                      outputFields: Seq[String],
                      schema: StructType,
                      properties: Map[String, JSerializable])
  extends Parameterizable(properties) with Ordered[Parser] {

  val outputFieldsSchema = schema.fields.filter(field => outputFields.contains(field.name))

  val inputFieldIndex = Try(schema.fieldIndex(inputField)).getOrElse(0)

  val errorWithNullInputs = Try(properties.getBoolean("errorWithNullValues")).getOrElse(true)

  def parse(data: Row, removeRaw: Boolean): Row

  def getOrder: Integer = order

  def checkFields(keyMap: Map[String, JSerializable]): Map[String, JSerializable] =
    keyMap.flatMap(key => if (outputFields.contains(key._1)) Some(key) else None)

  def compare(that: Parser): Int = this.getOrder.compareTo(that.getOrder)

  //scalastyle:off
  def returnNullValue(exception: Exception): Null =
    if (errorWithNullInputs) throw exception
    else null

  def parseToOutputType(outSchema: StructField, inputValue: Any): Any =
    TypeOp.transformValueByTypeOp(outSchema.dataType, inputValue.asInstanceOf[Any])

  //scalastyle:on
}

object Parser {

  final val ClassSuffix = "Parser"
  final val DefaultOutputType = "string"
  final val TypesFromParserClass = Map("datetime" -> "timestamp")
}
