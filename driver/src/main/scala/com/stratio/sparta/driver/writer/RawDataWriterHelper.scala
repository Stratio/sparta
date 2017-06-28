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

import com.stratio.sparta.driver.factory.SparkContextFactory
import com.stratio.sparta.driver.step.RawData
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.sdk.utils.AggregationTime
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType, TimestampType}
import org.apache.spark.streaming.dstream.DStream


object RawDataWriterHelper {

  def writeRawData(rawData: RawData, outputs: Seq[Output], input: DStream[Row]): Unit = {
    val RawSchema = StructType(Seq(
      StructField(rawData.timeField, TimestampType, nullable = false),
      StructField(rawData.dataField, StringType, nullable = true)))
    val eventTime = AggregationTime.millisToTimeStamp(System.currentTimeMillis())

    input.map(row => Row.merge(Row(eventTime), row))
      .foreachRDD(rdd => {
        if (!rdd.isEmpty()) {
          val rawDataFrame = SparkContextFactory.xdSessionInstance.createDataFrame(rdd, RawSchema)

          WriterHelper.write(rawDataFrame, rawData.writerOptions, Map.empty[String, String], outputs)
        }
      })
  }
}
