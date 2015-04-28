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

package com.stratio.sparkta.plugin.output.mongodb

import java.io.{Serializable => JSerializable}
import scala.util.Try

import com.mongodb.casbah
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.mongodb.casbah.commons.{Imports, MongoDBObject}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.dstream.DStream

import com.stratio.sparkta.plugin.output.mongodb.dao.AbstractMongoDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk.{WriteOp, _}

class MongoDbOutput(keyName: String,
                    properties: Map[String, JSerializable],
                    sqlContext: SQLContext,
                    operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                    bcSchema: Option[Broadcast[Seq[TableSchema]]])
  extends Output(keyName, properties, sqlContext, operationTypes, bcSchema)
  with AbstractMongoDAO {

  RegisterJodaTimeConversionHelpers()

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.Set, WriteOp.Max, WriteOp.Min, WriteOp.AccAvg,
    WriteOp.AccMedian, WriteOp.AccVariance, WriteOp.AccStddev, WriteOp.FullText, WriteOp.AccSet)

  override val mongoClientUri = properties.getString("clientUri", "mongodb://localhost:27017")

  override val dbName = properties.getString("dbName", "sparkta")

  override val connectionsPerHost = Try(properties.getInt("connectionsPerHost")).getOrElse(DEFAULT_CONNECTIONS_PER_HOST)

  override val threadsAllowedToBlock = Try(properties.getInt("threadsAllowedToBlock"))
    .getOrElse(DEFAULT_THREADS_ALLOWED_TO_BLOCK)

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val timeBucket = properties.getString("timestampBucket", None)

  override val granularity = properties.getString("granularity", None)

  override val fieldsSeparator = properties.getString("fieldsSeparator", ",")

  override val textIndexFields = properties.getString("textIndexFields", "")
    .split(fieldsSeparator)

  override val language = properties.getString("language", "none")

  override val pkTextIndexesCreated: (Boolean, Boolean) =
    filterSchemaByKeyAndField(bcSchema.get.value, timeBucket)
      .map(tableSchema => createPkTextIndex(tableSchema.tableName, timeBucket))
      .reduce((a, b) => (if (!a._1 || !b._1) false else true, if (!a._2 || !b._2) false else true))

  override def doPersist(stream: DStream[UpdateMetricOperation]): Unit = {
    persistMetricOperation(stream)
  }

  def rollupKeyFilteredByTime(rollupkey: Seq[DimensionValue]): Seq[DimensionValue] = {
    timeBucket match {
      case None => rollupkey
      case Some(bucket) => rollupkey.filter(rollup => (rollup.bucketType.id != bucket))
    }
  }

  // scalastyle:off
  override def upsert(metricOperations: Iterator[UpdateMetricOperation]): Unit = {
    metricOperations.toList.groupBy(metricOp => metricOp.keyString)
      .filter(_._1.size > 0)
      .foreach(f = collMetricOp => {
      val languageObject = (LANGUAGE_FIELD_NAME, language)
      val bulkOperation = db().getCollection(collMetricOp._1).initializeOrderedBulkOperation()
      val idField = if (timeBucket.isDefined && (collMetricOp._1.contains(timeBucket) || granularity.isDefined)) {
        idAuxFieldName
      } else DEFAULT_ID

      collMetricOp._2.foreach(metricOp => {
        val unknownFields: Set[String] = metricOp.aggregations.keySet.filter(!operationTypes.get.value.hasKey(_))
        if (unknownFields.nonEmpty) {
          throw new Exception(s"Got fields not present in schema: ${unknownFields.mkString(",")}")
        }

        val eventTimeObject: Option[(String, JSerializable)] = {
          val eventTimeValue = getTime(metricOp)
          eventTimeValue.map(timeBucket.get -> _)
        }

        val identitiesField: Seq[Imports.DBObject] = metricOp.rollupKey
          .filter(_.bucketType.id == Bucketer.identityField.id)
          .map(dimVal => MongoDBObject(dimVal.dimension.name -> dimVal.value))

        val find: Imports.DBObject = {
          val builder = MongoDBObject.newBuilder
          builder += idField -> rollupKeyFilteredByTime(metricOp.rollupKey)
            .map(dimVal => dimVal.value.toString)
            .mkString(idValuesSeparator)
          if (eventTimeObject.isDefined) builder += eventTimeObject.get
          builder.result
        }

        val mapOperations: Map[Seq[(String, Any)], JSFunction] = (
          for {
            (fieldName, value) <- metricOp.aggregations.toSeq
            op = operationTypes.get.value(fieldName)._1
          } yield (op, (fieldName, value)))
          .groupBy(_._1)
          .mapValues(_.map(_._2))
          .map({
          case (op, seq) => op match {
            case WriteOp.Inc =>
              (seq.asInstanceOf[Seq[(String, Long)]], "$inc")
            case WriteOp.Set =>
              (seq, "$set")
            case WriteOp.Avg | WriteOp.Median | WriteOp.Variance | WriteOp.Stddev =>
              (seq.asInstanceOf[Seq[(String, Double)]], "$set")
            case WriteOp.Max =>
              (seq.asInstanceOf[Seq[(String, Double)]], "$max")
            case WriteOp.Min =>
              (seq.asInstanceOf[Seq[(String, Double)]], "$min")
            case WriteOp.AccAvg | WriteOp.AccMedian | WriteOp.AccVariance | WriteOp.AccStddev =>
              (seq.asInstanceOf[Seq[(String, Double)]], "$addToset")
            case WriteOp.FullText | WriteOp.AccSet =>
              (seq.asInstanceOf[Seq[(String, String)]], "$addToSet")
          }
        })

        val combinedOptions: Map[Seq[(String, Any)], casbah.Imports.JSFunction] = mapOperations ++
          Map((Seq(languageObject), "$set")) ++ {
          if (identitiesField.size > 0) {
            Map((Seq(Bucketer.identityField.id -> identitiesField), "$set"))
          } else Map()
        }

        val update = combinedOptions.groupBy(_._2)
          .map(grouped => MongoDBObject(grouped._1 -> MongoDBObject(grouped._2.flatMap(f => f._1).toSeq: _*)))
          .reduce(_ ++ _)

        bulkOperation.find(find).upsert().updateOne(update)
      })
      bulkOperation.execute()
    })
  }
}
