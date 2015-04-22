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
package com.stratio.sparkta.plugin.output.mongodb.dao

import java.io.Closeable

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI, MongoDB}
import com.mongodb.{DBObject, MongoClientOptions, WriteConcern, casbah, MongoClientURI => JMongoClientURI}
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.WriteOp._

import scala.collection.mutable

trait AbstractMongoDAO extends Closeable {

  final val DEFAULT_CONNECTIONS_PER_HOST = 5
  final val DEFAULT_THREADS_ALLOWED_TO_BLOCK = 10
  final val LANGUAGE_FIELD_NAME = "language"
  final val DEFAULT_ID = "_id"
  final val INDEX_NAME_SEPARATOR = "_"
  final val DEFAULT_WRITE_CONCERN = casbah.WriteConcern.Unacknowledged

  def mongoClientUri : String
  def dbName : String
  def connectionsPerHost : Int
  def threadsAllowedToBlock : Int
  def language : String
  def textIndexFields : Array[String]
  def timestampField : String = "eventTime"
  def idValuesSeparator : String = "_"
  def fieldsSeparator : String = ","
  def idAuxFieldName : String = "id"
  def pkTextIndexesCreated : (Boolean, Boolean) = (false, false)

  protected def client: MongoClient = AbstractMongoDAO.client(
    mongoClientUri,
    connectionsPerHost,
    threadsAllowedToBlock
  )

  protected def db(dbName: String): MongoDB = AbstractMongoDAO.db(
    mongoClientUri,
    dbName,
    connectionsPerHost,
    threadsAllowedToBlock
  )

  protected def db(): MongoDB = db(dbName)

  protected def createPkTextIndex(collection : String,
                                  timeBucket : String,
                                  granularity : String) : (Boolean, Boolean) = {
    var textIndexCreated = false
    var pkIndexCreated = false

    if (textIndexFields.size > 0) {
      createTextIndex(collection, textIndexFields.mkString(INDEX_NAME_SEPARATOR), textIndexFields, language)
      textIndexCreated = true
    }
    if (!timeBucket.isEmpty && !granularity.isEmpty) {
      createIndex(collection, idAuxFieldName + "_" + timestampField,
        Map(idAuxFieldName -> 1, timestampField -> 1), true, true)
      pkIndexCreated = true
    }
    (pkIndexCreated, textIndexCreated)
  }

  protected def indexExists(collection : String, indexName : String) : Boolean = {

    var indexExists = false
    val itObjects = db.getCollection(collection).getIndexInfo().iterator()

    while(itObjects.hasNext && !indexExists){
      val indexObject = itObjects.next()
      if(indexObject.containsField("name") && (indexObject.get("name") == indexName)) indexExists = true
    }
    indexExists
  }

  protected def createTextIndex(collection : String,
                       indexName : String,
                       indexFields : Array[String],
                       language : String) : Unit = {
    if(collection.nonEmpty && indexFields.nonEmpty && indexName.nonEmpty && !indexExists(collection, indexName)){
      val fields = indexFields.map(_ -> "text").toList
      val options = MongoDBObject.newBuilder
      options += "name" -> indexName
      options += "background" -> true
      if(language != "") options += "default_language" -> language

      db.getCollection(collection).createIndex(MongoDBObject(fields), options.result
      )
    }
  }

  protected def createIndex(collection : String,
                  indexName : String,
                  indexFields : Map[String, Int],
                  unique : Boolean,
                  background : Boolean) : Unit = {
    if(collection.nonEmpty && indexFields.nonEmpty && indexName.nonEmpty && !indexExists(collection, indexName)){
      val fields = indexFields.map(field => field._1 -> field._2).toList
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

    dbOjects.map(dbObjectsBatch =>
        builder.insert(dbObjectsBatch)
    )
    if(writeConcern.isEmpty) builder.execute(DEFAULT_WRITE_CONCERN) else builder.execute(writeConcern.get)

  }

  override def close(): Unit = {
  }
}

private object AbstractMongoDAO {

  private val clients: mutable.Map[String, MongoClient] = mutable.Map()
  private val dbs: mutable.Map[(String, String), MongoDB] = mutable.Map()

  private def options(connectionsPerHost : Integer, threadsAllowedToBlock : Integer) =
    MongoClientOptions.builder()
      .connectionsPerHost(connectionsPerHost)
      .writeConcern(casbah.WriteConcern.Unacknowledged)
      .threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlock)

  private def client(mongoClientUri: String, connectionsPerHost : Integer,
                     threadsAllowedToBlock : Integer): MongoClient = {
    if (!clients.contains(mongoClientUri)) {
      clients.put(mongoClientUri, MongoClient(
        new MongoClientURI(new JMongoClientURI(mongoClientUri, options(connectionsPerHost, threadsAllowedToBlock)))
      ))
    }
    clients(mongoClientUri)
  }

  private def db(mongoClientUri: String, dbName: String,
                 connectionsPerHost : Integer, threadsAllowedToBlock : Integer): MongoDB = {
    val key = (mongoClientUri, dbName)
    if (!dbs.contains(key)){
      dbs.put(key, client(mongoClientUri, connectionsPerHost, threadsAllowedToBlock).getDB(dbName))
    }
    dbs(key)
  }

}
