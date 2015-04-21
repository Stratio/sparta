
/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.sdk

import java.io.Serializable

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext, Row}
import org.apache.spark.streaming.dstream.DStream
import org.joda.time.DateTime

abstract class Output(keyName :String, properties: Map[String, Serializable],
                      sqlContext : SQLContext,
                      operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]])
                      extends Parameterizable(properties) with Multiplexer {

  if (operationTypes.isEmpty) {
    throw new NullPointerException("operationTypes")
  }

  /* TODO NPE because access to supportedWriteOps
  if(operationTypes.isDefined) {
    operationTypes.get.value.values.map(_._1).toSet.diff(supportedWriteOps.toSet).toSeq match {
      case s if s.size == 0 =>
      case badWriteOps =>
        throw new Exception(s"The following write ops are not supported by this output: ${badWriteOps.mkString(", ")}")
    }
  }*/

  def supportedWriteOps : Seq[WriteOp]

  def multiplexer : Boolean

  def timeBucket : String

  def granularity : String

  def persist(streams: Seq[DStream[UpdateMetricOperation]])
             (implicit bcSchema : Option[Broadcast[Seq[TableSchema]]] = None) : Unit = {
    if (bcSchema.isDefined) {
      streams.foreach(stream => doPersist(stream, bcSchema))
    } else streams.foreach(stream => persistMetricOperation(stream))
  }

  protected def persistMetricOperation(stream: DStream[UpdateMetricOperation]) : Unit = {
    getStreamsFromOptions(stream, multiplexer, timeBucket)
      .foreachRDD(rdd => rdd.foreachPartition(ops => upsert(ops)))
  }

  protected def persistDataFrame(stream: DStream[UpdateMetricOperation],
                                 bcSchema : Broadcast[Seq[TableSchema]]) : Unit = {
    stream.map(updateMetricOp => updateMetricOp.toKeyRow).foreachRDD(rdd => {
      bcSchema.value.filter(tschema => (tschema.operatorName == keyName))
        .foreach(tschemaFiltered => {
          val rddRow: RDD[Row] = Output.extractRow(rdd.filter(_._1.get == tschemaFiltered.tableName))
          upsert(sqlContext.createDataFrame(rddRow, tschemaFiltered.schema))
        })
    })
  }

  protected def doPersist(stream: DStream[UpdateMetricOperation],
                          bcSchema : Option[Broadcast[Seq[TableSchema]]]) : Unit = {
    if(bcSchema.isDefined) {
      persistDataFrame(getStreamsFromOptions(stream, multiplexer, timeBucket), bcSchema.get)
    } else {
      persistMetricOperation(stream)
    }
  }

  def upsert(dataFrame : DataFrame): Unit = {}

  def upsert(metricOperations: Iterator[UpdateMetricOperation]): Unit = {}

  def getEventTime(metricOp : UpdateMetricOperation) : Option[Serializable] = {
    if(timeBucket.isEmpty){
      None
    } else {
      val metricOpFiltered = metricOp.rollupKey.filter(dimVal => timeBucket == dimVal.bucketType.id)
      if (metricOpFiltered.size > 0){
        Some(metricOpFiltered.last.value)
      } else if(granularity.isEmpty) None else Some(Output.dateFromGranularity(DateTime.now(), granularity))
    }
  }
}

object Output {

  final val SEPARATOR = "_"

  def dateFromGranularity(value: DateTime, granularity : String): DateTime = {
      val secondsDate = new DateTime(value).withMillisOfSecond(0)
      val minutesDate = secondsDate.withSecondOfMinute(0)
      val hourDate = minutesDate.withMinuteOfHour(0)
      val dayDate = hourDate.withHourOfDay(0)
      val monthDate = dayDate.withDayOfMonth(1)
      val yearDate = monthDate.withMonthOfYear(1)

      granularity.toLowerCase match {
        case "minute" => minutesDate
        case "hour" => hourDate
        case "day" => dayDate
        case "month" => monthDate
        case "year" => yearDate
        case _ => secondsDate
      }
  }

  def genericRowSchema (rdd : RDD[(Option[String], Row)]) : (Option[String], RDD[Row]) = {
    val keySchema: Array[String] = rdd.map(rowType => rowType._1.get.split(SEPARATOR))
      .reduce((a, b) => if(a.length > b.length) a else b)
    (Some(keySchema.mkString(SEPARATOR)), extractRow(rdd))
  }

  def extractRow (rdd : RDD[(Option[String], Row)]) : RDD[Row] = rdd.map(rowType => rowType._2)
}
