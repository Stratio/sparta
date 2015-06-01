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
import org.joda.time.DateTime

import com.stratio.sparkta.plugin.output.mongodb.dao.MongoDbDAO
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk.{WriteOp, _}

class MongoDbOutput(keyName: String,
                    properties: Map[String, JSerializable],
                    @transient sparkContext: SparkContext,
                    operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]],
                    bcSchema: Option[Broadcast[Seq[TableSchema]]],
                    timeName: String)
  extends Output(keyName, properties, sparkContext, operationTypes, bcSchema, timeName) with MongoDbDAO {

  RegisterJodaTimeConversionHelpers()

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.IncBig, WriteOp.Set, WriteOp.Max, WriteOp.Min,
    WriteOp.AccAvg, WriteOp.AccMedian, WriteOp.AccVariance, WriteOp.AccStddev, WriteOp.FullText, WriteOp.AccSet)

  override val mongoClientUri = properties.getString("clientUri", "mongodb://localhost:27017")

  override val dbName = properties.getString("dbName", "sparkta")

  override val connectionsPerHost = Try(properties.getInt("connectionsPerHost")).getOrElse(DefaultConnectionsPerHost)

  override val threadsAllowedB = Try(properties.getInt("threadsAllowedToBlock"))
    .getOrElse(DefaultThreadsAllowedToBlock)

  override val retrySleep = Try(properties.getInt("retrySleep")).getOrElse(DefaultRetrySleep)

  override val multiplexer = Try(properties.getString("multiplexer").toBoolean).getOrElse(false)

  override val identitiesSaved = Try(properties.getString("identitiesSaved").toBoolean).getOrElse(false)

  override val identitiesSavedAsField = Try(properties.getString("identitiesSavedAsField").toBoolean).getOrElse(false)

  override val fieldsSeparator = properties.getString("fieldsSeparator", ",")

  override val textIndexFields = properties.getString("textIndexFields", None).map(_.split(fieldsSeparator))

  override val fixedBuckets: Array[String] = properties.getString("fixedBuckets", None) match {
    case None => Array()
    case Some(fixBuckets) => fixBuckets.split(fieldsSeparator)
  }

  override val language = properties.getString("language", None)

  override val pkTextIndexesCreated: Boolean =
    filterSchemaByKeyAndField.map(tableSchema => createPkTextIndex(tableSchema.tableName, timeName))
      .forall(result => result._1 && result._2)

  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    persistMetricOperation(stream)
  }

  override def upsert(metricOperations: Iterator[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    metricOperations.toList.groupBy(upMetricOp => AggregateOperations.keyString(upMetricOp._1))
      .filter(_._1.size > 0).foreach(f = collMetricOp => {
      val bulkOperation = db().getCollection(collMetricOp._1).initializeOrderedBulkOperation()
      val idFieldName = if (!timeName.isEmpty) Output.Id else DefaultId

      val updateObjects = collMetricOp._2.map { case (rollupKey, aggregations) => {
        checkFields(aggregations.keySet, operationTypes)
        val eventTimeObject = if (!timeName.isEmpty) Some(timeName -> new DateTime(rollupKey.time)) else None
        val identitiesField = getIdentitiesField(rollupKey)
        val identities = if(identitiesSaved) Some(getIdentities(rollupKey)) else None
        val mapOperations = getOperations(aggregations.toSeq, operationTypes)
          .groupBy { case (writeOp, op) => writeOp }
          .mapValues(operations => operations.map { case (writeOp, op) => op })
          .map({ case (op, seq) => getSentence(op, seq) })

        (getFind(
          idFieldName,
          eventTimeObject,
          AggregateOperations.filterDimensionValuesByBucket(rollupKey.dimensionValues, if (timeName.isEmpty) None
          else Some(timeName))),
          getUpdate(mapOperations, identitiesField, identities))
      }
      }

      Try(executeBulkOperation(bulkOperation, updateObjects)).getOrElse({
        val retryBulkOperation = reconnect.getCollection(collMetricOp._1).initializeOrderedBulkOperation()
        Try(executeBulkOperation(retryBulkOperation, updateObjects)).getOrElse(log.error("Error connecting to MongoDB"))
      })
    })
  }
}
