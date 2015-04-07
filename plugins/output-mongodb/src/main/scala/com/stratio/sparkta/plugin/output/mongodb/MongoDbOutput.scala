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
import com.mongodb.casbah.commons.conversions.scala._
import org.joda.time.DateTime

import scala.util.Try

class MongoDbOutput(properties: Map[String, Serializable], schema : Map[String,WriteOp])
  extends Output(properties, schema) with AbstractMongoDAO with Multiplexer with Serializable with Logging {

  RegisterJodaTimeConversionHelpers()

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.Set, WriteOp.Max, WriteOp.Min)

  override val mongoClientUri = properties.getString("clientUri", "mongodb://localhost:27017")

  override val dbName = properties.getString("dbName", "sparkta")

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val eventTimeFieldName = properties.getString("timestampFieldName", "timestamp")

  override val timeBucket = properties.getString("timestampBucket", "")

  override val granularity = properties.getString("granularity", "")

  override val textIndexName = properties.getString("textIndexFieldName", "")

  override val language = properties.getString("language", "none")

  override def getStreamsFromOptions(stream : DStream[UpdateMetricOperation],
                           multiplexer : Boolean, fixedBucket : String) : DStream[UpdateMetricOperation] = {
    multiplexer match {
        case false => stream
        case _ => fixedBucket match {
          case "" => Multiplexer.multiplexStream(stream)
          case _ => Multiplexer.multiplexStream[fixedBucket.type](stream, fixedBucket)
        }
    }
  }

  override def persist(stream: DStream[UpdateMetricOperation]): Unit = {
    getStreamsFromOptions(stream, multiplexer, timeBucket)
      .foreachRDD(rdd => rdd.foreachPartition(ops => upsert(ops)))
  }

  override def persist(streams: Seq[DStream[UpdateMetricOperation]]): Unit = {
    streams.foreach(persist)
  }

  def upsert(metricOperations: Iterator[UpdateMetricOperation]): Unit = {
    metricOperations.toList.groupBy(metricOp => metricOp.keyString).foreach(collMetricOp => {

      if(collMetricOp._1.size > 0){
        val languageObject = languageFieldName -> language
        val bulkOperation = db().getCollection(collMetricOp._1).initializeOrderedBulkOperation()

        //TODO refactor out of here
        if(textIndexName != ""){
          createTextIndex(collMetricOp._1, textIndexName, Bucketer.fulltext.id, language)
        }
        if((timeBucket != "") && ((collMetricOp._1.contains(timeBucket)) || (granularity != ""))){
          createIndex(collMetricOp._1, eventTimeFieldName, eventTimeFieldName, 1)
        }

        collMetricOp._2.foreach(metricOp => {

          val eventTimeObject = timeBucket match {
            case "" => None
            case _ => metricOp.rollupKey.filter(dimVal => timeBucket == dimVal.bucketType.id) match {
              case c if(c.size > 0) => Some(eventTimeFieldName -> c.last.value)
              case _ => granularity match {
                case "" => None
                case _ => Some(eventTimeFieldName -> Output.dateFromGranularity(DateTime.now(),granularity))
              }
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
                (rollup.bucketType.id != Bucketer.fulltext.id) && (rollup.bucketType.id != timeBucket))
              .map(dimVal => dimVal.value.toString)
              .mkString(idSeparator)

            if(eventTimeObject != None) builder += eventTimeObject.get

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
                $addToSet(Bucketer.fulltext.id -> identitiesText.mkString(" _ ")) ++ $set(languageObject)
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
