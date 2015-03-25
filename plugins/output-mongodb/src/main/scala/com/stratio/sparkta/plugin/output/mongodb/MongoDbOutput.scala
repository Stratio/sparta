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

  override val multiplexer = Try(properties.getString("multiplexer").toLowerCase().toBoolean).getOrElse(false)

  override val eventTimeFieldName = properties.getString("timeField", "eventTime")

  override val timeDimension = properties.getString("timeDimension", "")

  override def multiplexStream(stream: DStream[UpdateMetricOperation]) : DStream[UpdateMetricOperation] = {

    if(!multiplexer) stream
    else {
      for {
        upMetricOp: UpdateMetricOperation <- stream
        comb: Set[DimensionValue] <- upMetricOp.rollupKey.toSet[DimensionValue].subsets
          .filter(dimVals =>
          (dimVals.size > 1) ||
            ((dimVals.size == 1) && (dimVals.head.bucketType != Bucketer.fulltext))
          ).toTraversable
      } yield UpdateMetricOperation(comb.toSeq, upMetricOp.aggregations) : UpdateMetricOperation
      /*
      //Other way
      stream.flatMap(upMetricOp =>
        upMetricOp.rollupKey.toSet[DimensionValue].subsets.filter(_.size > 0).map(comb =>
          UpdateMetricOperation(comb.toSeq, upMetricOp.aggregations)).toIndexedSeq)*/
    }
  }

  override def multiplexStream(stream: DStream[UpdateMetricOperation], fixedDimension : String) : DStream[UpdateMetricOperation] = {

    if(!multiplexer) stream
    else {
      for {
        upMetricOp: UpdateMetricOperation <- stream
        fixedDim = upMetricOp.rollupKey.find(dimValue => dimValue.dimension.name == fixedDimension).get
        comb: Set[DimensionValue] <- upMetricOp.rollupKey.toSet[DimensionValue]
          .subsets
          .filter(dimVals =>
            (dimVals.size > 1) ||
            ((dimVals.size == 1) && ((dimVals.head.bucketType != Bucketer.fulltext) && (dimVals.head.dimension.name != fixedDimension)))
          )
          .map(setDimVal => setDimVal + fixedDim).toTraversable
      } yield UpdateMetricOperation(comb.toSeq, upMetricOp.aggregations) : UpdateMetricOperation
    }
  }

  def getStreamFromOptions(stream : DStream[UpdateMetricOperation], multiplexer : Boolean, timeDimension : String) : DStream[UpdateMetricOperation] = {

    multiplexer match {
        case false => stream
        case _ => timeDimension match {
          case "" => multiplexStream(stream)
          case _ => multiplexStream(stream, timeDimension)
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

  override def persist(streams: Seq[DStream[UpdateMetricOperation]]): Unit = {
    streams.foreach(persist)
  }

  def upsert(metricOperations: Iterator[UpdateMetricOperation]): Unit = {

    metricOperations.toList.groupBy(metricOp => metricOp.keyString).foreach(collMetricOp => {

      if(collMetricOp._1.size > 0){

        val languageObject = languageFieldName -> language
        val bulkOperation = db().getCollection(collMetricOp._1).initializeOrderedBulkOperation()

        if(textIndexName != "")
          createTextIndex(collMetricOp._1, textIndexName, Bucketer.fulltext.id, language)

        //TODO fixed dateTimeField in documents??
        createIndex(collMetricOp._1, eventTimeFieldName, eventTimeFieldName, 1)

        collMetricOp._2.foreach(metricOp => {
          val dateObject = metricOp.rollupKey.filter(dimVal =>
            (timeDimension != "") && (timeDimension == dimVal.dimension.name)
          )

          val eventTimeObject = eventTimeFieldName -> {
            if((dateObject != null) && (dateObject.size > 0))
              dateObject.last.value
            else DateTime.now()
              .withMillisOfSecond(0).withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(1)//.withDayOfMonth(1).withMonthOfYear(1)
          }

          val identitiesText = metricOp.rollupKey
            .filter(_.bucketType.id == Bucketer.fulltext.id)
            .map( dimVal => dimVal.value.toString)

          val identities = metricOp.rollupKey
            .filter(_.bucketType.id == Bucketer.identity.id)
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
                  $inc(seq: _*)
                case WriteOp.Set =>
                  $set(seq: _*)
                case WriteOp.Max =>
                  MongoDBObject("$max" -> MongoDBObject(seq: _*))
                case WriteOp.Min =>
                  MongoDBObject("$min" -> MongoDBObject(seq: _*))
              }
          })
            .reduce(_ ++ _)

          bulkOperation.find(find)
            .upsert().updateOne(update ++
            {
              if(identities.size > 0)
                $set(Bucketer.identity.id -> identities)
              else DBObject()
            } ++
            {
              if(identitiesText.size > 0)
                $addToSet(Bucketer.fulltext.id -> identitiesText.mkString(" __ ")) ++ $set(languageObject)
              else DBObject()
            }
            )
        })

        bulkOperation.execute()

      }
    })
  }

}
