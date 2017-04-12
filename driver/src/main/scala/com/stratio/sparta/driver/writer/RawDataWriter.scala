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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.factory.SparkContextFactory
import com.stratio.sparta.driver.step.RawData
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.sdk.utils.AggregationTime
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType, TimestampType}
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Success, Try}


case class RawDataWriter(rawData: RawData, outputs: Seq[Output]) extends SLF4JLogging {

  def write(input: DStream[Row]): Unit = {
    val RawSchema = StructType(Seq(
      StructField(rawData.timeField, TimestampType, nullable = false),
      StructField(rawData.dataField, StringType, nullable = true)))
    val saveOptions = Map(Output.TableNameKey -> rawData.rawDataStorageWriterOptions.tableName) ++
      rawData.rawDataStorageWriterOptions.partitionBy.fold(Map.empty[String, String]) { partition =>
        Map(Output.PartitionByKey -> partition)
      }
    val eventTime = AggregationTime.millisToTimeStamp(System.currentTimeMillis())

    input.map(row => Row.merge(Row(eventTime), row))
      .foreachRDD(rdd => {
        if (!rdd.isEmpty()) {
          val rawDataFrame = SparkContextFactory.sparkSessionInstance.createDataFrame(rdd, RawSchema)

          rawData.rawDataStorageWriterOptions.outputs.foreach(outputName =>
            outputs.find(output => output.name == outputName) match {
              case Some(outputWriter) => Try {
                outputWriter.save(rawDataFrame, SaveModeEnum.Append, saveOptions)
              } match {
                case Success(_) =>
                  log.debug(s"Raw data stored with timestamp $eventTime")
                case Failure(e) =>
                  log.error(s"Something goes wrong storing raw data." +
                    s" Table: ${rawData.rawDataStorageWriterOptions.tableName}")
                  log.error(s"Schema. ${rawDataFrame.schema}")
                  log.error(s"Head element. ${rawDataFrame.head}")
                  log.error(s"Error message : ${e.getMessage}")
              }
              case None => log.error(s"The output ($outputName) in the raw data, not match in the outputs")
            })
        }
      })
  }
}
