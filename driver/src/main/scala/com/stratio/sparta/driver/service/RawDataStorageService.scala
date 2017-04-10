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
package com.stratio.sparta.driver.service

import com.stratio.sparta.driver.factory.SparkContextFactory
import com.stratio.sparta.sdk.utils.AggregationTime
import org.apache.spark.sql._
import org.apache.spark.sql.types.{StringType, StructField, StructType, TimestampType}
import org.apache.spark.streaming.dstream.DStream

object RawDataStorageService {

  val TimeStampField = "timeStamp"
  val DataField = "data"
  val RawSchema =
    StructType(Seq(StructField(TimeStampField, TimestampType, false), StructField(DataField, StringType)))

  def save(raw: DStream[Row], path: String): Unit = {
    val eventTime = AggregationTime.millisToTimeStamp(System.currentTimeMillis())
    raw.map(row => Row.merge(Row(eventTime), row))
      .foreachRDD(rdd => {
        if (!rdd.isEmpty()) {
          SparkContextFactory.sparkSessionInstance.createDataFrame(rdd, RawSchema)
            .write
            .format("parquet")
            .partitionBy(TimeStampField)
            .mode(SaveMode.Append)
            .save(s"$path")
        }
      }
      )
  }
}
