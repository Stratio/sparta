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
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark.Logging
import org.apache.spark.streaming.dstream.DStream
import ValidatingPropertyMap._

class MongoDbOutput(properties: Map[String, Serializable], schema : Map[String,WriteOp])
  extends Output(properties, schema) with AbstractMongoDAO with Serializable with Logging {

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.Set, WriteOp.Max, WriteOp.Min)

  override val mongoClientUri = properties.getString("clientUri", "mongodb://localhost:27017")

  override val dbName = properties.getString("dbName", "sparkta")

  override def persist(stream: DStream[UpdateMetricOperation]): Unit = {
    stream.foreachRDD(rdd =>
      rdd.foreach(op =>
        upsert(op)
      )
    )
  }

  override def persist(streams: Seq[DStream[UpdateMetricOperation]]): Unit = {
    streams.foreach(persist)
  }

  def upsert(metricOp: UpdateMetricOperation): Unit = {

    val find = {
      val builder = MongoDBObject.newBuilder
      metricOp.rollupKey.foreach(dimVal => builder += dimVal.dimension.name -> dimVal.value)
      builder += "_id" -> metricOp.rollupKey.map(_.value.toString).mkString("__")
      builder.result()
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
              $inc(seq : _*)
            case WriteOp.Set =>
              $set(seq : _*)
            case WriteOp.Max =>
              MongoDBObject("$max" -> MongoDBObject(seq : _*))
            case WriteOp.Min =>
              MongoDBObject("$min" -> MongoDBObject(seq : _*))
          }
      })
      .reduce(_ ++ _)

    val collection = db().getCollection(metricOp.keyString)

    collection.update(
      find,
      update,
      true, false)
  }

}
