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

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.conversions.scala._
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.dstream.DStream

import com.stratio.sparkta.plugin.output.mongodb.dao.MongoDbDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk.{WriteOp, _}

class MongoDbOutput(keyName: String,
                    properties: Map[String, JSerializable],
                    @transient sparkContext: SparkContext,
                    operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                    bcSchema: Option[Broadcast[Seq[TableSchema]]])
  extends Output(keyName, properties, sparkContext, operationTypes, bcSchema) with MongoDbDAO {

  RegisterJodaTimeConversionHelpers()

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.IncBig, WriteOp.Set, WriteOp.Max, WriteOp.Min,
    WriteOp.AccAvg, WriteOp.AccMedian, WriteOp.AccVariance, WriteOp.AccStddev, WriteOp.FullText, WriteOp.AccSet)

  override val mongoClientUri = properties.getString("clientUri", "mongodb://localhost:27017")

  override val dbName = properties.getString("dbName", "sparkta")

  override val connectionsPerHost = Try(properties.getInt("connectionsPerHost")).getOrElse(DEFAULT_CONNECTIONS_PER_HOST)

  override val threadsAllowedB = Try(properties.getInt("threadsAllowedToBlock"))
    .getOrElse(DEFAULT_THREADS_ALLOWED_TO_BLOCK)

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val timeBucket = properties.getString("dateBucket", None)

  override val granularity = properties.getString("granularity", None)

  override val fieldsSeparator = properties.getString("fieldsSeparator", ",")

  override val textIndexFields = properties.getString("textIndexFields", "").split(fieldsSeparator)

  override val language = properties.getString("language", "none")

  override val pkTextIndexesCreated: (Boolean, Boolean) =
    filterSchemaByKeyAndField(bcSchema.get.value, timeBucket)
      .map(tableSchema => createPkTextIndex(tableSchema.tableName, timeBucket))
      .reduce((a, b) => (if (!a._1 || !b._1) false else true, if (!a._2 || !b._2) false else true))

  override def doPersist(stream: DStream[UpdateMetricOperation]): Unit = persistMetricOperation(stream)

  override def upsert(metricOperations: Iterator[UpdateMetricOperation]): Unit = {
    metricOperations.toList.groupBy(metricOp => metricOp.keyString).filter(_._1.size > 0).foreach(f = collMetricOp => {
      val bulkOperation = db().getCollection(collMetricOp._1).initializeOrderedBulkOperation()
      val idFieldName = if (timeBucket.isDefined && (collMetricOp._1.contains(timeBucket) || granularity.isDefined))
        Output.ID
      else DEFAULT_ID

      collMetricOp._2.foreach(metricOp => {
        checkFields(metricOp.aggregations.keySet, operationTypes)

        val eventTimeObject = getTime(metricOp).map(timeBucket.get -> _)
        val identitiesField = metricOp.rollupKey.filter(_.bucketType.id == Bucketer.identityField.id)
          .map(dimVal => MongoDBObject(dimVal.dimension.name -> dimVal.value))
        val mapOperations = getOperations(metricOp.aggregations.toSeq, operationTypes)
          .groupBy { case (writeOp, op) => writeOp }
          .mapValues(operations => operations.map { case (writeOp, op) => op })
          .map({ case (op, seq) => getSentence(op, seq) })

        bulkOperation.find(getFind(idFieldName, eventTimeObject, metricOp.rollupKey, timeBucket))
          .upsert().updateOne(getUpdate(mapOperations, identitiesField))
      })
      bulkOperation.execute()
    })
  }
}
