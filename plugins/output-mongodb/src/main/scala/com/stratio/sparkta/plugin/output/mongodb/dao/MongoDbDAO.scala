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

package com.stratio.sparkta.plugin.output.mongodb.dao

import java.io.{Closeable, Serializable => JSerializable}
import scala.collection.mutable

import com.mongodb
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.{Imports, MongoDBObject}
import com.mongodb.casbah.{MongoClient, MongoClientURI, MongoDB}
import com.mongodb.{DBObject, MongoClientOptions, MongoClientURI => JMongoClientURI, WriteConcern, casbah}
import org.apache.spark.broadcast.Broadcast
import org.joda.time.DateTime

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp._
import com.stratio.sparkta.sdk._

//scalastyle:off
trait MongoDbDAO extends Closeable {

  final val DefaultConnectionsPerHost = 5
  final val DefaultThreadsAllowedToBlock = 10
  final val DefaultRetrySleep = 1000
  final val LanguageFieldName = "language"
  final val DefaultId = "_id"
  final val DefaultWriteConcern = casbah.WriteConcern.Unacknowledged

  def mongoClientUri: String

  def dbName: String

  def connectionsPerHost: Int

  def threadsAllowedB: Int

  def language: Option[String]

  def textIndexFields: Option[Array[String]]

  def pkTextIndexesCreated: Boolean

  def identitiesSaved: Boolean

  def identitiesSavedAsField: Boolean

  def idAsField: Boolean

  def retrySleep: Int

  protected def client: MongoClient = MongoDbDAO.client(mongoClientUri, connectionsPerHost, threadsAllowedB, false)

  protected def db(dbName: String): MongoDB = MongoDbDAO.db(mongoClientUri, dbName, connectionsPerHost, threadsAllowedB)

  protected def reconnect(): MongoDB =
    MongoDbDAO.reconnect(retrySleep, mongoClientUri, dbName, connectionsPerHost, threadsAllowedB)

  protected def db(): MongoDB = db(dbName)

  def executeBulkOperation(bulkOperation: mongodb.BulkWriteOperation,
                           updateObjects: List[(Imports.DBObject, Imports.DBObject)]): Unit = {
    updateObjects.foreach { case (find, update) => bulkOperation.find(find).upsert().updateOne(update) }
    bulkOperation.execute()
  }

  protected def createPkTextIndex(collection: String, timePrecision: String): (Boolean, Boolean) = {
    val textIndexCreated = if (textIndexFields.isDefined && language.isDefined) {
      if (textIndexFields.get.size > 0) {
        createTextIndex(collection, textIndexFields.mkString(Output.Separator), textIndexFields.get, language.get)
        true
      } else false
    } else false

    if (!timePrecision.isEmpty) {
      createIndex(collection, Output.Id + Output.Separator + timePrecision,
        Map(Output.Id -> 1, timePrecision -> 1), true, true)
    }
    (!timePrecision.isEmpty, textIndexCreated)
  }

  protected def indexExists(collection: String, indexName: String): Boolean = {
    var indexExists = false
    val itObjects = db.getCollection(collection).getIndexInfo().iterator()

    while (itObjects.hasNext && !indexExists) {
      val indexObject = itObjects.next()
      if (indexObject.containsField("name") && (indexObject.get("name") == indexName)) indexExists = true
    }
    indexExists
  }

  protected def createTextIndex(collection: String,
                                indexName: String,
                                indexFields: Array[String],
                                language: String): Unit = {
    if (collection.nonEmpty && indexFields.nonEmpty && indexName.nonEmpty && !indexExists(collection, indexName)) {
      val fields = indexFields.map(_ -> "text").toList
      val options = MongoDBObject.newBuilder

      options += "name" -> indexName
      options += "background" -> true
      if (language != "") options += "default_language" -> language
      db.getCollection(collection).createIndex(MongoDBObject(fields), options.result)
    }
  }

