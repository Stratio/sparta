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

package com.stratio.sparta.sdk.pipeline.output

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.properties.{CustomProperties, Parameterizable}
import org.apache.spark.Logging
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, DataFrameWriter, SaveMode}

abstract class Output(val name: String, properties: Map[String, JSerializable])
  extends Parameterizable(properties) with SLF4JLogging with CustomProperties {

  val customKey = "saveOptions"
  val customPropertyKey = "saveOptionsKey"
  val customPropertyValue = "saveOptionsValue"
  val propertiesWithCustom = properties ++ getCustomProperties

  def setup(options: Map[String, String] = Map.empty[String, String]): Unit = {}

  def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit

  def supportedSaveModes: Seq[SaveModeEnum.Value] = SaveModeEnum.allSaveModes

  def validateSaveMode(saveMode: SaveModeEnum.Value): Unit = {
    if (!supportedSaveModes.contains(saveMode))
      log.info(s"Save mode $saveMode selected not supported by the output $name." +
        s" Using the default mode ${SaveModeEnum.Append}"
      )
  }
}

object Output extends SLF4JLogging {

  final val ClassSuffix = "Output"
  final val Separator = "_"
  final val FieldsSeparator = ","
  final val PrimaryKey = "primaryKey"
  final val TableNameKey = "tableName"
  final val PartitionByKey = "partitionBy"
  final val TimeDimensionKey = "timeDimension"
  final val MeasureMetadataKey = "measure"
  final val PrimaryKeyMetadataKey = "pk"

  def getSparkSaveMode(saveModeEnum: SaveModeEnum.Value): SaveMode =
    saveModeEnum match {
      case SaveModeEnum.Append => SaveMode.Append
      case SaveModeEnum.ErrorIfExists => SaveMode.ErrorIfExists
      case SaveModeEnum.Overwrite => SaveMode.Overwrite
      case SaveModeEnum.Ignore => SaveMode.Ignore
      case SaveModeEnum.Upsert => SaveMode.Append
      case _ =>
        log.warn(s"Save Mode $saveModeEnum not supported, using default save mode ${SaveModeEnum.Append}")
        SaveMode.Append
    }

  def getTimeFromOptions(options: Map[String, String]): Option[String] = options.get(TimeDimensionKey).notBlank

  def getPrimaryKeyOptions(options: Map[String, String]): Option[String] = options.get(PrimaryKey).notBlank

  def getTableNameFromOptions(options: Map[String, String]): String =
    options.getOrElse(TableNameKey, {
      log.error("Table name not defined")
      throw new NoSuchElementException("tableName not found in options")
    })

  def applyPartitionBy(options: Map[String, String],
                       dataFrame: DataFrameWriter,
                       schemaFields: Array[StructField]): DataFrameWriter = {

    options.get(PartitionByKey).notBlank.fold(dataFrame)(partitions => {
      val fieldsInDataFrame = schemaFields.map(field => field.name)
      val partitionFields = partitions.split(",")
      if (partitionFields.forall(field => fieldsInDataFrame.contains(field)))
        dataFrame.partitionBy(partitionFields: _*)
      else {
        log.warn(s"Impossible to execute partition by fields: $partitionFields because the dataFrame not contain all" +
          s" fields. The dataFrame only contains: ${fieldsInDataFrame.mkString(",")}")
        dataFrame
      }
    })
  }

  def defaultTimeStampField(fieldName: String, nullable: Boolean, metadata: Metadata = Metadata.empty): StructField =
    StructField(fieldName, TimestampType, nullable, metadata)

  def defaultDateField(fieldName: String, nullable: Boolean, metadata: Metadata = Metadata.empty): StructField =
    StructField(fieldName, DateType, nullable, metadata)

  def defaultStringField(fieldName: String, nullable: Boolean, metadata: Metadata = Metadata.empty): StructField =
    StructField(fieldName, StringType, nullable, metadata)

  def defaultGeoField(fieldName: String, nullable: Boolean, metadata: Metadata = Metadata.empty): StructField =
    StructField(fieldName, ArrayType(DoubleType), nullable, metadata)

  def defaultLongField(fieldName: String, nullable: Boolean, metadata: Metadata = Metadata.empty): StructField =
    StructField(fieldName, LongType, nullable, metadata)
}