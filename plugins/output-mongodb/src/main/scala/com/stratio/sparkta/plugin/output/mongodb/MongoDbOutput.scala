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
import com.stratio.sparkta.sdk._

class MongoDbOutput(keyName: String,
                    properties: Map[String, JSerializable],
                    @transient sparkContext: SparkContext,
                    operationTypes: Option[Map[String, (WriteOp, TypeOp)]],
                    bcSchema: Option[Seq[TableSchema]])
  extends Output(keyName, properties, sparkContext, operationTypes, bcSchema) with MongoDbDAO {

  RegisterJodaTimeConversionHelpers()

  override val mongoClientUri = properties.getString("clientUri", "mongodb://localhost:27017")

  override val dbName = properties.getString("dbName", "sparkta")

  override val connectionsPerHost = Try(properties.getInt("connectionsPerHost")).getOrElse(DefaultConnectionsPerHost)

  override val threadsAllowedB = Try(properties.getInt("threadsAllowedToBlock"))
    .getOrElse(DefaultThreadsAllowedToBlock)

  override val retrySleep = Try(properties.getInt("retrySleep")).getOrElse(DefaultRetrySleep)


  override val idAsField = Try(properties.getString("idAsField").toBoolean).getOrElse(true)

  override val textIndexFields = properties.getString("textIndexFields", None).map(_.split(FieldsSeparator))

  override val language = properties.getString("language", None)

  override val pkTextIndexesCreated: Boolean =
    if (bcSchema.isDefined) {
      val schemasFiltered =
        bcSchema.get.filter(schemaFilter => schemaFilter.outputName == keyName).map(getTableSchemaFixedId(_))
      filterSchemaByFixedAndTimeDimensions(schemasFiltered)
        .map(tableSchema => createPkTextIndex(tableSchema.tableName, tableSchema.timeDimension))
        .forall(result => result._1 && result._2)
    } else false

  override def doPersist(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    persistMetricOperation(stream)
  }

  override def upsert(metricOperations: Iterator[(DimensionValuesTime, Map[String, Option[Any]])]): Unit = {
    metricOperations.toList.filter(!_._1.dimensionValues.isEmpty).groupBy(upMetricOp =>
      AggregateOperations.keyString(upMetricOp._1, upMetricOp._1.timeDimension, fixedDimensions)).filter(_._1.size > 0)
      .foreach(f = collMetricOp => {
      val dimensionTime = collMetricOp._2.head._1.timeDimension
      val bulkOperation = db().getCollection(collMetricOp._1).initializeOrderedBulkOperation()
      val idFieldName = if (!dimensionTime.isEmpty) Output.Id else DefaultId

      val updateObjects = collMetricOp._2.map { case (cubeKey, aggregations) => {
        checkFields(aggregations.keySet, operationTypes)
        val eventTimeObject = if (!dimensionTime.isEmpty) Some(dimensionTime -> new DateTime(cubeKey.time)) else None
        val idFields = if (idAsField) Some(getIdFields(cubeKey)) else None
        val mapOperations = getOperations(aggregations.toSeq, operationTypes)
          .groupBy { case (writeOp, op) => writeOp }
          .mapValues(operations => operations.map { case (writeOp, op) => op })
          .map({ case (op, seq) => getSentence(op, seq) })

        (getFind(
          idFieldName,
          eventTimeObject,
          AggregateOperations.filterDimensionValuesByName(cubeKey.dimensionValues, if (dimensionTime.isEmpty) None
          else Some(dimensionTime))),
          getUpdate(mapOperations, idFields))
      }
      }

      Try(executeBulkOperation(bulkOperation, updateObjects)).getOrElse({
        val retryBulkOperation = reconnect.getCollection(collMetricOp._1).initializeOrderedBulkOperation()
        Try(executeBulkOperation(retryBulkOperation, updateObjects)).getOrElse(log.error("Error connecting to MongoDB"))
      })
    })
  }
}