  protected def createIndex(collection: String,
                            indexName: String,
                            indexFields: Map[String, Int],
                            unique: Boolean,
                            background: Boolean): Unit = {
    if (collection.nonEmpty && indexFields.nonEmpty && indexName.nonEmpty && !indexExists(collection, indexName)) {
      val fields = indexFields.map { case (field, value) => field -> value }.toList
      val options = MongoDBObject.newBuilder

      options += "name" -> indexName
      options += "background" -> background
      options += "unique" -> unique
      db.getCollection(collection).createIndex(MongoDBObject(fields), options.result)
    }
  }

  protected def insert(dbName: String, collName: String, dbOjects: Iterator[DBObject],
                       writeConcern: Option[WriteConcern] = None): Unit = {
    val coll = db(dbName).getCollection(collName)
    val builder = coll.initializeUnorderedBulkOperation

    dbOjects.map(dbObjectsBatch => builder.insert(dbObjectsBatch))
    if (writeConcern.isEmpty) builder.execute(DefaultWriteConcern) else builder.execute(writeConcern.get)
  }

  protected def getFind(idFieldName: String,
                        eventTimeObject: Option[(String, DateTime)],
                        dimensionValues: Seq[DimensionValue]): Imports.DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += idFieldName -> dimensionValues.map(dimVal => dimVal.value.toString)
      .mkString(Output.Separator)
    if (eventTimeObject.isDefined) builder += eventTimeObject.get
    builder.result
  }

  protected def getOperations(aggregations: Seq[(String, Option[Any])],
                              operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]])
  : Seq[(WriteOp, (String, Option[Any]))] = {
    for {
      (fieldName, value) <- aggregations
      op = operationTypes.get.value(fieldName)._1
    } yield (op, (fieldName, value))
  }

  protected def getUpdate(mapOperations: Map[Seq[(String, Any)], String],
                          identitiesField: Seq[Imports.DBObject],
                          identities: Option[Map[Seq[(String, JSerializable)], String]],
                          idFields: Option[Map[Seq[(String, JSerializable)], String]]): Imports.DBObject = {
    val combinedOptions: Map[Seq[(String, Any)], casbah.Imports.JSFunction] = mapOperations ++ {
      if (language.isDefined) Map((Seq((LanguageFieldName, language.get)), "$set")) else Map()
    } ++ {
      if (identitiesField.size > 0) Map((Seq(DimensionType.IdentityFieldName -> identitiesField), "$set")) else Map()
    } ++ {
      identities match {
        case Some(identity) => identity
        case None => Map()
      }
    } ++ {
      idFields match {
        case Some(field) => field
        case None => Map()
      }
    }

    val updateObjects = combinedOptions.filter(_._2.nonEmpty).groupBy(_._2)
      .map { case (name, value) => MongoDBObject(name -> MongoDBObject(value.flatMap(f => f._1).toSeq: _*)) }

    if(updateObjects.nonEmpty) updateObjects.reduce(_ ++ _) else MongoDBObject()
  }

  protected def valuesBigDecimalToDouble(seq: Seq[(String, Option[Any])]): Seq[(String, Double)] = {
    seq.asInstanceOf[Seq[(String, Option[BigDecimal])]].map(s => (s._1, s._2 match {
      case None => 0
      case Some(value) => value.toDouble
    }))
  }

  /*
   * With stateful all are set, but in the future is possible that we need more $max, $min, $avg for efficiency
   */
  protected def getSentence(op: WriteOp, seq: Seq[(String, Option[Any])]): (Seq[(String, Any)], String) = {
    op match {
      case WriteOp.Inc => (seq.asInstanceOf[Seq[(String, Long)]], "$set")
      case WriteOp.IncBig => (valuesBigDecimalToDouble(seq), "$set")
      case WriteOp.Set => (seq, "$set")
      case WriteOp.Avg | WriteOp.Median | WriteOp.Variance | WriteOp.Stddev =>
        (seq.asInstanceOf[Seq[(String, Double)]], "$set")
      case WriteOp.Max =>
        (seq.asInstanceOf[Seq[(String, Double)]], "$set")
      case WriteOp.Min =>
        (seq.asInstanceOf[Seq[(String, Double)]], "$set")
      case WriteOp.AccAvg | WriteOp.AccMedian | WriteOp.AccVariance | WriteOp.AccStddev =>
        (seq.asInstanceOf[Seq[(String, Double)]], "$set")
      case WriteOp.FullText | WriteOp.AccSet =>
        (seq.asInstanceOf[Seq[(String, String)]], "$set")
    }
  }

  protected def getIdFields(cubeKey : DimensionValuesTime): Map[Seq[(String, JSerializable)], String] =
    cubeKey.dimensionValues.map(dimVal => (Seq(dimVal.getNameDimension -> dimVal.value), "$set")).toMap

  protected def getIdentities(cubeKey : DimensionValuesTime): Map[Seq[(String, JSerializable)], String] =
    cubeKey.dimensionValues.filter(dimVal => dimVal.dimensionPrecision.precision.id == DimensionType.IdentityName)
    .map(dimVal => (Seq(dimVal.getNameDimension -> dimVal.value), "$set")).toMap

  protected def getIdentitiesField(cubeKey : DimensionValuesTime): Seq[Imports.DBObject] = cubeKey.dimensionValues
    .filter(dimVal => dimVal.dimensionPrecision.precision.id == DimensionType.IdentityFieldName ||
    (identitiesSavedAsField && dimVal.dimensionPrecision.precision.id == DimensionType.IdentityName))
    .map(dimVal => MongoDBObject(dimVal.getNameDimension -> dimVal.value))

  protected def checkFields(aggregations: Set[String],
                            operationTypes: Option[Broadcast[Map[String, (WriteOp, TypeOp)]]]): Unit = {
    val unknownFields = aggregations.filter(!operationTypes.get.value.hasKey(_))
    if (unknownFields.nonEmpty) throw new Exception(s"Fields not present in schema: ${unknownFields.mkString(",")}")
  }

  override def close(): Unit = {
  }
}

