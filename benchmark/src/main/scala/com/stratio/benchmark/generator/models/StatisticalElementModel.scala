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
package com.stratio.benchmark.generator.models

import java.util.Date

import scala.collection.mutable
import scala.io.Source
import java.io.File


case class StatisticalElementModel(id: Long,
                                   processingDelay: Long,
                                   schedulingDelay: Long,
                                   totalDelay: Long,
                                   processingTime: Long,
                                   completedBatches: Long,
                                   processedRecords: Long,
                                   receivedRecords: Long) {}

case class AvgStatisticalModel( id: Long,
                                processingDelay: Long,
                                schedulingDelay: Long,
                                totalDelay: Long,
                                processingTime: Long,
                                completedBatches: Long,
                                processedRecords: Long,
                                receivedRecords: Long) {
}

case object StatisticalElementModel {

  val PrefixFileName = Seq(
    "StreamingMetrics.streaming.lastCompletedBatch_processingDelay.csv",
    "StreamingMetrics.streaming.lastCompletedBatch_schedulingDelay.csv",
    "StreamingMetrics.streaming.lastCompletedBatch_totalDelay.csv",
    "StreamingMetrics.streaming.lastCompletedBatch_processingEndTime.csv",
    "StreamingMetrics.streaming.lastCompletedBatch_processingStartTime.csv",
    "StreamingMetrics.streaming.totalCompletedBatches.csv",
    "StreamingMetrics.streaming.totalProcessedRecords.csv",
    "StreamingMetrics.streaming.totalReceivedRecords.csv"
  )

  val SufixToSearch = "StreamingMetrics.streaming.lastCompletedBatch_processingDelay.csv"

  def parsePathToStatisticalElementModels(sparkPathMetrics: String): Seq[StatisticalElementModel] = {
    val fileNames =
      (new File(sparkPathMetrics)).list.filter(file => file.endsWith(SufixToSearch))

    val prefix = if(fileNames.size == 0) {
      throw new NoSuchElementException(s"No file metrics in the path $sparkPathMetrics")
    } else if(fileNames.size > 1) {
      throw new IllegalStateException(s"There are two files with the suffix $SufixToSearch")
    } else {
      fileNames(0).replace(SufixToSearch, "")
    }

    val joinedFiles: Seq[(Long, Seq[(Long, Long)])]  = PrefixFileName.flatMap(path => {
      Source.fromFile(s"$sparkPathMetrics/$prefix$path").getLines().toSeq.drop(1).map( element =>
        (element.split(",")(0).toLong -> element.split(",")(1).toLong)
      )
    }).groupBy(element => element._1).toList.sortBy(element => element._1)

    val result = for {
      (id, Seq(
      (_, processingDelay),
      (_, schedulingDelay),
      (_, totalDelay),
      (_, processingEndTime),
      (_, processingStartTime),
      (_, completedBatches),
      (_, processedRecords),
      (_, receivedRecords))) <- joinedFiles
    } yield
      StatisticalElementModel(
        id,
        processingDelay,
        schedulingDelay,
        totalDelay,
        processingEndTime - processingStartTime,
        completedBatches,
        processedRecords,
        receivedRecords
      )
    result
  }

  def parseTotals(statisticalElementModels: Seq[StatisticalElementModel]): AvgStatisticalModel = {
    val result = new mutable.HashMap[String, Long]()

      statisticalElementModels.map(element => {
        addElementToTotal("processingDelay", element.processingDelay, result)
        addElementToTotal("schedulingDelay", element.schedulingDelay, result)
        addElementToTotal("totalDelay", element.totalDelay, result)
        addElementToTotal("processingTime", element.processingTime, result)
        overwriteElementNoZero("completedBatches", element.completedBatches, result)
        overwriteElementNoZero("processedRecords", element.processedRecords, result)
        overwriteElementNoZero("receivedRecords", element.receivedRecords, result)
      })

    val avgPairs = (for {
      element <- result
    } yield(
        if(element._1 == "receivedRecords" || element._1 == "completedBatches" || element._1 == "processedRecords")
          (element._1, element._2) else (element._1, element._2 / statisticalElementModels.size))
      ).toMap

    AvgStatisticalModel(
      new Date().getTime,
      avgPairs.get("processingDelay").get,
      avgPairs.get("schedulingDelay").get,
      avgPairs.get("totalDelay").get,
      avgPairs.get("processingTime").get,
      avgPairs.get("completedBatches").get,
      avgPairs.get("processedRecords").get,
      avgPairs.get("receivedRecords").get
    )
  }

  def addElementToTotal(key: String, value: Long, result: mutable.HashMap[String, Long]): Unit = {
    if(result.contains(key)) {
      result.put(key, result.get(key).get + value)
    } else {
      result.put(key, value)
    }
  }

  def overwriteElementNoZero(key: String, value: Long, result: mutable.HashMap[String, Long]): Unit = {
    if(result.contains(key) && value != 0) {
      result.put(key, value)
    } else if(!result.contains(key)) {
      result.put(key, value)
    }
  }
}

