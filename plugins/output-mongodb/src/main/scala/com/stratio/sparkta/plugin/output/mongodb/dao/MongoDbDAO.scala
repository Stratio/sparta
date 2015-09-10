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
import java.net.UnknownHostException

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

import com.mongodb.{DBObject, MongoClientOptions, MongoClientURI => JMongoClientURI, WriteConcern, casbah}
import com.mongodb.casbah.{MongoClient, MongoDB}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.{Imports, MongoDBObject}
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp._
import org.apache.spark.Logging
import org.joda.time.DateTime

trait MongoDbDAO extends Closeable {

  final val DefaultConnectionsPerHost = "5"
  final val DefaultThreadsAllowedToBlock = "10"
  final val DefaultRetrySleep = "1000"
  final val LanguageFieldName = "language"
  final val DefaultId = "_id"
  final val DefaultWriteConcern = casbah.WriteConcern.Unacknowledged
  final val DefaultHost = "localhost"
  final val DefaultPort = "27017"

  def hosts: String

  def dbName: String

  def connectionsPerHost: Int

  def threadsAllowedB: Int

  def language: Option[String]

  def textIndexFields: Option[Array[String]]

  def idAsField: Boolean

  def retrySleep: Int

  protected def client: MongoClient = MongoDbDAO.client(hosts, connectionsPerHost, threadsAllowedB, false)

  protected def db(dbName: String): MongoDB = MongoDbDAO.db(hosts, dbName, connectionsPerHost, threadsAllowedB)

  protected def reconnect(): MongoDB =
    MongoDbDAO.reconnect(retrySleep, hosts, dbName, connectionsPerHost, threadsAllowedB)

  protected def db(): MongoDB = db(dbName)

  protected def createPkTextIndex(collection: String, timeDimension: String): (Boolean, Boolean) = {
    val textIndexCreated = if (textIndexFields.isDefined && language.isDefined) {
      if (textIndexFields.get.size > 0) {
        createTextIndex(collection, textIndexFields.mkString(Output.Separator), textIndexFields.get, language.get)
        true
      } else false
    } else false

    if (!timeDimension.isEmpty) {
      createIndex(collection, Output.Id + Output.Separator + timeDimension,
        Map(Output.Id -> 1, timeDimension -> 1), true, true)
    }
    (!timeDimension.isEmpty, textIndexCreated)
  }

  protected def indexExists(collection: String, indexName: String): Boolean = {
    var indexExists = false
    val itObjects =  db.getCollection(collection).getIndexInfo().iterator()

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
                              operationTypes: Option[Map[String, (WriteOp, TypeOp)]])
  : Seq[(WriteOp, (String, Option[Any]))] = {
    for {
      (fieldName, value) <- aggregations
      op = operationTypes.get(fieldName)._1
    } yield (op, (fieldName, value))
  }

  protected def getUpdate(mapOperations: Map[Seq[(String, Any)], String],
                          idFields: Option[Map[Seq[(String, JSerializable)], String]]): Imports.DBObject = {
    val combinedOptions: Map[Seq[(String, Any)], casbah.Imports.JSFunction] = mapOperations ++ {
      if (language.isDefined) Map((Seq((LanguageFieldName, language.get)), "$set")) else Map()
    } ++ {
      idFields match {
        case Some(field) => field
        case None => Map()
      }
    }

    val updateObjects = combinedOptions.filter(_._2.nonEmpty).groupBy(_._2)
      .map { case (name, value) => MongoDBObject(name -> MongoDBObject(value.flatMap(f => f._1).toSeq: _*)) }

    if (updateObjects.nonEmpty) updateObjects.reduce(_ ++ _) else MongoDBObject()
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
      case WriteOp.Set | WriteOp.Range => (seq, "$set")
      case WriteOp.Max | WriteOp.Min | WriteOp.Avg | WriteOp.Median | WriteOp.Variance | WriteOp.Stddev |
           WriteOp.AccAvg | WriteOp.AccMedian | WriteOp.AccVariance | WriteOp.AccStddev =>
        (seq.asInstanceOf[Seq[(String, Double)]], "$set")
      case WriteOp.FullText | WriteOp.AccSet | WriteOp.Mode =>
        (seq.asInstanceOf[Seq[(String, String)]], "$set")
    }
  }

  protected def getIdFields(cubeKey: DimensionValuesTime): Map[Seq[(String, JSerializable)], String] =
    cubeKey.dimensionValues.map(dimVal => (Seq(dimVal.dimension.name -> dimVal.value), "$set")).toMap

  protected def checkFields(aggregations: Set[String],
                            operationTypes: Option[Map[String, (WriteOp, TypeOp)]]): Unit = {
    val unknownFields = aggregations.filter(!operationTypes.get.hasKey(_))
    if (unknownFields.nonEmpty) throw new Exception(s"Fields not present in schema: ${unknownFields.mkString(",")}")
  }

  override def close(): Unit = {
  }
}

private object MongoDbDAO extends Logging {

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
      clients.put(mongoClientUri, mongoClient(mongoClientUri.split(","), connectionsPerHost, threadsAllowedToBlock))
    }
    clients(mongoClientUri)
  }

  private def mongoClient(addresses: Seq[String], connectionsPerHost: Integer, threadsAllowedToBlock: Integer):
    MongoClient = {

    val serverAddresses = addresses.flatMap(address => {
      Try(new ServerAddress(address)) match {
        case Success(serverAddress) => Some(serverAddress)
        case Failure(e: IllegalArgumentException) =>
          log.warn("EndPoint " + address + e.getMessage)
          None
        case Failure(e: UnknownHostException) =>
          log.warn("Unable to connect to " + address + e.getMessage)
          None
      }
    })
    MongoClient(serverAddresses.toList, options(connectionsPerHost, threadsAllowedToBlock).build())
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
