/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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


import org.apache.spark.sql._
import org.apache.spark.streaming.dstream.DStream

import com.stratio.sparkta.sdk.Event

/**
 * Created by arincon on 17/04/15.
 */
class RawDataStorageService(sc: SQLContext, path: String) extends Serializable {

  import sc.implicits._

  case class RawEvent(timeStamp: String, data: String)

  def extractRawDataFromEvent(event: Event) = {
    if ( event.rawData.isEmpty)
      event.keyMap.map(e => e._2).toSeq.toString
    else
      event.rawData.get.toString
  }

  def save(raw: DStream[Event]) = {
    raw.map(event => RawEvent(System.currentTimeMillis().toString, extractRawDataFromEvent(event))).foreachRDD(_.toDF()
      .save(path, SaveMode.Append))
  }
}