private object MongoDbDAO {

  private val clients: mutable.Map[String, MongoClient] = mutable.Map()
  private val dbs: mutable.Map[(String, String), MongoDB] = mutable.Map()

  private def options(connectionsPerHost: Integer, threadsAllowedToBlock: Integer) =
    MongoClientOptions.builder()
      .connectionsPerHost(connectionsPerHost)
      .writeConcern(casbah.WriteConcern.Unacknowledged)
      .threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlock)

  private def client(mongoClientUri: String, connectionsPerHost: Integer,
                     threadsAllowedToBlock: Integer, force: Boolean): MongoClient = {
    if (!clients.contains(mongoClientUri) || force) {
      clients.put(mongoClientUri, MongoClient(
        new MongoClientURI(new JMongoClientURI(mongoClientUri, options(connectionsPerHost, threadsAllowedToBlock)))
      ))
    }
    clients(mongoClientUri)
  }

  private def db(mongoClientUri: String, dbName: String,
                 connectionsPerHost: Integer, threadsAllowedB: Integer): MongoDB = {
    val key = (mongoClientUri, dbName)

    if (!dbs.contains(key))
      dbs.put(key, client(mongoClientUri, connectionsPerHost, threadsAllowedB, false).getDB(dbName))
    dbs(key)
  }

  private def reconnect(retrySleep: Int, mongoClientUri: String, dbName: String,
                        connectionsPerHost: Integer, threadsAllowedB: Integer): MongoDB = {
    Thread.sleep(retrySleep)
    val key = (mongoClientUri, dbName)
    dbs.put(key, client(mongoClientUri, connectionsPerHost, threadsAllowedB, true).getDB(dbName))
    dbs(key)
  }
}
