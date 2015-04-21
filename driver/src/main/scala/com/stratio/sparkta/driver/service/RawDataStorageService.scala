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
    if (null == event.rawData)
      event.keyMap.map(e => e._2).toSeq.toString
    else
      event.rawData.toString
  }

  def save(raw: DStream[Event]) = {
    raw.map(event => RawEvent(System.currentTimeMillis().toString, extractRawDataFromEvent(event))).foreachRDD(_.toDF()
      .save(path, SaveMode.Append))
  }
}
