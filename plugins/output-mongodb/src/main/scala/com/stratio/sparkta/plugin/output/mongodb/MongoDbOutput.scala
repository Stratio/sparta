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
package com.stratio.sparkta.plugin.output.mongodb

import java.io.Serializable

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.stratio.sparkta.plugin.output.mongodb.dao.AbstractMongoDAO
import com.stratio.sparkta.sdk.WriteOp
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark.Logging
import org.apache.spark.streaming.dstream.DStream
import ValidatingPropertyMap._
import org.joda.time.DateTime
import com.mongodb.casbah.commons.conversions.scala._

import scala.util.Try

class MongoDbOutput(properties: Map[String, Serializable], schema : Map[String,WriteOp])
  extends Output(properties, schema) with AbstractMongoDAO with Multiplexer with Serializable with Logging {

  RegisterJodaTimeConversionHelpers()

  override val textIndexName = properties.getString("textIndex", "")

  override val language = properties.getString("language", "none")

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.Set, WriteOp.Max, WriteOp.Min)

  override val mongoClientUri = properties.getString("clientUri", "mongodb://localhost:27017")

  override val dbName = properties.getString("dbName", "sparkta")

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val eventTimeFieldName = properties.getString("timeFieldName", "eventTime")

  override val timeDimension = properties.getString("timeDimension", "")

  override val granularity = properties.getString("granularity", "minute")

  def combine(in: Seq[DimensionValue]): Seq[Seq[DimensionValue]] = {
    for {
      len <- 1 to in.length
      combinations <- in combinations len
    } yield combinations
  }

  override def multiplexStream(stream: DStream[UpdateMetricOperation]) : DStream[UpdateMetricOperation] = {
    if(multiplexer) {
      for {
        upMetricOp: UpdateMetricOperation <- stream
        comb: Seq[DimensionValue] <- combine(upMetricOp.rollupKey)
          .filter(dimVals =>
          (dimVals.size > 1) ||
            ((dimVals.size == 1) && (dimVals.last.bucketType != Bucketer.fulltext))
          )
        } yield UpdateMetricOperation(
        comb.sortWith((dim1,dim2) =>
          (dim1.dimension.name + dim1.bucketType.id) < (dim2.dimension.name + dim2.bucketType.id)),
        upMetricOp.aggregations)
    } else {
      stream
    }
  }

  override def multiplexStream(stream: DStream[UpdateMetricOperation],
                               fixedDimension : String) : DStream[UpdateMetricOperation] = {
    if(multiplexer){
      for {
        upMetricOp: UpdateMetricOperation <- stream
        fixedDim = upMetricOp.rollupKey.find(dimValue => dimValue.dimension.name == fixedDimension).get
        comb: Seq[DimensionValue] <- combine(upMetricOp.rollupKey)
          .filter(dimVals =>
          (dimVals.size > 1) ||
            ((dimVals.size == 1) &&
              (dimVals.last.bucketType != Bucketer.fulltext) &&
              (dimVals.last.dimension.name != fixedDim.dimension.name))
          )
          .map(seqDimVal => seqDimVal ++ Seq(fixedDim))
      } yield UpdateMetricOperation(
        comb.sortWith((dim1,dim2) =>
          (dim1.dimension.name + dim1.bucketType.id) < (dim2.dimension.name + dim2.bucketType.id)),
        upMetricOp.aggregations)
    } else {
      stream
    }
  }

  def getStreamFromOptions(stream : DStream[UpdateMetricOperation],
                           multiplexer : Boolean, timeDimension : String) : DStream[UpdateMetricOperation] = {
    multiplexer match {
        case false => stream
        case _ => timeDimension match {
          //TODO crear una DimensionValue con la fecha actual
          case "" => multiplexStream(stream)
          case _ => multiplexStream(stream,
            timeDimension)
        }
    }
  }

  override def persist(stream: DStream[UpdateMetricOperation]): Unit = {
    getStreamFromOptions(stream, multiplexer, timeDimension)
      .foreachRDD(rdd =>
        rdd.foreachPartition(ops =>
          upsert(ops)
        )
      )
  }

  //TODO remove from here
  override def dateFromGranularity(value: DateTime, granularity : String): DateTime = {
    val secondsDate = new DateTime(value).withMillisOfSecond(0)
    val minutesDate = secondsDate.withSecondOfMinute(0)
    val hourDate = minutesDate.withMinuteOfHour(0)
    val dayDate = hourDate.withHourOfDay(0)
    val monthDate = dayDate.withDayOfMonth(1)
    val yearDate = monthDate.withMonthOfYear(1)

    granularity match {
      case "second" => secondsDate
      case "minute" => minutesDate
      case "hour" => hourDate
      case "day" => dayDate
      case "month" => monthDate
      case "year" => yearDate
    }
  }

  override def persist(streams: Seq[DStream[UpdateMetricOperation]]): Unit = {
    streams.foreach(persist)
  }

  def upsert(metricOperations: Iterator[UpdateMetricOperation]): Unit = {
    metricOperations.toList.groupBy(metricOp => metricOp.keyString).foreach(collMetricOp => {

      if(collMetricOp._1.size > 0){
        val languageObject = languageFieldName -> language
        val bulkOperation = db().getCollection(collMetricOp._1).initializeOrderedBulkOperation()

        if(textIndexName != "") createTextIndex(collMetricOp._1, textIndexName, Bucketer.fulltext.id, language)
        createIndex(collMetricOp._1, eventTimeFieldName, eventTimeFieldName, 1)

        collMetricOp._2.foreach(metricOp => {

          //TODO cuando este creada la DimensionValue del tiempo hay que cambiar esto y mirar el bucketType
          val dateObject = metricOp.rollupKey.filter(dimVal =>
            (timeDimension != "") && (timeDimension == dimVal.dimension.name)
          )

          val eventTimeObject = eventTimeFieldName -> {
            if((dateObject != null) && (dateObject.size > 0)) {
              dateObject.last.value
            } else {
              dateFromGranularity(DateTime.now(),granularity)
            }
          }

          val identitiesText = metricOp.rollupKey
            .filter(_.bucketType.id == Bucketer.fulltext.id)
            .map( dimVal => dimVal.value.toString)

          val identitiesField = metricOp.rollupKey
            .filter(_.bucketType.id == Bucketer.identityField.id)
            .map( dimVal => MongoDBObject(dimVal.dimension.name -> dimVal.value)
          )

          val find = {
            val builder = MongoDBObject.newBuilder
            builder += idFieldName -> metricOp.rollupKey
              .filter(rollup =>
                (rollup.bucketType.id != Bucketer.fulltext.id) && (rollup.dimension.name != timeDimension)
              )
              .map(dimVal => dimVal.value.toString
            ).mkString(idSeparator)

            builder += eventTimeObject

            builder.result
          }

          val unknownFields = metricOp.aggregations.keySet.filter(!schema.hasKey(_))
          if (unknownFields.nonEmpty) {
            throw new Exception(s"Got fields not present in schema: ${unknownFields.mkString(",")}")
          }

          val update = (
            for {
              (fieldName, value) <- metricOp.aggregations.toSeq
              op = schema(fieldName)
            } yield (op, (fieldName, value))
            )
            .groupBy(_._1)
            .mapValues(_.map(_._2))
            .map({
            case (op, seq) =>
              op match {
                case WriteOp.Inc =>
                  $inc(seq.asInstanceOf[Seq[(String, Long)]]: _*)
                case WriteOp.Set =>
                  $set(seq: _*)
                case WriteOp.Max =>
                  MongoDBObject("$max" -> MongoDBObject(seq.asInstanceOf[Seq[(String, Double)]]: _*))
                case WriteOp.Min =>
                  MongoDBObject("$min" -> MongoDBObject(seq.asInstanceOf[Seq[(String, Double)]]: _*))
                case WriteOp.Avg =>
                  MongoDBObject("$avg" -> MongoDBObject(seq.asInstanceOf[Seq[(String, Double)]]: _*))
              }
          })
            .reduce(_ ++ _)

          bulkOperation.find(find)
            .upsert().updateOne(update ++
            {
              if(identitiesField.size > 0) $set(Bucketer.identityField.id -> identitiesField) else DBObject()
            } ++
            {
              if(identitiesText.size > 0) {
                $addToSet(Bucketer.fulltext.id -> identitiesText.mkString(" __ ")) ++ $set(languageObject)
              } else {
                DBObject()
              }
            }
            )
        })

        bulkOperation.execute()

      }
    })
  }

}
