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
package com.stratio.sparta.plugin.output.fileSystem

import java.io.{Serializable => JSerializable}
import java.text.SimpleDateFormat
import java.util.Date

import com.stratio.sparta.sdk.pipeline.output.{Output, OutputFormatEnum, SaveModeEnum}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

import scala.collection.mutable.ListBuffer


/**
  * This output sends all AggregateOperations or DataFrames data  to a directory stored in
  * HDFS, Amazon S3, Azure, etc.
  *
  * @param name
  * @param properties
  */
class FileSystemOutput(name: String, properties: Map[String, JSerializable]) extends Output(name, properties) {

  val DateFormat = "YYYY-MM-DD'-'HH.mm.ss"
  val FieldName = "extractedData"

  val path = properties.getString("path")
  val outputFormat = OutputFormatEnum.withName(properties.getString("outputFormat", "json").toUpperCase)
  val fileWithDate = properties.getBoolean("fileWithDate")
  val dateFormat = properties.getString("dateFormat", DateFormat)
  val delimiter = properties.getString("delimiter", ",")
  val partitionUntil = PartitionLimit.withName(properties.getString("partitionUntil", "NONE"))

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {

    writeOutput(dataFrame)
  }

  def formatPath(currentDate: Date): String = {

    if (!fileWithDate) path
    else {

      if(partitionUntil == PartitionLimit.NONE)
        path + "/" + new SimpleDateFormat(partitionFormat(partitionUntil)).format(currentDate)

      else {
        val lastFormatter = dateFormat.substring(dateFormat.size - 2, dateFormat.size)
        val lastFormatValue = lastFormatter match {
          case "DD" | "DZ" => PartitionLimit.DD
          case _ => PartitionLimit.ss
        }

        val formattedDate = partitionUntil.id compare lastFormatValue.id match {
          case 0 => new SimpleDateFormat(partitionFormat(partitionUntil)).format(currentDate)
          case 1 => new SimpleDateFormat(partitionFormat(lastFormatValue)).format(currentDate)
          case -1 => {
            val usedDateValues = ListBuffer.empty[String]
            for (value <- PartitionLimit.values)
              if (value.id <= partitionUntil.id)
                usedDateValues += value.toString

            val filtered = dateFormat.filterNot(Set('-', '.', ''', 'T')).replace(usedDateValues.mkString, "")

            new SimpleDateFormat(partitionFormat(partitionUntil)).format(currentDate) + "/" +
              new SimpleDateFormat(filtered).format(currentDate)
          }
        }
        s"$path/$formattedDate"
      }
    }
  }

  def writeOutput(dataFrame: DataFrame): Unit = {

    val dateNow = new Date()
    if (outputFormat == OutputFormatEnum.JSON)
      dataFrame.write.json(formatPath(dateNow))
    else {
      val colSeq = dataFrame.schema.fields.flatMap(field => Some(col(field.name))).toSeq
      val df = dataFrame.withColumn(FieldName, concat_ws(delimiter, colSeq: _*)).select(FieldName)

      df.write.text(formatPath(dateNow))
    }
  }

  private def partitionFormat(inputFormat: PartitionLimit.Limit): String = {
    inputFormat match {
      case PartitionLimit.YYYY => "YYYY"
      case PartitionLimit.MM => "YYYY/MM"
      case PartitionLimit.DD => "YYYY/MM/DD"
      case PartitionLimit.HH => "YYYY/MM/DD/HH"
      case PartitionLimit.mm => "YYYY/MM/DD/HH/mm"
      case PartitionLimit.ss => "YYYY/MM/DD/HH/mm/ss"

      case _ => dateFormat
    }
  }
}

