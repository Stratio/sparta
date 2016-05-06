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

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.ValidatingPropertyMap._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

abstract class OperatorEntityCount(name: String,
                                   schema: StructType,
                                   properties: Map[String, JSerializable])
  extends Operator(name, schema, properties) {

  val split = if (properties.contains("split")) Some(properties.getString("split")) else None

  val replaceRegex =
    if (properties.contains("replaceRegex")) Some(properties.getString("replaceRegex")) else None

  override def processMap(inputFieldsValues: Row): Option[Seq[String]] = {
    if (inputField.isDefined && schema.fieldNames.contains(inputField.get))
      applyFilters(inputFieldsValues).flatMap(filteredFields => filteredFields.get(inputField.get).map(applySplitters))
    else None
  }

  private def applySplitters(value: Any): Seq[String] = {
    val replacedValue = applyReplaceRegex(value.toString)
    if (split.isDefined) replacedValue.split(split.get) else Seq(replacedValue)
  }

  private def applyReplaceRegex(value: String): String =
    replaceRegex match {
      case Some(regex) => value.replaceAll(regex, "")
      case None => value
    }
}
