/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.sdk

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}

abstract class Parser(name: String,
                      order: Integer,
                      inputField: String,
                      outputFields: Seq[String],
                      schema: StructType,
                      properties: Map[String, JSerializable]) extends Parameterizable(properties) {

  def parse(data: Row): Row

  def getOrder: Integer = order

  def checkFields(keyMap: Map[String, JSerializable]) : Map[String, JSerializable] =
    keyMap.flatMap(key => if(outputFields.contains(key._1)) Some(key) else None)

}

object Parser {
  final val ClassSuffix = "Parser"
  final val DefaultOutputType = "string"
}
