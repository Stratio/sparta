/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
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

package com.stratio.sparkta.driver.service

import java.sql.Timestamp

import com.stratio.sparkta.sdk.{DateOperations, Event, Input}
import org.apache.spark.sql._
import org.apache.spark.streaming.dstream.DStream

/**
 * Saves the raw data from a stream in Parquet.
 * @author arincon
 */
class RawDataStorageService(sc: SQLContext, path: String) extends Serializable {

  import sc.implicits._

  case class RawEvent(timeStamp: Timestamp, data: String)

  /**
   * From an event, it tries to parse the raw data in this order:
   * it looks if exists Raw field.
   * it looks if exists RawDataKey field.
   * It compose all values from the event separated with ###
   * @param event to parse
   * @return the raw data as a string.
   */
  def extractRawFromEvent(event: Event): String =
    event.rawData.getOrElse(
      if (event.keyMap.size == 1 && event.keyMap.head._1.equals(Input.RawDataKey))
        event.keyMap.head._2.toString
      else event.keyMap.values.mkString("###")
    ).toString

  /**
   * For each stream, it tries to extract and save the raw data.
   * @param raw with the original stream.
   * @return the original stream.
   */
  def save(raw: DStream[Event]): DStream[Event] = {
    raw.map(event => RawEvent(DateOperations.millisToTimeStamp(System.currentTimeMillis()), extractRawFromEvent(event)))
      .foreachRDD(rdd => rdd.toDF()
        .write
        .format("parquet")
        //FIXME when .partitionBy is stable in Spark we can activate this line
        //.partitionBy("timeStamp")
        .mode(SaveMode.Append)
        .save(s"$path"))
    raw
  }
}
