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

package com.stratio.sparkta.sdk

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.types._

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import org.apache.spark.Logging
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext, Row}
import org.apache.spark.streaming.dstream.DStream
import org.joda.time.DateTime

abstract class Output(keyName: String,
                      properties: Map[String, JSerializable],
                      sqlContext: SQLContext,
                      operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                      bcSchema: Option[Broadcast[Seq[TableSchema]]])
  extends Parameterizable(properties) with Multiplexer with Logging {

  if (operationTypes.isEmpty) {
    log.info("Operation types is empty, you don't have aggregations defined in your policy.")
  }

  def supportedWriteOps: Seq[WriteOp]

  def multiplexer: Boolean

  def timeBucket: Option[String]

  def granularity: Option[String]

  def persist(streams: Seq[DStream[UpdateMetricOperation]]): Unit = {
    if (bcSchema.isDefined) {
      streams.foreach(stream => doPersist(stream))
    } else streams.foreach(stream => persistMetricOperation(stream))
  }

  protected def persistMetricOperation(stream: DStream[UpdateMetricOperation]): Unit = {
    getStreamsFromOptions(stream, multiplexer, timeBucket)
      .foreachRDD(rdd => rdd.foreachPartition(ops => upsert(ops)))
  }

  protected def persistDataFrame(stream: DStream[UpdateMetricOperation]): Unit = {
    stream.map(updateMetricOp =>
      updateMetricOp.toKeyRow(timeBucket, Output.getTimeFromGranularity(timeBucket, granularity))).foreachRDD(rdd => {
      bcSchema.get.value.filter(tschema => (tschema.outputName == keyName)).foreach(tschemaFiltered => {
        val tableSchemaTime = Output.getTableSchemaTime(tschemaFiltered, timeBucket, granularity)
        val rddRow: RDD[Row] = Output.extractRow(rdd.filter(_._1.get == tableSchemaTime.tableName))
        upsert(sqlContext.createDataFrame(rddRow, tableSchemaTime.schema), tableSchemaTime.tableName)
      })
    })
  }

  protected def doPersist(stream: DStream[UpdateMetricOperation]): Unit = {
    if (bcSchema.isDefined) {
      persistDataFrame(getStreamsFromOptions(stream, multiplexer, timeBucket))
    } else {
      persistMetricOperation(stream)
    }
  }

  def upsert(dataFrame: DataFrame, tableName: String): Unit = {}

  def upsert(metricOperations: Iterator[UpdateMetricOperation]): Unit = {}

  protected def getTime(metricOp: UpdateMetricOperation): Option[JSerializable] =
    timeBucket match {
      case None => None
      case Some(bucket) => {
        val metricOpFiltered = metricOp.rollupKey.filter(dimVal => bucket == dimVal.bucketType.id)
        if (metricOpFiltered.size > 0) {
          Some(metricOpFiltered.last.value)
        } else if (granularity.isEmpty) None else Some(Output.dateFromGranularity(DateTime.now(), granularity.get))
      }
    }

  protected def filterSchemaByKeyAndField(tSchemas: Seq[TableSchema], field: Option[String]): Seq[TableSchema] =
    tSchemas.filter(schemaFilter => schemaFilter.outputName == keyName &&
      field.forall(schemaFilter.schema.fieldNames.contains(_) &&
        schemaFilter.schema.filter(!_.nullable).length > 1))

  protected def checkOperationTypes: Boolean = {
    if (operationTypes.isDefined) {
      operationTypes.get.value.values.map(_._1).toSet.diff(supportedWriteOps.toSet).toSeq match {
        case s if s.size == 0 => true
        case badWriteOps => {
          log.info(s"The following write operators are not supported by this output: ${badWriteOps.mkString(", ")}")
          false
        }
      }
    } else false
  }
}

object Output {

  final val SEPARATOR = "_"

  def getTimeFromGranularity(timeBucket: Option[String], granularity: Option[String]): Option[DateTime] =
    timeBucket match {
      case None => None
      case Some(_) => granularity.flatMap(value => Some(dateFromGranularity(DateTime.now(), value)))
    }

  def dateFromGranularity(value: DateTime, granularity: String): DateTime = {
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

  def getTableSchemaTime(tbSchema: TableSchema,
                         timeBucket: Option[String],
                         granularity : Option[String]): TableSchema = {
    if(timeBucket.isDefined && granularity.isDefined && !tbSchema.schema.fieldNames.contains(timeBucket.get)){
      new TableSchema(tbSchema.outputName,
        tbSchema.tableName + SEPARATOR + timeBucket.get,
        StructType(tbSchema.schema.fields.toSeq ++ Seq(defaultTimeField(timeBucket.get))))
    } else tbSchema
  }

  def defaultTimeField(fieldName: String) : StructField = {
    StructField(fieldName, TimestampType, false)
  }

  def defaultStringField(fieldName: String) : StructField = {
    StructField(fieldName, StringType, false)
  }

  def genericRowSchema(rdd: RDD[(Option[String], Row)]): (Option[String], RDD[Row]) = {
    val keySchema: Array[String] = rdd.map(rowType => rowType._1.get.split(SEPARATOR))
      .reduce((a, b) => if (a.length > b.length) a else b)
    (Some(keySchema.mkString(SEPARATOR)), extractRow(rdd))
  }

  def extractRow(rdd: RDD[(Option[String], Row)]): RDD[Row] = rdd.map(rowType => rowType._2)
}
