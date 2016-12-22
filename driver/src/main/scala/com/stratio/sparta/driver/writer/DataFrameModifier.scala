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

package com.stratio.sparta.driver.writer

import com.stratio.sparta.sdk.pipeline.autoCalculations.AutoCalculatedField
import com.stratio.sparta.sdk.pipeline.output.Output
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.{Column, DataFrame}

trait DataFrameModifier {

  def applyAutoCalculateFields(dataFrame: DataFrame,
                               autoCalculateFields: Seq[AutoCalculatedField],
                               auxSchema: StructType): DataFrame =
    autoCalculateFields.headOption match {
      case Some(firstAutoCalculate) =>
        applyAutoCalculateFields(
          addColumnToDataFrame(dataFrame, firstAutoCalculate, auxSchema), autoCalculateFields.drop(1), auxSchema)
      case None =>
        dataFrame
    }

  private def addColumnToDataFrame(dataFrame: DataFrame,
                                   autoCalculateField: AutoCalculatedField,
                                   auxSchema: StructType): DataFrame = {
    (autoCalculateField.fromNotNullFields,
      autoCalculateField.fromPkFields,
      autoCalculateField.fromFields,
      autoCalculateField.fromFixedValue) match {
      case (Some(fromNotNullFields), _, _, _) =>
        val fields = fieldsWithAuxMetadata(dataFrame.schema.fields, auxSchema.fields).flatMap(field =>
          if (!field.nullable) Some(col(field.name)) else None).toSeq
        addField(fromNotNullFields.field.name, fromNotNullFields.field.outputType, dataFrame, fields)
      case (None, Some(fromPkFields), _, _) =>
        val fields = fieldsWithAuxMetadata(dataFrame.schema.fields, auxSchema.fields).flatMap(field =>
          if (field.metadata.contains(Output.PrimaryKeyMetadataKey)) Some(col(field.name)) else None).toSeq
        addField(fromPkFields.field.name, fromPkFields.field.outputType, dataFrame, fields)
      case (None, None, Some(fromFields), _) =>
        val fields = autoCalculateField.fromFields.get.fromFields.map(field => col(field))
        addField(fromFields.field.name, fromFields.field.outputType, dataFrame, fields)
      case (None, None, None, Some(fromFixedValue)) =>
        addLiteral(fromFixedValue.field.name, fromFixedValue.field.outputType, dataFrame, fromFixedValue.value)
      case _ => dataFrame
    }
  }

  private def addField(name: String, outputType: String, dataFrame: DataFrame, fields: Seq[Column]): DataFrame =
    outputType match {
      case "string" => dataFrame.withColumn(name, concat_ws(Output.Separator, fields: _*))
      case "array" => dataFrame.withColumn(name, array(fields: _*))
      case "map" => dataFrame.withColumn(name, struct(fields: _*))
      case _ => dataFrame
    }

  private def addLiteral(name: String, outputType: String, dataFrame: DataFrame, literal: String): DataFrame =
    outputType match {
      case "string" => dataFrame.withColumn(name, lit(literal))
      case "array" => dataFrame.withColumn(name, array(lit(literal)))
      case "map" => dataFrame.withColumn(name, struct(lit(literal)))
      case _ => dataFrame
    }

  private def fieldsWithAuxMetadata(dataFrameFields: Array[StructField], auxFields: Array[StructField]) =
    dataFrameFields.map(field => {
      auxFields.find(auxField => auxField.name == field.name) match {
        case Some(auxFounded) => field.copy(metadata = auxFounded.metadata)
        case None => field
      }
    })
}
