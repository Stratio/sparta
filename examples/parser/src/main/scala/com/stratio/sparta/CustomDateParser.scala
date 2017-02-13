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
package com.stratio.sparta.plugin.transformation.json

import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import java.io.{Serializable => JSerializable}

import org.joda.time.DateTime

import scala.util.Try

class CustomDateParser(order: Integer,
                       inputField: Option[String],
                       outputFields: Seq[String],
                       schema: StructType,
                       properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) {

  val dateField = propertiesWithCustom.getString("dateField", "date")
  val hourField = propertiesWithCustom.getString("hourField", "hourRounded")
  val dayField = propertiesWithCustom.getString("dayField", "dayRounded")
  val weekField = propertiesWithCustom.getString("weekField", "week")
  val hourDateField = propertiesWithCustom.getString("hourDateField", "hourDate")
  val yearPrefix = propertiesWithCustom.getString("yearPrefix", "20")

  //scalastyle:off
  override def parse(row: Row): Seq[Row] = {
    val inputValue = Try(row.get(schema.fieldIndex(dateField))).toOption
    val newData = Try {
      inputValue match {
        case Some(value) =>
          val valueStr = {
            value match {
              case valueCast: Array[Byte] => new Predef.String(valueCast)
              case valueCast: String => valueCast
              case _ => value.toString
            }
          }

          val valuesParsed = Map(
            hourField -> getDateWithBeginYear(valueStr).concat(valueStr.substring(4, valueStr.length)),
            hourDateField -> getHourDate(valueStr),
            dayField -> getDateWithBeginYear(valueStr).concat(valueStr.substring(4, valueStr.length - 2)),
            weekField -> getWeek(valueStr)
          )

          outputFields.map { outputField =>
            val outputSchemaValid = outputFieldsSchema.find(field => field.name == outputField)
            outputSchemaValid match {
              case Some(outSchema) =>
                valuesParsed.get(outSchema.name) match {
                  case Some(valueParsed) =>
                    parseToOutputType(outSchema, valueParsed)
                  case None =>
                    returnWhenError(new IllegalStateException(
                      s"The values parsed don't contain the schema field: ${outSchema.name}"))
                }
              case None =>
                returnWhenError(new IllegalStateException(
                  s"Impossible to parse outputField: $outputField in the schema"))
            }
          }
        case None =>
          returnWhenError(new IllegalStateException(s"The input value is null or empty"))
      }
    }

    returnData(newData, removeInputField(row))
  }

  def getDateWithBeginYear(inputDate: String): String =
    inputDate.substring(0, inputDate.length - 4).concat(yearPrefix)

  def getHourDate(inputDate: String): Long = {
    val day = inputDate.substring(0, 2).toInt
    val month = inputDate.substring(2, 4).toInt
    val year = yearPrefix.concat(inputDate.substring(4, 6)).toInt
    val hour = inputDate.substring(6, inputDate.length).toInt
    val date = new DateTime(year, month, day, hour, 0)

    date.getMillis
  }

  def getWeek(inputDate: String): Int = {
    val day = inputDate.substring(0, 2).toInt
    val month = inputDate.substring(2, 4).toInt
    val year = yearPrefix.concat(inputDate.substring(4, 6)).toInt
    val date = new DateTime(year, month, day, 0, 0)

    date.getWeekOfWeekyear
  }

  //scalastyle:on
}

